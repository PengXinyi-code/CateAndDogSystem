# CateAndDogSystem 项目文档

> 文档基线：当前 `main` 分支源码、配置文件和 `animal-succour.sql`。本文只描述仓库中可直接核对的实现，不把规划或推测写为已实现功能。

## 1. 项目范围

CateAndDogSystem 是前后端分离的校园流浪猫狗管理与领养项目。

当前业务模块：

- 动物档案及其图片特征。
- 猫狗图片识别辅助建档与已有档案匹配。
- 领养申请、审核状态修改、撤销和领养记录。
- 轮播图、用户、角色、菜单和个人资料。

当前代码中没有线下人员调度、工单、处置记录、医疗档案或其他线下服务模块。

## 2. 工程结构

```text
CateAndDogSystem/
├─ springboot/
│  ├─ src/main/java/com/fast/animal/     动物、领养、识别、分类和文件模块
│  ├─ src/main/java/com/fast/system/     登录、JWT、用户、角色、菜单和通用模块
│  └─ src/main/resources/
│     ├─ application.yml                 服务、文件和匹配阈值配置
│     ├─ application-druid.yml           MySQL 数据源配置
│     ├─ mapper/animal/                  动物业务 Mapper XML
│     └─ static/uploads/images/          动物图片目录
├─ vue/src/
│  ├─ api/animal/                        动物、领养、轮播、类别、品种 API 封装
│  ├─ views/animal/                      管理端业务页面
│  ├─ views/userPage/                    用户端页面
│  ├─ store/                             Pinia 用户与动态路由状态
│  └─ permission.js                      登录态恢复和路由守卫
├─ photo_recognize/
│  ├─ main.py                            FastAPI 聚合分析与特征提取接口
│  ├─ reclassify_animals.py              全量重新分类与特征更新脚本
│  └─ verify_cropped_similarity.py       裁剪图特征验证脚本
├─ file/                                 头像和轮播图等运行期文件
├─ animal-succour.sql                    数据库导出基线
├─ README.md
└─ PROJECT_DOCUMENTATION.md
```

## 3. 技术与进程关系

| 层 | 当前实现 | 职责 |
| --- | --- | --- |
| 前端 | Vue 3、Vite、Pinia、Element Plus、Axios | 用户端、管理端、受控表单、登录态与接口调用 |
| 后端 | Java 17、Spring Boot 3、Spring Security、MyBatis、Druid | 业务处理、数据校验、文件、相似度计算、Python 调用 |
| 识别服务 | FastAPI、PyTorch、torchvision、timm、Ultralytics | 检测、裁剪、品种映射、特征提取 |
| 数据库 | MySQL | 业务数据、菜单、角色、图片路径与特征向量 |

```text
浏览器
  -> Vue 开发服务（默认 90）
  -> Spring Boot（默认 8080）
       -> MySQL
       -> FastAPI（默认 localhost:8000）
```

后端配置的监听地址是 `0.0.0.0:8080`；Python 服务地址目前写在 `PythonServiceImp` 中，为 `http://localhost:8000`。

## 4. 分类与动物档案

### 4.1 数据模型

导出 SQL 当前创建以下表：

| 表 | 用途 |
| --- | --- |
| `animals` | 动物档案与审核、领养状态 |
| `animal_images` | 图片访问路径与特征向量 |
| `category` | 一级类别字典 |
| `breed` | 品种字典 |
| `adopt` | 领养申请 |
| `adoption_record` | 已完成领养的记录 |
| `banner` | 轮播图 |
| `sys_user`、`sys_role`、`sys_menu`、`sys_user_role`、`sys_role_menu` | 用户、角色与菜单 |

`animals` 使用 `category_id`、`breed_id`。`category` 当前只导出 `cat`、`dog`；`breed` 当前有 10 条启用字典记录：

| 类别 | 品种代码和名称 |
| --- | --- |
| `cat` | `british_shorthair` 英短、`ragdoll` 布偶猫、`siamese` 暹罗猫、`maine_coon` 缅因猫、`other_cat` 其他猫 |
| `dog` | `golden_retriever` 金毛、`labrador_retriever` 拉布拉多、`corgi` 柯基、`border_collie` 边牧、`other_dog` 其他狗 |

### 4.2 表单校验与最终入库值

`AnimalServiceImp.normalizeCategoryAndBreed` 在新增和修改时执行以下规则：

1. 查询提交的 `categoryId`，要求类别存在且启用。
2. 查询提交的 `breedId`，要求品种存在且启用。
3. 要求品种的 `category_id` 等于提交类别的 `category_id`。
4. 未提交品种时，采用该类别的默认品种。

