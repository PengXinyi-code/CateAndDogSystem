# 基于图像识别的校园流浪猫狗管理平台项目文档

> 项目名称：CateAndDogSystem  
> 文档日期：2026-07-01  
> 文档用途：项目梳理、课程设计说明、系统部署、测试设计、答辩准备和后续维护  
> 核验范围：当前仓库源码、配置文件、前端路由/API、后端 Controller/Service/Mapper、Python 图像识别服务、`animal-succour.sql` 数据脚本和根目录 `README.md`  
> 说明：本文以当前仓库真实代码为准。旧文档中提到的 `test-artifacts` 自动化测试目录在当前重新克隆的仓库中未出现，因此本文不把它作为当前项目资产描述。

## 1. 项目概述

本项目是一个面向校园场景的流浪猫狗管理平台。系统围绕“发现流浪猫狗、建立动物档案、展示待领养猫狗、提交领养申请、管理员审核、生成领养记录、通过图片识别辅助匹配猫狗档案”这一业务闭环实现。当前代码主要覆盖档案管理、识别匹配和领养申请管理，没有实现线下救助工单、救助人员调度或救助记录等救助业务。

项目由四个核心部分组成：

| 部分 | 路径 | 主要职责 |
|---|---|---|
| 后端服务 | `springboot/` | 提供登录认证、用户角色菜单管理、动物档案、领养申请、轮播图、分类、文件上传和图像识别转发接口 |
| 前端应用 | `vue/` | 提供普通用户端和后台管理端页面，使用 Vite 启动开发服务器 |
| 图像识别服务 | `photo_recognize/` | 使用 FastAPI + PyTorch ResNet50 提取图片特征向量 |
| 数据与资源 | `animal-succour.sql`、`file/`、`springboot/src/main/resources/static/uploads/images/` | 初始化数据库、运行期头像/轮播图、动物图片和历史样例图片 |

系统用户主要分为两类：

| 用户类型 | 主要行为 |
|---|---|
| 普通用户 | 注册登录、浏览首页、查看动物档案、上传照片识别动物、提交领养申请、查看和撤销自己的申请、维护个人信息 |
| 管理员 | 管理动物档案、审核领养申请、查看领养记录、管理轮播图、分类、用户、角色和菜单 |

当前项目更适合教学演示、课程设计和局域网部署验证。它已经具备比较完整的 CRUD、权限菜单、图片上传、领养流程和图像识别链路，但安全性、配置化、生产部署脚本和自动化测试仍有提升空间。

## 2. 总体架构

```text
浏览器
  |
  | HTTP，开发环境默认访问 http://localhost:90
  v
Vue 3 + Vite
  |
  | 开发环境：/dev-api 由 Vite 代理到 http://localhost:8080
  | 生产环境：VITE_APP_BASE_API 直连后端
  v
Spring Boot 3 后端，默认 0.0.0.0:8080
  |                         |
  | MyBatis / JDBC          | HTTP GET /extract_by_path
  v                         v
MySQL 8                 FastAPI，默认 127.0.0.1:8000
                            |
                            v
                    PyTorch ResNet50 特征提取
```

架构特点：

- 前后端分离：前端使用 Axios 请求后端 REST 接口，开发环境通过 Vite 代理解决跨域和局域网访问问题。
- 后端分层：Controller 处理 HTTP 请求，Service 承载业务流程，Mapper/XML 负责 SQL 映射。
- 权限模型：后端使用 Spring Security + JWT；后台菜单从数据库 `sys_menu` 动态生成。
- 图像识别：Python 服务只负责“根据本地图片路径提取特征”，相似度匹配由 Java 后端完成。
- 文件访问：后端将 `/uploads/**` 映射到 `springboot/src/main/resources/static/uploads/`，将 `/profile/**` 映射到根目录 `file/`。

## 3. 仓库结构

```text
CateAndDogSystem/
├─ springboot/                         Spring Boot 后端
│  ├─ pom.xml                          Maven 依赖与构建配置
│  └─ src/main/
│     ├─ java/com/fast/
│     │  ├─ succour/                   猫狗档案与领养业务模块
│     │  │  ├─ controller/             动物、领养、分类、轮播图、识别、上传接口
│     │  │  ├─ domain/                 业务实体
│     │  │  ├─ mapper/                 MyBatis Mapper 接口
│     │  │  └─ service/                业务服务接口与实现
│     │  └─ system/                    登录、权限、用户、角色、菜单、通用框架代码
│     └─ resources/
│        ├─ application.yml            主配置
│        ├─ application-druid.yml      数据源配置
│        ├─ mapper/                    MyBatis XML
│        └─ static/uploads/images/     动物样例图片与上传图片目录
├─ vue/                                Vue 3 前端
│  ├─ package.json                     前端依赖与脚本
│  ├─ vite.config.js                   Vite 配置，端口 90
│  └─ src/
│     ├─ api/                          Axios API 封装
│     ├─ router/                       静态路由
│     ├─ store/                        Pinia 状态管理
│     ├─ views/                        页面
│     ├─ layout/                       后台布局
│     └─ utils/                        请求、图片、缓存等工具
├─ photo_recognize/                    FastAPI 图像识别服务
│  ├─ main.py                          在线特征提取接口
│  ├─ generate_features.py             批量生成 animal_images 特征向量
│  ├─ rename.py                        辅助重命名脚本
│  └─ requirement.txt                  Python 依赖
├─ file/                               运行期文件目录，头像和轮播图等
├─ temp/                               临时目录，识别上传文件和 Tomcat 临时文件
├─ animal-succour.sql                  数据库初始化脚本
├─ README.md                           快速启动和局域网部署说明
└─ PROJECT_DOCUMENTATION.md            本文档
```

