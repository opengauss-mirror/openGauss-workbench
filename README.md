# openGauss DataKit
openGauss的安装、运维场景对于初级用户或单纯想要测试openGauss数据库基本特性的使用者来说技术难度较大、过程较为复杂、学习曲线较为陡峭，尤其企业版安装对一般用户来说操作难度很大。使用可视化运维平台可以屏蔽openGauss的技术细节，让普通用户能够快速上手体验功能，让运维人员能够快速在企业环境中部署、卸载各类openGauss集群，减少了用户的学习成本和运维成本，实现了对openGauss各种常见操作的可视化，屏蔽了各种不同openGauss版本中的运维命令差异，可以让用户使用相同的方式操作数据库，不用知道命令细节也可以使用openGauss数据库的各项能力，让用户可以专注于自身的业务领域。

因此需要开发一些有针对性的运维监控工具，为不同配置不同运维要求的客户提供运维技术支撑，这些都将是openGauss社区的宝贵资产。而社区急需一个一体化的平台通过插件的方式将这些工具进行整合，并支持方便快捷的个性化配置。

本项目是基于Web的openGauss的可视化的平台系统，目的是方便客户使用和管理openGauss可视化工具，可以为客户降低openGauss数据库安装使用门槛，做到安全中心管理，插件管理，以及其它功能包括一键化部署、卸载、组件化安装、多版本升级、日常运维和监控。


## 项目仓库结构
```
├── openGauss-datakit  //平台项目
├── plugins
├───├─alert-monitor         // 告警插件
├───├─base-ops              //基础运维插件项目
├───├─data-migration        //MySql数据迁移插件项目
├───├─data-studio           // Web 版本DataStudio
├───├─datakit-demo-plugin  //平台项目插件开发脚手架
├───├─datasync-mysql        //MySql数据迁移插件项目(已弃用)
├───├─observability-instance  //智能运维插件实例监控项目
├───├─observability-log-search  //智能运维插件日志检索项目
├───├─observability-sql-diagnosis  //智能运维插件慢sql诊断项目
├───├─openGauss-tools-monitor  // openGauss 数据库监控插件
```
## 下载链接

https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/Datakit-7.0.0-RC1.tar.gz

## 说明
1. 插件需要安装在平台上运行，因此需要先将平台项目安装部署后，再将其他插件项目打包安装使用。

2. 插件开发脚手架项目是为了方便开发者快速开发与平台适配的插件，而搭建的插件开发脚手架，开发者可在此脚手架之上开发业务功能。该脚手架配置的各项依赖版本已经经过验证，和平台兼容性最好，因此建议不要修改依赖版本。

## 编译代码
1. 请提前安装java 11+, maven 3.8.0+, node v18+(含npm)，并配置好maven镜像源和node镜像源
2. 执行`sh build.sh`
3. 编译输出件在output/Datakit-${pom_version}.tar.gz

## 安装步骤
1. 解压安装包\
   通过下载链接或编译代码获取安装包`Datakit-7.0.0-RC1.tar.gz`，解压安装包至`datakit`安装目录下，例如安装目录为`/path/datakit_server`时，解压目录如下:
   ```shell
   $ tar -zxvf Datakit-7.0.0-RC1.tar.gz -C /path/datakit_server
   ./application-temp.yml
   ./doc/
   ./doc/datasync-mysql-README.md
   ./doc/data-migration-README.md
   ./doc/datakit-README.md
   ./doc/datakit-demo-plugin-README.md
   ./doc/observability-log-search-README.md
   ./doc/observability-instance-README.md
   ./doc/data-studio-README.md
   ./doc/alert-monitor-README.md
   ./doc/observability-sql-diagnosis-README.md
   ./doc/compatibility-assessment-README.md
   ./doc/openGauss-tools-monitor-README.md
   ./doc/base-ops-README.md
   ./openGauss-datakit-7.0.0-RC1.jar
   ./run.sh
   ./visualtool-plugin/
   ./visualtool-plugin/webds-plugin-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/compatibility-assessment-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/observability-instance-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/observability-sql-diagnosis-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/observability-log-search-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/monitor-tools-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/data-migration-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/alert-monitor-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/datakit-demo-plugin-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/datasync-mysql-7.0.0-RC1-repackage.jar
   ./visualtool-plugin/base-ops-7.0.0-RC1-repackage.jar
   ```
2. 创建新目录\
   在`datakit`安装目录下，创建新的目录`config`, `files`, `ssl`, `logs`
   ```shell
   $ cd /path/datakit_server
   mkdir config files ssl logs
   ```
3. 更改配置文件 - 修改工作目录\
   修改`datakit`安装目录下的`application-temp.yml`文件，文件中的`/ops`默认工作目录路径统一修改为实际`datakit`安装目录的路径`/path/datakit_server`，而第二步创建的目录就是为了此处统一使用的
   ```shell
   $ vim application-temp.yml
   system.defaultStoragePath: /ops/files
   server.ssl.key-store: /ops/ssl/keystore.p12
   logging.file.path: /ops/logs
   ```
