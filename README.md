# CateAndDogSystem
基于图像识别的校园流浪动物管理系统

## 项目启动

### 1. 后端启动

```bash
cd springboot
mvn spring-boot:run
```

### 2. 前端启动

```bash
cd vue
npm install
npm run dev
```

### 3. Python 图像识别服务启动

```bash
# 进入 Python 服务目录
cd photo_recognize

# 激活虚拟环境（如果没有虚拟环境可跳过此步）
# Windows 使用：
.\venv\Scripts\activate
# 或者
call venv\Scripts\activate.bat

# 安装依赖（首次运行或依赖更新时）
pip install -r requirement.txt

# 启动服务
uvicorn main:app --port 8000
```

## 配置说明

### 数据库配置

请修改 `application-druid.yml` 文件中的数据库连接信息：

- 数据库 URL
- 用户名
- 密码

### 导入 SQL 文件

在 MySQL 中导入 `animal-succour.sql` 即可建立所有需要的数据库表
注意数据库名字需为animal-succour，不然需要修改密码，且默认是root，密码默认是123456，否则需要修改[application-druid.yml](springboot/src/main/resources/application-druid.yml)配置。

> **说明**：该文件已包含所有表结构（动物业务表 + 系统管理表）。

> **常见问题**：如果导入 SQL 时报错 `utf8mb4_0900_ai_ci`，请将文件中的 `utf8mb4_0900_ai_ci` 全部替换为 `utf8mb4_general_ci`。
>
> **原因**：`utf8mb4_0900_ai_ci` 是 MySQL 8.0 的排序规则，某些 MySQL 版本（如 5.7 或较老的 8.0）不支持该规则。`utf8mb4_general_ci` 是更通用的排序规则，兼容性更好。

### 路径配置

所有文件路径已在 `application.yml` 中统一配置，使用动态路径 `${user.dir}`，项目移动位置后无需修改代码：

```yaml
file:
  static-path: ${user.dir}/springboot/src/main/resources/static
  upload-path: ${user.dir}/springboot/src/main/resources/static/uploads
  image-path: ${user.dir}/springboot/src/main/resources/static/uploads/images
  temp-path: ${user.dir}/temp
```

> 注意：确保 `${user.dir}/temp` 目录存在

## 注意事项

> **浏览器推荐**：如果前端页面显示不全或有样式问题，建议使用兼容性更好的 **Google Chrome 浏览器**。

## 局域网部署说明

本项目已经具备局域网开发部署所需的配置：

- Spring Boot 显式监听 `0.0.0.0:8080`；
- Vite 监听所有网卡，端口为 `90`；
- 开发环境使用 `/dev-api`，由 Vite 代理到 `VITE_BACKEND_TARGET`；
- 后端允许局域网来源的跨域请求；
- Python 图像识别服务只由同一台电脑上的后端访问，无需向局域网开放。

### 1. 启动服务

建议从项目根目录启动后端，保证 `${user.dir}` 指向项目根目录：

请分别打开三个终端：

```powershell
# 终端 1：后端（项目根目录）
mvn -f springboot\pom.xml spring-boot:run

# 终端 2：前端
cd vue
npm run dev

# 终端 3：Python 图像识别服务
cd photo_recognize
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

前后端在同一台电脑运行时，`vue/.env.development` 保持以下配置即可，不需要改成局域网 IP：

```properties
VITE_APP_BASE_API = '/dev-api'
VITE_BACKEND_TARGET = 'http://localhost:8080'
```

### 2. 局域网访问

```
http://服务器电脑的IPv4地址:90
```

例如：`http://192.168.1.100:90`。

使用 `ipconfig` 查询服务器电脑的 IPv4 地址。访问设备需要和服务器电脑处于同一局域网，并在 Windows 防火墙中按需开放 TCP `90` 和 `8080` 端口。

### 3. 生产构建

生产构建不会使用 Vite 开发代理，需要把 API 地址设置为服务器电脑的局域网地址：