## 4. 技术栈

### 4.1 后端技术栈

配置来源：`springboot/pom.xml`。

| 技术 | 当前声明 | 用途 |
|---|---:|---|
| Spring Boot | Parent `3.5.0` | 后端基础框架 |
| Java | Spring Boot 3 运行基线，建议 JDK 17+ | 运行后端 |
| Spring Web | 随 Spring Boot 管理 | REST 接口 |
| Spring Security | 随 Spring Boot 管理 | 登录认证、JWT 过滤和接口授权 |
| MyBatis Spring Boot Starter | `3.0.3` | SQL 映射与数据访问 |
| PageHelper | `2.1.0` | 分页查询 |
| Druid | `1.2.23` | 数据库连接池 |
| MySQL Connector/J | 运行期依赖 | 连接 MySQL |
| JJWT | `0.9.1` | JWT 生成与解析 |
| Fastjson2 | `2.0.53` | JSON 处理 |
| Lombok | 未显式写版本，由依赖管理决定 | 实体简化 |
| Commons IO / Lang3 | `2.13.0` / 依赖管理 | 文件和字符串工具 |

注意：

- 后端使用 MyBatis，不是 MyBatis-Plus。
- `pom.xml` 中 Spring Boot Parent 是 `3.5.0`，同时又在 dependencies 中放入了 `spring-boot-dependencies 3.3.5` 的 import 依赖。这种写法不常见，通常应放在 `dependencyManagement` 中。若后续 Maven 构建出现依赖管理警告，应优先检查这里。

### 4.2 前端技术栈

配置来源：`vue/package.json`。

| 技术 | 当前版本 | 用途 |
|---|---:|---|
| Vue | `3.4.31` | 页面和组件开发 |
| Vue Router | `4.4.0` | 路由管理 |
| Pinia | `2.1.7` | 用户、权限等状态管理 |
| Element Plus | `2.12.0` | UI 组件库 |
| Axios | `0.28.1` | HTTP 请求 |
| ECharts | `5.5.1` | 首页统计图表 |
| Vite | `5.3.2` | 开发服务器和生产构建 |
| Sass | `1.77.5` | 样式预处理 |
| VXE Table / VXE UI | `4.x` | 部分弹窗和表格能力 |
| WangEditor | `5.x` | 富文本编辑器依赖 |
| vite-plugin-svg-icons | `2.0.1` | SVG 图标加载 |

### 4.3 图像识别技术栈

配置来源：`photo_recognize/main.py`、`photo_recognize/generate_features.py`、`requirement.txt`。

| 技术 | 用途 |
|---|---|
| FastAPI | 提供 `/extract_by_path` 特征提取接口 |
| Uvicorn | 启动 ASGI 服务 |
| PyTorch / torchvision | 加载 ResNet50 并进行前向推理 |
| Pillow | 打开和转换图片 |
| NumPy | 特征向量归一化和 float32 转换 |
| PyMySQL | 批量生成特征时更新 MySQL |
| tqdm | 批量脚本进度条 |

识别模型当前使用 `torchvision.models.resnet50(pretrained=True)`，删除最后一层分类层，输出 2048 维特征，并进行 L2 归一化。首次运行可能需要下载 ResNet50 预训练权重。

## 5. 配置说明

### 5.1 后端主配置

文件：`springboot/src/main/resources/application.yml`

| 配置项 | 当前值 | 说明 |
|---|---|---|
| `server.address` | `0.0.0.0` | 监听所有网卡，支持本机和局域网访问 |
| `server.port` | `8080` | 后端 HTTP 端口 |
| `server.servlet.context-path` | `/` | 应用根路径 |
| `server.tomcat.basedir` | `./temp` | Tomcat 临时目录 |
| `spring.profiles.active` | `druid` | 加载 `application-druid.yml` |
| `spring.servlet.multipart.max-file-size` | `10MB` | 单文件上传上限 |
| `spring.servlet.multipart.max-request-size` | `20MB` | 单次请求上传上限 |
| `token.header` | `Authorization` | JWT 请求头 |
| `token.secret` | `abcdefghijklmnopqrstuvwxyz` | JWT 密钥，教学环境默认值 |
| `token.expireTime` | `300` | Token 有效期，单位按代码配置为分钟 |
| `upload.path` | `springboot/src/main/resources/static/uploads/images` | 通用上传保存目录 |
| `file.static-path` | `springboot/src/main/resources/static` | 静态资源根目录 |
| `file.upload-path` | `springboot/src/main/resources/static/uploads` | `/uploads/**` 映射目录 |
| `file.image-path` | `springboot/src/main/resources/static/uploads/images` | 动物图片保存目录 |
| `file.temp-path` | `temp` | 图片识别临时目录 |

### 5.2 数据库配置

文件：`springboot/src/main/resources/application-druid.yml`

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/animal-succour?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

部署到新电脑时通常需要按本机 MySQL 修改：

- 数据库名：默认 `animal-succour`
- 用户名：默认 `root`
- 密码：默认 `123456`
- MySQL 地址：如果数据库就在本机，保持 `localhost:3306`

### 5.3 前端开发配置

文件：`vue/.env.development`

```env
VITE_APP_ENV = 'development'
VITE_APP_BASE_API = '/dev-api'
VITE_BACKEND_TARGET = 'http://localhost:8080'
```

文件：`vue/vite.config.js`

| 配置项 | 当前值 |
|---|---|
| 开发端口 | `90` |
| host | `true` |
| 自动打开浏览器 | `open: true` |
| `/dev-api` 代理目标 | `VITE_BACKEND_TARGET`，默认 `http://localhost:8080` |

