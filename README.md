# openGauss DataKit
openGauss的安装、运维场景对于初级用户或单纯想要测试openGauss数据库基本特性的使用者来说技术难度较大、过程较为复杂、学习曲线较为陡峭，尤其企业版安装对一般用户来说操作难度很大。使用可视化运维平台可以屏蔽openGauss的技术细节，让普通用户能够快速上手体验功能，让运维人员能够快速在企业环境中部署、卸载各类openGauss集群，减少了用户的学习成本和运维成本，实现了对openGauss各种常见操作的可视化，屏蔽了各种不同openGauss版本中的运维命令差异，可以让用户使用相同的方式操作数据库，不用知道命令细节也可以使用openGauss数据库的各项能力，让用户可以专注于自身的业务领域。

因此需要开发一些有针对性的运维监控工具，为不同配置不同运维要求的客户提供运维技术支撑，这些都将是openGauss社区的宝贵资产。而社区急需一个一体化的平台通过插件的方式将这些工具进行整合，并支持方便快捷的个性化配置。

本项目是基于Web的openGauss的可视化的平台系统，目的是方便客户使用和管理openGauss可视化工具，可以为客户降低openGauss数据库安装使用门槛，做到安全中心管理，插件管理，以及其它功能包括一键化部署、卸载、组件化安装、多版本升级、日常运维和监控。


## 项目仓库结构
```
├── openGauss-datakit                      // 平台项目
├── openGauss-datakit-agent                // 平台agent项目
├── plugins
├───├─alert-monitor                        // 告警监控插件
├───├─base-ops                             // 基础运维插件
├───├─compatibility-assessment             // 兼容性评估插件
├───├─container-management-plugin          // 容器管理插件
├───├─data-migration                       // 数据迁移插件
├───├─data-studio                          // 业务开发插件，Web 版本DataStudio
├───├─observability-instance               // 智能运维插件-实例监控项目
├───├─observability-log-search             // 智能运维插件-日志检索项目
├───├─observability-sql-diagnosis          // 智能运维插件-慢sql诊断项目
```

## 下载链接

##### Datakit下载链接

| 包名                            | 备注                                                        | 下载链接                                                                                                           |
|:------------------------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| openGauss-Datakit-All-7.0.0-RC3.tar.gz  | 完整包（包含所有插件）                                               | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/openGauss-Datakit-All-7.0.0-RC3.tar.gz |
| openGauss-Datakit-Mini-7.0.0-RC3.tar.gz | （推荐）最小化包（插件仅包含基础功能【业务开发】【基础运维】，其他插件可在DataKit的【插件管理】页面按需下载） | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/openGauss-Datakit-Mini-7.0.0-RC3.tar.gz          |

