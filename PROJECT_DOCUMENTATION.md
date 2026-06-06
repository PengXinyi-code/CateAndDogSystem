# 校园流浪动物救助领养平台项目说明

> 文档用途：课程设计、系统部署、测试设计与答辩准备  
> 核验日期：2026-06-06  
> 核验依据：当前仓库源码、配置文件、SQL 脚本和 `test-artifacts` 测试资产  
> 说明：本文只记录能够从代码确认的事实。运行结果、性能指标和兼容性结论须在实际执行后填写，不应预先写成“通过”。

## 1. 项目概述

本项目是一个面向校园场景的流浪动物档案与领养管理系统。系统由 Vue 前端、Spring Boot 后端、MySQL 数据库和 Python 图像特征提取服务组成，包含普通用户端和后台管理端。

主要业务包括：

- 用户注册、登录和个人资料维护；
- 动物档案浏览、筛选、详情查看和后台增删改查；
- 领养申请提交、审核、撤销和领养记录查询；
- 动物分类、轮播图、用户、角色和菜单管理；
- 上传动物图片后，通过 ResNet50 提取特征并进行相似度匹配。

本项目页面数量和数据库 CRUD 功能均超过课程设计要求，可直接作为《应用系统开发综合实践》的部署与测试对象，无需重新开发无关业务。

## 2. 系统架构

```text
浏览器
  |
  | HTTP（开发环境默认端口 90）
  v
Vue 3 + Vite
  |
  | /dev-api 代理或生产环境直连
  v
Spring Boot 3（端口 8080）
  |                         |
  | JDBC / MyBatis          | HTTP GET
  v                         v
MySQL 8                 FastAPI（端口 8000）
                            |
                            v
                    PyTorch ResNet50
```

系统采用前后端分离架构。前端负责页面展示和交互，后端提供 REST 风格接口并操作数据库。新增动物和图片识别时，后端调用本机 Python 服务提取图像特征。

## 3. 实际技术栈

### 3.1 前端

| 技术 | 仓库声明版本 | 用途 |
|---|---:|---|
| Vue | 3.4.31 | 页面与组件开发 |
| Vue Router | 4.4.0 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.12.0 | UI 组件 |
| Axios | 0.28.1 | HTTP 请求 |
| ECharts | 5.5.1 | 图表支持 |
| Vite | 5.3.2 | 开发服务器和构建 |
| Sass | 1.77.5 | 样式预处理 |
| WangEditor | 5.x | 富文本编辑器依赖 |

配置来源：`vue/package.json`。

### 3.2 后端

| 技术 | 仓库声明版本 | 用途 |
|---|---:|---|
| Spring Boot | 3.5.0 | Web 后端框架 |
| Java | 17 或以上 | Spring Boot 3.5 的最低运行基线 |
| Spring Security | 随 Spring Boot 管理 | JWT 认证与接口权限 |
| MyBatis | 3.0.3 Starter | 数据访问 |
| PageHelper | 2.1.0 | 分页 |
| Druid | 1.2.23 | 数据库连接池 |
| MySQL Connector/J | 8.2.0 属性声明 | MySQL 驱动 |
| JJWT | 0.9.1 | Token 生成与解析 |
| Maven | Maven Wrapper 未提供 | 构建和依赖管理 |

后端使用的是 MyBatis，不是 MyBatis-Plus。

### 3.3 图像识别服务

| 技术 | 用途 |
|---|---|
| FastAPI | 提供 `/extract_by_path` 接口 |
| Uvicorn | ASGI 服务 |
| PyTorch / torchvision | 加载预训练 ResNet50 |
| Pillow | 读取和转换图片 |
| NumPy | 特征向量处理 |
| PyMySQL | 批量生成特征时连接数据库 |

`photo_recognize/main.py` 删除 ResNet50 最后一层分类层，输出 2048 维特征并进行 L2 归一化。当前使用 `pretrained=True`，首次运行可能需要下载模型权重。

## 4. 仓库结构

