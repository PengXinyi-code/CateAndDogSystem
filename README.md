# CateAndDogSystem

基于图像识别的校园流浪猫狗管理平台，包含：

- `springboot`：后端服务
- `vue`：前端管理端和用户端
- `photo_recognize`：Python 图像识别服务
- `file`：运行期文件目录，保存头像、轮播图等资源

## 项目启动

### 1. 后端

建议在项目根目录执行，保证 `${user.dir}` 指向仓库根目录：

```powershell
mvn -f springboot\pom.xml spring-boot:run
```

### 2. 前端

```powershell
cd vue
npm install
npm run dev
```

前端默认访问地址：

```text
http://localhost:90
```

### 3. Python 图像识别服务

```powershell
cd photo_recognize
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

如果你本地使用虚拟环境，也可以先激活 `venv` 再启动。

#### 进入 Python 服务目录

```powershell
cd photo_recognize
```

#### 激活虚拟环境

如果没有虚拟环境可跳过此步。

Windows PowerShell 使用：

```powershell
.\venv\Scripts\activate
```

或者 CMD 使用：

```cmd
call venv\Scripts\activate.bat
```

#### 安装依赖

首次运行或依赖更新时执行：

```powershell
pip install -r requirement.txt
```

#### 启动服务

当前 Python 服务包含两步能力：

- `/detect_cat_dog_by_path`：使用 YOLOv8n 先判断图片中是否存在猫或狗。
- `/extract_by_path`：使用 ResNet50 提取特征向量，用于同类动物档案相似度匹配。

首次启动或首次导入服务时，YOLOv8n 权重会自动下载为 `photo_recognize/yolov8n.pt`。该文件属于本地模型缓存，已被 `.gitignore` 忽略，不需要提交。

```powershell
uvicorn main:app --port 8000
```

## 图像识别流程

平台只支持猫、狗建档。用户上传图片后，系统按以下顺序完成分析：

1. Spring Boot 保存临时图片，并调用 Python 聚合接口 `/analyze_cat_dog_by_path`。
2. YOLOv8n 在图中检测猫或狗目标；若有多个目标，选择猫/狗候选中置信度最高的一个。未达到检测阈值的图片会被拦截，不再进入建档或相似度匹配。
3. 系统按检测框加 15% 边距裁剪主体，减少背景、人物和其他物体带来的干扰。
4. 根据类别调用对应品种分类器：猫使用 GCViT-Tiny 猫品种模型，狗使用 EfficientNet-B0 的 ImageNet 预训练分类能力。
5. 原始模型标签会映射到系统限定选项。当前猫支持英短、布偶、暹罗、缅因和其他猫；狗支持金毛、拉布拉多、柯基、边牧和其他狗。未命中支持列表或置信度不足时，统一归入其他猫或其他狗。
6. ResNet50 基于同一张裁剪图提取特征向量。后端只在相同类别和相同品种的档案图片中计算余弦相似度，达到匹配阈值时返回已有档案。
7. 未匹配时，前端使用识别结果自动填充类别和品种；用户仍可在系统下拉选项中确认或修改，最终以提交表单的 `categoryId` 和 `breedId` 为准。

上传时建议使用仅包含一只猫或狗、主体清晰且正面的照片。多目标、遮挡、主体过小或逆光图片会降低检测、品种分类和个体相似度匹配的稳定性。

### 阈值说明

| 阈值 | 当前值 | 用途 |
|---|---:|---|
| 猫狗检测阈值 | `0.40` | YOLOv8n 检测结果低于该值时按非猫狗拦截 |
| 猫品种阈值 | `0.25` | 猫品种模型命中系统支持标签的最低置信度 |
| 狗品种阈值 | `0.40` | 狗品种模型命中系统支持标签的最低置信度 |
| 相似度匹配阈值 | `0.90` | 同类别、同品种候选中认定为已有档案的最低余弦相似度 |

猫狗检测与品种阈值当前由 Python 服务的 `main.py` 常量维护；相似度匹配阈值由 Spring Boot 的 `recognition.match-threshold` 读取。

### 已完成的改进

- 分类数据标准化：动物档案使用 `category_id` 和 `breed_id`，不再接受自由填写的种类或品种。
- 非猫狗前置拦截：非猫狗图片不会进入特征匹配和新建档案流程。
- 主体裁剪后再分类和提取特征：特征向量更聚焦于猫狗主体，减少环境噪声。
- 分层识别：类别检测、品种映射、个体相似度匹配职责分离，便于独立调整阈值和替换模型。
- 候选集收敛：相似度匹配从全库比较收敛到相同类别、相同品种的图片集合。
- 前端受控确认：自动填充提升录入效率，受限下拉框保证最终入库数据规范。

### 当前局限与后续方向

- 当前模型均为预训练模型，没有使用校园流浪猫狗数据进行专门微调，因此对遮挡、低清晰度、特殊姿态和混合背景的适应性有限。
- 狗模型来自 ImageNet 通用类别，系统只保留少量校园场景更有代表性的品种；高置信度但不属于支持列表的结果仍会归入其他狗。
- 猫的品种分类与校园常见猫的描述方式并不完全一致。国际猫品种通常依据血统标准划分，而校园流浪猫更常以橘猫、狸花、奶牛、三花等毛色或花纹描述；两套分类体系并非一一对应。
- 目前每个动物档案通常只有一张图片，无法通过同一动物的跨图片检索计算真实的 Top-1 识别准确率。现有验证只能确认裁剪图特征已正确写入数据库。
- 系统定位是公益性校园救助与领养平台，核心价值在于可靠的建档流程、规范的数据结构、可解释的拦截与确认机制，以及可持续替换的识别模块架构，而不是训练新的前沿视觉模型。

## 数据库配置

数据库配置位于 [application-druid.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application-druid.yml)。

- 默认数据库名：`animal-succour`
- 默认用户名：`root`
- 默认密码：`123456`

初始化时导入根目录下的 `animal-succour.sql` 即可。

当前项目已收敛为“基于图像识别的校园流浪猫狗管理平台”。

数据库结构更新以重新导出的 `animal-succour.sql` 为准。

如果导入 SQL 时出现 `utf8mb4_0900_ai_ci` 相关报错，可替换为 `utf8mb4_general_ci` 后再导入。

## 局域网部署

适用场景：一台电脑作为服务器运行后端、前端和 Python 图像识别服务；手机开启热点，服务器电脑和另一台访问电脑都连接这个热点，然后访问电脑通过局域网 IP 打开系统。

### 换电脑部署时需要改或确认的地方

| 位置 | 是否需要改 | 说明 |
|---|---|---|
| [application-druid.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application-druid.yml) | 通常需要 | 按新电脑 MySQL 修改数据库名、用户名、密码；如果 MySQL 就装在部署电脑本机，`localhost:3306` 保持不变 |
| [application.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application.yml) | 通常不需要 | 后端已经监听 `0.0.0.0:8080`，支持本机和局域网访问 |
| [vue/.env.development](/F:/CateAndDogSystem/vue/.env.development) | 开发启动通常不需要 | 前端通过 `/dev-api` 交给 Vite 代理，代理目标默认是部署电脑本机后端 `http://localhost:8080` |
| [vue/.env.production](/F:/CateAndDogSystem/vue/.env.production) | 打包部署时需要 | 如果执行 `npm run build:prod` 后把 `dist` 当静态文件部署，需要把 `VITE_APP_BASE_API` 改成部署电脑的局域网地址，例如 `http://192.168.137.45:8080` |
| [PythonServiceImp.java](/F:/CateAndDogSystem/springboot/src/main/java/com/fast/succour/service/impl/PythonServiceImp.java) | 通常不需要 | 后端固定调用 `http://localhost:8000`，所以 Python 图像识别服务要和 Spring Boot 后端运行在同一台电脑 |
| Windows 防火墙 | 需要确认 | 放行 `90`、`8080`，Python 服务只给后端本机调用时不必对外放行 `8000` |
| 数据库文件和上传文件 | 需要迁移 | 新电脑需要导入 `animal-succour.sql`；如需保留旧图片，连同根目录 `file/` 一起复制 |