模型分析结果不覆盖表单提交的类别和品种。用户端新建弹窗会把识别结果中的 `categoryId`、`breedId` 作为初始选择；用户修改后，以最终提交值建档。

### 4.3 图片与特征保存

- 动物图片 URL 保存在 `animal_images.image_url`，访问形式为 `/uploads/images/...`。
- 图片物理目录由 `file.image-path` 配置，当前为 `springboot/src/main/resources/static/uploads/images`。
- 动物列表与详情通过 `animals` 左连接 `animal_images`、`category`、`breed` 查询显示字段。
- 新增时，后端会对上传文件或已有图片路径调用 Python 服务生成特征，再以 little-endian `float32` 序列化后写入 `animal_images.feature_vector`。
- 修改动物图片 URL 时，当前代码只更新 `animal_images.image_url`；不会重新计算该图片的特征向量。
- 删除动物时，代码尝试删除该动物当前图片文件，再删除 `animal_images` 和 `animals` 记录。

当已有 `imageUrl` 的特征提取失败时，`AnimalServiceImp` 会继续使用 512 维零向量写入。正常在线特征为 2048 维；该兜底向量会影响后续相似度计算，应作为当前实现限制看待。

## 5. 图片识别

### 5.1 Python 接口

| 方法 | 接口 | 返回职责 |
| --- | --- | --- |
| `GET` | `/analyze_cat_dog_by_path?path=...` | 猫狗检测、裁剪、品种分类和标签映射 |
| `GET` | `/extract_by_path?path=...` | ResNet50 特征向量 |

`/analyze_cat_dog_by_path` 的关键返回字段包括：`isCatDog`、`categoryCode`、`categoryName`、`categoryConfidence`、`breedCode`、`breedName`、`breedConfidence`、`rawDetectLabel`、`rawBreedLabel`、`cropPath` 和 `cropBox`。未检测到猫狗时，返回 `isCatDog=false`、`categoryCode=non_cat_dog` 与提示信息。

### 5.2 检测、类别和品种

`main.py` 使用 YOLOv8n 遍历检测结果，只考虑标签为 `cat` 或 `dog` 且置信度不低于 `0.40` 的候选。若存在多个符合条件的候选，选择置信度最高者。

这里的 YOLO 标签就是猫/狗大类：

```text
YOLOv8n 检测目标并判定 cat 或 dog
  -> cat：使用 GCViT-Tiny 猫品种模型
  -> dog：使用 EfficientNet-B0 狗品种模型
```

因此“检测猫狗”之后的分类是品种分类，不是第二次猫狗类别分类。

猫品种候选标签映射到英短、布偶、暹罗、缅因；狗品种候选标签映射到金毛、拉布拉多、柯基、边牧。猫品种阈值为 `0.25`，狗品种阈值为 `0.40`。候选为空、置信度不足或不在映射表中时，分别使用 `other_cat`、`other_dog`。

### 5.3 裁剪与特征

检测成功后，代码以检测框宽高的 `15%` 作为四周边距，并在图像边界内裁剪主体。品种分类和 ResNet50 特征提取都使用这张裁剪图。

ResNet50 去掉最后分类层，输出特征后执行 L2 归一化，并以 `float32` 返回。识别服务与建档服务都会清理本次生成的临时裁剪图。

### 5.4 后端匹配

`POST /api/recognition/identify` 的后端流程：

```text
接收 MultipartFile
  -> 保存到 file.temp-path
  -> 调用 analyze_cat_dog_by_path
  -> 非猫狗：返回 matched=false 和提示
  -> 根据 categoryCode 查询 category
  -> 根据 categoryId + breedCode 查询 breed，失败时使用该类别默认 breed
  -> 对 cropPath 调用 extract_by_path
  -> 查询相同 category_id 和 breed_id 的 animal_images
  -> 对每个候选向量计算余弦相似度
  -> 最大值大于 recognition.match-threshold 时返回 matched=true 与动物档案
  -> 否则返回 matched=false，并返回字典解析后的类别和品种
  -> 删除临时原图和裁剪图
```

当前 `recognition.match-threshold` 为 `0.90`。匹配候选明确按类别和品种双重过滤；品种分类错误会导致真实同一动物不在候选集中。

## 6. 领养实现

### 6.1 申请与撤销

`POST /api/adoptions` 创建申请时会：

1. 生成去掉连字符的 UUID 作为 `adopt_id`。
2. 从当前登录用户取得 `user_id`。
3. 把动物的 `status` 更新为字符串“审核中”。
4. 插入 `adopt` 记录。