```text
CateAndDogSystem/
├─ springboot/                 Spring Boot 后端
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/fast/
│     └─ resources/
├─ vue/                        Vue 3 前端
│  ├─ package.json
│  ├─ vite.config.js
│  └─ src/
├─ photo_recognize/            FastAPI + ResNet50 服务
├─ test-artifacts/
│  ├─ automation/              Selenium 自动化脚本
│  ├─ performance/             JMeter 测试计划
│  └─ compatibility/           Pairwise 生成器和用例
├─ file/                       头像和通用上传文件
├─ temp/                       识别临时文件目录
├─ animal-succour.sql          数据库初始化脚本
├─ README.md                   快速启动说明
└─ PROJECT_DOCUMENTATION.md    本文档
```

## 5. 页面与功能

### 5.1 公共和用户端页面

| 页面 | 路由 | 主要功能 |
|---|---|---|
| 登录 | `/login` | 用户认证、跳转首页 |
| 注册 | `/register` | 创建普通用户 |
| 用户首页 | `/user/home` | 轮播图、动物展示、图片识别入口 |
| 动物列表 | `/user/animal` | 名称/种类等条件查询、卡片展示 |
| 动物详情 | `/user/animalDetail/:id` | 查看档案、提交领养申请 |
| 我的申请 | `/user/record` | 查看申请状态、撤销审核中申请 |
| 个人中心 | `/user/self` | 修改资料、头像和密码 |

### 5.2 后台管理页面

后台菜单主要由数据库 `sys_menu` 动态生成。

| 页面模块 | 主要功能 |
|---|---|
| 动物档案管理 | 查询、新增、修改、删除动物 |
| 动物分类管理 | 分类增删改查 |
| 轮播图管理 | 轮播图增删改查 |
| 领养申请审核 | 查询、审核和删除申请 |
| 领养记录 | 查询已完成领养记录 |
| 用户管理 | 用户增删改查、状态和密码管理 |
| 角色管理 | 角色增删改查和用户分配 |
| 菜单管理 | 菜单增删改查 |

仓库中还存在固定路由 `/adoption-record/list`，用于领养记录页面。

## 6. 后端接口

### 6.1 认证与用户

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/login` | 登录并返回 Token | 匿名 |
| POST | `/register` | 注册 | 匿名 |
| POST | `/logout` | 退出登录 | 已登录 |
| GET | `/getInfo` | 当前用户信息 | 已登录 |
| GET | `/getRouters` | 当前用户动态路由 | 已登录 |
| GET/PUT | `/system/user/profile` | 查询或修改个人资料 | 已登录 |
| PUT | `/system/user/profile/updatePwd` | 修改密码 | 已登录 |
| POST | `/system/user/profile/avatar` | 上传头像 | 已登录 |

### 6.2 动物和识别

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/animal/list` | 分页和条件查询动物 | 匿名 |
| GET | `/api/animal/{id}` | 查询动物详情 | 匿名 |
| POST | `/api/animal/add` | 新增动物，接收表单和可选图片 | 匿名 |
| PUT | `/api/animal` | 修改动物 | 匿名 |
| DELETE | `/api/animal/{animalIds}` | 删除一个或多个动物 | 匿名 |
| POST | `/api/recognition/identify` | 上传图片并匹配动物 | 匿名 |
| POST | `/api/common/upload` | 通用文件上传 | 匿名 |

注意：`SecurityConfig` 当前放行全部 `/api/**`，便于演示，但不适合生产环境。

