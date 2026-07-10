# CateAndDogSystem

面向校园场景的流浪猫狗管理与领养个人项目。平台提供动物档案、图片识别辅助建档、领养申请与审核、领养记录、用户权限和运营内容管理等能力。

项目将图像识别拆分为猫狗目标检测、受控品种自动填充和个体相似度匹配三层：非猫狗图片在入口处拦截，猫狗档案使用标准化的类别和品种字典，匹配仅在同类别、同品种候选中进行。

## 项目组成

| 目录 | 说明 |
| --- | --- |
| `springboot/` | Spring Boot 后端，提供业务、认证、文件与识别转发接口 |
| `vue/` | Vue 3 前端，含用户端和管理端 |
| `photo_recognize/` | FastAPI 图像识别服务与批处理、验证脚本 |
| `springboot/src/main/resources/static/uploads/images/` | 数据库引用的动物图片 |
| `file/` | 运行期头像、轮播图等资源 |
| `animal-succour.sql` | 数据库结构、分类字典、动物数据及特征向量基线 |

## 核心能力

- 只支持猫、狗建档；未检测到猫狗的图片会直接拦截。
- `category` 只维护 `cat`、`dog`；动物通过 `category_id`、`breed_id` 建档。
- 品种为受控下拉选项，用户可以确认或修改，但不能自由输入；后端校验品种与类别的归属关系。
- 上传图片经 YOLOv8n 检测后，以目标框外扩 15% 的裁剪图完成品种分类和 ResNet50 特征提取。
- 匹配结果与类别、品种建议一并返回；最终入库值以用户确认的 `categoryId`、`breedId` 为准。
- 覆盖动物档案、领养申请与审核、领养记录、用户角色菜单、轮播图和图片资源管理。

## 图像识别流程

```text
上传图片
  -> Spring Boot 保存临时文件
  -> GET /analyze_cat_dog_by_path
  -> YOLOv8n 检测猫或狗
       -> 未检测到：返回非猫狗，停止建档与匹配
       -> 检测到：选择置信度最高的猫/狗候选
  -> 检测框外扩 15%，裁剪主体
  -> 品种分类并映射到系统字典
  -> ResNet50 对裁剪图提取特征
  -> 查询同 category + 同 breed 的 animal_images
  -> 余弦相似度匹配，返回已有档案或新建档案建议
```

当前品种字典：

| 类别 | 支持选项 |
| --- | --- |
| 猫 | 英短、布偶、暹罗、缅因、其他猫 |
| 狗 | 金毛、拉布拉多、柯基、边牧、其他狗 |

| 阈值 | 当前值 | 用途 |
| --- | ---: | --- |
| 猫狗检测 | `0.40` | YOLOv8n 猫狗候选的最低置信度 |
| 猫品种 | `0.25` | 映射到支持猫品种的最低置信度 |
| 狗品种 | `0.40` | 映射到支持狗品种的最低置信度 |
| 个体匹配 | `0.90` | 判定已有档案的最低余弦相似度 |

建议上传仅包含一只、主体清晰且相对正面的猫狗照片。复杂背景、遮挡、逆光、低清晰度和多目标画面会降低识别稳定性。

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18 LTS 或更高版本
- MySQL 8.0
- Python 3.10 至 3.12

## 快速启动

### 1. 初始化数据库

创建数据库后，导入根目录中的最新 SQL：

```powershell
mysql -u root -p -e "CREATE DATABASE `animal-succour` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"
mysql -u root -p animal-succour < animal-succour.sql
```

数据库连接配置位于 [application-druid.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application-druid.yml)。请按本机 MySQL 修改数据库名、用户名和密码。若 MySQL 不支持 `utf8mb4_0900_ai_ci`，可在导入前将其替换为 `utf8mb4_general_ci`。

### 2. 启动 Python 图像识别服务

```powershell
cd photo_recognize
python -m venv venv
.\venv\Scripts\activate
pip install -r requirement.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

服务启动时会加载 YOLOv8n、GCViT-Tiny 猫品种模型、EfficientNet-B0 狗品种模型和 ResNet50 特征提取模型。首次运行可能下载模型权重；模型缓存不应提交到 Git。

### 3. 启动后端

在项目根目录执行，确保路径解析一致：

```powershell
$env:JAVA_HOME = 'D:\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -f springboot\pom.xml spring-boot:run
```

后端默认监听 `http://localhost:8080`，Python 服务默认由后端通过 `http://localhost:8000` 调用。

编译验证：

```powershell
$env:JAVA_HOME = 'D:\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -f springboot\pom.xml -DskipTests compile
```

### 4. 启动前端

```powershell
cd vue
npm install
npm run dev
```

开发环境默认地址：`http://localhost:90`。

## 数据与图片基线

`animal-succour.sql` 是数据库结构与演示数据的唯一基线，不维护迁移脚本。当前 SQL 包含标准化的分类和品种字典、动物档案、`animal_images` 中的裁剪图特征向量，以及与数据库一致的图片路径。

动物图片位于 `springboot/src/main/resources/static/uploads/images/`。提交或清理图片前，请确保该目录与 SQL 中 `animal_images.image_url` 的引用集合一致，避免出现数据库引用缺图或无引用孤立图。

## 局域网访问

后端监听 `0.0.0.0:8080`，Vite 开发服务器默认使用端口 `90`。让部署电脑和访问设备加入同一局域网后，访问：

```text
http://部署电脑IPv4:90
```

确认 Windows 防火墙允许 `90` 和 `8080` 入站。Python 服务默认只供同机后端调用，无需对局域网开放 `8000`。

## 当前边界

- 模型未使用校园流浪猫狗样本微调，对遮挡、姿态变化和低清晰度场景的识别能力有限。
- 狗品种分类使用 ImageNet 通用模型，不在支持字典内的高置信度标签仍会归入“其他狗”。
- 校园常见猫的毛色/花纹描述与国际血统品种并不完全对应；品种结果是受控的自动填充建议，用户确认始终优先。
- 当前多数动物仅有一张图片，尚不具备跨视角个体识别准确率的统计基础。

详细架构、接口、数据模型、运维与验证说明见 [PROJECT_DOCUMENTATION.md](/F:/CateAndDogSystem/PROJECT_DOCUMENTATION.md)。
