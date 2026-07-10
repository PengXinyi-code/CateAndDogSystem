# CateAndDogSystem 项目文档

> 项目定位：持续维护的个人项目，用于校园流浪猫狗信息建档、图片识别辅助和领养管理。
>
> 文档基线：2026-07-10，依据当前仓库源码、配置、`animal-succour.sql`、静态图片目录和图像识别服务整理。

## 1. 项目概览

CateAndDogSystem 是一个前后端分离的校园流浪猫狗管理与领养平台。它将动物信息管理、领养申请和审核、图片资源管理、用户权限管理与图像识别辅助录入结合在同一套业务流程中。

平台的图像识别不承担无条件的自动决策，而是服务于可靠建档：先阻止非猫狗图片进入流程，再以模型结果预填受控类别和品种，最后由用户在限定选项内确认。个体相似度匹配用于发现可能已建档的动物，不能替代人工核验。

### 1.1 目标用户与功能

| 角色 | 主要能力 |
| --- | --- |
| 普通用户 | 注册登录、浏览动物档案、通过图片识别查找或建档、提交和撤销领养申请、维护个人资料 |
| 管理员 | 管理动物档案、审核申请、查看领养记录、维护轮播图、分类字典、用户、角色和菜单 |

当前业务边界聚焦动物档案与领养闭环；系统不包含线下人员调度、工单、处置记录、医疗档案或其他线下服务功能。

## 2. 系统架构

```text
浏览器
  |
  v
Vue 3 + Vite (开发端口 90)
  |
  v
Spring Boot 3 (0.0.0.0:8080)
  |                  |
  | MyBatis           | HTTP
  v                  v
MySQL 8          FastAPI (127.0.0.1:8000)
                     |
                     +-- YOLOv8n 猫狗检测与主体框
                     +-- GCViT-Tiny 猫品种分类
                     +-- EfficientNet-B0 狗品种分类
                     +-- ResNet50 裁剪图特征提取
```

| 层级 | 技术 | 责任 |
| --- | --- | --- |
| 前端 | Vue 3、Vite、Pinia、Element Plus、Axios | 用户端、管理端、受控表单和识别结果展示 |
| 后端 | Java 17、Spring Boot 3、Spring Security、JWT、MyBatis、Druid | 业务规则、权限、文件、相似度计算、Python 服务编排 |
| 识别服务 | FastAPI、PyTorch、torchvision、timm、Ultralytics | 猫狗检测、裁剪、品种映射、特征提取 |
| 数据 | MySQL 8 | 动物、图片特征、领养业务、权限和运营数据 |

## 3. 仓库结构

```text
CateAndDogSystem/
├─ springboot/                         Spring Boot 后端
│  └─ src/main/
│     ├─ java/com/fast/animal/          动物、领养、分类、识别业务
│     ├─ java/com/fast/system/          登录、权限、用户、角色、菜单
│     └─ resources/                     配置、Mapper、静态上传图片
├─ vue/                                Vue 前端
│  └─ src/                             API、路由、状态、页面和布局
├─ photo_recognize/                    FastAPI 服务与批处理脚本
│  ├─ main.py                          聚合识别与特征提取接口
│  ├─ reclassify_animals.py            全量重新分类和特征重建
│  └─ verify_cropped_similarity.py     裁剪图特征写入验证
├─ file/                               运行期头像、轮播图等资源
├─ animal-succour.sql                  数据库与特征数据基线
├─ README.md                           快速启动说明
└─ PROJECT_DOCUMENTATION.md            本文档
```

## 4. 关键业务规则

### 4.1 分类标准化

系统只支持猫、狗建档：

- `category` 只维护 `cat`、`dog` 两条启用记录。
- `breed` 维护受控字典；每个品种属于一个类别，且每个类别都有默认“其他”品种。
- `animals` 使用 `category_id` 和 `breed_id` 保存分类结果，不再依赖自由文本 `species`。
- 新增或修改档案时，后端验证 `breed.category_id = animals.category_id`。
- 模型识别只提供表单默认值，用户可修改；入库以用户确认后的 `categoryId`、`breedId` 为准。

当前字典：