#### 注意：
1. 7.0.0-RC2之前的包不做区分，都包含所有插件，包名是Datakit-_$VERSION_.tar.gz（比如6.0.0版：Datakit-6.0.0.tar.gz）；
2. 7.0.0-RC2及之后的版本，下载链接为以*上表*内容，分为整包和最小化包；
3. 用最小化包安装部署后，插件可在datakit的【插件管理】页面按需下载，有在线下载、离线下载两种方式；
4. 在线下载，在下拉框选择需要下载的插件，点击确认即可；
5. 离线下载，在在[官网下载页面](https://opengauss.org/zh/download/)的openGauss Tools部分中Datakit_Mini_7.0.0-RC3下拉框中选择下载插件jar包（复制对应jar包的sha256值，算出的sha256值做对比，如果一致则可以确认下载下来的包是完整的，否则需要重新下载）上传下载的jar包进行离线下载；
6. 离线下载上传的插件版本需要和部署的datakit版本保持一致。

##### 插件下载链接

| 包名                                                  | 备注          | 下载链接                                                                                                                                         |
|:----------------------------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| alert-monitor-7.0.0-RC3-repackage.jar               | 告警监控插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/alert-monitor-7.0.0-RC3-repackage.jar                                         |
| data-migration-7.0.0-RC3-repackage.jar              | 数据迁移插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/data-migration-7.0.0-RC3-repackage.jar             |
| webds-plugin-7.0.0-RC3-repackage.jar                | 业务开发插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/webds-plugin-7.0.0-RC3-repackage.jar               |
| base-ops-7.0.0-RC3-repackage.jar                    | 基础运维插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/base-ops-7.0.0-RC3-repackage.jar                   |
| observability-instance-7.0.0-RC3-repackage.jar      | 实例监控插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-instance-7.0.0-RC3-repackage.jar     |
| observability-log-search-7.0.0-RC3-repackage.jar    | 日志检索插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-log-search-7.0.0-RC3-repackage.jar   |
| compatibility-assessment-7.0.0-RC3-repackage.jar    | 兼容性评估工具    | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/compatibility-assessment-7.0.0-RC3-repackage.jar |
| observability-sql-diagnosis-7.0.0-RC3-repackage.jar | 智能诊断插件      | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-sql-diagnosis-7.0.0-RC3-repackage.jar                  |

     
## 正式发布版下载

https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/Datakit/Datakit-7.0.0-RC1.tar.gz

https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC2/tools/Datakit/Datakit-All-7.0.0-RC2.tar.gz
https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC2/tools/Datakit/Datakit-Mini-7.0.0-RC2.tar.gz

为确认软件包在传输过程中由于网络原因或存储介质原因是否出现下载不完整的情况，需对软件包的完整性进行校验，通过校验的软件包才能部署，完整性校验步骤如下：

1. 计算下载包的sha256值（以Datakit 7.0.0-RC1为例，其他版本操作相同）

~~~
sha256sum Datakit-7.0.0-RC1.tar.gz
~~~

2. 在[官网下载页面](https://opengauss.org/zh/download/)的openGauss Tools部分中复制对应软件包的sha256值，与步骤1计算出的sha256值做对比，如果一致则可以确认下载下来的包是完整的，否则需要重新下载。

## 说明
1. 插件需要安装在平台上运行，因此需要先将平台项目安装部署后，再将其他插件项目打包安装使用。

2. 插件开发脚手架项目是为了方便开发者快速开发与平台适配的插件，而搭建的插件开发脚手架，开发者可在此脚手架之上开发业务功能。该脚手架配置的各项依赖版本已经经过验证，和平台兼容性最好，因此建议不要修改依赖版本。

## 编译代码
1. 请提前安装java 17+, maven 3.9.0+, node v18+(含npm)，并配置好maven镜像源和node镜像源
2. 在本地编译安装SpringBrick组件 https://gitcode.com/wang4721/springboot-plugin-framework-parent.git
3. 下载DataKit代码，执行`sh build.sh`
4. 编译输出件在output目录下

## 安装步骤
1. 解压安装包\
   通过下载链接或编译代码获取安装包`openGauss-Datakit-All-7.0.0-RC3.tar.gz`或`openGauss-Datakit-Mini-7.0.0-RC3.tar.gz`，（如果不需要所有插件，建议下载`openGauss-Datakit-Mini-7.0.0-RC3.tar.gz`，安装部署完成后，可以在插件管理界面按需下载插件）；
   
   解压安装包，解压命令如下:
   
   ```shell
   $ tar -zxvf openGauss-Datakit-All-7.0.0-RC3.tar.gz
   openGauss-Datakit-All-7.0.0-RC3/
   openGauss-Datakit-All-7.0.0-RC3/agent/
   openGauss-Datakit-All-7.0.0-RC3/agent/datakit-agent-7.0.0-RC3-runner.jar
   openGauss-Datakit-All-7.0.0-RC3/agent/application.yml
   openGauss-Datakit-All-7.0.0-RC3/build_commit_id.log
   openGauss-Datakit-All-7.0.0-RC3/config/
   openGauss-Datakit-All-7.0.0-RC3/config/application-temp.yml
   openGauss-Datakit-All-7.0.0-RC3/config/log4j2.xml
   openGauss-Datakit-All-7.0.0-RC3/doc/
   openGauss-Datakit-All-7.0.0-RC3/doc/datakit-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/container-management-plugin-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/alert-monitor-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/compatibility-assessment-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/oauth-login-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/observability-instance-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/data-studio-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/data-migration-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/intelligent-parameter-tuning-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/openGauss-tools-monitor-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/observability-sql-diagnosis-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/observability-log-search-README.md
   openGauss-Datakit-All-7.0.0-RC3/doc/base-ops-README.md
   openGauss-Datakit-All-7.0.0-RC3/install.sh
   openGauss-Datakit-All-7.0.0-RC3/openGauss-datakit-7.0.0-RC3.jar
   openGauss-Datakit-All-7.0.0-RC3/run.sh
   openGauss-Datakit-All-7.0.0-RC3/uninstall.sh
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/alert-monitor-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/base-ops-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/compatibility-assessment-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/webds-plugin-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/data-migration-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/observability-instance-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/observability-sql-diagnosis-7.0.0-RC3-repackage.jar
   openGauss-Datakit-All-7.0.0-RC3/visualtool-plugin/observability-log-search-7.0.0-RC3-repackage.jar
   ```
2. 运行安装脚本

   切换到解压后的 DataKit 根目录下，执行安装脚本`install.sh`，安装脚本会自动创建 DataKit 服务所需的目录结构，并修改配置文件中相关目录结构。

   ```shell
   sh install.sh
   ```

   运行日志如下：

   ```
   Checking the DataKit jar...
   Check the DataKit jar is openGauss-datakit-7.0.0-RC3.jar
   Check the DataKit jar successfully
   Creating required directories...
   Create required directories successfully
   Checking Java version...
   Check Java version is 17.0.13
   Generating the SSL key...
   Generating 4,096 bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 365 days
           for: CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, ST=Beijing, C=CN
   Generate the SSL key successfully
   Configuring the application-temp.yml...
   Configuring the application-temp.yml successfully
   Datakit has been installed successfully.
   Please go to the '/path/datakit_server' directory to manually start the DataKit server.
   The command is as follows:
       sh ./run.sh start --aes-key ******
       sh ./run.sh status
       sh ./run.sh stop
       sh ./run.sh restart --aes-key ******
   ```

3. 启动与日常运维

   安装成功后，参考如下命令运行`run.sh`脚本启停和管理 DataKit 服务。

   启动脚本中`--aes-key`参数的值为 DataKit 启动密码，用于内部加解密操作，DataKit不保存该密码，且暂不支持修改。 对该密码需要妥善保管，多次启停时，启动密码需要保持一致，否则DataKit会启动失败。

   启动应用：
   ```shell
   sh ./run.sh start --aes-key xxxxxx
   ```
   停止应用：
   ```shell
   sh ./run.sh stop
   ```
   重启应用：
   ```shell
   sh ./run.sh restart --aes-key xxxxxx
   ```
   检查应用状态：
   ```shell
   sh ./run.sh status
   ```
4. 访问服务

   启动成功后，通过浏览器输入如下地址：`https://ip:9494/` 访问`datakit`服务，这里的`ip`为`datakit`服务安装在的主机`ip`，`9494`为`datakit`服务默认端口，如有修改请根据实际情况替换。初始用户为`admin`，初始密码为`admin123`，首次登录需修改初始密码。

## 卸载说明

### 卸载步骤

   切换到 Datakit 根目录下，执行卸载脚本`uninstall.sh`，卸载脚本会自动停止 DataKit 服务，并删除 DataKit 服务相关目录结构，且脚本支持通过选项控制是否卸载已安装的`migration-portal`、`prometheus`、`instance-exporter`工具。

   脚本使用方式如下：

   | 命令格式                            | 说明                                                         |
   |:--------------------------------| :----------------------------------------------------------- |
   | `sh uninstall.sh`               | 默认行为：卸载 DataKit 自身及所有已安装的工具（同 `--all`）  |
   | `sh uninstall.sh -s\|--self`    | 仅卸载 DataKit 自身                                          |
   | `sh uninstall.sh -a\|--all`     | 卸载 DataKit 自身及所有已安装的工具                          |
   | `sh uninstall.sh -h\|--help`    | 显示帮助信息，列出所有支持的命令选项                         |

### 使用限制

1. 卸载远程环境上已安装的工具时，需要用户交互式输入安装环境的用户密码，用于连接远程主机执行卸载命令。
2. 卸载远程环境上已安装的工具时，Datakit 所在环境需要提前安装 `sshpass` 命令，用于连接远程主机时传输用户密码。

## 补充：切换DataKit后台数据库

   DataKit 安装完成后，会默认使用`Intarkdb`内置数据库作为 DataKit 后台数据库，用于保存 DataKit 的操作数据。 如果需要修改后台数据库为`openGauss`，请参考如下教程：

### 启用openGauss数据库
   
   编辑 DataKit 安装目录下的`config/application-temp.yml`配置文件，首先注释`Intarkdb`的`driver-class-name`和`url`配置项， 然后对如下`openGauss`的配置内容解开注释，并准确配置其`jdbc`连接信息即可。
   
   **注意**：使用`openGauss`作为后台数据库时，`openGauss`需要支持远程连接，并且配置的连接用户需要拥有`sysadmin`权限，远程连接配置和用户权限配置步骤请参考目录**补充：openGauss参数配置**

   ```yml
   # For openGauss
   driver-class-name: org.opengauss.Driver
   url: jdbc:opengauss://ip:port/database?currentSchema=public&batchMode=off
   username: dbuser
   password: ******
   ```

### 启用Intarkdb数据库

   编辑 DataKit 安装目录下的`config/application-temp.yml`配置文件，首先注释`openGauss`的`driver-class-name`、`url`、`username`和`password`配置项，然后对如下`Intarkdb`的配置内容解开注释即可。
   
   ```yml
   # For Intarkdb
   driver-class-name: org.intarkdb.Driver
   url: jdbc:intarkdb:data/datakit
   ```

## 补充：openGauss参数配置
1. 安装`openGauss`数据库\
   `openGauss`数据库的下载及安装请参考官网教程，这里不做赘述，下载地址：https://opengauss.org/zh/download/
2. 切换数据库安装用户，并加载环境变量\
   成功安装`openGauss`数据库后，主机切换到数据库安装用户，如`omm`用户。然后`source`环境变量文件，来加载`openGauss`的环境变量，如`omm`用户环境变量文件`~/.bashrc`。注意此环境变量文件为`openGauss`数据库环境变量所在文件，请根据实际情况替换。
   ```shell
   source ~/.bashrc
   ```
3. 参数配置（开启远程连接）\
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
6. 创建用户及数据库（赋予用户`sysadmin`权限）\
   成功连接数据库后，依次执行如下三条命令，分别进行创建用户，赋予用户管理员权限，创建数据库的操作。
   ```shell
   create user opengauss_test with password 'Sample@123';
   grant all privileges to opengauss_test;
   create database db_datakit;
   ```
   由于`openGauss`数据库不支持通过初始用户进行远程连接，因此此处创建新的用户供`datakit`远程连接时使用。同时，由于`datakit`需要拥有管理员权限对数据库进行操作，因此需要赋予连接用户管理员权限。此处新建`db_datakit`数据库作为`datakit`平台的底层数据库使用，不用做任何操作，`datakit`成功连接后会自动初始化数据。
7. 所有配置完成，保持`openGauss`数据库服务启动

## 补充：IntarkDB使用说明
### 1.使用源码编译运行`IntarkDB`  
   `IntarkDB`数据库的编译与运行可参考[openGauss-embedded](https://gitee.com/opengauss/openGauss-embedded/tree/master)
### 2.使用`IntarkDB-JDBC`本地连接或创建`IntarkDB`  
   在Java项目中，可使用`IntarkDB-JDBC`，直接创建和连接`IntarkDB`，相应的jar包放置在 `lib` 目录下。  
#### 连接 URL 格式
   ```jdbc:intarkdb:{数据库所在路径}```  
   例如，连接`data/datakit`目录下的数据库：```jdbc:intarkdb:data/datakit```  
   当目标路径数据库不存在时，`IntarkDB-JDBC`会自动创建数据库后再连接
### 3.使用`IntarkDB-JDBC`开启`IntarkDB`的网络服务  
   连接或创建`IntarkDB`，同时开启网络服务，以便其他程序通过网络连接 `IntarkDB` 数据库    
#### 连接 URL 格式   
   ```jdbc:intarkdb:{数据库所在路径}?open_remote=true&port={}&dbname={}```  
   例如，开启9000端口、数据库名称为intarkdb：```jdbc:intarkdb:data/datakit?open_remote=true&port=9000&dbname=intarkdb```  
### 4.使用`IntarkDB-JDBC`通过网络连接至`IntarkDB`  
   当`IntarkDB`开启网络服务时，可通过网络连接至远程的`IntarkDB`实例。
#### 连接 URL 格式
   ```jdbc:intarkdb:tcp://{ip:port}?dbname={dbname}```  
   例如，连接到127.0.0.1:9000的intarkdb数据库：```jdbc:intarkdb:tcp://127.0.0.1:9000?dbname=intarkdb```
   

## 参与开发
开发环境搭建参考 [开发环境搭建](https://gitee.com/opengauss/openGauss-workbench/tree/master/openGauss-datakit/doc/DataKit%20Dev%20Setup.md)

插件开发请参考`openGauss-datakit/doc`目录下的开发手册

新增插件请务必更新`build.sh`脚本，保证可以一键编译
