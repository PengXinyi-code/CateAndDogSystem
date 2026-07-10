import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as transforms
from torchvision.models import EfficientNet_B0_Weights, ResNet50_Weights
import numpy as np
from fastapi import FastAPI, UploadFile, File
from PIL import Image
from pathlib import Path
from urllib.request import urlretrieve

import timm


_path_exists = Path.exists


def _safe_path_exists(self):
    try:
        return _path_exists(self)
    except OSError:
        return False


Path.exists = _safe_path_exists
from ultralytics import YOLO
import io

app = FastAPI()
BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = BASE_DIR / "models"
CAT_MODEL_PATH = MODEL_DIR / "best_gcvit_tiny_cats.pth"
CAT_MODEL_URL = "https://huggingface.co/spaces/bfarhad/cat-breed-classifier/resolve/main/best_gcvit_tiny_cats.pth"
IMAGE_SIZE = 224

# ======================
# 模型加载
# ======================
model = models.resnet50(weights=ResNet50_Weights.IMAGENET1K_V1)
model = torch.nn.Sequential(*list(model.children())[:-1])
model.eval()

breed_weights = EfficientNet_B0_Weights.IMAGENET1K_V1
breed_model = models.efficientnet_b0(weights=breed_weights)
breed_model.eval()
breed_categories = breed_weights.meta["categories"]
breed_transform = breed_weights.transforms()

CAT_CLASSES = [
    "Abyssinian",
    "Bengal",
    "Birman",
    "Bombay",
    "British_Shorthair",
    "Egyptian_Mau",
    "Maine_Coon",
    "Persian",
    "Ragdoll",
    "Russian_Blue",
    "Siamese",
    "Sphynx",
]

cat_transform = transforms.Compose([
    transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406],
                         [0.229, 0.224, 0.225])
])


def ensure_cat_model_file():
    if CAT_MODEL_PATH.exists():
        return
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    print(f"Downloading cat breed model to {CAT_MODEL_PATH} ...")
    urlretrieve(CAT_MODEL_URL, CAT_MODEL_PATH)


def build_cat_model(num_classes):
    return timm.create_model("gcvit_tiny", pretrained=False, num_classes=num_classes)


def load_cat_model():
    ensure_cat_model_file()
    model = build_cat_model(num_classes=len(CAT_CLASSES))
    state = torch.load(CAT_MODEL_PATH, map_location="cpu")
    if isinstance(state, dict) and "state_dict" in state:
        state = state["state_dict"]

    cleaned = {}
    for key, value in state.items():
        key = key[7:] if key.startswith("module.") else key
        if key == "classifier.1.weight":
            key = "head.fc.weight"
        elif key == "classifier.1.bias":
            key = "head.fc.bias"
        cleaned[key] = value

    load_result = model.load_state_dict(cleaned if cleaned else state, strict=False)
    if load_result.missing_keys or load_result.unexpected_keys:
        print(
            "[cat-model] "
            f"missing_keys={load_result.missing_keys} "
            f"unexpected_keys={load_result.unexpected_keys}"
        )
    model.eval()
    return model


cat_breed_model = load_cat_model()


def validate_classifier_output(model, label_count, model_name):
    with torch.no_grad():
        output = model(torch.zeros(1, 3, IMAGE_SIZE, IMAGE_SIZE))
    output_count = output.shape[1]
    if output_count != label_count:
        raise RuntimeError(
            f"{model_name} output count {output_count} does not match label count {label_count}"
        )
    print(f"[model-check] {model_name} output_count={output_count} label_count={label_count}")


validate_classifier_output(cat_breed_model, len(CAT_CLASSES), "cat_breed_model")
validate_classifier_output(breed_model, len(breed_categories), "dog_breed_model")

detector = YOLO(str(BASE_DIR / "yolov8n.pt"))
CAT_DOG_CLASS_NAMES = {"cat": "猫", "dog": "狗"}
CAT_DOG_CONFIDENCE = 0.40
CAT_BREED_CONFIDENCE = 0.25
DOG_BREED_CONFIDENCE = 0.40
CROP_PADDING_RATIO = 0.15

BREED_DISPLAY_NAMES = {
    "british_shorthair": "英短",
    "ragdoll": "布偶猫",
    "siamese": "暹罗猫",
    "maine_coon": "缅因猫",
    "other_cat": "其他猫",
    "golden_retriever": "金毛",
    "labrador_retriever": "拉布拉多",
    "corgi": "柯基",
    "border_collie": "边牧",
    "other_dog": "其他狗",
}

CAT_BREED_MAP = {
    "siamese": "siamese",
    "british shorthair": "british_shorthair",
    "ragdoll": "ragdoll",
    "maine coon": "maine_coon",
}

