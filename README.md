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
