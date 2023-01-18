

# 实例监控插件介绍


## 版本介绍

observability-instance 项目目前版本为 1.0.0，主要目的是为 openGauss 用户提供实例监控能力。

它作为一体化平台的可插拔插件开发，本特性依赖于openGauss一体化平台的插件特性。


## 项目背景

实例监控支持监控 openGauss 数据库以及数据库所在服务器，本特性主要关注数据库和操作系统监控指标，以及 TOPSQL 捕获和分析。

与其他需求及特性的交互分析：支持关联 SQL 诊断功能，进入 TOPSQL 详情页面，用户可触发创建诊断任务，对 TOPSQL 进行诊断分析。


## 功能特性介绍



<table>
    <tr>
        <th>特性名称</th>
        <th>特性描述</th>
        <th>备注</th>
    </tr>
    <tr>
        <td>实例监控</td>
        <td>支持openGauss数据库实例监控关键信息监控能力</td>
        <td>无</td>  
    </tr>
</table>



## 版本使用注意事项

- observability-instance 项目是作为 openGauss 一体化平台插件进行开发，使用本项目必须依赖一体化平台。
- observability-instance 为一个 Java 应用，在构建时需确保配置 Java 11+的JDK

## 安装说明

```
#下载源码：
git clone git@gitee.com:opengauss/openGauss-workbench.git
#进入根目录
cd plugins/observability-instance
#修改Promtheus的连接信息
vim src/main/resources/application-prod.yml
```

  ```
prometheus:
  server:
    url: http://127.0.0.1:9090
  ```

```
mvn clean package -P prod
```

- 打包完成后在plugins/observability-instance 目录中找到observability-instance-1.0.x-SNAPSHOT-repackage.jar即为插件安装包。
- 打开并登陆openGauss一体化平台，点击插件管理-安装插件，将上诉步骤获得的安装包上传并安装。
- 刷新页面可在右侧菜单栏看到”智能运维”一级菜单下的“实例监控”二级菜单即为安装成功。

## 数据采集部署说明

实例监控插件需要在数据库所在服务器部署node-exporter、openGauss-prometheus-exporter，以及部署Prometheus存储采集数据，相关部署说明如下

### 一、node-exporter安装

1. 下载服务器对应版本的node-exporter安装包（插件测试中使用1.3.1版本）

   下载地址：https://github.com/prometheus/node_exporter/releases

   以arm64平台为例：
    ```
   wget https://github.com/prometheus/node_exporter/releases/download/v1.3.1/node_exporter-1.3.1.linux-arm64.tar.gz
    ```

2. 解压安装包

   ```
   tar xvfz node_exporter-1.3.1.linux-arm64.tar.gz
   ```

3. 可以直接启动应用

   ```
   cd /data/node-exporter/node_exporter-1.3.1.linux-arm64/
   ./node_exporter --collector.systemd
   ```
   
4. 配置开机启动

   创建文件

```
vim /usr/lib/systemd/system/node_exporter.service
```

   添加以下内容，注意路径根据自己解压路径修改
   ```
   [Service]
   ExecStart=/data/node-exporter/node_exporter-1.3.1.linux-arm64/node_exporter --collector.systemd
   ExecReload=/bin/kill -HUP $MAINPID
   KillMode=process
   Restart=on-failure
   
   
   [Install]
   WantedBy=multi-user.target
   
   [Unit]
   Description=node_exporter
After=network.target
   ```
   执行命令启动程序
   ```
   systemctl daemon-reload
   
   systemctl enable node_exporter.service
   
   systemctl start node_exporter.service
   ```


5. 验证安装成功

   程序默认端口为9100，也可以通过--web.listen-address=":6000"参数指定端口

   ```
   curl http://localhost:9100/metrics
   ```

   以上操作有采集数据返回即代表node-exporter安装成功

### 二、openGauss-exporter安装

1. 安装方式

   openGauss-exporter支持源码编译运行和docker运行

   具体安装方式查看https://gitee.com/opengauss/openGauss-prometheus-exporter

   以下以docker安装为例

