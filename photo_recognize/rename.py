#!/usr/bin/env python3
"""
图片批量编号脚本
将指定目录下的图片文件重命名为 001.ext, 002.ext, 003.ext ... 的格式
"""

import os
import sys

# ===== 配置区域 =====
IMAGE_DIR = "/Cat-and-Dog-System/springboot/src/main/resources/static/uploads/images"

# 支持的图片格式（不区分大小写）
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".tif", ".svg"}
# ====================


def rename_images(directory: str):
    # 检查目录是否存在
    if not os.path.isdir(directory):
        print(f"❌ 目录不存在：{directory}")
        sys.exit(1)

    # 获取所有图片文件，按文件名排序（保证顺序稳定）
    all_files = sorted(os.listdir(directory))
    image_files = [
        f for f in all_files
        if os.path.isfile(os.path.join(directory, f))
           and os.path.splitext(f)[1].lower() in IMAGE_EXTENSIONS
    ]

    if not image_files:
        print(f"⚠️  目录中未找到任何图片文件：{directory}")
        return

    print(f"📂 目录：{directory}")
    print(f"📸 共找到 {len(image_files)} 张图片，开始重命名...\n")

    # 第一步：先把所有文件临时重命名（避免名称冲突）
    temp_names = []
    for original in image_files:
        ext = os.path.splitext(original)[1].lower()
        temp_name = f"__temp__{original}"
        os.rename(
            os.path.join(directory, original),
            os.path.join(directory, temp_name)
        )
        temp_names.append((temp_name, ext))

    # 第二步：按序号正式重命名
    for index, (temp_name, ext) in enumerate(temp_names, start=1):
        new_name = f"{index:03d}{ext}"
        os.rename(
            os.path.join(directory, temp_name),
            os.path.join(directory, new_name)
        )
        original_display = temp_name.replace("__temp__", "")
        print(f"  ✅  {original_display:40s} →  {new_name}")

    print(f"\n🎉 完成！共重命名 {len(temp_names)} 张图片。")


if __name__ == "__main__":
    # 支持通过命令行参数传入自定义目录
    target_dir = sys.argv[1] if len(sys.argv) > 1 else IMAGE_DIR
    rename_images(target_dir)