### 手机热点局域网访问步骤

1. 手机开启热点。
2. 部署电脑连接这个热点。
3. 访问电脑也连接同一个热点。
4. 在部署电脑上查看无线网卡的 IPv4 地址：

```powershell
ipconfig
```

在输出里找到当前热点对应的 WLAN/Wi-Fi 网卡，记录 `IPv4 地址`。手机热点下常见地址类似 `192.168.137.x`、`172.20.10.x` 或 `192.168.43.x`，以你电脑实际显示为准。

5. 在部署电脑启动三个服务：

```powershell
mvn -f springboot\pom.xml spring-boot:run
```

```powershell
cd photo_recognize
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

```powershell
cd vue
npm install
npm run dev
```

6. 在另一台电脑浏览器访问：

```text
http://部署电脑IPv4地址:90
```

例如：

```text
http://192.168.137.45:90
```

### 访问不通时优先检查

- 两台电脑是否都连在同一个手机热点下。
- 访问电脑能否访问部署电脑的 IP。可以先用 `ping 部署电脑IPv4地址` 判断基础网络是否连通。
- Windows 防火墙是否允许 Java/Spring Boot 的 `8080` 端口和 Node/Vite 的 `90` 端口入站访问。
- 前端控制台如果接口报错，先确认后端是否正常启动，并在部署电脑本机打开 `http://localhost:8080` 验证。
- 动物识别相关功能报错时，确认 Python 服务是否在部署电脑本机的 `127.0.0.1:8000` 正常运行。
- 部分手机热点可能开启了设备隔离，如果两台电脑互相不能 `ping` 通，需要更换热点设置、换一台手机热点，或改用路由器/Wi-Fi。

