import argparse
import gc
import json
import time
from pathlib import Path

import numpy as np
import pymysql
from tqdm import tqdm

from main import analyze_cat_dog, extract_feature


def parse_args():
    parser = argparse.ArgumentParser(
        description="Verify cropped-image feature consistency and same-animal retrieval."
    )
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=3306)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", required=True)
    parser.add_argument("--database", default="animal-succour")
    parser.add_argument("--static-root", default="../springboot/src/main/resources/static")
    parser.add_argument("--match-threshold", type=float, default=0.90)
    parser.add_argument("--report", default="cropped_similarity_report.json")
    return parser.parse_args()


def image_url_to_path(static_root, image_url):
    return static_root / image_url.lstrip("/")


def bytes_to_feature(value):
    return np.frombuffer(value, dtype="<f4")


def cosine_similarity(left, right):
    left_norm = np.linalg.norm(left)
    right_norm = np.linalg.norm(right)
    if left_norm == 0 or right_norm == 0:
        return 0.0
    return float(np.dot(left, right) / (left_norm * right_norm))


def delete_file(path):
    if not path:
        return
    file = Path(path)
    for _ in range(3):
        try:
            if file.exists():
                file.unlink()
            return
        except PermissionError:
            gc.collect()
            time.sleep(0.1)


def main():
    args = parse_args()
    script_dir = Path(__file__).resolve().parent
    static_root = (script_dir / args.static_root).resolve()
    report_path = (script_dir / args.report).resolve()

    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset="utf8mb4",
    )

    try:
        with conn.cursor(pymysql.cursors.DictCursor) as cursor:
            cursor.execute(
                """
                select ai.id, ai.animal_id, ai.image_url, ai.feature_vector,
                       a.category_id, a.breed_id,
                       c.code as category_code, b.code as breed_code
                from animal_images ai
                inner join animals a on ai.animal_id = a.id
                left join category c on a.category_id = c.category_id
                left join breed b on a.breed_id = b.breed_id
                where ai.image_url is not null and ai.image_url <> ''
                order by ai.id
                """
            )
            rows = cursor.fetchall()
    finally:
        conn.close()

    valid_rows = []
    failures = []
    consistency_scores = []

    for row in tqdm(rows, desc="Recomputing cropped features"):
        image_path = image_url_to_path(static_root, row["image_url"])
        if not image_path.exists():
            failures.append({"image_id": row["id"], "reason": "missing_file"})
            continue

        crop_path = None
        try:
            analysis = analyze_cat_dog(str(image_path))
            if not analysis.get("isCatDog"):
                failures.append({"image_id": row["id"], "reason": "non_cat_dog"})
                continue

            crop_path = analysis.get("cropPath")
            feature = extract_feature(crop_path or str(image_path))
            stored_feature = bytes_to_feature(row["feature_vector"]) if row["feature_vector"] else None
            consistency = None
            if stored_feature is not None and len(stored_feature) == len(feature):
                consistency = cosine_similarity(feature, stored_feature)
                consistency_scores.append(consistency)

            valid_rows.append({
                **row,
                "feature": feature,
                "stored_consistency": consistency,
            })
        except Exception as exc:
            failures.append({
                "image_id": row["id"],
                "reason": "exception",
                "message": str(exc),
            })
        finally:
            delete_file(crop_path)

    groups = {}
    for row in valid_rows:
        key = (row["category_id"], row["breed_id"])
        groups.setdefault(key, []).append(row)

    duplicate_rows = 0
    top1_hits = 0
    threshold_hits = 0
    evaluated = 0
    details = []

    for query in valid_rows:
        candidates = [
            candidate for candidate in groups[(query["category_id"], query["breed_id"])]
            if candidate["id"] != query["id"]
        ]
        same_animal_candidates = [
            candidate for candidate in candidates
            if candidate["animal_id"] == query["animal_id"]
        ]

        if not same_animal_candidates:
            continue

        duplicate_rows += 1
        scored = sorted(
            [
                (
                    cosine_similarity(query["feature"], candidate["feature"]),
                    candidate,
                )
                for candidate in candidates
            ],
            key=lambda item: item[0],
            reverse=True,
        )
        evaluated += 1
        top1 = scored[0] if scored else None
        same_animal_scores = [score for score, candidate in scored if candidate["animal_id"] == query["animal_id"]]
        best_same_animal = max(same_animal_scores)
        same_animal_rank = next(
            index for index, (score, candidate) in enumerate(scored, start=1)
            if candidate["animal_id"] == query["animal_id"]
        )
        if top1 and top1[1]["animal_id"] == query["animal_id"]:
            top1_hits += 1
        if best_same_animal >= args.match_threshold:
            threshold_hits += 1

        if not top1 or top1[1]["animal_id"] != query["animal_id"] or best_same_animal < args.match_threshold:
            details.append({
                "image_id": query["id"],
                "animal_id": query["animal_id"],
                "category_code": query["category_code"],
                "breed_code": query["breed_code"],
                "same_animal_rank": same_animal_rank,
                "same_animal_similarity": round(best_same_animal, 6),
                "top1_animal_id": top1[1]["animal_id"] if top1 else None,
                "top1_similarity": round(top1[0], 6) if top1 else None,
            })

    report = {
        "match_threshold": args.match_threshold,
        "total_database_images": len(rows),
        "valid_images": len(valid_rows),
        "failed_images": len(failures),
        "single_image_animals_not_evaluable": len(valid_rows) - duplicate_rows,
        "leave_one_out_evaluated_images": evaluated,
        "leave_one_out_top1_hits": top1_hits,
        "leave_one_out_top1_accuracy": round(top1_hits / evaluated, 6) if evaluated else None,
        "same_animal_threshold_hits": threshold_hits,
        "same_animal_threshold_recall": round(threshold_hits / evaluated, 6) if evaluated else None,
        "stored_vs_recomputed_mean_cosine": round(float(np.mean(consistency_scores)), 6) if consistency_scores else None,
        "stored_vs_recomputed_min_cosine": round(float(np.min(consistency_scores)), 6) if consistency_scores else None,
        "failures": failures,
        "problem_cases": details,
    }
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    print(json.dumps(report, ensure_ascii=False, indent=2))
    print(f"report_path={report_path}")


if __name__ == "__main__":
    main()
