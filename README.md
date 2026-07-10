# CateAndDogSystem

校园流浪猫狗管理与领养个人项目。当前实现包括动物档案、图片识别辅助建档、领养申请与记录、轮播图、分类品种字典、用户角色和菜单管理。

项目不包含线下人员调度、工单、处置记录或医疗档案等功能。

## 目录

| 路径 | 当前职责 |
| --- | --- |
| `springboot/` | Spring Boot 后端，动物、领养、分类、文件、登录和菜单接口 |
| `vue/` | Vue 3 用户端与管理端 |
| `photo_recognize/` | FastAPI 图像识别服务、重分类与特征验证脚本 |
| `springboot/src/main/resources/static/uploads/images/` | 动物图片文件 |
| `file/` | 头像、轮播图等运行期文件 |
| `animal-succour.sql` | 数据库结构、数据、动态菜单路径和图片特征向量的导出基线 |

## 已实现业务

- 动物档案：查询、统计、新增、修改、删除，支持类别、品种、发现时间、地点、介绍、审核状态和领养状态。
- 分类字典：一级类别固定为猫和狗；品种字典按类别维护。
- 图片识别：上传图片后检测猫/狗、给出品种建议、尝试匹配已有动物档案。
- 领养：提交申请、查询、修改、删除、撤销；申请状态更新为“已完成”时写入领养记录并把动物标为已领养。
- 运营与系统管理：轮播图、用户、角色、菜单和个人资料页面。

## 图片识别的实际流程

```text
上传图片
  -> Spring Boot 保存临时文件
  -> Python GET /analyze_cat_dog_by_path
  -> YOLOv8n 定位目标，并同时判定猫或狗大类
       -> 未找到达到阈值的猫/狗：返回非猫狗，停止匹配
       -> 找到多个猫/狗候选：选择置信度最高的候选
  -> 按检测框四周 15% 边距裁剪主体
  -> 根据 YOLO 判定的大类进行品种分类
       -> 猫：GCViT-Tiny
       -> 狗：EfficientNet-B0 ImageNet 预训练模型
  -> 将原始品种标签映射到系统品种，或映射为“其他猫/其他狗”
  -> ResNet50 对裁剪图提取 2048 维、L2 归一化特征
  -> 后端只查询相同 category_id 与 breed_id 的 animal_images
  -> 计算余弦相似度，返回最佳匹配或新建档案建议
```

YOLO 的检测结果已经包含猫/狗大类；后续步骤识别的是品种，不会再次判定猫或狗。

当前支持字典：

| 大类 | 品种选项 |
| --- | --- |
| 猫 | 英短、布偶猫、暹罗猫、缅因猫、其他猫 |
| 狗 | 金毛、拉布拉多、柯基、边牧、其他狗 |

| 配置/常量 | 当前值 | 位置 |
| --- | ---: | --- |
| 猫狗检测阈值 | `0.40` | `photo_recognize/main.py` |
| 猫品种阈值 | `0.25` | `photo_recognize/main.py` |
| 狗品种阈值 | `0.40` | `photo_recognize/main.py` |
| 裁剪边距 | `15%` | `photo_recognize/main.py` |
| 匹配阈值 | `0.90` | `springboot/src/main/resources/application.yml` |

识别结果用于预填类别和品种。用户可以在下拉框中修改；新增或修改动物时，后端只校验提交的 `breedId` 是否属于提交的 `categoryId`，最终入库以表单值为准。

## 本地启动

### 1. 初始化数据库

```powershell
mysql -u root -p -e "CREATE DATABASE `animal-succour` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"
mysql -u root -p animal-succour < animal-succour.sql
```

数据库连接配置在 [application-druid.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application-druid.yml)。如果本机 MySQL 不支持 `utf8mb4_0900_ai_ci`，导入前可替换为 `utf8mb4_general_ci`。

### 2. 启动识别服务

```powershell
cd photo_recognize
python -m venv venv
.\venv\Scripts\activate
pip install -r requirement.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

服务监听 `127.0.0.1:8000`。首次运行时，YOLO、torchvision 预训练权重和猫品种模型可能需要下载；本地模型缓存与虚拟环境不应提交。

### 3. 启动后端

在仓库根目录执行：

```powershell
$env:JAVA_HOME = 'D:\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -f springboot\pom.xml spring-boot:run
```

编译检查：

```powershell
$env:JAVA_HOME = 'D:\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -f springboot\pom.xml -DskipTests compile
```

后端默认监听 `0.0.0.0:8080`，并通过 `http://localhost:8000` 调用 Python 服务。

### 4. 启动前端

```powershell
cd vue
npm install
npm run dev
```

开发地址默认是 `http://localhost:90`。

## 数据与文件约定

- 数据库结构和数据以重新导出的 `animal-succour.sql` 为准；仓库不维护数据库迁移脚本。
- `animal_images.image_url` 使用 `/uploads/images/...`，对应文件放在 `springboot/src/main/resources/static/uploads/images/`。
- 头像和轮播图使用 `/profile/...`，对应文件放在根目录 `file/`。
- 重导 SQL 或清理图片时，应核对 SQL 中的 `animal_images.image_url`、本地动物图片目录和 Git 跟踪文件是否一致。
- 不提交虚拟环境、模型缓存、临时裁剪图、开发日志、个人材料和 IDE 配置。

## 当前实现边界

- 模型未针对校园流浪猫狗数据微调，遮挡、复杂背景、姿态变化和低清晰度会影响结果。
- 狗品种分类使用 ImageNet 通用标签；不在当前字典中的结果会归为“其他狗”。
- 当前匹配策略把品种作为候选过滤条件，品种误判会缩小候选集。
- `AnimalServiceImp` 在通过已有 `imageUrl` 建档但特征提取失败时，会写入 512 维零向量；正常 ResNet50 特征为 2048 维。这是当前代码的兜底行为，不应当作有效识别结果。
- Spring Security 当前放行 `/api/**`，前端路由守卫不能替代后端接口授权。

详细实现说明见 [PROJECT_DOCUMENTATION.md](/F:/CateAndDogSystem/PROJECT_DOCUMENTATION.md)。
