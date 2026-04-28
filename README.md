# CateAndDogSystem
基于图像识别的校园流浪动物管理系统

### 项目配置说明

#### 1. 数据库连接配置

请修改 `application-druid.yml` 文件，根据新电脑的 MySQL 环境更新以下信息：

-   **文件路径**: `application-druid.yml`
-   **第 5 行**: 数据库连接 URL（需修改数据库名）
-   **第 6 行**: MySQL 用户名
-   **第 7 行**: MySQL 密码

#### 2. 图片上传路径配置

请在**三处**位置统一修改为新电脑上的**绝对路径**，确保路径一致：

-   **位置 1**: `application.yml` (第 76 行)
    ```yaml
    upload:
      path: E:\Cat-and-Dog-System\springboot\src\main\resources\static\uploads\images
    ```
-   **位置 2**: `ResourcesConfig.java` (第 24 行)
    ```java
    .addResourceLocations("file:E:/Cat-and-Dog-System/springboot/src/main/resources/static/uploads/");
    ```
-   **位置 3**: `AnimalServiceImp.java` (第 61 行)
    ```java
    String uploadDir = "E:/Cat-and-Dog-System/springboot/src/main/resources/static/uploads/images/";
    ```

#### 3. 图像识别服务配置

-   **环境依赖**: 请查看 `requirement.txt` 配置 Python 环境。

**启动图像识别模型步骤：**

1.  进入模型根目录：
    ```bash
    cd photo_recognize
    ```
2.  激活虚拟环境（如果已创建）：
    ```bash
    conda activate <环境名>
    ```
    *(注：如果未创建虚拟环境，可忽略此步，但建议创建)*
3.  启动模型服务：
    ```bash
    uvicorn main:app --port 8000
    ```