`PUT /api/adoptions/revoke/{adoptId}` 只允许撤销状态为“审核中”的申请。撤销时把动物 `is_adopted` 设为 `false`，然后删除申请记录；当前实现不会恢复动物的 `status`。

### 6.2 完成申请

`PUT /api/adoptions` 在提交状态为“已完成”时，会查询完整申请，设置动物 `is_adopted=true`，并写入一条 `adoption_record`。当前代码没有在写入前检查是否已存在相同申请的领养记录。

## 7. 登录、角色与路由

### 7.1 登录态

- `POST /login` 返回 JWT Token。
- `GET /getInfo` 返回 `user`、`roles` 和 `permissions`。
- 前端把角色对象转换为 `roleKey`，若 `roleKey` 为空则使用 `roleName`。
- `permission.js` 在刷新页面后发现 Token 但 Pinia 角色为空时，先请求 `/getInfo` 和 `/getRouters`，再添加动态路由。

当前角色跳转规则：角色数组包含 `admin` 时默认进入 `/index`；其他角色默认进入 `/user/home`。普通用户访问 `/` 或 `/index` 时，路由守卫会跳转到 `/user/home`。

### 7.2 当前授权范围

`SecurityConfig` 当前放行 `/api/**`、`/uploads/**`、`/profile/**`、`/druid/**`、登录和注册等路径。前端对用户端和管理端页面有路由守卫，但后端没有对 `/api/**` 按角色限制访问。

这表示当前“管理员页面”与“管理员接口”不是同一层级的访问控制；前端跳转规则不构成后端授权。

## 8. HTTP 接口清单

### 8.1 动物与识别

| 方法 | 路径 |
| --- | --- |
| `GET` | `/api/animals/list` |
| `GET` | `/api/animals/stats` |
| `GET` | `/api/animals/{id}` |
| `POST` | `/api/animals/add` |
| `PUT` | `/api/animals` |
| `DELETE` | `/api/animals/{animalIds}` |
| `POST` | `/api/recognition/identify` |
| `POST` | `/api/common/upload` |

### 8.2 领养、轮播和字典

| 资源 | 路径前缀 | 已实现方法 |
| --- | --- | --- |
| 领养申请 | `/api/adoptions` | 列表、详情、新增、修改、删除、撤销、领养记录查询 |
| 轮播图 | `/api/banners` | 列表、详情、新增、修改、删除 |
| 品种 | `/api/breeds` | 列表、详情、新增、修改、删除 |
| 类别 | `/api/categories` | 列表、详情、新增、修改、删除 |

`CategoryServiceImpl` 会拒绝新增一级类别，且不允许停用或删除 `cat`、`dog`。

## 9. 运行与部署

### 9.1 环境

| 组件 | 当前要求或配置 |
| --- | --- |
| Java | JDK 17 |
| Maven | 用于后端构建 |
| Node.js | 用于 Vue 开发与构建 |
| Python | 安装 `photo_recognize/requirement.txt` |
| MySQL | 配置默认指向 `localhost:3306/animal-succour` |

### 9.2 命令

```powershell
# Python
cd photo_recognize
python -m uvicorn main:app --host 127.0.0.1 --port 8000

# Spring Boot（在仓库根目录）
$env:JAVA_HOME = 'D:\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -f springboot\pom.xml spring-boot:run

# Vue
cd vue
npm install
npm run dev
```

前端开发服务器默认端口为 `90`。局域网访问时，后端监听 `0.0.0.0:8080`；Vite 代理把前端请求转发到后端。Python 服务默认只给同机后端调用。

前端生产构建：

```powershell
cd vue
npm run build:prod
```

## 10. 数据维护脚本

### 10.1 `reclassify_animals.py`

该脚本读取数据库中的动物图片，调用 `analyze_cat_dog` 和 `extract_feature`，并更新动物类别、品种和图片特征。执行后需要重新导出 `animal-succour.sql`，同时核对动物图片目录。

### 10.2 `verify_cropped_similarity.py`

该脚本重新生成裁剪图特征并与数据库中的向量比较，也会按类别和品种分组计算候选相似度。它验证的是裁剪图特征是否与数据库向量一致，不是跨图片个体识别准确率评测。

## 11. 数据与提交约定

- `animal-succour.sql` 是数据库结构、数据与特征向量的导出基线，不新增或提交迁移脚本。
- SQL 中 `animal_images.image_url` 引用的图片应在 `springboot/src/main/resources/static/uploads/images/` 中存在。
- 导出 SQL 后，检查数据库引用、本地图片和 Git 跟踪图片的一致性。
- Git 提交信息使用中文。
- 不提交开发日志、个人材料、虚拟环境、模型权重缓存、临时目录或 IDE 配置。
