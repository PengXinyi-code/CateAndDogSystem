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

当前代码已经具备局域网开发访问所需配置：

- 后端监听 `0.0.0.0:8080`
- 前端开发服务监听所有网卡，端口为 `90`
- 开发环境通过 Vite 代理访问后端
- Python 图像识别服务由后端本机调用，不需要单独对外开放

局域网内其他设备访问前端时，地址格式如下：

```text
http://服务器电脑IP:90
```

例如：

```text
http://192.168.1.100:90
```

请确保：

- 访问设备和服务器电脑处于同一局域网
- 后端、前端、Python 服务都已启动
- Windows 防火墙已放行 `90` 和 `8080` 端口

## 文件存储说明

### 头像和轮播图

用户头像和轮播图现在都按相对路径保存，不再把磁盘绝对路径、`localhost` 或固定局域网 IP 写入数据库。

| 类型 | 物理目录 | 数据库存储示例 |
|---|---|---|
| 用户头像 | `./file/avatar/yyyy/MM/dd/` | `/profile/avatar/yyyy/MM/dd/demo.jpg` |
| 轮播图 | `./file/upload/yyyy/MM/dd/` | `/profile/upload/yyyy/MM/dd/demo.jpg` |

前端显示图片时，会基于当前后端地址拼接完整访问路径，所以项目换电脑、换端口、换局域网 IP 后不需要批量改数据库。

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