| `category_id` | `breed_id` 与显示名 |
| --- | --- |
| `cat` | `cat_british_shorthair` 英短、`cat_ragdoll` 布偶猫、`cat_siamese` 暹罗猫、`cat_maine_coon` 缅因猫、`cat_other` 其他猫 |
| `dog` | `dog_golden_retriever` 金毛、`dog_labrador` 拉布拉多、`dog_corgi` 柯基、`dog_border_collie` 边牧、`dog_other` 其他狗 |

### 4.2 图片识别与建档

```text
用户上传图片
  -> 后端保存临时文件
  -> Python /analyze_cat_dog_by_path
  -> YOLOv8n 检测猫狗候选
       -> 没有达阈值的猫狗：返回 non_cat_dog，流程结束
       -> 多个候选：选择置信度最高的猫或狗
  -> 检测框外扩 15%，裁剪主体
  -> 猫或狗品种分类，映射到受控字典或“其他”
  -> ResNet50 对裁剪图生成归一化特征
  -> 后端按 category_id + breed_id 查询 animal_images
  -> 计算余弦相似度，返回档案匹配或新建建议
```

`RecognitionServiceImp` 会删除识别过程的临时原图与裁剪图；`AnimalServiceImp` 新增档案时也使用裁剪图生成入库特征，保证建档与查询处于同一特征空间。

### 4.3 模型与阈值

| 项目 | 当前实现 |
| --- | --- |
| 猫狗检测 | YOLOv8n，阈值 `0.40` |
| 主体裁剪 | 检测框四周增加 `15%` 边距 |
| 猫品种 | GCViT-Tiny，阈值 `0.25` |
| 狗品种 | EfficientNet-B0 ImageNet 预训练模型，阈值 `0.40` |
| 个体特征 | ResNet50 ImageNet 预训练模型，输出 L2 归一化 2048 维向量 |
| 匹配阈值 | `recognition.match-threshold=0.90` |

猫模型支持标签映射到英短、布偶、暹罗和缅因；狗模型映射到金毛、拉布拉多、柯基和边牧。低置信度或不在支持列表内的结果进入“其他猫”或“其他狗”。

## 5. 核心数据模型

`animal-succour.sql` 是当前数据库结构和初始数据的唯一基线，不单独维护数据库迁移脚本。

| 表 | 作用 |
| --- | --- |
| `animals` | 动物档案，包含类别、品种、发现时间、地点、审核和领养状态 |
| `animal_images` | 动物图片 URL 与 `feature_vector` 二进制特征向量 |
| `category` | 猫狗一级分类字典 |
| `breed` | 受控品种字典，关联 `category_id` |
| `adopt` | 领养申请 |
| `adoption_record` | 已完成领养的归档记录 |
| `banner` | 首页轮播图 |
| `sys_user`、`sys_role`、`sys_menu` 等 | 登录、角色和菜单权限 |

### 5.1 图片和特征

- 动物图片路径写入 `animal_images.image_url`，示例：`/uploads/images/002.jpg`。
- 对应文件位于 `springboot/src/main/resources/static/uploads/images/`。
- 特征向量以 little-endian `float32` 序列化为 MySQL `blob`；Java 侧用 `VectorUtil` 还原并计算余弦相似度。
- 每次重导 SQL 时，应同步提交 SQL 中实际引用的图片。数据库引用与本地文件集合必须一致。

### 5.2 运行期文件

| 类型 | 数据库存储路径 | 物理目录 |
| --- | --- | --- |
| 动物图片 | `/uploads/images/...` | `springboot/src/main/resources/static/uploads/images/` |
| 用户头像 | `/profile/avatar/...` | `file/avatar/...` |
| 轮播图 | `/profile/upload/...` | `file/upload/...` |

后端将 `/uploads/**` 与 `/profile/**` 映射到相应目录。更换电脑部署时，数据库、动物图片与 `file/` 目录应一并迁移。

## 6. 服务接口

