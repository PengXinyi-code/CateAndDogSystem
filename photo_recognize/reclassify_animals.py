import argparse
import gc
import time
import traceback
from collections import Counter
from pathlib import Path

import numpy as np
import pymysql
from tqdm import tqdm

from main import analyze_cat_dog, extract_feature


def parse_args():
    parser = argparse.ArgumentParser(
        description="Reclassify existing animals with cat/dog detection, breed mapping, and cropped image features."
    )
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=3306)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", required=True)
    parser.add_argument("--database", default="animal-succour")
    parser.add_argument("--static-root", default="../springboot/src/main/resources/static")
    parser.add_argument("--execute", action="store_true", help="Actually update/delete database rows.")
    parser.add_argument("--delete-files", action="store_true", help="Also delete image files for removed animals.")
    parser.add_argument("--only-animal-id", type=int, help="Only process one animal, useful for debugging.")
    parser.add_argument("--only-category-code", choices=["cat", "dog"], help="Only process animals currently in this category.")
    parser.add_argument("--skip-feature", action="store_true", help="Only run detection/classification, do not extract/update features.")
    parser.add_argument("--other-ranking-limit", type=int, default=20, help="Print top N animals mapped to other_cat/other_dog by breed confidence.")
    return parser.parse_args()


def image_url_to_path(static_root, image_url):
    return static_root / image_url.lstrip("/")


def feature_to_bytes(feature):
    return np.asarray(feature, dtype="<f4").tobytes()


def delete_file(path):
    if not path:
        return
    file = Path(path)
    for attempt in range(3):
        if not file.exists():
            return
        try:
            file.unlink()
            return
        except PermissionError as exc:
            gc.collect()
            time.sleep(0.2 * (attempt + 1))
            if attempt < 2:
                continue
            print(f"[warn] failed to delete temp file path={file} error={exc}")


def load_dictionaries(cursor):
    cursor.execute("select category_id, code from category where enabled = 1")
    categories = {row["code"]: row["category_id"] for row in cursor.fetchall()}

    cursor.execute(
        """
        select breed_id, category_id, code, is_default
        from breed
        where enabled = 1
        """
    )
    breed_by_category_and_code = {}
    default_breed_by_category = {}
    for row in cursor.fetchall():
        breed_by_category_and_code[(row["category_id"], row["code"])] = row["breed_id"]
        if row["is_default"]:
            default_breed_by_category[row["category_id"]] = row["breed_id"]

    return categories, breed_by_category_and_code, default_breed_by_category


def resolve_breed_id(category_id, breed_code, breed_by_category_and_code, default_breed_by_category):
    breed_id = breed_by_category_and_code.get((category_id, breed_code))
    if breed_id:
        return breed_id
    default_breed_id = default_breed_by_category.get(category_id)
    if not default_breed_id:
        raise RuntimeError(f"No default breed configured for category_id={category_id}")
    return default_breed_id


def fetch_animals(cursor, only_animal_id=None, only_category_code=None):
    cursor.execute(
        """
        select
            a.id as animal_id,
            a.category_id,
            a.breed_id,
            ai.id as image_id,
            ai.image_url
        from animals a
        left join category c on a.category_id = c.category_id
        left join animal_images ai on a.id = ai.animal_id
        where (%s is null or a.id = %s)
          and (%s is null or c.code = %s)
        order by a.id, ai.id
        """,
        (only_animal_id, only_animal_id, only_category_code, only_category_code),
    )
    animals = {}
    for row in cursor.fetchall():
        animal = animals.setdefault(
            row["animal_id"],
            {
                "animal_id": row["animal_id"],
                "category_id": row["category_id"],
                "breed_id": row["breed_id"],
                "images": [],
            },
        )
        if row["image_id"] is not None:
            animal["images"].append(
                {
                    "image_id": row["image_id"],
                    "image_url": row["image_url"],
                }
            )
    return list(animals.values())