4. 更改配置文件 - 配置数据库\
   数据库可选用`openGauss`或轻量嵌入式数据库`Intarkdb`，平台默认使用`openGauss`作为后台数据库。使用`openGauss`作为后台数据库时，需要正确配置`openGauss`的连接信息。配置内容如下：
   ```yaml
   # For openGauss
   driver-class-name: org.opengauss.Driver
   url: jdbc:opengauss://ip:port/database?currentSchema=public&batchMode=off
   username: dbuser
   password: dbpassword
   ```
   使用轻量嵌入式数据库`Intarkdb`作为后台数据库时，只需注释`openGauss`的配置内容，并解开对`Intarkdb`配置内容的注释，即可完成配置。目前`datakit`、`base-ops`和`alert-monitor`在启动时会在数据库初始化数据。配置内容如下： 
   ```yaml
   # For Intarkdb
   driver-class-name: org.intarkdb.IntarkdbJDBC
   url: jdbc:intarkdb:data/datakit
   ```
   配置文件更改完成后，保存并退出文件编辑，然后执行如下命令，将`application-temp.yml`文件移动到第二步创建的`config`目录下
   ```shell
   mv application-temp.yml config
   ```
   *注意*：此处使用`openGauss`作为后台数据库时，需要提前对数据库做一些参数配置，详细步骤请参考下方目录**补充：openGauss参数配置**
5. 生成密钥信息\
   修改并执行如下命令生成密钥信息。修改`-storepass`参数值与`application.yml`配置文件中的`key-store-password`值保持一致，默认时两者均为`123456`；修改`-keystore`路径值与配置文件中的`key-store`路径值保持一致，即第三步中修改`/ops`后的路径。
   ```shell
   keytool -genkey -noprompt -dname "CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, S=Beijing, C=CN" -alias opengauss -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore /ops/ssl/keystore.p12 -validity 3650 -storepass 123456
   ```
   *注意*：此处为一条完整命令。
6. 启动与日常运维\
   启动应用：
   ```shell
   sh ./run.sh start
   ```
   停止应用：
   ```shell
   sh ./run.sh stop
   ```
   重启应用：
   ```shell
   sh ./run.sh restart
   ```
   检查应用状态：
   ```shell
   sh ./run.sh status
   ```
7. 访问服务\
   启动成功后，通过浏览器输入如下地址：`https://ip:9494/` 访问`datakit`服务，这里的`ip`为`datakit`服务安装在的主机`ip`，`9494`为`datakit`服务默认端口，如有修改请根据实际情况替换。初始用户为`admin`，初始密码为`admin123`，首次登录需修改初始密码。

## 补充：openGauss参数配置
1. 安装`openGauss`数据库\
   `openGauss`数据库的下载及安装请参考官网教程，这里不做赘述，下载地址：https://opengauss.org/zh/download/
2. 切换数据库安装用户，并加载环境变量\
   成功安装`openGauss`数据库后，主机切换到数据库安装用户，如`omm`用户。然后`source`环境变量文件，来加载`openGauss`的环境变量，如`omm`用户环境变量文件`~/.bashrc`。注意此环境变量文件为`openGauss`数据库环境变量所在文件，请根据实际情况替换。
   ```shell
   source ~/.bashrc
   ```
3. 参数配置\
   修改并执行如下命令，设置配置文件`pg_hba.conf`相关参数（如果是ipv6地址，则将以下“0.0.0.0/0”换成“::/0”）
   ```shell
   gs_guc set -D /opt/software/openGauss/data/single_node -h "host all all 0.0.0.0/0 sha256"
   ```
   修改并执行如下命令，配置文件`postgresql.conf`相关参数
   ```shell
   gs_guc set -D /opt/software/openGauss/data/single_node -c "listen_addresses = '*'"
   ```
   上述命令中的`/opt/software/openGauss/data/single_node`为数据库节点的安装目录路径，此目录下包含有上述两个文件，请根据数据库的实际情况替换。此处参数配置的目的是使得数据库接受来自任意`ip`地址的连接请求，以便在外部服务器可以成功连接到数据库。
4. 重启数据库\
   执行如下命令重新启动数据库，使参数配置生效。此处的`/opt/software/openGauss/data/single_node`请按第三步的方法替换为实际路径
   ```shell
   gs_ctl restart -D /opt/software/openGauss/data/single_node
   ```
5. 连接数据库\
   执行如下命令连接数据库，此处`5432`为`openGauss`数据库默认端口，请根据实际情况替换
   ```shell
   gsql -d postgres -p 5432 -r
   ```
6. 创建用户及数据库\
   成功连接数据库后，依次执行如下三条命令，分别进行创建用户，赋予用户管理员权限，创建数据库的操作。
   ```shell
   create user opengauss_test with password 'Sample@123';
   grant all privileges to opengauss_test;
   create database db_datakit;
   ```
   由于`openGauss`数据库不支持通过初始用户进行远程连接，因此此处创建新的用户供`datakit`远程连接时使用。同时，由于`datakit`需要拥有管理员权限对数据库进行操作，因此需要赋予连接用户管理员权限。此处新建`db_datakit`数据库作为`datakit`平台的底层数据库使用，不用做任何操作，`datakit`成功连接后会自动初始化数据。
7. 所有配置完成，保持`openGauss`数据库服务启动

## 参与开发
开发环境搭建参考 [开发环境搭建](https://gitee.com/opengauss/openGauss-workbench/tree/master/openGauss-datakit/doc/DataKit%20Dev%20Setup.md)

插件开发请参考`openGauss-datakit/doc`目录下的开发手册

新增插件请务必更新`build.sh`脚本，保证可以一键编译