2. 安装docker

   拉取包并解压

   ```
   mkdir /data/docker-server
   wget https://download.docker.com/linux/static/stable/aarch64/docker-18.09.8.tgz --no-check-certificate 
   wget https://download.docker.com/linux/static/stable/x86_64/docker-18.09.8.tgz --no-check-certificate
   tar xvpf docker-18.09.8.tgz -C /data/docker-server
   ```

   复制文件

   ```
   cp -p -f /data/docker-server/docker/* /usr/bin
   ```

   配置 docker.service

   ```
   vim /usr/lib/systemd/system/docker.service
   ```

   输入以下内容

   ```
   [Unit]
   Description=Docker Application Container Engine
   Documentation=http://docs.docker.com
   After=network.target docker.socket
   [Service]
   Type=notify
   EnvironmentFile=-/run/flannel/docker
   WorkingDirectory=/usr/local/bin
   ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:9877 -H unix:///var/run/docker.sock --selinux-enabled=false --log-opt max-size=1g 
   ExecReload=/bin/kill -s HUP
   \# Having non-zero Limit*s causes performance problems due to accounting overhead
   \# in the kernel. We recommend using cgroups to do container-local accounting. 
   LimitNOFILE=infinity
   LimitNPROC=infinity
   LimitCORE=infinity
   \# Uncomment TasksMax if your systemd version supports it. 
   \# Only systemd 226 and above support this version. 
   \#TasksMax=infinity
   TimeoutStartSec=0
   \# set delegate yes so that systemd does not reset the cgroups of docker containers 
   Delegate=yes
   \# kill only the docker process, not all processes in the cgroup 
   KillMode=process
   Restart=on-failure
   [Install]
   WantedBy=multi-user.target
   ```

   启动 docker

   ```
   systemctl daemon-reload
   systemctl restart docker
   systemctl enable docker
   ```

3. 使用源码中配置文件运行openGauss-exporter

   在插件源码根目录下，有og_exporter.yml文件，该文件基于openGauss-exporter源码中的配置文件添加一些额外的数据采集

   使用docker运行时可以使用以下命令指定配置文件启动openGauss-exporter

   ```
   mkdir -p /data/docker-vol/openGauss-exporter
   ```

   复制配置源码中预设的配置文件，注意文件路径对应自己的源码下载路径

   ```
   cp 源码下载路径/plugins/observability-instance/files/og_exporter.yml /data/docker-vol/openGauss-exporter/og_exporter.yml
   ```

   执行以下命令启动openGauss-exporter

   注意修改语句中username、password、localhost、5432，对应值为数据库连接信息的账号、密码、IP、端口

   密码中如果有特殊字符，需要做URLencode处理

   ```
   docker run -d --net=host -e \
   DATA_SOURCE_NAME="postgresql://username:password@localhost:5432/postgres?sslmode=disable" \
   -v /data/docker-vol/openGauss-exporter:/data/docker-vol/openGauss-exporter \
   --name opex2 \
   enmotech/opengauss_exporter \
   --config /data/docker-vol/openGauss-exporter/og_exporter.yml
   ```

4. 测试数据采集

   程序默认端口为9187

   ```
   curl http://localhost:9187/metrics
   ```

   以上操作有采集数据返回即代表node-exporter安装成功

### 三、Prometheus安装与配置

1. Prometheus安装

   具体安装方式参考官网：https://prometheus.io/docs/prometheus/latest/installation/

   以下以docker安装为例

2. 打开实例监控插件，获取数据库实例的节点ID

   目前可以从插件的集群/实例下拉框中，获取到实例的ID

   <img src="doc\1.png" alt="image-20221216153920466" style="zoom: 33%;" />

3. 修改Prometheus配置文件

   修改源码下载路径/plugins/observability-instance/files/prometheus.yml的prometheus配置文件

   将2个job"node_exporter","gauss_exporter"的IP、端口和instance label做如下修改

   IP：修改为2个exporter所在服务器的内部IP（prometheus和exporter同一环境时）或外部IP，请不要使用localhost和127.0.0.1

   端口：2个exporter使用的端口

   instance label：2中获取到的实例ID

   例子如下

   ```
     - job_name: "node_exporter"
       static_configs:
         - targets: ["192.168.110.19:9100"]
           labels:
                   instance: '1603653090037374978'
   
   
     - job_name: "gauss_exporter"
       static_configs:
         - targets: ["192.168.110.19:9187"]
           labels:
                   instance: '1603653090037374978'
   ```

4. 搭建prometheus

   创建文件夹

   ```
   mkdir -p /data/docker/prometheus
   ```

   复制修改后的配置文件和授权

   ```
   cp 源码下载路径/plugins/observability-instance/files/prometheus.yml /data/docker/prometheus/prometheus.yml
   chmod 777 /data/docker/prometheus/*
   ```

   启动prometheus

   ```
   docker run -d -p 9090:9090 \
   -v /data/docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
   -v /etc/localtime:/etc/localtime \
   --name prom \
   --privileged=true \
   prom/prometheus
   ```

5. 访问prometheus的web端查看target是否正常

   访问http://prometheus的IP地址:9090/targets

   target如果State状态为UP，代表该exporter能正常采集到数据，实例监控插件接入此Prometheus后可读取相关数据

   ![image-20221216155652896](doc\2.png)

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