开发环境访问路径：

```text
http://localhost:90
```

局域网设备访问路径：

```text
http://部署电脑IPv4:90
```

只要前端和后端都运行在同一台部署电脑，局域网设备访问前端时，请求会先到 Vite，再由 Vite 从部署电脑本机代理到 `localhost:8080`，所以开发环境一般不需要把 `VITE_BACKEND_TARGET` 改成局域网 IP。

### 5.4 前端生产配置

文件：`vue/.env.production`

```env
VITE_APP_ENV = 'production'
VITE_APP_BASE_API = 'http://localhost:8080'
VITE_BUILD_COMPRESS = gzip
```

如果打包后给其他设备访问，`VITE_APP_BASE_API` 不能保留 `localhost`，应改成后端所在电脑的局域网地址，例如：

```powershell
$env:VITE_APP_BASE_API='http://192.168.137.45:8080'
npm run build:prod
```

## 6. 数据库设计

SQL 脚本：`animal-succour.sql`

当前脚本创建 11 张表，并包含初始演示数据。

### 6.1 表清单

| 模块 | 表名 | 说明 | SQL 初始插入条数 |
|---|---|---|---:|
| 动物档案 | `animals` | 动物基础信息 | 120 |
| 动物图片 | `animal_images` | 图片路径和 ResNet 特征向量 | 120 |
| 领养申请 | `adopt` | 用户提交的领养申请 | 4 |
| 领养记录 | `adoption_record` | 审核完成后的领养归档 | 4 |
| 首页轮播 | `banner` | 轮播图配置 | 3 |
| 动物分类 | `category` | 猫、狗等分类 | 13 |
| 用户 | `sys_user` | 登录账号和个人资料 | 7 |
| 角色 | `sys_role` | 管理员、普通用户等角色 | 2 |
| 菜单 | `sys_menu` | 后台动态菜单 | 8 |
| 用户角色 | `sys_user_role` | 用户与角色关联 | 5 |
| 角色菜单 | `sys_role_menu` | 角色与菜单关联 | 0 |

当前数据库结构以重新导出的 `animal-succour.sql` 为准。`category` 已收敛为一级分类“猫/狗”，`animals` 通过 `category_id`、`breed_id` 关联标准分类与品种字典，业务接口统一使用标准分类字段。

### 6.2 核心业务表

#### `animals`

动物档案主表。

| 字段 | 说明 |
|---|---|
| `id` | 动物 ID，自增主键 |
| `name` | 动物名称 |
| `first_found_time` | 初次发现时间 |
| `location` | 发现位置 |
| `is_adopted` | 是否已被领养 |
| `status` | 审核状态，页面中使用 `pending`、`approved`、`rejected` |
| `description` | 动物介绍 |
| `image_url` | SQL 中保留字段，但当前查询主要从 `animal_images.image_url` 左连接取得 |

#### `animal_images`

动物图片和识别特征表。

| 字段 | 说明 |
|---|---|
| `id` | 图片 ID |
| `animal_id` | 所属动物 ID，外键关联 `animals.id` |
| `image_url` | 图片访问路径，例如 `/uploads/images/002.jpg` |
| `feature_vector` | ResNet50 特征向量序列化后的 `blob` |

图片特征由 Python 服务或批量脚本生成。Java 后端按 little-endian 把 `blob` 还原为 `float[]`，再计算余弦相似度。

#### `adopt`

领养申请表。

| 字段 | 说明 |
|---|---|
| `adopt_id` | 申请 ID，Java 使用 UUID 去横线生成 |
| `animal_id` | 申请领养的动物 ID |
| `name` | 申请人姓名 |
| `phone` | 联系电话 |
| `email` | 电子邮箱 |
| `address` | 居住地址 |
| `occupation` | 职业或学院专业 |
| `reason` | 申请理由 |
| `pet_experience` | 养宠经验 |
| `status` | 申请状态，默认 `审核中` |
| `description` | 处理说明 |
| `user_id` | 提交申请的登录用户 |
| `create_time` / `update_time` | 创建和更新时间 |

#### `adoption_record`

领养完成记录表。管理员将申请状态改为 `已完成` 后，后端会把申请信息复制到此表。

| 字段 | 说明 |
|---|---|
| `id` | 记录 ID |
| `animal_id` | 动物 ID |
| `animal_name` | 动物名称 |
| `user_id` | 领养人 ID |
| `user_name` | 领养人姓名 |
| `phone`、`email`、`address`、`occupation` | 领养人联系信息 |
| `adopt_time` | 领养时间 |
| `remark` | 备注 |
| `create_time` | 创建时间 |

### 6.3 权限表关系

```text
sys_user
  |
  | sys_user_role
  v
sys_role
  |
  | sys_role_menu
  v
sys_menu
```

当前 SQL 中 `sys_role_menu` 初始插入条数为 0，因此角色菜单关联需要重点核验。系统也可能通过默认角色或已有代码逻辑加载菜单，但答辩和测试时应实际登录确认后台菜单是否完整显示。

### 6.4 初始化账号

旧文档和 README 参考信息中提到管理员账号为：

```text
admin / admin123
```

当前后端密码编码器是明文比较，不进行 BCrypt 加密。该设计便于教学演示，但不适合生产环境。

## 7. 后端模块说明

### 7.1 启动入口

| 文件 | 说明 |
|---|---|
| `springboot/src/main/java/com/fast/fastEasyApplication.java` | Spring Boot 主启动类 |
| `springboot/src/main/java/com/fast/system/fastServletInitializer.java` | WAR 部署初始化类 |

