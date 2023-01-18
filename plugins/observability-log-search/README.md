# 日志检索插件介绍



## 版本介绍

observability-log-search项目目前版本为1.0.0，主要目的是为openGauss用户提供OS日志、数据库日志统一检索入口。

它作为一体化平台的可插拔插件开发，本特性依赖于openGauss一体化平台的插件特性。



## 项目背景

openGuass用户以往查看日志，需要登录服务器查看或者下载对应日志文件，操作繁琐且不支持复杂的查询过滤，查找效率慢。日志检索插件支持统一Web页面检索日志相关内容，能提高用户解决生产问题的效率。

日志检索插件支持使用FileBeat采集数据库、系统等日志上传到ElasticSearch中，然后在Web端对存储在ElasticSearch中的日志进行统一检索。



## 功能特性介绍



<table>
    <tr>
        <th>特性名称</th>
        <th>特性描述</th>
        <th>备注</th>
    </tr>
    <tr>
        <td>日志检索</td>
        <td>支持openGauss数据库日志和系统日志的统一检索</td>
        <td>无</td>  
    </tr>
</table>



## 版本使用注意事项

- observability-log-search项目是作为openGauss一体化平台插件进行开发，使用本项目必须依赖一体化平台。
- observability-log-search为一个Java应用，在构建时需确保配置Java 11+的JDK

## 安装说明

- ```
  #下载源码：
  git clone git@gitee.com:opengauss/openGauss-workbench.git
  #进入根目录
  cd plugins/observability-log-search
  #修改ElasticSearch的连接信息
  vim src/main/resources/application-prod.yml
  ```
  
  ```
  elasticsearch:
    # Clusters can be separated by ',' such as 127.0.0.1:9200,127.0.0.2:9200
    uris: localhost:9200
    username: elastic
    password: changeme
  ```
  
- ```
  mvn clean package -P prod
  ```

- 打包完成后在plugins/observability-log-search目录中找到observability-log-search-1.0.x-SNAPSHOT-repackage.jar即为插件安装包。

- 打开并登陆openGauss一体化平台，点击插件管理-安装插件，将上述步骤获得的安装包上传并安装。

- 刷新页面可在右侧菜单栏看到”智能运维”一级菜单下的“日志检索”二级菜单即为安装成功。

## 日志采集部署与配置

日志检索插件依赖FileBeat采集日志存储到ElasticSearch中，用户需要部署FileBeat和ElasticSearch，具体部署方法如下

### 1.ElasticSearch安装

以下为openEuler 20.03 (LTS)下通过安装包的安装步骤：

（1）从elastic官网下载安装包，本特性是基于Linux版本Elasticsearch 8.3.3开发

    https://www.elastic.co/cn/products/elasticsearch

（2）上传压缩包并解压

    tar -zxvf elasticsearch-8.3.3-linux-aarch64.tar.gz
    # 目录重命名,根据需要
    mv elasticsearch-8.3.3 elasticsearch

（3）创建elastic用户，处于安全考虑ElasticSearch不允许root用户运行

    # 创建用户
    useradd elastic
    # 设置密码
    passwd elastic
    # 修改权限
    chown -R elastic:elastic elasticsearch-8.3.3
    # 切换用户
    su - elastic

（4）修改配置

    # 修改内存占用 
    cd /config/jvm.options
    vi jvm.options
    -Xms1g
    -Xmx1g
    # 修改基本配置
    vi elasticsearch.yml
    # 集群名字
    cluster.name: my-application
    # 节点名字
    node.name: node-1
    # 数据目录位置
    path.data: /path/to/data
    # 日志目录位置
    path.logs: /path/to/logs
    # 绑定的ip
    network.host: 192.168.0.1
    # 集群
    discovery.seed_hosts: ["host1", "host2"]
    cluster.initial_master_nodes: ["node-1", "node-2"]
    # 根据指定数据、日志目录位置创建文件夹
    # 创建文件夹
    mkdir -p /path/to/data
    # 修改权限
    chown -R elastic:elastic /path/to/data
    # 创建文件夹
    mkdir -p /path/to/logs
    # 修改权限
    chown -R elastic:elastic /path/to/logs

（5）后台运行

    ./bin/elasticsearch -d

