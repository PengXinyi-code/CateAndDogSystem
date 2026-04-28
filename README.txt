必须修改的配置
1. 数据库连接配置
application-druid.yml
第5行 ：数据库连接URL（需要修改数据库名）
第6行 ：MySQL用户名
第7行 ：MySQL密码

2. 图片上传路径配置（3处）
位置1： application.yml 第76行
upload:
path:
E:\Cat-and-Dog-System\springboot\src\main\resources\static\uploads\images

位置2： ResourcesConfig.java 第24行
.addResourceLocations("file:E:/Cat-and-Dog-System/springboot/src/main/
resources/static/uploads/");

位置3： AnimalServiceImp.java 第61行
String uploadDir ="E:/Cat-and-Dog-System/springboot/src/main/resources/
static/uploads/images/";

这三处路径需要统一修改为新电脑上的绝对路径

数据库配置也需要根据新电脑的MySQL设置进行修改

图像识别需要配置的环境见requirement.txt
启动图像识别的模型：
1.cd到模型根目录下面：cd photo_recognize
2.如果创建了虚拟环境，激活环境conda activate xxx(环境名)，如果没创建虚拟环境，忽略这一步(建议创建虚拟环境)
3.启动模型：uvicorn main:app --port 8000