DOG_BREED_MAP = {
    "golden retriever": "golden_retriever",
    "labrador retriever": "labrador_retriever",
    "pembroke": "corgi",
    "cardigan": "corgi",
    "border collie": "border_collie",
}

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# ======================
# 特征提取
# ======================
def extract_feature(path):
    # 1. 必须先读取图片
    # 把路径字符串转换成 PIL Image 对象
    with Image.open(path) as image:
        img = image.convert('RGB')

    # 2. 然后再进行预处理和模型推理
    # 这时候 img 是图片对象，transform 才能正常工作
    img = transform(img).unsqueeze(0)

    with torch.no_grad():
        feature = model(img)

    feature = feature.squeeze().numpy()
    feature = feature / np.linalg.norm(feature)

    return feature.astype(np.float32)


def get_box_metrics(box, image_width, image_height):
    x1, y1, x2, y2 = box
    box_width = max(0.0, x2 - x1)
    box_height = max(0.0, y2 - y1)
    image_area = max(1.0, float(image_width * image_height))
    area_ratio = (box_width * box_height) / image_area
    return box_width, box_height, area_ratio


def detect_cat_dog(path):
    with Image.open(path) as image:
        image_width, image_height = image.size
    results = detector(path, verbose=False)
    best = None
    candidates = []

    for result in results:
        for box in result.boxes:
            class_id = int(box.cls[0])
            class_name = result.names[class_id]
            confidence = float(box.conf[0])
            xyxy = [float(v) for v in box.xyxy[0].tolist()]
            box_width, box_height, area_ratio = get_box_metrics(xyxy, image_width, image_height)
            is_cat_dog = class_name in CAT_DOG_CLASS_NAMES
            passed = is_cat_dog and confidence >= CAT_DOG_CONFIDENCE
            candidates.append({
                "label": class_name,
                "confidence": confidence,
                "box": xyxy,
                "box_width": box_width,
                "box_height": box_height,
                "area_ratio": area_ratio,
                "passed": passed,
            })

            if class_name not in CAT_DOG_CLASS_NAMES:
                continue
            if confidence < CAT_DOG_CONFIDENCE:
                continue
            if best is None or confidence > best["confidence"]:
                best = {
                    "category_code": class_name,
                    "category_name": CAT_DOG_CLASS_NAMES[class_name],
                    "confidence": confidence,
                    "raw_label": class_name,
                    "box": xyxy,
                    "boxAreaRatio": area_ratio,
                    "imageWidth": image_width,
                    "imageHeight": image_height,
                }

    if candidates:
        candidates_text = "; ".join(
            f"{item['label']} conf={item['confidence']:.4f} "
            f"area={item['area_ratio']:.4f} "
            f"size={item['box_width']:.0f}x{item['box_height']:.0f} "
            f"passed={item['passed']} "
            f"box={[round(v, 1) for v in item['box']]}"
            for item in candidates
        )
    else:
        candidates_text = "none"

    print(
        "[detect] "
        f"path={path} "
        f"image={image_width}x{image_height} "
        f"threshold={CAT_DOG_CONFIDENCE:.2f} "
        f"candidates=[{candidates_text}]"
    )

    if best is None:
        print(
            "[detect] selected=none "
            f"path={path} reason=no_cat_dog_above_threshold"
        )
        return {
            "is_cat_dog": False,
            "message": "未检测到猫狗，请上传猫或狗的清晰照片"
        }

    print(
        "[detect] "
        f"selected={best['category_code']} "
        f"confidence={best['confidence']:.4f} "
        f"area={best['boxAreaRatio']:.4f} "
        f"box={[round(v, 1) for v in best['box']]}"
    )

    return {
        "is_cat_dog": True,
        **best
    }


def expand_box(box, width, height, padding_ratio=CROP_PADDING_RATIO):
    x1, y1, x2, y2 = box
    box_width = x2 - x1
    box_height = y2 - y1
    padding_x = box_width * padding_ratio
    padding_y = box_height * padding_ratio

    return [
        max(0, int(x1 - padding_x)),
        max(0, int(y1 - padding_y)),
        min(width, int(x2 + padding_x)),
        min(height, int(y2 + padding_y)),
    ]


def crop_detected_subject(path, box):
    source = Path(path)
    with Image.open(source) as image:
        image = image.convert("RGB")
        crop_box = expand_box(box, image.width, image.height)
        cropped = image.crop(crop_box)
        _, _, crop_area_ratio = get_box_metrics(crop_box, image.width, image.height)

    crop_dir = source.parent / "crops"
    crop_dir.mkdir(parents=True, exist_ok=True)
    crop_path = crop_dir / f"{source.stem}_crop{source.suffix or '.jpg'}"
    cropped.save(crop_path)
    print(
        "[crop] "
        f"path={path} "
        f"crop_path={crop_path} "
        f"padding={CROP_PADDING_RATIO:.2f} "
        f"crop_area={crop_area_ratio:.4f} "
        f"crop_box={crop_box} "
        f"crop_size={cropped.width}x{cropped.height}"
    )
    return str(crop_path), crop_box


