import torch
import torchvision.models as models
import torchvision.transforms as transforms
from torchvision.models import ResNet50_Weights
import numpy as np
from fastapi import FastAPI, UploadFile, File
from PIL import Image
from pathlib import Path


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

# ======================
# 模型加载
# ======================
model = models.resnet50(weights=ResNet50_Weights.IMAGENET1K_V1)
model = torch.nn.Sequential(*list(model.children())[:-1])
model.eval()

detector = YOLO("yolov8n.pt")
CAT_DOG_CLASS_NAMES = {"cat": "猫", "dog": "狗"}
CAT_DOG_CONFIDENCE = 0.35

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
    img = Image.open(path).convert('RGB')

    # 2. 然后再进行预处理和模型推理
    # 这时候 img 是图片对象，transform 才能正常工作
    img = transform(img).unsqueeze(0)

    with torch.no_grad():
        feature = model(img)

    feature = feature.squeeze().numpy()
    feature = feature / np.linalg.norm(feature)

    return feature.astype(np.float32)


def detect_cat_dog(path):
    results = detector(path, verbose=False)
    best = None

    for result in results:
        for box in result.boxes:
            class_id = int(box.cls[0])
            class_name = result.names[class_id]
            confidence = float(box.conf[0])

            if class_name not in CAT_DOG_CLASS_NAMES:
                continue
            if confidence < CAT_DOG_CONFIDENCE:
                continue
            if best is None or confidence > best["confidence"]:
                best = {
                    "category_code": class_name,
                    "category_name": CAT_DOG_CLASS_NAMES[class_name],
                    "confidence": confidence,
                    "box": [float(v) for v in box.xyxy[0].tolist()]
                }

    if best is None:
        return {
            "is_cat_dog": False,
            "message": "未检测到猫狗，请上传猫或狗的清晰照片"
        }

    return {
        "is_cat_dog": True,
        **best
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


@app.get("/detect_cat_dog_by_path")
def detect_cat_dog_by_path(path: str):
    return detect_cat_dog(path)