### 6.3 领养、分类和轮播图

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/succour/adopt/list` | 申请列表 |
| GET | `/succour/adopt/{adoptId}` | 申请详情 |
| POST | `/succour/adopt` | 提交申请 |
| PUT | `/succour/adopt` | 审核或修改申请 |
| DELETE | `/succour/adopt/{adoptIds}` | 删除申请 |
| PUT | `/succour/adopt/revoke/{adoptId}` | 撤销审核中申请 |
| GET | `/succour/adopt/record/list` | 领养记录列表 |
| GET | `/succour/adopt/record/{id}` | 领养记录详情 |
| GET | `/succour/adopt/record/animal/{animalId}` | 按动物查询记录 |
| GET/POST/PUT/DELETE | `/succour/banner...` | 轮播图 CRUD |
| GET/POST/PUT/DELETE | `/sccour/category...` | 分类 CRUD |

源码中的分类前缀是 `/sccour/category`，其中 `sccour` 是现有拼写，前后端保持一致。除领养记录 GET 接口外，上述 `/succour/**` 和 `/sccour/**` 请求默认需要 JWT。

### 6.4 系统管理

- `/system/user`：用户查询、新增、修改、删除、重置密码和状态修改；
- `/system/role`：角色查询、增删改、状态修改和用户授权；
- `/system/menu`：菜单查询、增删改和树形选择。

## 7. 数据库

### 7.1 数据源

默认配置位于 `springboot/src/main/resources/application-druid.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/animal-succour
    username: root
    password: 123456
```

提交报告或共享仓库前，建议通过本机配置或环境变量管理密码，不要继续公开真实数据库凭据。

### 7.2 数据表

SQL 脚本共创建 11 张表：

| 类型 | 数据表 |
|---|---|
| 动物业务 | `animals`、`animal_images` |
| 领养业务 | `adopt`、`adoption_record` |
| 内容配置 | `banner`、`category` |
| 系统权限 | `sys_user`、`sys_role`、`sys_menu`、`sys_user_role`、`sys_role_menu` |

`animal-succour.sql` 当前包含 120 条动物记录和 120 条动物图片记录。具体数量会随系统操作变化，报告截图应以测试时数据库实际查询结果为准。

### 7.3 核心关系

- `animal_images.animal_id` 关联动物档案；
- `adopt.animal_id` 表示申请领养的动物；
- 审核完成时写入 `adoption_record`；
- `sys_user_role` 连接用户和角色；
- `sys_role_menu` 连接角色和菜单。

### 7.4 测试账号

SQL 脚本包含管理员账号 `admin / admin123`，自动化脚本默认使用该账号。脚本中还包含若干普通用户测试数据。账号密码目前采用明文比较，仅适合教学演示环境。

## 8. 核心业务流程

### 8.1 登录

1. 前端向 `/login` 提交用户名和密码；
2. 后端校验用户并生成 JWT；
3. 前端保存 Token；
4. 前端请求 `/getInfo` 和 `/getRouters`；
5. 根据角色加载后台菜单或进入用户页面。

### 8.2 领养申请

1. 用户从动物详情页提交申请；
2. 后端生成申请 ID 和创建时间，并把动物状态改为“审核中”；
3. 管理员审核申请；
4. 状态为“已完成”时，后端写入 `adoption_record` 并设置动物为已领养；
5. 审核中的申请可以撤销，撤销时删除申请并将动物设为未领养。

当前实现中，撤销操作只更新 `isAdopted=false`，未明确把动物 `status` 恢复为“可领养”，应作为功能测试关注点。

### 8.3 图片识别

1. 用户上传图片到 `/api/recognition/identify`；
2. 后端把图片保存到 `temp`；
3. 后端调用 `http://localhost:8000/extract_by_path`；
4. Python 使用 ResNet50 输出归一化特征；
5. 后端读取数据库中的图片特征并计算余弦相似度；
6. 最大相似度大于 `0.90` 时返回对应动物，否则返回未匹配；
7. 后端尝试删除临时文件。

当前识别服务地址固定为 `localhost:8000`，因此 Spring Boot 与 Python 服务应运行在同一台机器，除非后续将地址改为可配置项。

## 9. 本机部署

### 9.1 建议环境

| 软件 | 建议 |
|---|---|
| Windows | Windows 10/11 |
| JDK | 17 或 21 |
| Maven | 3.9.x |
| Node.js | 18 LTS 或 20 LTS |
| MySQL | 8.0 |
| Python | 3.10-3.13，需确认 PyTorch 可安装 |
| 浏览器 | Chrome、Edge、Firefox |
| JMeter | 5.6.3 |

实际报告中的版本应使用 `java -version`、`mvn -version`、`node -v`、`npm -v`、`python --version` 和 MySQL 查询结果截图填写。

### 9.2 初始化数据库

```powershell
mysql -u root -p
```

```sql
CREATE DATABASE `animal-succour`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

然后导入根目录的 `animal-succour.sql`，并按本机环境修改 `application-druid.yml` 中的用户名和密码。

### 9.3 启动 Python 服务

```powershell
cd F:\CateAndDogSystem\photo_recognize
python -m pip install -r requirement.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

验证地址：`http://localhost:8000/docs`。

### 9.4 启动后端

建议从项目根目录启动，使 `${user.dir}` 指向 `F:\CateAndDogSystem`：

```powershell
cd F:\CateAndDogSystem
mvn -f springboot\pom.xml spring-boot:run
```

后端监听 `0.0.0.0:8080`。启动成功后可访问：

- `http://localhost:8080`
- `http://localhost:8080/druid/`

如果在 `springboot` 子目录直接启动，当前 `${user.dir}/springboot/...` 路径可能重复拼接 `springboot`，导致上传或静态资源目录错误。

### 9.5 启动前端

```powershell
cd F:\CateAndDogSystem\vue
npm install
npm run dev
```

开发服务器配置为 `host: true`、端口 `90`，访问地址为：

- `http://localhost:90`
- `http://本机局域网IP:90`

开发环境通过 `/dev-api` 代理到 `VITE_BACKEND_TARGET`，默认值为 `http://localhost:8080`。其他设备访问前端时，请求仍由运行 Vite 的电脑转发，因此同机部署通常无需把开发环境 API 改成局域网 IP。

### 9.6 生产构建

本机后端地址：

```powershell
cd F:\CateAndDogSystem\vue
npm run build:prod
```

局域网直连后端：

```powershell
$env:VITE_APP_BASE_API='http://192.168.1.100:8080'
npm run build:prod
```

构建输出在 `vue/dist`。仓库目前未提供 Nginx 或将 `dist` 自动复制到 Spring Boot 的脚本，生产静态文件部署方式需另行选择。

## 10. 局域网访问

1. 使用 `ipconfig` 查询本机 IPv4 地址；
2. 确认后端运行于 `0.0.0.0:8080`；
3. 确认 Vite 运行于端口 `90` 且 `host: true`；
4. 同一局域网设备访问 `http://本机IP:90`；
5. 必要时仅为专用网络开放 TCP 90 和 8080 端口；
6. Python 8000 端口无需对局域网开放，因为当前仅由同机后端访问。

Windows 防火墙规则属于系统级改动，应由用户确认后手动执行，并在测试结束后按需删除。

## 11. 课程设计测试资产

### 11.1 Selenium 自动化测试

目录：`test-artifacts/automation`

当前脚本覆盖：

- 管理员登录并验证跳转；
- 打开动物列表、输入名称查询、进入详情；
- 可选的分类新增、查询和删除闭环。

默认不执行写数据库的 CRUD 用例。使用测试数据库并确认允许写入后，可设置：

```powershell
$env:RUN_CRUD='1'
python test-artifacts\automation\selenium_course_test.py
```

### 11.2 JMeter 性能测试

文件：`test-artifacts/performance/campus-animal-platform.jmx`

覆盖请求：

- 登录；
- 动物分页查询；
- 轮播图查询。

建议按 10、30、50、100 线程逐步测试，记录平均响应时间、最大响应时间、90% 响应时间、错误率、成功率和吞吐量。正式压测时应关闭“察看结果树”监听器。

### 11.3 Pairwise 兼容性测试

目录：`test-artifacts/compatibility`

因素包括：

- 3 种操作系统；
- 4 种浏览器；
- 3 种分辨率；
- 3 种网络环境；
- 3 类页面。

原始组合数为：

```text
3 × 4 × 3 × 3 × 3 = 324
```

当前贪心全对偶脚本生成 16 条组合，输出为 `pairwise_cases.csv`。兼容性结果仍为“待测试”，执行后需填写页面布局、图片显示、字体、遮挡和功能可用性。

## 12. 适合课程设计的功能测试点

建议至少覆盖以下类型，并与自动化、性能和兼容性用例合计达到 30 条以上：

1. 登录表单：正确账号、错误密码、空账号、空密码；
2. 注册表单：必填项、重复用户名、密码规则；
3. 动物查询：名称、种类、状态、分页和详情参数；
4. 动物 CRUD：新增、修改、单条删除、批量删除、图片显示；
5. 分类 CRUD：新增、重名、修改排序、删除；
6. 领养流程：提交、审核通过、审核拒绝、撤销和记录生成；
7. 文件与图片：支持格式、空文件、超限文件、失效图片链接；
8. 页面跳转：登录跳转、详情参数、404 页面、动态菜单；
9. 权限：匿名访问、普通用户访问后台、Token 失效；
10. 图像识别：同图匹配、不同图不匹配、Python 服务未启动。

实际结果只能在执行测试后填写。对数据库写入类用例，应使用可恢复的测试数据。

## 13. 已知问题与风险

以下内容由静态代码核验发现，适合纳入缺陷测试或后续优化：

1. 密码使用明文存储和明文比较，不满足生产安全要求；
2. `/api/**` 全部匿名放行，包括动物新增、修改和删除；
3. JWT 密钥和数据库密码直接写在配置文件中；
4. Python 服务地址 `http://localhost:8000` 为硬编码；
5. `RecognitionServiceImp` 定义了 `THRESHOLD = 0.85`，实际判断却硬编码为 `0.90`；
6. 新增动物特征提取失败时使用 512 维零向量，而正常 ResNet50 特征为 2048 维；
7. 新增动物直接接收文件时，保存路径前拼接了字符串 `file:`，需要实际运行验证路径是否正确；
8. 撤销领养申请未明确恢复动物的文本状态；
9. SQL 初始数据中的动物状态同时存在中文值和 `pending/approved/rejected` 英文值，可能影响筛选和展示一致性；
10. 文档和旧 README 中曾出现端口 5173，但当前 Vite 实际端口是 90；
11. `pom.xml` 同时出现 Spring Boot 3.5.0 parent、3.3.5 dependencies BOM 和 3.3.0 插件版本，版本管理不统一；
12. 当前没有后端单元测试和集成测试源码，课程设计自动化主要依赖 Selenium。

## 14. 部署与答辩截图清单

建议按编号保存截图，便于报告引用：

| 编号 | 截图内容 |
|---|---|
| DEP-01 | JDK、Maven、Node、Python 和 MySQL 版本 |
| DEP-02 | MySQL 数据库及 11 张表 |
| DEP-03 | Python 服务启动及 `/docs` |
| DEP-04 | Spring Boot 启动成功和 8080 端口 |
| DEP-05 | Vite 启动成功和 90 端口 |
| DEP-06 | 登录页 |
| DEP-07 | 用户首页图片展示 |
| DEP-08 | 动物列表或详情页 |
| DEP-09 | 后台动物管理表格 |
| DEP-10 | 数据新增、修改和删除结果 |
| DEP-11 | 局域网设备通过 IP 访问 |
| AUT-01 | Selenium 命令和通过结果 |
| PERF-01 | JMeter 线程组参数 |
| PERF-02 | 聚合报告 |
| PERF-03 | HTML Dashboard |
| COMP-01 | Pairwise 用例表 |
| COMP-02 | 不同浏览器和分辨率执行结果 |

## 15. 答辩演示顺序

1. 展示系统目录和四层架构；
2. 展示数据库表和测试数据；
3. 启动 Python、Spring Boot 和 Vue；
4. 演示登录、动物查询和详情；
5. 演示一组可恢复的新增、修改、删除；
6. 演示领养申请与后台审核；
7. 演示图片识别，说明 Python 服务依赖；
8. 运行 Selenium 并展示结果；
9. 打开 JMeter 测试计划和已执行报告；
10. 展示 324 个原始兼容性组合如何缩减为 16 条 Pairwise 用例；
11. 总结已发现问题和改进方向。

## 16. 后续文档计划

本文件是项目事实基线，后续课程设计材料应以此为依据继续拆分：

- `docs/deployment.md`：部署和局域网访问；
- `docs/functional-testing.md`：功能测试方案、30 条以上用例和缺陷表；
- `docs/automation-testing.md`：Selenium 说明和执行结果；
- `docs/performance-testing.md`：JMeter 参数、结果和分析；
- `docs/compatibility-testing.md`：Pairwise 方法和执行结果；
- 最终《校园流浪动物救助领养平台软件测试报告》。