### 6.1 Python 服务

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/analyze_cat_dog_by_path?path=...` | 检测、裁剪、品种分类的聚合分析 |
| `GET` | `/extract_by_path?path=...` | 基于指定图片生成 ResNet50 特征 |

聚合分析成功时返回类别、品种、置信度、原始标签、Top 5 品种候选与裁剪路径；非猫狗时返回 `isCatDog=false` 和可直接展示的提示。

### 6.2 后端业务接口

常用入口包括：

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `POST` | `/api/recognition/identify` | 上传图片并查询匹配档案或建档建议 |
| `GET` | `/api/animals/list` | 动物列表与筛选 |
| `GET` | `/api/animals/{id}` | 动物详情 |
| `POST` | `/api/animals/add` | 新增动物档案 |
| `PUT` | `/api/animals` | 修改动物档案 |
| `GET` | `/api/animals/stats` | 首页动物统计 |
| `POST` | `/api/adoptions` | 提交领养申请 |

类别与品种字典由 `/api/categories`、`/api/breeds` 相关接口维护。前端以受控下拉框使用这些字典。

## 7. 本地运行

### 7.1 前置条件

- JDK 17、Maven 3.9+
- Node.js 18 LTS 或更高版本
- Python 3.10 至 3.12
- MySQL 8.0

### 7.2 初始化数据库

```powershell
mysql -u root -p -e "CREATE DATABASE `animal-succour` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"
mysql -u root -p animal-succour < animal-succour.sql
```

连接参数在 [application-druid.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application-druid.yml) 中。请勿将个人或生产环境凭据提交到仓库。

### 7.3 启动识别服务

```powershell
cd photo_recognize
python -m venv venv
.\venv\Scripts\activate
pip install -r requirement.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

### 7.4 启动后端

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

### 7.5 启动前端

```powershell
cd vue
npm install
npm run dev
```

访问 `http://localhost:90`。后端默认端口是 `8080`，Python 服务默认端口是 `8000`。

## 8. 局域网与构建

后端已监听 `0.0.0.0`，Vite 开发服务器启用局域网访问。将服务电脑和访问设备加入同一网络后，可通过 `http://部署电脑IPv4:90` 访问前端。Windows 防火墙需允许端口 `90`、`8080`；Python 服务仅供后端本机调用时，无需开放 `8000`。

前端构建：

```powershell
cd vue
npm run build:prod
```

如需把构建产物部署给其他设备，将 `vue/.env.production` 中的 `VITE_APP_BASE_API` 设置为后端可访问地址，而不是 `localhost`。

## 9. 数据维护与验证

### 9.1 全量重新分类

`photo_recognize/reclassify_animals.py` 会读取现有动物图片，执行检测、裁剪、类别与品种映射，并更新 `animals.category_id`、`animals.breed_id` 及 `animal_images.feature_vector`。完成后重新导出 `animal-succour.sql`，使数据库基线与代码保持一致。

### 9.2 裁剪图特征验证

`photo_recognize/verify_cropped_similarity.py` 会重新生成裁剪图特征，并与数据库中的向量比较。它用于确认特征是否按裁剪图正确写入数据库，不等同于跨图片个体识别准确率评估。

### 9.3 图片集合校验

每次更新 SQL 或图片后，核对以下三者：

1. `animal_images.image_url` 的数据库引用。
2. `springboot/src/main/resources/static/uploads/images/` 的本地文件。
3. Git 跟踪的上传图片。

三者应保持一一对应。开发日志、模型缓存、临时裁剪图、个人材料和本地 IDE 配置不应提交。

## 10. 已知边界与演进方向

- 识别模型没有针对校园流浪猫狗数据微调，复杂背景、遮挡、姿态变化和低清晰度会影响结果。
- EfficientNet-B0 的狗品种分类来自 ImageNet 通用标签，许多高置信度结果不属于当前支持字典，因此会被归入“其他狗”。
- 国际猫品种与校园常见的橘猫、狸花、奶牛、三花等毛色或花纹描述不完全对应。是否引入更贴近校园场景的业务标签，应在积累真实数据后再评估。
- 当前多数动物档案只有一张图，不能计算可靠的跨图片 Top-1、Top-5 个体识别准确率，也不应仅凭当前数据重新校准 `0.90` 阈值。
- 识别结果应始终保留人工确认入口；平台的价值在于流程可靠、数据规范、结果可控和模块可替换，而不是宣称模型能够独立完成档案或领养判断。

## 11. 提交约定

- 数据库结构和初始化数据以重新导出的 `animal-succour.sql` 为准，不新增迁移脚本。
- 更新 SQL 时同步核对动物图片和特征向量。
- Git 提交信息使用中文。
- 不提交虚拟环境、模型权重缓存、临时文件、开发日志、个人材料和 IDE 配置。
