# Portal数据迁移插件介绍



## 版本介绍

data-migration项目目前版本为1.0.0。

  

## 功能特性介绍
1、可将Mysql源端数据库通过在线或离线的方式将数据迁移至openGauss目的端数据库

2、基于资源最大化原则分配调度迁移任务运行机器。

2、可批量管理迁移任务

## 版本使用注意事项
- data-migration为一个Java 插件，在构建时需确保配置Java 11+的JDK

## 安装说明

- ```
  #下载源码：
  git clone git@gitee.com:opengauss/openGauss-workbench.git
  #进入根目录
  cd data-migration
  #编译打包
  mvn clean package -Dmaven.test.skip
  
- 打包完成后在data-migration/target目录中找到data-migration-1.0.x-repackage.jar即为插件安装包。

- 打开并登录openGauss Datakit，点击插件管理-安装插件，将上诉步骤获得的安装包上传并安装。


## Portal离线安装步骤
数据迁移插件底层依赖Portal组件，在任务执行时如果执行机器未安装Portal，将会执行Portal的安装，因为安装过程时间较长，因此可选择在执行机器上提前安装Portal，安装步骤如下。

1、在执行机器上创建非root用户，并授予sudo免密权限
> + 添加用户 **useradd og_ops**
> + 设置密码 **passwd og_ops**
> + 增加文件编辑权限 **chmod u+w /etc/sudoers**
> + 编辑文件 **/etc/sudoers**，在文件底部增加：**og_ops ALL=(ALL) NOPASSWD: ALL**
> + 回收文件编辑权限 **chmod u-w /etc/sudoers**
> 
2、切换非root用户，进入根目录下，cd ~

3、执行wget -P ~ portal下载地址 -O portal.zip。

4、执行unzip -d ~/portal ~/portal.zip

5、sed -i 's#/ops/portal#/home/非root用户名/portal#g' ~/portal/config/toolspath.properties

6、java -Dpath=/home/非root用户名/portal/ -Dorder=install_mysql_all_migration_tools -Dskip=true -jar /home/非root用户名/portal/portalControl-1.0-SNAPSHOT-exec.jar

### 注意 
1、以上命令中的"portal下载地址"需要更换成实际的下载地址。

2、以上命令中的"非root用户名"需要跟换成实际操作用户名

# 参与贡献

**参与贡献**

作为openGauss用户，你可以通过多种方式协助openGauss社区。参与社区贡献的方法请参见[社区贡献](https://opengauss.org/zh/contribution.html)，这里简单列出部分方式供参考。

**特别兴趣小组**

openGauss将拥有共同兴趣的人们聚在一起，组成了不同的特别兴趣小组（SIG）。当前已有的SIG请参见[SIG列表](https://opengauss.org/zh/contribution.html)。

我们欢迎并鼓励你加入已有的SIG或创建新的SIG，创建方法请参见[SIG管理指南](https://opengauss.org/zh/contribution.html)。

**邮件列表和任务**

欢迎你积极地帮助用户解决在[邮件列表](https://opengauss.org/zh/community/mails.html)和issue任务（包括[代码仓任务](https://gitee.com/organizations/opengauss/issues)） 中提出的问题。另外，我们也欢迎你提出问题。这些都将帮助openGauss社区更好地发展。

**文档**

你不仅可以通过提交代码参与社区贡献，我们也欢迎你反馈遇到的问题、困难，或者对文档易用性、完整性的改进建议等。例如获取软件或文档过程中的问题，使用系统过程中的难点。欢迎关注并改进openGauss社区的文档模块。

**IRC**

openGauss也在IRC开辟了频道，作为提供社区支持和交互的额外渠道。详情请参见[openGauss IRC](https://opengauss.org/zh/community/onlineCommunication.html)。

# 开源的资料文档应附有对应的文档许可证

本文档遵循[知识共享许可协议CC 4.0](https://creativecommons.org/licenses/by/4.0/) (http://creativecommons.org/Licenses/by/4.0/)。
