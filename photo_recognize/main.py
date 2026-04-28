import torch
import torchvision.models as models
import torchvision.transforms as transforms
import numpy as np
from fastapi import FastAPI, UploadFile, File
from PIL import Image
import io

app = FastAPI()

# ======================
# 模型加载
# ======================
model = models.resnet50(pretrained=True)
model = torch.nn.Sequential(*list(model.children())[:-1])
model.eval()

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