def delete_animal(cursor, animal_id):
    animal_id_text = str(animal_id)
    cursor.execute("delete from adopt where animal_id = %s", (animal_id_text,))
    cursor.execute("delete from adoption_record where animal_id = %s", (animal_id_text,))
    cursor.execute("delete from animal_images where animal_id = %s", (animal_id,))
    cursor.execute("delete from animals where id = %s", (animal_id,))


def update_animal(cursor, animal_id, category_id, breed_id):
    cursor.execute(
        """
        update animals
        set category_id = %s,
            breed_id = %s
        where id = %s
        """,
        (category_id, breed_id, animal_id),
    )


def update_image_feature(cursor, image_id, feature_bytes):
    cursor.execute(
        "update animal_images set feature_vector = %s where id = %s",
        (feature_bytes, image_id),
    )


def analyze_image(image_path, skip_feature=False):
    crop_path = None
    try:
        analysis = analyze_cat_dog(str(image_path))
        if not analysis.get("isCatDog"):
            return analysis, None

        crop_path = analysis.get("cropPath")
        if skip_feature:
            return analysis, None

        feature_path = crop_path or str(image_path)
        feature = extract_feature(feature_path)
        return analysis, feature_to_bytes(feature)
    finally:
        delete_file(crop_path)


def main():
    args = parse_args()
    script_dir = Path(__file__).resolve().parent
    static_root = (script_dir / args.static_root).resolve()

    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )

    stats = {
        "updated_animals": 0,
        "updated_images": 0,
        "deleted_animals": 0,
        "missing_files": 0,
        "non_cat_dog_images": 0,
        "failed_images": 0,
        "skipped_animals": 0,
    }
    mapped_breed_counter = Counter()
    raw_breed_counter = Counter()
    category_counter = Counter()
    other_breed_rankings = []

    try:
        with conn.cursor() as cursor:
            categories, breed_by_category_and_code, default_breed_by_category = load_dictionaries(cursor)
            animals = fetch_animals(cursor, args.only_animal_id, args.only_category_code)

        print(f"static_root={static_root}")
        print(
            f"animals={len(animals)} execute={args.execute} delete_files={args.delete_files} "
            f"only_category_code={args.only_category_code} skip_feature={args.skip_feature}"
        )

        for animal in tqdm(animals, desc="Reclassifying animals"):
            animal_id = animal["animal_id"]
            image_results = []
            files_to_delete = []
            had_processing_error = False

            if not animal["images"]:
                print(f"[skip] animal_id={animal_id} reason=no_image")
                stats["skipped_animals"] += 1
                continue

            for image in animal["images"]:
                image_path = image_url_to_path(static_root, image["image_url"])
                if not image_path.exists():
                    print(f"[missing] animal_id={animal_id} image_id={image['image_id']} path={image_path}")
                    stats["missing_files"] += 1
                    continue

                try:
                    analysis, feature_bytes = analyze_image(image_path, args.skip_feature)
                except Exception as exc:
                    had_processing_error = True
                    stats["failed_images"] += 1
                    print(
                        f"[error] animal_id={animal_id} image_id={image['image_id']} "
                        f"path={image_path} error={type(exc).__name__}: {exc}"
                    )
                    traceback.print_exc()
                    continue

                if not analysis.get("isCatDog"):
                    print(
                        f"[non-cat-dog] animal_id={animal_id} image_id={image['image_id']} "
                        f"url={image['image_url']}"
                    )
                    stats["non_cat_dog_images"] += 1
                    files_to_delete.append(image_path)
                    continue

                category_code = analysis.get("categoryCode")
                category_id = categories.get(category_code)
                if not category_id:
                    raise RuntimeError(f"Unknown category_code={category_code} for animal_id={animal_id}")

                breed_id = resolve_breed_id(
                    category_id,
                    analysis.get("breedCode"),
                    breed_by_category_and_code,
                    default_breed_by_category,
                )
                category_counter[category_id] += 1
                mapped_breed_counter[breed_id] += 1
                raw_breed_counter[analysis.get("rawBreedLabel") or "unknown"] += 1

                image_results.append(
                    {
                        "image_id": image["image_id"],
                        "image_url": image["image_url"],
                        "image_path": image_path,
                        "category_id": category_id,
                        "breed_id": breed_id,
                        "feature_bytes": feature_bytes,
                        "category_confidence": analysis.get("categoryConfidence") or 0,
                        "breed_confidence": analysis.get("breedConfidence") or 0,
                        "raw_detect_label": analysis.get("rawDetectLabel"),
                        "raw_breed_label": analysis.get("rawBreedLabel"),
                        "breed_predictions": analysis.get("breedPredictions") or [],
                    }
                )

            if not image_results:
                if had_processing_error:
                    print(f"[skip] animal_id={animal_id} reason=image_processing_error")
                    stats["skipped_animals"] += 1
                    continue

                print(f"[delete-animal] animal_id={animal_id} reason=no_valid_cat_dog_image")
                if args.execute:
                    with conn.cursor() as cursor:
                        delete_animal(cursor, animal_id)
                    conn.commit()
                    if args.delete_files:
                        for path in files_to_delete:
                            delete_file(path)
                stats["deleted_animals"] += 1
                continue

            best = max(
                image_results,
                key=lambda item: (item["category_confidence"], item["breed_confidence"]),
            )
            if str(best["breed_id"]).endswith("_other"):
                other_breed_rankings.append(
                    {
                        "animal_id": animal_id,
                        **best,
                    }
                )
            print(
                f"[update-animal] animal_id={animal_id} "
                f"{animal['category_id']}/{animal['breed_id']} -> {best['category_id']}/{best['breed_id']} "
                f"raw_detect={best['raw_detect_label']} raw_breed={best['raw_breed_label']} "
                f"category_conf={best['category_confidence']:.4f} breed_conf={best['breed_confidence']:.4f}"
            )

            if args.execute:
                with conn.cursor() as cursor:
                    update_animal(cursor, animal_id, best["category_id"], best["breed_id"])
                    if not args.skip_feature:
                        for item in image_results:
                            update_image_feature(cursor, item["image_id"], item["feature_bytes"])
                    if len(image_results) < len(animal["images"]):
                        valid_image_ids = [item["image_id"] for item in image_results]
                        cursor.execute(
                            f"""
                            delete from animal_images
                            where animal_id = %s
                              and id not in ({",".join(["%s"] * len(valid_image_ids))})
                            """,
                            [animal_id, *valid_image_ids],
                        )
                conn.commit()
                if args.delete_files:
                    for path in files_to_delete:
                        delete_file(path)

            stats["updated_animals"] += 1
            stats["updated_images"] += len(image_results)

        print(
            "done. "
            + " ".join(f"{key}={value}" for key, value in stats.items())
            + f" execute={args.execute}"
        )
        print(f"category_summary={dict(category_counter)}")
        print(f"mapped_breed_summary={dict(mapped_breed_counter)}")
        print(f"raw_breed_top10={dict(raw_breed_counter.most_common(10))}")
        if args.other_ranking_limit > 0:
            other_breed_rankings.sort(key=lambda item: item["breed_confidence"], reverse=True)
            print(f"other_breed_confidence_top{args.other_ranking_limit}=")
            for item in other_breed_rankings[: args.other_ranking_limit]:
                top5 = ", ".join(
                    f"{prediction['label']}={prediction['confidence']:.4f}"
                    for prediction in item["breed_predictions"]
                )
                print(
                    f"  animal_id={item['animal_id']} "
                    f"image_id={item['image_id']} "
                    f"raw_breed={item['raw_breed_label']} "
                    f"breed_conf={item['breed_confidence']:.4f} "
                    f"category_conf={item['category_confidence']:.4f} "
                    f"top5=[{top5}]"
                )
    finally:
        conn.close()


if __name__ == "__main__":
    main()