以下为openEuler 20.03 (LTS)下通过docker的安装步骤：

    docker pull elasticsearch:8.3.3
    mkdir -p /opt/elasticsearch
    mkdir -p /opt/elasticsearch/data
    mkdir -p /opt/elasticsearch/config
    mkdir -p /opt/elasticsearch/log
    sudo chmod -R 777 elasticsearch/
    从插件源码里面拷贝文件/files/elasticsearch.yml到elasticsearch/config/elasticsearch.yml
    --启动es
    docker run --name es \
    -p 9200:9200 -p 9300:9300 \
    -v /opt/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
    -v /opt/elasticsearch/data:/usr/share/elasticsearch/data \
    -v /opt/elasticsearch/log:/usr/share/elasticsearch/log \
    -e ES_JAVA_OPTS="-Xms512m -Xmx512m" \
    -d elasticsearch:8.3.3
    --查看日志
    docker logs -f -t --tail 100 es

### 2.Filebeat安装

以下为openEuler 20.03 (LTS)下通过在线下载安装包的安装步骤：

1. 获取集群ID和实例ID

   进入日志检索插件功能中，在集群/实例控件中，可以获取到集群ID和实例ID

   <img src="doc\1.png" alt="1.png" style="zoom:50%;" />

2. 安装filebeat

    ```
    wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-8.3.3-linux-arm64.tar.gz
    tar -zxvf filebeat-8.3.3-linux-arm64.tar.gz
    mv filebeat-8.3.3-linux-arm64 filebeat
    #如果安装ElasticSearch已新增elastic用户，这里可以不用再增加
    useradd elastic
    passwd elastic
    chown -R elastic:elastic filebeat
    ```

3. 修改配置文件

   修改openGauss-workbench\plugins\observability-log-search\filebeat\filebeat.yml文件

   ```
   output.elasticsearch.hosts: es的ip
   output.elasticsearch.indices.*.index: "" 的后缀ogbrench2改成实例ID
   ```

   修改openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\log\ingest\pipeline.yml文件

   ```
   set:
   field: nodeId
   value: test238//改成实例ID
   ```

   修改以下文件：

   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\errorlog\manifest.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\log\manifest.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\slowlog\manifest.yml

   ```
   三个文件的var.0.default改成对应opengauss的日志文件路径
   ```

   多节点部署需修改pipeline名称，修改以下文件：
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\errorlog\manifest.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\log\manifest.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\slowlog\manifest.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\\system\errorlog\manifest.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\\system\syslog\manifest.yml

   ```
   ingest_pipeline:
   	ingest/pipeline-实例ID.yml//修改名字加-实例ID
   	ingest/pipeline-log-实例ID.yml//修改名字加-实例ID
   	ingest/pipeline-csv-实例ID.yml//修改名字加-实例ID
   	
   如：
   ingest_pipeline:
   	ingest/pipeline-1610210132873134081.yml
   	ingest/pipeline-log-1610210132873134081.yml
   	ingest/pipeline-csv-1610210132873134081.yml
   
   ```
    修改以下文件夹中的3个文件pipeline.yml、pipeline-csv.yml、pipeline-log.yml的名称，对应上面配置的文件名称
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\errorlog\ingest
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\log\ingest
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\slowlog\ingest
   openGauss-workbench\plugins\observability-log-search\filebeat\module\system\errorlog\ingest
   openGauss-workbench\plugins\observability-log-search\filebeat\module\system\syslog\ingest

   修改以下5个文件的内容，把pipeline-log和pipeline-csv改为pipeline-log-实例ID和pipeline-csv-实例ID

   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\errorlog\ingest\pipeline.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\log\ingest\pipeline.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\opengauss\slowlog\ingest\pipeline.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\\system\errorlog\ingest\pipeline.yml
   openGauss-workbench\plugins\observability-log-search\filebeat\module\\system\syslog\ingest\pipeline.yml

   ```
      - pipeline:
          name: '{< IngestPipeline "pipeline-log-实例ID" >}'//修改文件名字
          if: ctx.separator != ','
             - pipeline:
          name: '{< IngestPipeline "pipeline-csv-实例ID" >}'//修改文件名字
          if: ctx.separator == ','
   ```

4. 将修改好的配置文件复制到filebeat目录

   将openGauss-workbench\plugins\observability-log-search\filebeat合并到\filebeat
   
6. 修改文件权限

   ```
   #数据库日志文件夹授权(递归到根目录),系统日志文件夹授权
   chmod -R 607 /data/opengauss/install/data/datanode1/pg_log
   chmod -R 601 /data/opengauss/install/data/datanode1
   chmod -R 601 /data/opengauss/install/data
   chmod -R 607 /var/log
   ```

5. 配置/启动filebeat
    
    ```
    ./filebeat modules enable system opengauss
    nohup ./filebeat -e -c filebeat.yml &
    ```

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
