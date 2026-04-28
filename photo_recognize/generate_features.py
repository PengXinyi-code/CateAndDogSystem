import os
import pymysql
import torch
import torchvision.models as models
import torchvision.transforms as transforms
import numpy as np
from PIL import Image
from tqdm import tqdm

# 1. 数据库配置
DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "123456",
    "database": "animal-succour",
    "charset": "utf8mb4"
}


# 2. 图片根目录
IMAGE_BASE_PATH = "../springboot/src/main/resources/static"

# 3. 加载 ResNet50 模型
model = models.resnet50(pretrained=True)

# 去掉最后分类层
model = torch.nn.Sequential(*list(model.children())[:-1])
model.eval()

# 4. 图像预处理
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# 5. 特征提取函数
def extract_feature(image_path):
    try:
        img = Image.open(image_path).convert("RGB")
    except Exception as e:
        print(f"图片读取失败: {image_path}")
        return None

    img = transform(img).unsqueeze(0)

    with torch.no_grad():
        feature = model(img)

    feature = feature.squeeze().numpy()

    # 归一化
    feature = feature / np.linalg.norm(feature)

    return feature.astype(np.float32)


# 6. 流程
def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # 查询所有图片
    cursor.execute("SELECT id, image_url FROM animal_images")
    rows = cursor.fetchall()

    print(f"共 {len(rows)} 条图片需要处理")

    for row in tqdm(rows):
        image_id = row[0]
        image_url = row[1]

        # 拼接本地路径
        image_path = os.path.join(IMAGE_BASE_PATH, image_url.lstrip("/"))

        if not os.path.exists(image_path):
            print(f"文件不存在: {image_path}")
            continue

        # 提取特征
        feature = extract_feature(image_path)

        if feature is None:
            continue

        # 转成二进制
        feature_bytes = feature.tobytes()

        # 更新数据库
        sql = "UPDATE animal_images SET feature_vector=%s WHERE id=%s"
        cursor.execute(sql, (feature_bytes, image_id))

    conn.commit()
    cursor.close()
    conn.close()

    print("所有特征向量生成完成！")

# 7. 启动
if __name__ == "__main__":
    main()
