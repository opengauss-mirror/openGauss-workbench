# Base-Ops 基础运维插件介绍



## 版本介绍

Base-Ops项目目前版本为1.0.0，主要目的是为openGauss用户提供简单易上手的运维、可视化、可监控能力。它作为一体化平台的可插拔插件开发，本特性依赖于openGauss一体化平台的插件特性。



## 项目背景

openGauss的安装、运维场景对于初级用户或单纯想要测试openGauss数据库基本特性的使用者来说技术难度较大、过程较为复杂、学习曲线较为陡峭，尤其企业版安装对一般用户来说操作难度很大。

使用可视化运维平台可以屏蔽openGauss的技术细节，让普通用户能够快速上手体验功能，让运维人员能够快速在企业环境中部署、卸载各类openGauss集群，减少了用户的学习成本和运维成本，实现了对openGauss各种常见操作的可视化，屏蔽了各种不同openGauss版本中的运维命令差异，可以让用户使用相同的方式操作数据库，不用知道命令细节也可以使用openGauss数据库的各项能力，让用户可以专注于自身的业务领域。



基于上述背景本插件主要提供如下能力：

- 实现不同版本的openGauss安装、不同版本的升级；实现简单快捷的数据库日常运维，例如启停卸载等

- 实现在画面上查看openGauss数据库基本状态、实时数据推送等。

- 数据算子的可视化编辑、SQL的预览和运行、图表的生成与分享。

  

## 功能特性介绍



<table>
    <tr>
        <th>特性名称</th>
        <th>特性描述</th>
        <th>备注</th>
    </tr>
    <tr>
        <td>并行安装任务</td>
        <td>支持openGauss企业版、极简版、轻量版安装</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>集群安装</td>
        <td>支持openGauss企业版、极简版、轻量版安装</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>集群导入</td>
        <td>支持openGauss企业版、极简版、轻量版导入</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>一键安装</td>
        <td>支持openGauss极简版3.0.0一键安装</td>
        <td>无</td>  
    </tr>
    <tr>
        <td>集群升级</td>
        <td>支持openGauss企业版升级</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>集群运维</td>
        <td>支持openGauss企业版、极简版、轻量版的各类运维操作，例如：启动、停止、卸载、备机重建等</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>集群监控图表</td>
        <td>支持openGauss企业版、极简版、轻量版的各类运维监控指标，例如CPU使用率、内存占用率、会话数量等</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>物理机资源管理</td>
        <td>实现对物理机及用户的管理</td>
        <td>无</td> 
    </tr>
    <tr>
        <td>AZ管理</td>
        <td>实现对AZ的管理</td>
        <td>无</td>
    </tr>
    <tr>
        <td>快照管理</td>
        <td>支持openGauss企业版、极简版、轻量版的快照生成与删除</td>
        <td>无</td>
    </tr>
    <tr>
        <td>WDR报告</td>
        <td>支持openGauss企业版的WDR报告生成与在线查看</td>
        <td>无</td>
    </tr>
    <tr>
        <td>慢SQL查询</td>
        <td>支持openGauss企业版、极简版、轻量版的慢SQL在线查看</td>
        <td>无</td>
    </tr>
    <tr>
        <td>集群日志</td>
        <td>支持openGauss企业版、极简版、轻量版的日志在线查看</td>
        <td>无</td>
    </tr>
    <tr>
        <td>备份恢复</td>
        <td>支持openGauss企业版、极简版、轻量版的数据备份恢复</td>
        <td>无</td>
    </tr>
    <tr>
        <td>可视化编辑</td>
        <td>提供拖拽、连线的操作方式完成数据流算子的组装、配置</td>
        <td>支持3.0.0+</td>  
    </tr>
    <tr>
        <td>实时图表生成</td>
        <td>支持读取单步算子执行结果为用户生成自定义图表，并可以记录快照</td>
        <td>支持3.0.0+</td> 
    </tr>
       <tr>
        <td>报表构建和分享</td>
        <td>可将一个或多个图表快照通过自由排布的方式构建报表，并可通过外链分享给游客用户</td>
        <td>无</td> 
    </tr>
</table>


# 

## 版本使用注意事项

- base-ops项目是作为openGauss一体化平台插件进行开发，使用本项目必须依赖一体化平台。
- base-ops为一个Java应用，在构建时需确保配置Java 11+的JDK

## 安装说明

- ```
  #下载源码：
  git clone git@gitee.com:opengauss/openGauss-workbench.git
  #进入根目录
  cd base-ops
  #编译打包
  mvn clean package -Dmaven.test.skip
  ```
  
- 打包完成后在bsae-ops/target目录中找到base-ops-{version}-repackage.jar即为插件安装包。

- 打开并登陆openGauss一体化平台，点击插件管理-安装插件，将上诉步骤获得的安装包上传并安装。

- 刷新页面可在右侧菜单栏看到**安装部署、可观测运维、业务设计**三个一级菜单即为安装成功。

## 使用说明

- 具体使用方式见使用文档



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
