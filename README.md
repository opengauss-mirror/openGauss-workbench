# openGauss DataKit
openGauss的安装、运维场景对于初级用户或单纯想要测试openGauss数据库基本特性的使用者来说技术难度较大、过程较为复杂、学习曲线较为陡峭，尤其企业版安装对一般用户来说操作难度很大。使用可视化运维平台可以屏蔽openGauss的技术细节，让普通用户能够快速上手体验功能，让运维人员能够快速在企业环境中部署、卸载各类openGauss集群，减少了用户的学习成本和运维成本，实现了对openGauss各种常见操作的可视化，屏蔽了各种不同openGauss版本中的运维命令差异，可以让用户使用相同的方式操作数据库，不用知道命令细节也可以使用openGauss数据库的各项能力，让用户可以专注于自身的业务领域。

因此需要开发一些有针对性的运维监控工具，为不同配置不同运维要求的客户提供运维技术支撑，这些都将是openGauss社区的宝贵资产。而社区急需一个一体化的平台通过插件的方式将这些工具进行整合，并支持方便快捷的个性化配置。

本项目是基于Web的openGauss的可视化的平台系统，目的是方便客户使用和管理openGauss可视化工具，可以为客户降低openGauss数据库安装使用门槛，做到安全中心管理，插件管理，以及其它功能包括一键化部署、卸载、组件化安装、多版本升级、日常运维和。


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

https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/Datakit-5.1.1.tar.gz

## 说明
1、插件需要安装在平台上运行，因此需要先将平台项目安装部署后，再将其他插件项目打包安装使用。

2、插件开发脚手架项目是为了方便开发者快速开发与平台适配的插件，而搭建的插件开发脚手架，开发者可在此脚手架之上开发业务功能。该脚手架配置的各项依赖版本已经经过验证，和平台兼容性最好，因此建议不要修改依赖版本。

## 编译代码
1. 请提前安装java 11+, maven 3.8.0+, node v18+(含npm)，并配置好maven镜像源和node镜像源
2. 执行`sh build.sh`
3. 编译输出件在output/Datakit-${pom_version}.tar.gz

## 使用
1. 解压安装包，目录如下:
   ```java
   $ ls -l
   total 92389
   -rw-r--r-- 1 Administrator 197121    22173  8月 29 15:25 alert-monitor-README.md
   -rw-r--r-- 1 Administrator 197121      977  8月 29 15:22 application-temp.yml
   -rw-r--r-- 1 Administrator 197121     6923  8月 29 15:30 base-ops-README.md
   -rw-r--r-- 1 Administrator 197121     1162  8月 29 15:32 datakit-demo-plugin-README.md
   -rw-r--r-- 1 Administrator 197121     7928  8月 29 15:22 datakit-README.md
   -rw-r--r-- 1 Administrator 197121     3839  8月 29 15:34 data-migration-README.md
   -rw-r--r-- 1 Administrator 197121    86951  8月 29 15:36 data-studio-README.md
   -rw-r--r-- 1 Administrator 197121       69  8月 29 15:37 datasync-mysql-README.md
   -rw-r--r-- 1 Administrator 197121    18837  8月 29 15:41 observability-instance-README.md
   -rw-r--r-- 1 Administrator 197121     8931  8月 29 15:44 observability-log-search-README.md
   -rw-r--r-- 1 Administrator 197121 94425028  8月 29 15:22 openGauss-datakit-5.1.1.jar
   drwxr-xr-x 1 Administrator 197121        0  8月 29 15:44 visualtool-plugin```
2. 创建新的目录
 ```shell
mkdir config files ssl logs
```
3. 更改配置文件
解压的文件中`application-temp.yml`内容需要移动到第二步的config目录下,同时需要正确编辑, /ops路径统一修改为实际路径，而第二步的目录就是为了此处统一使用的:
   system.defaultStoragePath: /ops/files  
   server.ssl.key-store: /ops/ssl/keystore.p12
   logging.file.path: /ops/logs
   还有数据库采用openGauss请正确配置连接信息.
4. 生成密钥信息
   ```shell
   keytool -genkey -noprompt \
   -dname "CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, S=Beijing, C=CN" \
   -alias opengauss \
   -storetype PKCS12 \
   -keyalg RSA \
   -keysize 2048 \
   -keystore /ops/ssl/keystore.p12 \
   -validity 3650 \
   -storepass 123456
   ```


     **注意: 这里的storepass与配置文件中的key-store-password应该保持一致。keystore路径即为配置文件中的key-store路径** 

5.启动
nohup java -Xms2048m -Xmx4096m -jar openGauss-datakit-5.1.1.jar --spring.profiles.active=temp >datakit.out 2>&1 &

## 参与开发
插件开发请参考 openGauss-datakit/doc 目录下的开发手册
新增插件请勿必更新build.sh脚本，保证可以一键编译