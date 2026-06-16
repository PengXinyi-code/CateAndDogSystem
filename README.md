# CateAndDogSystem

基于图像识别的校园流浪动物管理系统，包含：

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

## 数据库配置

数据库配置位于 [application-druid.yml](/F:/CateAndDogSystem/springboot/src/main/resources/application-druid.yml)。

- 默认数据库名：`animal-succour`
- 默认用户名：`root`
- 默认密码：`123456`

初始化时导入根目录下的 `animal-succour.sql` 即可。

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