## 文件存储说明

### 头像和轮播图

用户头像和轮播图现在都按相对路径保存，不再把磁盘绝对路径、`localhost` 或固定局域网 IP 写入数据库。

| 类型 | 物理目录 | 数据库存储示例 |
|---|---|---|
| 用户头像 | `./file/avatar/yyyy/MM/dd/` | `/profile/avatar/yyyy/MM/dd/demo.jpg` |
| 轮播图 | `./file/upload/yyyy/MM/dd/` | `/profile/upload/yyyy/MM/dd/demo.jpg` |

前端显示图片时，会基于当前后端地址拼接完整访问路径，所以项目换电脑、换端口、换局域网 IP 后不需要批量改数据库。

### 换电脑后图片不显示的常见原因

图片保存在 `file/` 下，数据库保存 `/profile/...` 相对访问路径。后端启动时会自动识别项目根目录，并把 `/profile/**` 映射到项目根目录下的 `file/`，所以从项目根目录、`springboot` 目录或常见 IDE 工作目录启动，都应能找到同一份图片目录。

推荐启动方式仍然是：

```powershell
mvn -f springboot\pom.xml spring-boot:run
```

`/profile/upload/xxx.png` 会映射到：

```text
项目根目录/file/upload/xxx.png
```

排查方式：

- 打开浏览器开发者工具，查看图片实际请求地址。
- 如果数据库里是 `/profile/upload/...`，浏览器应请求 `http://后端IP:8080/profile/upload/...`。
- 把这个地址复制到浏览器单独打开；如果返回 404，优先确认项目根目录下是否确实存在对应文件。
- 如果数据库里是 `/uploads/images/...`，图片应存在于 `springboot/src/main/resources/static/uploads/images/`；这一路径也会按项目根目录解析。
- 如果生产打包后访问，确认 [vue/.env.production](/F:/CateAndDogSystem/vue/.env.production) 里的 `VITE_APP_BASE_API` 不是 `localhost`，而是部署电脑的局域网 IP。

### 文件删除策略

当前代码已经补上旧文件清理逻辑：

- 用户更换头像时，会删除旧头像文件
- 轮播图替换图片时，会删除旧图片文件
- 删除轮播图记录时，会同步删除图片文件

这样可以避免数据库记录删掉了，但磁盘图片还一直残留。

## 动物统计说明

首页动物统计已经改成独立统计口径，不再依赖分页列表推算。

当前后端提供专门的统计接口：

```text
GET /api/animal/stats
```

首页展示的总数、审核通过数、待审核数，都会直接以数据库聚合结果为准，避免出现“总数和状态数对不上”的情况。

## 与最开始版本的差异

| 项目 | 早期情况 | 当前情况 |
|---|---|---|
| 局域网访问 | README 和实际配置不完全一致 | 文档与实际端口、监听地址统一 |
| 图片存储 | 轮播图历史数据可能带完整 URL | 新数据统一保存相对路径 |
| 图片兼容 | `localhost` 图片只适合本机访问 | 前端会按当前后端地址重组访问路径 |
| 文件清理 | 换头像、删轮播图后旧文件可能残留 | 已补上旧文件删除逻辑 |
| 首页统计 | 统计口径混用分页结果 | 已改为独立聚合统计 |

## 开发说明

- `.vscode`、`.idea` 属于本地开发环境文件，不建议提交
- `file/` 是项目运行目录，需要保留
- 如果前端样式或接口数据看起来没更新，建议重启服务并强制刷新浏览器缓存
