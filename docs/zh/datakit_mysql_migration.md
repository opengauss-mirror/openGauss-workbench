# MySQL数据迁移

# 环境准备

## 数据库服务准备

### openGauss数据库

参考[openGauss安装指南](https://docs.opengauss.org/zh/docs/latest/installation_guide/installation_overview.html)安装企业版数据库，版本要求为5.1.0及以后版本（如5.0.5、6.0.5等）。

### MySQL数据库

安装MySQL数据库，支持迁移的版本范围为5.7至8.2。

## 迁移工具部署

### 安装DataKit

1. 准备Java 17+运行环境
2. 参考[DataKit安装步骤](https://gitcode.com/opengauss/openGauss-workbench#%E5%AE%89%E8%A3%85%E6%AD%A5%E9%AA%A4)完成Datakit服务安装
3. 确保DataKit所在服务器可正常访问MySQL和openGauss数据库，无网络连通性问题

### 安装数据迁移插件

登录DataKit页面，进入“插件管理”菜单，确认“数据迁移插件”已安装。如未安装，点击“安装插件”完成安装。

![插件管理](.\figures\datakit_mysql_migration_plugin_manage.png)

### 安装Portal

Portal是数据迁移门户工具，负责执行迁移任务，需单独安装。同样需要确保portal所在服务器可正常访问MySQL和openGauss数据库，无网络连通性问题。

1. **添加服务器**

   在DataKit页面“资源中心”→“服务器管理”中添加服务器，添加时请使用拥有sudo免密权限的普通用户。Portal支持安装的服务器系统架构有：CentOS7 x86_64、openEuler20.03 x86_64、openEuler20.03 aarch64、openEuler22.03 x86_64、openEuler22.03 aarch64、openEuler24.03 x86_64、openEuler24.03 aarch64。

   ![服务器管理](.\figures\datakit_mysql_migration_create_host.png)

   ![添加服务器](.\figures\datakit_mysql_migration_edit_host_info.png)

2. **安装Portal**

   在“数据迁移”→“迁移工具管理”中选择已添加的服务器，点击“开始安装”。

   ![安装portal](.\figures\datakit_mysql_migration_install_portal.png)

> [!NOTE] 说明
>
> Portal安装时需要在环境中安装运行所需的依赖，因此需要普通用户拥有sudo免密权限，否则安装可能失败。安装完成后可取消sudo权限，Portal运行不再依赖此权限。

## openGauss数据库配置

### 创建连接用户

在openGauss安装用户下，使用gsql连接openGauss，执行以下SQL创建用户并授予sysadmin权限：

```sql
CREATE USER username WITH PASSWORD '******';
GRANT ALL PRIVILEGES TO username;
```

> [!NOTE] 说明
>
> 迁移MySQL的视图、函数、触发器、存储过程时，openGauss连接用户必须拥有sysadmin权限，否则这些对象可能迁移失败。

### 配置白名单

openGauss需要配置白名单，才允许通过用户远程连接数据库，否则连接会报错。

在openGauss安装用户下执行如下命令，完成白名单配置：

```bash
# 配置白名单
gs_guc set -N all -I all -h "host all all 0.0.0.0/0 sha256"

# 重启数据库使配置生效
gs_om -t restart
```

### 开启逻辑复制权限

反向迁移阶段需要开启逻辑复制连接权限，在openGauss安装用户下执行如下命令，完成配置：

```bash
gs_guc set -N all -I all -h "host replication all 0.0.0.0/0 sha256"
gs_guc set -N all -I all -c "wal_level = logical"

# 6.0.5及之后版本需配置，低版本无需配置
gs_guc set -N all -I all -c "enable_subscription = on"

# 重启数据库使配置生效
gs_om -t restart
```

### 创建目标数据库

在openGauss创建B兼容模式数据库作为迁移目标库，参考SQL语句如下：

```sql
CREATE DATABASE target_db WITH DBCOMPATIBILITY = 'B' ENCODING = 'UTF8';

-- 切换到B库加载dolphin插件
\c target_db
```

> [!NOTE] 说明
>
> 1. 仅B兼容模式数据库可作为MySQL迁移目标库
> 2. 每次新建B库后，需切换至该库完成dolphin插件加载，否则可能无法正常连接

---

# 迁移执行流程

## 添加实例

在DataKit页面“资源中心”→“实例管理”中，添加源MySQL数据库和目标openGauss数据库。

![添加实例](.\figures\datakit_mysql_migration_jdbc_cluster.png)

## 创建迁移任务

在“数据迁移”→“迁移任务中心”中，点击“创建数据迁移任务”，按页面提示完成配置。

![迁移任务中心](.\figures\datakit_mysql_migration_task_center.png)

![创建迁移任务1](.\figures\datakit_mysql_migration_create_task_step1.png)

![创建迁移任务2](.\figures\datakit_mysql_migration_create_task_step2.png)

> [!TIP] 须知
>
> - **全量迁移**：迁移MySQL指定数据库中的表、视图、触发器、函数、存储过程，迁移完成自动停止。
> - **全量校验**：逐行比对有主键表的迁移结果数据，校验完成自动停止。
> - **增量迁移**：持续迁移MySQL侧的新增数据，需手动停止。
> - **反向迁移**：持续将openGauss侧新增数据迁移回MySQL，需手动停止。

## 启动与监控

1. **启动任务**

   在“迁移任务中心”选择任务，点击“启动”

   ![启动任务](.\figures\datakit_mysql_migration_start_task.png)

2. **查看详情**

   点击已启动任务，可下拉查看详情，再次点击“子任务ID”可查看表级进度

   ![任务详情1](.\figures\datakit_mysql_migration_task_detail1.png)

   ![子任务详情](.\figures\datakit_mysql_migration_task_detail2.png)

3. 前置校验失败

   若任务状态为“前置校验失败”，请参考目录**前置校验失败处理**，完成后点击“结束迁移”再点击“重置”即可重新启动任务。

   ![前置校验失败](.\figures\datakit_mysql_migration_task_check_failed.png)

## 迁移阶段操作

### 停止增量迁移

仅当任务包含增量迁移时涉及此操作。任务执行至增量阶段时，可在“迁移任务中心”或“子任务详情”页点击“停止增量”。

![停止增量](.\figures\datakit_mysql_migration_stop_incremental.png)

### 启动反向迁移

仅当任务包含反向迁移时涉及此操作。增量迁移停止成功后，可在“迁移任务中心”或“子任务详情”页点击“启动反向”。

![启动反向](.\figures\datakit_mysql_migration_start_reverse.png)

### 结束迁移任务

在“迁移任务中心”选择已启动任务，点击“结束迁移”。

![结束迁移](.\figures\datakit_mysql_migration_stop_task.png)

---

# 前置校验失败处理

## 服务可用性

迁移工具依赖Kafka作为消息中间件。Kafka已内置至Portal中，不可用时需连接Portal安装机器，使用Portal目录下的脚本`gs_rep_portal.sh`重启：

```bash
# 停止Kafka
sh gs_rep_portal.sh stop_kafka

# 启动Kafka
sh gs_rep_portal.sh start_kafka
```

## 数据库连接

确保Portal所在机器可正常连通MySQL和openGauss，无网络连通性问题。进而可以查看Portal日志定位问题，日志在Portal目录下：`logs/portal_子任务ID.log`。

## 数据库权限

### MySQL用户权限

不同迁移阶段所需的权限如下，也可直接授予ALL权限：

| 迁移阶段 | 所需权限                                        |
| -------- | ----------------------------------------------- |
| 全量迁移 | SELECT, RELOAD, LOCK TABLES, REPLICATION CLIENT |
| 增量迁移 | SELECT, REPLICATION CLIENT, REPLICATION SLAVE   |
| 反向迁移 | SELECT, UPDATE, INSERT, DELETE                  |

权限操作参考：

```sql
-- 查询用户权限
SELECT * FROM mysql.user WHERE USER = 'username';

-- 赋权语法参考如下，其中：
-- privileges：指用户的操作权限，如SELECT，INSERT，ALL等；
-- databasename：数据库名，可使用*表示所有库；
-- tablename：表名，可使用*表示所有表。
GRANT privileges ON databasename.tablename TO 'username';

-- 全量迁移权限
GRANT SELECT, RELOAD, LOCK TABLES, REPLICATION CLIENT ON *.* TO 'username';

-- 增量迁移权限
GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE ON *.* TO 'username';

-- 反向迁移权限
GRANT SELECT, UPDATE, INSERT, DELETE ON *.* TO 'username';

-- 赋予所有权限
GRANT ALL ON *.* TO 'username';

-- 刷新权限使生效
FLUSH PRIVILEGES;
```

### openGauss用户权限

openGauss连接用户的基础权限如下：

```sql
-- 赋予用户目标数据库操作权限
GRANT ALL ON DATABASE target_db TO username;

-- 反向迁移需赋予复制权限
ALTER ROLE username REPLICATION;
```

可以直接赋予用户sysadmin权限，一键解决权限问题，赋权语句参考如下：

```sql
GRANT ALL PRIVILEGES TO username;
```

## 日志参数

### MySQL参数

MySQL增量迁移需开启复制功能，在my.cnf中配置以下参数并重启MySQL：

```properties
log_bin=ON
binlog_format=ROW
binlog_row_image=FULL
```

查询当前参数值：
```sql
SHOW VARIABLES LIKE 'log_bin';
SHOW VARIABLES LIKE 'binlog_format';
SHOW VARIABLES LIKE 'binlog_row_image';
```

### openGauss参数

反向迁移时，openGauss需要配置如下参数，请使用openGauss安装用户在命令行执行如下操作：

```bash
gs_guc set -N all -I all -c "wal_level = logical"

# 6.0.5及之后版本需配置
gs_guc set -N all -I all -c "enable_subscription = on"

gs_om -t restart
```

查询当前参数值：
```sql
SHOW wal_level;
SHOW enable_subscription;
```

## 大小写敏感性

需确保MySQL与openGauss的表名大小写敏感性参数取值一致。

### MySQL配置

在my.cnf中设置：
```properties
lower_case_table_names=0
```

查询当前参数值：
```sql
SHOW VARIABLES LIKE 'lower_case_table_names';
```

### openGauss配置

openGauss控制表名大小写敏感性的参数为[dolphin.lower_case_table_names](https://docs.opengauss.org/zh/docs/latest/extension_reference/dolphin_guc_parameters.html#dolphinlower_case_table_names)。

```sql
ALTER USER username SET dolphin.lower_case_table_names TO 0;
```

查询时，需使用指定用户连接，才能查询到当前用户下的参数值：

```sql
show dolphin.lower_case_table_names;
```

## MySQL加密方式

要求MySQL连接用户的加密认证方式为`mysql_native_password`：

```sql
ALTER USER 'username'@'%' IDENTIFIED WITH mysql_native_password BY '******';
```

查询当前加密方式：
```sql
SELECT User, plugin FROM mysql.user;
```

## openGauss B兼容库

迁移目标库必须为B兼容模式数据库，创建B库的SQL语句参考如下：

```sql
CREATE DATABASE target_db WITH DBCOMPATIBILITY = 'B' ENCODING = 'UTF8';

-- 切换到B库加载dolphin插件
\c target_db
```

查询当前连接的数据库兼容模式：
```sql
SHOW sql_compatibility;
```

## openGauss逻辑复制槽位

反向迁移需在openGauss侧创建逻辑复制槽，若槽位被占满则无法启动反向。遇到槽位不足时，参考[逻辑复制函数](https://docs.opengauss.org/zh/docs/latest/sql_reference/logical_replication_functions.html)处理。

## openGauss复制连接权限

openGauss通过网络层和角色层交叉控制用户的逻辑复制能力，网络层控制网络准入，角色层控制用户权限。

### 网络层配置

使用openGauss安装用户执行如下命令，开启网络准入。

```bash
gs_guc set -N all -I all -h "host replication all 0.0.0.0/0 sha256"

# 重启数据库使配置生效
gs_om -t restart
```

### 角色层配置

openGauss连接用户需要拥有`rolreplication`权限或`sysadmin`权限。

```sql
ALTER ROLE username REPLICATION;
ALTER ROLE username SYSADMIN;
```

## MySQL Executed_Gtid_Set校验

迁移工具依赖MySQL的Executed_Gtid_Set感知已执行事务编号。若MySQL未开启GTID，需执行以下操作，将Executed_Gtid_Set的值变更到标准格式：`uuid:interval`，如：`cbb7e692-faac-11f0-a4fc-cc0577eeb3b0:1-1203`。

```sql
SET GLOBAL ENFORCE_GTID_CONSISTENCY = ON;
SET GLOBAL gtid_mode = OFF;
SET GLOBAL gtid_mode = OFF_PERMISSIVE;
SET GLOBAL gtid_mode = ON_PERMISSIVE;
SET GLOBAL gtid_mode = ON;
```

设置完成后，验证配置：
```sql
SHOW GLOBAL VARIABLES LIKE 'gtid_mode';
```

配置完成，再执行任意两条事务操作，如INSERT，然后验证Executed_Gtid_Set是否变为标准格式（`uuid:interval`）：
```sql
SHOW MASTER STATUS;
SHOW GLOBAL VARIABLES LIKE 'gtid_executed';
```

## openGauss发布订阅线程参数

openGauss 6.0.5及以上版本需开启[enable_subscription](https://docs.opengauss.org/zh/docs/latest/database_reference/sending_server.html#enable_subscription)参数后，才能正常使用逻辑复制功能，低版本无此限制。

请使用openGauss安装用户命令行执行如下操作完成开启。

```bash
gs_guc set -N all -I all -c "enable_subscription = on"

# 重启数据库使配置生效
gs_om -t restart
```

查询当前参数值：
```sql
SHOW enable_subscription;
```

## 数据库字符集

openGauss 7.0.0-RC3及以上版本，当MySQL字符集为UTF8时，openGauss需指定数据库采用UTF8编码。

openGauss在创建B库时，指定数据库字符集的方式参考如下：

```sql
CREATE DATABASE target_db WITH DBCOMPATIBILITY = 'B' ENCODING = 'UTF8';
```