```powershell
cd vue
$env:VITE_APP_BASE_API='http://192.168.1.100:8080'
npm run build:prod
```

构建结果位于 `vue/dist`。仓库当前没有提供 Nginx 配置或自动部署脚本，需要自行选择静态文件服务器。

## 轮播图和头像路径说明

轮播图和用户头像现在都以“相对访问路径”写入数据库，不保存服务器磁盘绝对路径，也不固定保存 `localhost` 或某个局域网 IP。

| 类型 | 服务器磁盘目录 | 数据库存储示例 | 浏览器访问映射 |
|---|---|---|---|
| 用户头像 | `./file/avatar/yyyy/MM/dd/` | `/profile/avatar/yyyy/MM/dd/文件名.jpg` | `/profile/**` 映射到 `./file/` |
| 轮播图 | `./file/upload/yyyy/MM/dd/` | `/profile/upload/yyyy/MM/dd/文件名.jpg` | `/profile/**` 映射到 `./file/` |

`/common/upload` 的 `url` 和 `fileName` 字段返回相对路径，`fullUrl` 字段仅用于需要完整预览地址的场景。前端轮播图上传保存相对路径，展示时再根据当前 API 地址拼接，因此更换电脑、端口或局域网 IP 后不需要批量修改数据库。

### 旧轮播图数据兼容

项目初始 SQL 中的部分轮播图地址采用以下完整 URL：

```text
http://localhost:8080/profile/upload/yyyy/MM/dd/文件名.png
```

这种地址在本机访问时可以显示，但在局域网其他设备上，`localhost` 指向访问设备本身，而不是运行后端的服务器电脑，因此会出现轮播图无法显示的问题。

当前前端已经对此做了兼容：

- 首页轮播图遇到 `/profile` 或 `/uploads` 类型的完整 URL 时，会提取资源路径，并使用当前配置的后端地址重新拼接；
- 后台轮播图管理页面的图片缩略图采用相同处理；
- 新上传的轮播图只保存 `/profile/upload/...` 相对路径。

因此，旧数据可以继续显示，新数据也不会再绑定某台电脑的域名、IP 或端口。为了保持数据库内容统一，仍建议把旧数据转换成相对路径：

```sql
UPDATE banner
SET image = REPLACE(image, 'http://localhost:8080', '')
WHERE image LIKE 'http://localhost:8080/profile/%';
```

执行前请先备份 `banner` 表。如果旧数据使用的是其他 IP 或域名，请把 SQL 中的 `http://localhost:8080` 替换成实际前缀。

## 与最开始版本的对比

| 项目 | 最开始 | 现在 |
|---|---|---|
| 后端监听地址 | 未显式配置 | 明确为 `0.0.0.0:8080` |
| 前端局域网入口 | 已有 `host: true`，实际端口为 `90`，README 曾误写 `5173` | README 与实际端口统一为 `90` |
| 开发环境后端地址 | Vite 配置中固定为 `http://localhost:8080` | 可通过 `VITE_BACKEND_TARGET` 配置，默认仍为同机后端 |
| 生产 API 地址 | `/prod-api`，仓库内没有对应生产代理 | 构建时通过 `VITE_APP_BASE_API` 指定后端地址 |
| 头像数据库路径 | 已经是 `/profile/avatar/...` 相对路径 | 保持相对路径 |
| 轮播图数据库路径 | 上传接口可能返回并保存带域名/IP的完整 URL | 统一保存 `/profile/upload/...` 相对路径 |
| 旧轮播图兼容 | `localhost` 完整地址只能在服务器本机正常访问 | 自动提取资源路径并按当前后端地址访问 |
| 跨域 | 允许通配来源 | 允许通配来源并支持认证信息 |

结论：代码配置已经支持局域网访问；是否能从其他设备成功访问，还取决于实际 IP、服务是否启动、设备是否同网段以及防火墙端口是否放行。
