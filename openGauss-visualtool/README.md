## 代码结构说明
```
├── visualtool-api          //服务端API模块
│   ├── pom.xml        
│   ├── com.xx.ops          //运维业务包
│   ├── com.xx.modeling     //数据建模业务包
├── visualtool-common       //公共工具模块
│   ├── pom.xml
│   ├── src
├── visualtool-framework    //集成框架模块
│   ├── pom.xml
│   ├── src
├── visualtool-service      //系统业务代码模块(service、mapper、domain、xml)
│   ├── pom.xml
│   ├── com.xx.xx.ops       //运维业务包
│   ├── com.xx.xx.modeling  //数据建模业务包
├── visualtool-ui           //前端模块
```
## 数据库对象说明

| 名称             | 类型  | 说明            |
|----------------|-----|---------------|
| sq_sys_log_config_id         | 序列  | 系统日志配置表自增序列   |
| sq_sys_menu_id     | 序列  | 系统菜单表自增序列     |
| sq_sys_log_id | 序列  | 系统操作日志表自增序列   |
| sq_sys_plugin_config_data_id | 序列  | 系统插件配置数据表自增序列 |
| sq_sys_plugin_config_id      | 序列  | 系统插件配置表自增序列   |
| sq_sys_plugin_id      | 序列  | 系统插件表自增序列     |
| sq_sys_role_id      | 序列  | 系统角色表自增序列     |
| sq_sys_user_id      | 序列  | 系统用户表自增序列     |
| sq_sys_white_list_id      | 序列  | 系统安全白名单表自增序列  |
| sys_log_config      | 表   | 系统日志配置表       |
| sys_menu      | 表   | 系统菜单表         |
| sys_oper_log      | 表   | 系统操作日志表       |
| sys_plugin_config      | 表   | 系统插件配置表       |
| sys_plugin_config_data      | 表   | 系统插件配置数据表     |
| sys_plugins      | 表   | 系统插件表         |
| sys_role      | 表   | 系统角色表         |
| sys_role_menu      | 表   | 系统角色与菜单关系表    |
| sys_user      | 表   | 系统用户表         |
| sys_user_role      | 表   | 系统用户与角色关系表    |
| sys_white_list      | 表   | 系统安全白名单表      |
| ops_az      | 表   | 系统可用区信息表      |
| ops_backup      | 表   | 系统备份信息表       |
| ops_check      | 表   | 系统一键自检信息表     |
| ops_cluster      | 表   | 系统集群信息表       |
| ops_cluster_node      | 表   | 系统集群节点信息表     |
| ops_encryption      | 表   | 系统密钥信息表       |
| ops_host      | 表   | 系统主机信息表       |
| ops_host_user      | 表   | 系统主机用户信息表     |
| ops_jdbcdb_cluster      | 表   | 系统数据库资源集群表    |
| ops_jdbcdb_cluster_node      | 表   | 系统数据库资源集群节点表  |
| ops_package_manager      | 表   | 系统安装包管理表      |
| ops_wdr      | 表   | 系统WDR表        |



## 支持的服务器系统
openEuler 20.3LTS（x86_x64，ARM），centos7.x（x86_x64）
## 安装部署

### jar包安装
```shell
#1、创建datakit工作目录，并在工作目录中创建存放系统运行数据的子目录
mkdir -p 自定义工作目录
cd 自定义工作目录
mkdir -p logs config ssl files
#2、将visualtool-main.jar包上传至 $自定义工作目录 下
#3、修改application-temp.yml文件中的数据链链接ip、port、database、dbuser、dbpassword。将修改后的配置文件application-temp.yml传至 $自定义工作目录/config/下
#4、修改配置文件中平台工作目录
sed -i s#/ops#$(pwd)#g config/application-temp.yml
#4、创建ssl文件
keytool -genkey -noprompt \
    -dname "CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, S=Beijing, C=CN"\
    -alias opengauss\
    -storetype PKCS12 \
    -keyalg RSA \
    -keysize 2048 \
    -keystore $(pwd)/ssl/keystore.p12 \
    -validity 3650 \
    -storepass 123456
#5、执行启动命令
nohup java -Xms2048m -Xmx4096m -jar $(pwd)/visualtool-main.jar \
    --spring.profiles.active=temp >$(pwd)/logs/visualtool-main.out 2>&1 &
```