### 7.2 猫狗档案与领养业务模块

路径：`springboot/src/main/java/com/fast/succour/`

| 子包 | 说明 |
|---|---|
| `controller` | 对外 REST 接口 |
| `domain` | `Animal`、`AnimalImage`、`Adopt`、`AdoptionRecord`、`Banner`、`Category` |
| `mapper` | MyBatis Mapper 接口 |
| `service` | 业务服务接口 |
| `service/impl` | 业务服务实现、Python 调用、向量工具、上传实现 |

核心服务：

| 服务 | 说明 |
|---|---|
| `AnimalServiceImp` | 动物档案查询、统计、新增、修改、删除；新增时保存图片、调用 Python 提特征、写入 `animal_images` |
| `AdoptServiceImpl` | 领养申请提交、审核、撤销、写入领养记录 |
| `RecognitionServiceImp` | 保存临时图片、调用 Python 提取特征、遍历数据库向量并计算相似度 |
| `PythonServiceImp` | HTTP 调用 `http://localhost:8000/extract_by_path?path=...` |
| `FileUploadServiceImp` | 保存上传图片到 `/uploads/images/` |
| `BannerServiceImp` | 轮播图 CRUD，并在替换或删除时清理旧图片 |
| `VectorUtil` | `blob` 与 `float[]` 转换、余弦相似度计算 |

### 7.3 系统管理模块

路径：`springboot/src/main/java/com/fast/system/`

| 模块 | 说明 |
|---|---|
| 登录注册 | `SysLoginController`、`SysRegisterController`、`SysLoginService`、`SysRegisterService` |
| JWT | `TokenService`、`JwtAuthenticationTokenFilter` |
| 用户管理 | `SysUserController`、`SysUserServiceImpl`、`SysUserMapper.xml` |
| 角色管理 | `SysRoleController`、`SysRoleServiceImpl`、`SysRoleMapper.xml` |
| 菜单管理 | `SysMenuController`、`SysMenuServiceImpl`、`SysMenuMapper.xml` |
| 个人中心 | `SysProfileController` |
| 通用文件 | `CommonController`、`FileUploadUtils`、`LocalUploadFileUtils` |
| 全局异常 | `GlobalExceptionHandler` |

## 8. 后端接口清单

### 8.1 登录与用户会话

| 方法 | 路径 | 说明 | 后端权限 |
|---|---|---|---|
| `POST` | `/login` | 登录，返回 Token | 匿名 |
| `POST` | `/register` | 注册普通用户 | 匿名 |
| `POST` | `/logout` | 退出登录 | 需要登录 |
| `GET` | `/getInfo` | 当前用户信息和角色 | 需要登录 |
| `GET` | `/getRouters` | 当前用户动态菜单路由 | 需要登录 |