def normalize_label(label):
    return label.replace("_", " ").replace("-", " ").strip().lower()


def classify_breed(path):
    with Image.open(path) as source:
        image = source.convert("RGB")
    batch = breed_transform(image).unsqueeze(0)

    with torch.no_grad():
        prediction = breed_model(batch).softmax(1)[0]

    values, indices = prediction.topk(5)
    return [
        {
            "label": breed_categories[int(index)],
            "confidence": float(value)
        }
        for value, index in zip(values, indices)
        if int(index) < len(breed_categories)
    ]


def classify_cat_breed(path):
    with Image.open(path) as source:
        image = source.convert("RGB")
    batch = cat_transform(image).unsqueeze(0)

    with torch.no_grad():
        prediction = cat_breed_model(batch).softmax(1)[0]

    values, indices = prediction.topk(5)
    return [
        {
            "label": CAT_CLASSES[int(index)],
            "confidence": float(value)
        }
        for value, index in zip(values, indices)
        if int(index) < len(CAT_CLASSES)
    ]


def classify_dog_breed(path):
    return classify_breed(path)


def map_breed(category_code, predictions):
    default_code = "other_cat" if category_code == "cat" else "other_dog"
    mapping = CAT_BREED_MAP if category_code == "cat" else DOG_BREED_MAP
    breed_confidence_threshold = CAT_BREED_CONFIDENCE if category_code == "cat" else DOG_BREED_CONFIDENCE

    raw_label = predictions[0]["label"] if predictions else None
    confidence = predictions[0]["confidence"] if predictions else 0.0

    for prediction in predictions:
        label = normalize_label(prediction["label"])
        if prediction["confidence"] < breed_confidence_threshold:
            continue
        if label in mapping:
            breed_code = mapping[label]
            return {
                "breedCode": breed_code,
                "breedName": BREED_DISPLAY_NAMES[breed_code],
                "breedConfidence": prediction["confidence"],
                "rawBreedLabel": prediction["label"],
            }

    return {
        "breedCode": default_code,
        "breedName": BREED_DISPLAY_NAMES[default_code],
        "breedConfidence": confidence,
        "rawBreedLabel": raw_label,
    }


def analyze_cat_dog(path):
    detection = detect_cat_dog(path)
    if not detection.get("is_cat_dog"):
        print(f"[analysis] non_cat_dog path={path}")
        return {
            "isCatDog": False,
            "categoryCode": "non_cat_dog",
            "categoryName": "非猫狗",
            "message": detection.get("message", "未检测到猫狗，请上传猫或狗的清晰照片")
        }

    crop_path, crop_box = crop_detected_subject(path, detection["box"])
    predictions = classify_cat_breed(crop_path) if detection["category_code"] == "cat" else classify_dog_breed(crop_path)
    breed = map_breed(detection["category_code"], predictions)
    candidates_text = ", ".join(
        f"{item['label']}={item['confidence']:.4f}"
        for item in predictions
    )
    print(
        "[analysis] "
        f"path={path} "
        f"category={detection['category_code']} "
        f"category_confidence={detection['confidence']:.4f} "
        f"crop_box={crop_box} "
        f"raw_breed={breed['rawBreedLabel']} "
        f"mapped_breed={breed['breedCode']} "
        f"breed_confidence={breed['breedConfidence']:.4f} "
        f"top5=[{candidates_text}]"
    )

    return {
        "isCatDog": True,
        "categoryCode": detection["category_code"],
        "categoryName": detection["category_name"],
        "categoryConfidence": detection["confidence"],
        "breedCode": breed["breedCode"],
        "breedName": breed["breedName"],
        "breedConfidence": breed["breedConfidence"],
        "rawDetectLabel": detection["raw_label"],
        "rawBreedLabel": breed["rawBreedLabel"],
        "breedPredictions": predictions,
        "cropPath": crop_path,
        "cropBox": crop_box,
    }

# ======================
# API
# ======================
# @app.post("/extract")
# async def extract(file: UploadFile = File(...)):
#     contents = await file.read()
#     image = Image.open(io.BytesIO(contents)).convert("RGB")
#
#     feature = extract_feature(image)
#
#     return {
#         "feature": feature.tolist()
#     }
@app.get("/extract_by_path")
def extract_by_path(path: str):
    feature = extract_feature(path)
    return {"feature": feature.tolist()}


@app.get("/analyze_cat_dog_by_path")
def analyze_cat_dog_by_path(path: str):
    return analyze_cat_dog(path)