### docker安装
```shell
#1、创建datakit工作目录，并在工作目录中创建存放系统运行数据的子目录
mkdir -p 自定义工作目录
cd 自定义工作目录
mkdir -p logs config ssl files
#2、将visualtool-main.jar包上传至 $自定义工作目录 下
#3、修改application-temp.yml文件中的数据链链接ip、port、database、dbuser、dbpassword。将修改后的配置文件application-temp.yml传至 $自定义工作目录/config/下
#4、创建ssl文件
keytool -genkey -noprompt \
    -dname "CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, S=Beijing, C=CN"\
    -alias opengauss\
    -storetype PKCS12 \
    -keyalg RSA \
    -keysize 2048 \
    -keystore $(pwd)/ssl/keystore.p12 \
    -validity 3650 \
    -storepass 123456
#5、编辑Dockerfile
vi Dockerfile
#复制以下内容进行粘贴
FROM openjdk:11
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 9494 5432
WORKDIR /ops/
ENTRYPOINT ["java","-Xms2048m","-Xmx4096m", "-jar", "visualtool-main.jar"]
#6、构建镜像
docker build -f Dockerfile -t datakit:5.0.0 .
#7、启动容器
docker run -idt -p 9494:9494 \
    -v /etc/localtime:/etc/localtime:ro \
    -v $(pwd):/ops \
    --name datakit datakit:5.0.0 \
    --spring.profiles.active=temp
```

## 升级步骤
### jar包升级
```shell
#1、将新的jar包传至 自定义工作目录 下，替换原有visualtool-main.jar
cd 自定义工作目录
#2、关闭原有进程后，再执行启动命令
nohup java -Xms2048m -Xmx4096m -jar $(pwd)/visualtool-main.jar \
    --spring.profiles.active=temp >$(pwd)/logs/visualtool-main.out 2>&1 &
```
### docker安装升级
```shell
#1、将新的jar包传至 自定义工作目录 下，替换原有visualtool-main.jar
#2、重启容器
docker restart datakit
```
### 平台配置修改
#### 端口修改
在工作目录的config目录中，修改配置文件，将默认9494端口修改为自定义端口，之后重启服务

### 注意事项：
1、当前平台运行依赖于openJdk11。

2、平台使用的数据库，当前仅支持openGauss数据库，并且需要提前创建database。

3、需要将部署服务器IP配置在平台使用的数据库（openGauss）的白名单列表中。

4、平台默认的登录账号密码：admin/admin123，请在首次登录后及时修改密码。

## 卸载步骤
1、停止java进程或者docker容器，删除工作目录。

2、手动清理数据库中所有的表和序列。

## 后端说明
> 1、后端返回给前端的响应编码，统一在org.opengauss.admin.common.enums.ResponseCode中定义，按照规则划分模块，规则如下：
+ 501xx 一体化模块；比如50101、50102

## 前端说明

## 主程序接口暴露
为了更好的通过插件扩展系统功能，主程序需要暴露接口给插件扩展和使用。主要有两类接口需要暴露：
+ 给插件扩展的Interface
> 在visualtool-service模块的org.opengauss.admin.system.plugin.extract包里定义。
+ 给插件调用的Service，
> 在visualtool-service模块的org.opengauss.admin.system.plugin.facade包里定义。

## 相关文档
[openGauss安装文档]( https://docs.opengauss.org/zh/docs/3.0.0/docs/installation/%E5%8D%95%E8%8A%82%E7%82%B9%E5%AE%89%E8%A3%85.html )

[主程序开放接口文档]( https://fullstack-dao.feishu.cn/docx/doxcnIa9e0ChR4bJWlx4IyBfzjf )

[主程序前端开发规范指导]( https://fullstack-dao.feishu.cn/docx/doxcnyE9BNt2mm0WV5o2dPqxAec )

[插件开发手册]( https://fullstack-dao.feishu.cn/docx/doxcnu2EjetnyXmL1sYIooyrivp )

[主程序扩展手册]( https://fullstack-dao.feishu.cn/docx/doxcnV63pz1w4bn4Zn1y2lxwJnf )