### 8.2 个人中心

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/user/profile` | 查询个人信息 |
| `PUT` | `/system/user/profile` | 修改个人信息 |
| `PUT` | `/system/user/profile/updatePwd` | 修改密码 |
| `POST` | `/system/user/profile/avatar` | 上传头像 |

当前 `SysProfileController.updateProfile` 设置了用户对象字段，但最后直接返回“修改个人信息异常，请联系管理员”，没有调用 `userService.updateUserProfile`。这意味着个人资料修改流程需要实际测试验证，可能存在未完成实现。

### 8.3 动物档案

| 方法 | 路径 | 说明 | 后端权限 |
|---|---|---|---|
| `GET` | `/api/animal/list` | 分页查询动物，支持名称、类别、品种、审核状态、领养状态筛选 | 匿名放行 |
| `GET` | `/api/animal/stats` | 动物统计，返回总数、通过、待审、拒绝、已领养、未领养 | 匿名放行 |
| `GET` | `/api/animal/{id}` | 查询动物详情 | 匿名放行 |
| `POST` | `/api/animal/add` | 新增动物，支持表单字段、图片文件或已上传图片 URL | 匿名放行 |
| `PUT` | `/api/animal` | 修改动物 | 匿名放行 |
| `DELETE` | `/api/animal/{animalIds}` | 删除一个或多个动物 | 匿名放行 |

注意：后端放行 `/api/**` 是为了演示方便，但前端路由守卫仍要求用户登录后才能进入大多数页面。

### 8.4 图片识别与上传

| 方法 | 路径 | 说明 | 后端权限 |
|---|---|---|---|
| `POST` | `/api/recognition/identify` | 上传图片并识别最相似动物 | 匿名放行 |
| `POST` | `/api/common/upload` | 上传图片到 `/uploads/images/` | 匿名放行 |
| `POST` | `/common/upload` | 系统通用上传接口 | 需要登录 |
| `GET` | `/common/download` | 通用下载接口 | 需要登录 |
| `GET` | `/common/download/resource` | 静态资源下载 | 需要登录 |

### 8.5 领养申请与记录

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| `GET` | `/succour/adopt/list` | 查询领养申请列表 | 需要登录 |
| `GET` | `/succour/adopt/{adoptId}` | 查询申请详情 | 需要登录 |
| `POST` | `/succour/adopt` | 提交领养申请 | 需要登录 |
| `PUT` | `/succour/adopt` | 修改或审核申请 | 需要登录 |
| `DELETE` | `/succour/adopt/{adoptIds}` | 删除申请 | 需要登录 |
| `PUT` | `/succour/adopt/revoke/{adoptId}` | 撤销审核中的申请 | 需要登录 |
| `GET` | `/succour/adopt/record/list` | 查询领养记录 | 后端匿名放行 |
| `GET` | `/succour/adopt/record/{id}` | 查询领养记录详情 | 后端匿名放行 |
| `GET` | `/succour/adopt/record/animal/{animalId}` | 按动物查询最新领养记录 | 后端匿名放行 |

### 8.6 轮播图与分类

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/succour/banner/list` | 查询轮播图 |
| `GET` | `/succour/banner/{bannerId}` | 查询轮播图详情 |
| `POST` | `/succour/banner` | 新增轮播图 |
| `PUT` | `/succour/banner` | 修改轮播图 |
| `DELETE` | `/succour/banner/{bannerIds}` | 删除轮播图 |
| `GET` | `/sccour/category/list` | 查询分类 |
| `GET` | `/sccour/category/{categoryId}` | 查询分类详情 |
| `POST` | `/sccour/category` | 新增分类 |
| `PUT` | `/sccour/category` | 修改分类 |
| `DELETE` | `/sccour/category/{categoryIds}` | 删除分类 |

注意：分类接口前缀是 `/sccour/category`，其中 `sccour` 是当前代码中的拼写。前后端保持一致即可，不要只在一端改成 `succour`。

### 8.7 系统管理

| 模块 | 主要接口 |
|---|---|
| 用户管理 | `/system/user/list`、`/system/user/{userId}`、`POST /system/user`、`PUT /system/user`、`DELETE /system/user/{userIds}`、`/resetPwd`、`/changeStatus` |
| 角色管理 | `/system/role/list`、`/system/role/{roleId}`、`POST /system/role`、`PUT /system/role`、`DELETE /system/role/{roleIds}`、`/changeStatus`、角色授权相关接口 |
| 菜单管理 | `/system/menu/list`、`/system/menu/{menuId}`、`/treeselect`、`/roleMenuTreeselect/{roleId}`、`POST /system/menu`、`PUT /system/menu`、`DELETE /system/menu/{menuId}` |

## 9. 前端页面与功能

### 9.1 路由结构

静态路由文件：`vue/src/router/index.js`

| 路由 | 页面 | 说明 |
|---|---|---|
| `/login` | `views/login.vue` | 登录 |
| `/register` | `views/register.vue` | 注册 |
| `/index` | `views/index.vue` | 后台首页和统计 |
| `/user/home` | `views/userPage/home.vue` | 普通用户首页、轮播图、识别入口、推荐动物 |
| `/user/animal` | `views/userPage/animal.vue` | 用户端动物列表 |
| `/user/animalDetail/:id` | `views/userPage/animalDetail.vue` | 动物详情和领养申请 |
| `/user/record` | `views/userPage/record.vue` | 我的申请 |
| `/user/self` | `views/system/user/profile/index.vue` | 个人中心 |
| `/user/profile` | `views/system/user/profile/index.vue` | 后台布局下的个人中心 |
| `/adoption-record/list` | `views/succour/record/index.vue` | 领养记录 |

后台业务页面：

| 页面 | 文件 | 主要功能 |
|---|---|---|
| 动物档案管理 | `views/succour/animals/index.vue` | 查询、新增、修改、删除、图片上传、审核状态维护 |
| 轮播图管理 | `views/succour/banner/index.vue` | 轮播图 CRUD |
| 领养申请审核 | `views/succour/adopt/index.vue` | 申请查询、审核、删除 |
| 领养记录 | `views/succour/record/index.vue` | 查看已完成领养记录 |
| 用户管理 | `views/system/user/index.vue` | 用户 CRUD、重置密码、状态修改 |
| 角色管理 | `views/system/role/index.vue` | 角色 CRUD 和授权 |
| 菜单管理 | `views/system/menu/index.vue` | 菜单 CRUD |

### 9.2 前端权限控制

文件：`vue/src/permission.js`

前端白名单只有：

```js
const whiteList = ['/login', '/register']
```

因此：

- 没有 Token 时，访问非白名单页面会跳转到 `/login`。
- 有 Token 后，前端请求 `/getInfo` 获取角色，再请求 `/getRouters` 生成动态路由。
- 后端虽然放行了 `/api/**`，但普通用户页面仍会被前端路由守卫保护。

### 9.3 请求封装

文件：`vue/src/utils/request.js`

主要行为：

- `baseURL` 使用 `import.meta.env.VITE_APP_BASE_API`。
- 如果本地存在 Token，会自动添加请求头：

```text
Authorization: Bearer <token>
```

- GET 参数会拼接到 URL。
- POST/PUT 默认启用 1 秒内重复提交拦截。
- 统一处理业务状态码 `401`、`500`、`601` 和网络异常。

### 9.4 图片路径处理

前端使用 `vue/src/utils/image.js` 中的 `resolveImageUrl` 统一拼接图片路径。当前项目中存在两类图片访问路径：

| 类型 | 访问前缀 | 物理目录 |
|---|---|---|
| 动物样例和上传图片 | `/uploads/**` | `springboot/src/main/resources/static/uploads/` |
| 头像、轮播图等 profile 文件 | `/profile/**` | 根目录 `file/` |

## 10. 核心业务流程

### 10.1 登录流程

```text
用户输入账号密码
  -> 前端 POST /login
  -> 后端 SysLoginService 校验用户
  -> TokenService 生成 JWT
  -> 前端保存 Token
  -> 前端 GET /getInfo
  -> 前端 GET /getRouters
  -> 根据角色加载用户端或后台菜单
```

当前密码校验为明文比较，适合课程演示，不适合真实生产环境。

### 10.2 动物档案新增流程

```text
管理员或用户填写动物信息
  -> 前端先通过 /api/common/upload 上传图片，或在 /api/animal/add 中直接带 file
  -> 后端保存图片到 springboot/src/main/resources/static/uploads/images
  -> 后端调用 Python /extract_by_path 提取特征
  -> 后端写入 animals
  -> 后端写入 animal_images，包括 image_url 和 feature_vector
```

如果新增动物时只传 `imageUrl`，后端会根据 `file.static-path` 拼出本地路径，再调用 Python 服务提取特征。

如果特征提取失败，`AnimalServiceImp` 会使用一个 512 维零向量兜底。但 ResNet50 正常输出是 2048 维，后续识别时如果遇到维度不一致，`VectorUtil.cosineSimilarity` 可能出现数组越界风险。这是后续应修复的点。

### 10.3 图片识别流程

```text
用户在首页上传图片
  -> 前端 POST /api/recognition/identify
  -> 后端保存临时文件到 temp/
  -> 后端调用 http://localhost:8000/extract_by_path
  -> Python 返回 2048 维归一化特征
  -> 后端读取 animal_images 中所有 feature_vector
  -> Java 逐条计算余弦相似度
  -> 最大相似度 > 0.90 时返回匹配动物
  -> 否则返回未匹配
  -> 删除临时文件
```

注意：

- `RecognitionServiceImp` 中声明了 `THRESHOLD = 0.85`，但实际判断代码写的是 `if (maxSim > 0.90)`，所以当前真实阈值是 `0.90`。
- Python 服务地址在 Java 中写死为 `http://localhost:8000`，因此默认要求 Spring Boot 和 Python 服务运行在同一台电脑。
- 识别服务通过本地路径读取图片，所以 Python 进程必须能访问 Java 保存的同一份文件系统路径。

### 10.4 领养申请流程

```text
用户打开动物详情
  -> 点击申请领养
  -> 填写姓名、电话、邮箱、地址、学院专业、申请理由、养宠经验
  -> 前端 POST /succour/adopt
  -> 后端生成 adopt_id，写入 user_id 和 create_time
  -> 后端将动物 status 改为“审核中”
  -> 管理员在后台审核申请
  -> 如果状态改为“已完成”
     -> 后端设置动物 isAdopted = true
     -> 写入 adoption_record
```

撤销流程：

```text
用户撤销审核中申请
  -> PUT /succour/adopt/revoke/{adoptId}
  -> 后端校验状态必须为“审核中”
  -> 后端设置动物 isAdopted = false
  -> 删除 adopt 申请记录
```

当前撤销逻辑没有明确将动物 `status` 恢复为 `approved` 或其他可领养状态，只更新了 `isAdopted=false`。如果前端列表按 `status=approved` 展示待领养动物，撤销后该动物可能仍处于“审核中”状态，需要测试确认并考虑修复。

### 10.5 领养记录生成流程

管理员把申请状态改为 `已完成` 后：

1. `AdoptServiceImpl.updateAdopt` 先更新 `adopt` 表。
2. 查询完整申请信息。
3. 将动物 `isAdopted` 设置为 `true`。
4. 构造 `AdoptionRecord`。
5. 查询动物名称并写入 `animal_name`。
6. 插入 `adoption_record`。

该流程没有看到“防重复写入领养记录”的判断。如果同一申请被重复提交为 `已完成`，理论上可能重复插入记录，应作为测试关注点。

## 11. 图像识别服务

### 11.1 在线服务

文件：`photo_recognize/main.py`

启动方式：

```powershell
cd F:\CateAndDogSystem\photo_recognize
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

接口：

```text
GET /extract_by_path?path=本地图片绝对路径
```

返回：

```json
{
  "feature": [0.001, 0.002, "..."]
}
```

处理流程：

1. 使用 PIL 打开图片并转为 RGB。
2. Resize 到 `224x224`。
3. 使用 ImageNet mean/std 归一化。
4. 输入 ResNet50 去掉分类层后的模型。
5. 得到 2048 维向量。
6. L2 归一化。
7. 转为 float32 并返回列表。

### 11.2 批量特征脚本

文件：`photo_recognize/generate_features.py`

用途：读取数据库 `animal_images` 表中的图片路径，逐张计算特征并更新 `feature_vector`。

默认数据库配置：

```python
DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "123456",
    "database": "animal-succour",
    "charset": "utf8mb4"
}
```

默认图片根目录：

```text
photo_recognize/../springboot/src/main/resources/static
```

运行：

```powershell
cd F:\CateAndDogSystem\photo_recognize
python generate_features.py
```

适用场景：

- 初次导入图片但没有特征向量。
- 修改了模型或预处理方式，需要重新生成特征。
- 数据库迁移后需要修复 `animal_images.feature_vector`。

## 12. 文件存储与静态资源

### 12.1 动物图片

动物图片主要保存到：

```text
springboot/src/main/resources/static/uploads/images/
```

数据库保存访问路径：

```text
/uploads/images/xxx.jpg
```

后端映射：

```text
/uploads/** -> springboot/src/main/resources/static/uploads/
```

访问示例：

```text
http://localhost:8080/uploads/images/002.jpg
```

### 12.2 头像和轮播图

`fast.profile` 当前配置为：

```yaml
fast:
  profile: ./file
```

后端映射：

```text
/profile/** -> ./file/
```

常见路径：

| 类型 | 物理目录 | 数据库存储示例 |
|---|---|---|
| 用户头像 | `file/avatar/...` | `/profile/avatar/...` |
| 轮播图 | `file/upload/...` | `/profile/upload/...` |

### 12.3 路径解析工具

文件：`ProjectPathUtils.java`

它会从当前 `user.dir` 开始向上查找项目根目录，判断依据是同时存在：

```text
springboot/pom.xml
vue/package.json
```

这使得从项目根目录或 `springboot` 子目录启动时，都尽量能解析到同一个项目根目录。

推荐启动后端方式仍然是：

```powershell
cd F:\CateAndDogSystem
mvn -f springboot\pom.xml spring-boot:run
```

## 13. 本机部署步骤

### 13.1 环境建议

| 软件 | 建议版本 |
|---|---|
| Windows | Windows 10/11 |
| JDK | 17 或 21 |
| Maven | 3.9.x |
| Node.js | 18 LTS 或 20 LTS |
| MySQL | 8.0 |
| Python | 3.10 到 3.12 更稳妥，需匹配 PyTorch 可安装版本 |
| 浏览器 | Chrome、Edge 或 Firefox |

### 13.2 初始化数据库

```powershell
mysql -u root -p
```

```sql
CREATE DATABASE `animal-succour`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

导入 SQL：

```powershell
mysql -u root -p animal-succour < animal-succour.sql
```

如果 MySQL 版本不支持 `utf8mb4_0900_ai_ci`，可将 SQL 中该排序规则替换为：

```text
utf8mb4_general_ci
```

### 13.3 启动 Python 识别服务

```powershell
cd F:\CateAndDogSystem\photo_recognize
python -m pip install -r requirement.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

验证：

```text
http://localhost:8000/docs
```

### 13.4 启动后端

```powershell
cd F:\CateAndDogSystem
mvn -f springboot\pom.xml spring-boot:run
```

验证：

```text
http://localhost:8080
http://localhost:8080/druid/
```

### 13.5 启动前端

```powershell
cd F:\CateAndDogSystem\vue
npm install
npm run dev
```

访问：

```text
http://localhost:90
```

## 14. 局域网部署

适用场景：一台电脑同时运行 MySQL、Spring Boot、Python 和 Vite，另一台电脑通过同一局域网访问。

步骤：

1. 部署电脑连接 Wi-Fi 或手机热点。
2. 访问电脑连接同一网络。
3. 在部署电脑执行：

```powershell
ipconfig
```

4. 找到当前网卡 IPv4 地址，例如：

```text
192.168.137.45
```

5. 在部署电脑启动三个服务：

```powershell
cd F:\CateAndDogSystem
mvn -f springboot\pom.xml spring-boot:run
```

```powershell
cd F:\CateAndDogSystem\photo_recognize
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

```powershell
cd F:\CateAndDogSystem\vue
npm run dev
```

6. 访问电脑打开：

```text
http://192.168.137.45:90
```

局域网注意事项：

- 后端已监听 `0.0.0.0:8080`。
- Vite 已设置 `host: true` 和端口 `90`。
- 防火墙需要允许 TCP `90` 和 `8080` 入站。
- Python `8000` 默认只被同机后端访问，不必向局域网开放。
- 如果两台电脑互相 `ping` 不通，可能是热点隔离或防火墙策略问题。

## 15. 生产构建建议

前端生产构建：

```powershell
cd F:\CateAndDogSystem\vue
npm run build:prod
```

输出目录：

```text
vue/dist
```

当前仓库没有提供 Nginx 配置，也没有把 `vue/dist` 自动复制到 Spring Boot 静态目录的脚本。因此生产部署有两种常见方式：

| 方式 | 说明 |
|---|---|
| Nginx 部署前端 | Nginx 托管 `vue/dist`，反向代理 API 到 `8080` |
| Spring Boot 托管前端 | 手动把构建产物复制到后端静态资源目录，但需要规划刷新路由 fallback |

如果前端静态文件给局域网其他设备访问，构建前设置：

```powershell
$env:VITE_APP_BASE_API='http://部署电脑IPv4:8080'
npm run build:prod
```

## 16. 测试建议

### 16.1 冒烟测试

| 编号 | 用例 | 预期 |
|---|---|---|
| S01 | 后端启动 | `8080` 可访问，无数据库连接错误 |
| S02 | 前端启动 | `90` 可访问，登录页显示 |
| S03 | Python 启动 | `8000/docs` 可访问 |
| S04 | 管理员登录 | 登录成功，进入后台或首页 |
| S05 | 普通用户注册 | 注册成功，可登录 |
| S06 | 首页加载 | 轮播图、推荐动物加载正常 |
| S07 | 动物列表 | 分页、名称、类别、品种、状态筛选正常 |
| S08 | 动物详情 | 图片、名称、介绍加载正常 |
| S09 | 图片识别 | 上传图片后返回匹配或未匹配结果 |
| S10 | 领养申请 | 用户能提交申请，后台能看到记录 |
| S11 | 审核完成 | 状态改为已完成后生成领养记录 |
| S12 | 文件访问 | `/uploads/...` 和 `/profile/...` 图片可打开 |

### 16.2 业务测试重点

领养流程：

- 同一动物是否允许重复提交多个审核中申请。
- 撤销审核中申请后，动物是否重新出现在待领养列表。
- 管理员拒绝申请后，动物状态是否符合预期。
- 重复把申请改为 `已完成` 是否会重复插入 `adoption_record`。

图片识别：

- Python 服务未启动时，前端提示是否友好。
- 上传非图片文件是否被前端阻止。
- 大于 5MB 或 10MB 的图片是否被前端/后端正确拒绝。
- 数据库中存在空向量或维度异常向量时，识别是否报错。
- 新增动物后是否能被识别出来。

文件管理：

- 删除动物后，对应图片文件是否删除。
- 替换轮播图后，旧图片是否删除。
- 删除轮播图记录后，图片文件是否删除。
- 更换头像后，旧头像是否删除。

权限：

- 未登录访问 `/user/home` 是否被前端重定向到登录页。
- 未登录直接请求 `/api/animal/list` 是否能返回数据。
- 普通用户是否能访问后台管理页面。
- 角色菜单配置为空时，后台菜单是否符合预期。

### 16.3 接口测试建议

可以使用 Postman、Apifox 或 curl 测试。

登录：

```http
POST http://localhost:8080/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

动物列表：

```http
GET http://localhost:8080/api/animal/list?pageNum=1&pageSize=10&status=approved
```

动物统计：

```http
GET http://localhost:8080/api/animal/stats
```

领养记录：

```http
GET http://localhost:8080/succour/adopt/record/list?pageNum=1&pageSize=10
```

## 17. 当前已知问题与风险

| 级别 | 问题 | 影响 | 建议 |
|---|---|---|---|
| 高 | 密码明文存储和明文比较 | 不适合真实生产环境 | 使用 BCryptPasswordEncoder，并迁移已有密码 |
| 高 | `/api/**` 全部匿名放行 | 未登录用户可直接调用动物新增、删除等接口 | 按用户端和管理端拆分权限，管理接口必须鉴权 |
| 高 | Python 服务地址写死 `localhost:8000` | 不能灵活部署到其他机器或容器 | 改为配置项，例如 `recognition.service-url` |
| 中 | 识别阈值常量 `0.85` 与实际判断 `0.90` 不一致 | 维护者容易误判真实阈值 | 统一使用常量 |
| 中 | 特征提取失败时使用 512 维零向量 | 可能与 2048 维查询向量不一致，导致识别异常 | 使用 2048 维兜底，或失败时不写入特征并阻止新增 |
| 中 | 撤销申请只恢复 `isAdopted=false`，未恢复 `status` | 动物可能仍处于“审核中”，影响展示 | 撤销时同步恢复业务状态 |
| 中 | 完成审核可能重复写入领养记录 | 数据可能重复 | 插入前判断是否已有对应申请/动物记录 |
| 中 | `SysProfileController.updateProfile` 未真正更新数据库 | 个人信息修改可能失败 | 补齐调用 `userService.updateUserProfile` |
| 中 | `sys_role_menu` 初始数据为 0 | 动态菜单可能依赖额外配置 | 登录后核验菜单，必要时补 SQL |
| 低 | 分类路径拼写为 `/sccour/category` | 容易误改 | 保持前后端一致，或统一迁移并保留兼容路由 |
| 低 | 动物图片分散在 static 目录，头像/轮播在 `file/` | 迁移时容易漏文件 | 部署文档明确备份两个目录 |

## 18. 后续优化建议

### 18.1 配置化

- 将 Python 服务地址改到 `application.yml`。
- 将识别阈值改到配置文件。
- 将上传目录统一到一个清晰的外部目录，例如 `data/uploads`。
- 数据库账号密码通过环境变量或本机配置注入，不提交真实密码。

### 18.2 安全

- 后台新增、修改、删除接口必须登录并限制角色。
- 使用 BCrypt 加密密码。
- Token 密钥改为环境变量，并使用更长随机值。
- 生产环境关闭或保护 `/druid/**`。
- 上传文件增加后端 MIME、后缀、大小和内容校验。

### 18.3 业务一致性

- 统一动物状态枚举。当前动物审核状态使用 `pending/approved/rejected`，领养申请状态使用中文 `审核中/已完成`，容易混淆。
- 领养申请新增前检查动物是否已领养或已有审核中申请。
- 审核拒绝、撤销、删除申请时同步恢复动物展示状态。
- 领养完成后禁止继续提交申请。

### 18.4 图像识别

- 批量特征脚本与在线服务共用同一套预处理代码，减少不一致。
- 识别时先过滤维度不正确的向量。
- 保存相似度分数，便于调试。
- 返回 Top N 候选，而不是只返回一个最佳匹配。
- 模型加载增加异常提示，避免首次下载权重失败时服务静默不可用。

### 18.5 工程化

- 增加 `.env.example`、`application-example.yml`。
- 增加后端单元测试和接口测试。
- 增加前端基础构建检查。
- 增加一键启动说明或脚本。
- 增加数据库迁移工具，例如 Flyway 或 Liquibase。
- 增加 Docker Compose，统一启动 MySQL、后端、Python 和前端。

## 19. 答辩可讲要点

可以按以下逻辑介绍项目：

1. 校园中流浪猫狗信息分散，档案管理和领养流程缺少统一管理平台。
2. 本系统提供普通用户端和管理员端，覆盖动物档案、领养申请、审核、记录归档等流程。
3. 技术架构采用 Vue 3 + Spring Boot + MySQL + FastAPI/PyTorch，前后端分离。
4. 图像识别功能使用 ResNet50 提取图片特征，通过余弦相似度匹配数据库中的动物图片。
5. 权限管理使用 JWT 和角色菜单模型，后台菜单可由数据库动态生成。
6. 系统支持本机部署和局域网演示，适合课程设计展示。
7. 后续可以从安全、状态一致性、识别准确率、自动化测试和容器化部署方向继续优化。

## 20. 快速命令汇总

后端：

```powershell
cd F:\CateAndDogSystem
mvn -f springboot\pom.xml spring-boot:run
```

前端：

```powershell
cd F:\CateAndDogSystem\vue
npm install
npm run dev
```

Python：

```powershell
cd F:\CateAndDogSystem\photo_recognize
python -m pip install -r requirement.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

数据库导入：

```powershell
mysql -u root -p animal-succour < animal-succour.sql
```

前端生产构建：

```powershell
cd F:\CateAndDogSystem\vue
npm run build:prod
```

局域网访问：

```text
http://部署电脑IPv4:90
```
