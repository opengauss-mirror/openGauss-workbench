/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  intarkdb-schema.sql
 *
 *  IDENTIFICATION
 *  plugins/data-migration/src/main/resources/intarkdb-schema.sql
 *
 *  -------------------------------------------------------------------------
 */

CREATE TABLE IF NOT EXISTS "tb_migration_main_task" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_name" varchar(255) ,
    "exec_status" int8,
    "create_time" timestamp,
    "finish_time" timestamp,
    "exec_time" timestamp,
    "exec_progress" varchar(10) ,
    "create_user" varchar(255)
    );

COMMENT ON COLUMN "tb_migration_main_task"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_main_task"."task_name" IS '任务名称';

COMMENT ON COLUMN "tb_migration_main_task"."exec_status" IS '执行状态（0：未执行；1：执行中；2：已完成；）';

COMMENT ON COLUMN "tb_migration_main_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "tb_migration_main_task"."finish_time" IS '完成时间';

COMMENT ON COLUMN "tb_migration_main_task"."exec_time" IS '执行时间';

COMMENT ON COLUMN "tb_migration_main_task"."create_user" IS '创建人';

COMMENT ON COLUMN "tb_migration_main_task"."exec_progress" IS '迁移进度';

COMMENT ON TABLE "tb_migration_main_task" IS '平台任务表';


CREATE TABLE IF NOT EXISTS "tb_migration_task" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "source_node_id" varchar(64) ,
    "source_db" varchar(255) ,
    "source_tables" varchar(255) ,
    "target_node_id" varchar(64) ,
    "target_db" varchar(255) ,
    "migration_operations" varchar(255) ,
    "run_host_id" varchar(64) ,
    "source_db_host" varchar(50) ,
    "source_db_port" varchar(10) ,
    "source_db_user" varchar(50) ,
    "source_db_pass" varchar(512) ,
    "target_db_host" varchar(50) ,
    "target_db_port" varchar(10) ,
    "target_db_user" varchar(50) ,
    "target_db_pass" varchar(512) ,
    "create_time" timestamp,
    "exec_status" int8,
    "run_host" varchar(50) ,
    "run_port" varchar(50) ,
    "run_user" varchar(50) ,
    "run_pass" varchar(512) ,
    "exec_time" timestamp,
    "finish_time" timestamp,
    "main_task_id" int8,
    "migration_model_id" int8,
    "migration_process" varchar(10) ,
    "run_hostname" varchar(255) ,
    "target_db_version" varchar(20),
    "is_adjust_kernel_param" BOOLEAN default false,
    "status_desc" text
    );

COMMENT ON COLUMN "tb_migration_task"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task"."source_node_id" IS '源端实例ID';

COMMENT ON COLUMN "tb_migration_task"."source_tables" IS '源端表';

COMMENT ON COLUMN "tb_migration_task"."source_db" IS '源端数据库';

COMMENT ON COLUMN "tb_migration_task"."target_node_id" IS '目标端实例ID';

COMMENT ON COLUMN "tb_migration_task"."target_db" IS '目标端数据库';

COMMENT ON COLUMN "tb_migration_task"."migration_operations" IS '迁移动作，json数组存储';

COMMENT ON COLUMN "tb_migration_task"."run_host_id" IS '运行环境ID';

COMMENT ON COLUMN "tb_migration_task"."source_db_host" IS '源端数据库host';

COMMENT ON COLUMN "tb_migration_task"."source_db_port" IS '源端数据库port';

COMMENT ON COLUMN "tb_migration_task"."source_db_user" IS '源端数据库用户名';

COMMENT ON COLUMN "tb_migration_task"."source_db_pass" IS '源端数据库密码';

COMMENT ON COLUMN "tb_migration_task"."target_db_host" IS '目标端数据库host';

COMMENT ON COLUMN "tb_migration_task"."target_db_port" IS '目标数据库port';

COMMENT ON COLUMN "tb_migration_task"."target_db_user" IS '目标数据库用户名';

COMMENT ON COLUMN "tb_migration_task"."target_db_pass" IS '目标数据库密码';

COMMENT ON COLUMN "tb_migration_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "tb_migration_task"."exec_status" IS '执行状态（0：未执行；1：执行中；2：已完成；3：执行失败）';

COMMENT ON COLUMN "tb_migration_task"."run_host" IS '运行环境host';

COMMENT ON COLUMN "tb_migration_task"."run_port" IS '运行环境port';

COMMENT ON COLUMN "tb_migration_task"."run_user" IS '运行环境user';

COMMENT ON COLUMN "tb_migration_task"."run_pass" IS '运行环境pass';

COMMENT ON COLUMN "tb_migration_task"."exec_time" IS '执行时间';

COMMENT ON COLUMN "tb_migration_task"."finish_time" IS '完成时间';

COMMENT ON COLUMN "tb_migration_task"."main_task_id" IS '平台主任务ID';

COMMENT ON COLUMN "tb_migration_task"."migration_model_id" IS '操作模式ID';
COMMENT ON COLUMN "tb_migration_task"."migration_process" IS '迁移进度';
COMMENT ON COLUMN "tb_migration_task"."run_hostname" IS '运行环境hostname';
COMMENT ON COLUMN "tb_migration_task"."target_db_version" IS '目标数据库版本';
COMMENT ON COLUMN "tb_migration_task"."is_adjust_kernel_param" IS '是否调整内核参数';
COMMENT ON COLUMN "tb_migration_task"."status_desc" IS '状态说明';
COMMENT ON TABLE "tb_migration_task" IS '迁移子任务表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_exec_result_detail" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_id" int8,
    "exec_result_detail" clob ,
    "create_time" timestamp,
    "process_type" int8
    );

COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."task_id" IS '任务ID';

COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."exec_result_detail" IS '执行结果进度详情';
COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."process_type" IS '进度类型；1：全量；2：增量 3: 反向';

COMMENT ON TABLE "tb_migration_task_exec_result_detail" IS '任务执行结果进度详情';

CREATE TABLE IF NOT EXISTS "tb_migration_task_global_param" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "param_key" varchar(255) ,
    "param_value" varchar(255) ,
    "param_desc" varchar(512) ,
    "main_task_id" int8
    );

COMMENT ON COLUMN "tb_migration_task_global_param"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_global_param"."param_key" IS '参数key';

COMMENT ON COLUMN "tb_migration_task_global_param"."param_value" IS '参数值';

COMMENT ON COLUMN "tb_migration_task_global_param"."param_desc" IS '参数说明';

COMMENT ON COLUMN "tb_migration_task_global_param"."main_task_id" IS '主任务ID';

COMMENT ON TABLE "tb_migration_task_global_param" IS '任务全局参数配置表';


CREATE TABLE IF NOT EXISTS "tb_migration_task_global_tools_param" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "param_key" varchar(255) ,
    "param_value" varchar(255) ,
    "param_value_type" int8,
    "param_change_value" varchar(512) ,
    "config_id" int8,
    "portal_host_id" varchar(255) ,
    "param_desc" varchar(1024) ,
    "delete_flag" int8,
    "new_param_flag" int8
    );

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."param_key" IS '参数key';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."param_value" IS '参数值';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."param_value_type" IS '参数类型';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."param_change_value" IS '参数修改值';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."config_id" IS '工具ID';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."portal_host_id" IS 'portal主机id';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."param_desc" IS '参数描述';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."delete_flag" IS '是否删除标识 0 未删除 1删除';

COMMENT ON COLUMN "tb_migration_task_global_tools_param"."new_param_flag" IS '是否新增参数 0 不是新增参数 1是新增参数';

COMMENT ON TABLE "tb_migration_task_global_tools_param" IS '工具全局参数配置表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_host_ref" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "main_task_id" int8,
    "run_host_id" varchar(64) ,
    "create_time" timestamp
    );

COMMENT ON COLUMN "tb_migration_task_host_ref"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_host_ref"."main_task_id" IS '主任务ID';

COMMENT ON COLUMN "tb_migration_task_host_ref"."run_host_id" IS '运行机器ID';

COMMENT ON COLUMN "tb_migration_task_host_ref"."create_time" IS '创建时间';


CREATE TABLE IF NOT EXISTS "tb_migration_task_init_global_param" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "param_key" varchar(255) ,
    "param_value" varchar(255) ,
    "param_desc" varchar(512),
    "param_type" int8,
    "param_extends" varchar(800),
    "param_rules" TEXT
    );

COMMENT ON COLUMN "tb_migration_task_init_global_param"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_key" IS '参数key';
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_value" IS '参数默认值';
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_desc" IS '参数说明';
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_type" IS '参数类型；1：字符；2：数值；3：布尔；4: 选择；5: 正则表达式；6: 变量；9：对象数组';
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_extends" IS '参数扩展信息';
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_rules" IS '参数校验信息';
COMMENT ON TABLE "tb_migration_task_init_global_param" IS '初始全局参数配置表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_model" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_name" varchar(255) ,
    "migration_operations" varchar(255)
    );

COMMENT ON COLUMN "tb_migration_task_model"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_task_model"."model_name" IS '迁移模式名';
COMMENT ON COLUMN "tb_migration_task_model"."migration_operations" IS '迁移动作，json数组存储';
COMMENT ON TABLE "tb_migration_task_model" IS '迁移模式表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_param" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "main_task_id" int8,
    "task_id" int8,
    "param_key" varchar(255) ,
    "param_value" varchar(255) ,
    "param_desc" varchar(512) ,
    "param_type" int8
    );

COMMENT ON COLUMN "tb_migration_task_param"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_task_param"."main_task_id" IS '主任务ID';
COMMENT ON COLUMN "tb_migration_task_param"."task_id" IS '任务ID';
COMMENT ON COLUMN "tb_migration_task_param"."param_key" IS '参数key';
COMMENT ON COLUMN "tb_migration_task_param"."param_value" IS '参数value';
COMMENT ON COLUMN "tb_migration_task_param"."param_desc" IS '参数说明';
COMMENT ON COLUMN "tb_migration_task_param"."param_type" IS '参数类型；1：全局；2：个性化';
COMMENT ON TABLE "tb_migration_task_param" IS '任务参数配置表';

CREATE OR REPLACE FUNCTION add_migration_task_source_tables_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_task'' AND COLUMN_NAME = ''source_tables'' ) = 0
THEN
ALTER TABLE public.tb_migration_task ADD COLUMN source_tables VARCHAR;
COMMENT ON COLUMN "public"."tb_migration_task"."source_tables" IS ''源端表'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_migration_task_source_tables_field_func();

DROP FUNCTION add_migration_task_source_tables_field_func;

CREATE TABLE IF NOT EXISTS "tb_migration_third_party_software_config" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "zk_port" varchar(50) ,
    "kafka_port" varchar(50) ,
    "schema_registry_port" varchar(50) ,
    "zk_ip" varchar(255) ,
    "kafka_ip" varchar(255) ,
    "schema_registry_ip" varchar(255) ,
    "install_dir" varchar(255) ,
    "bind_portal_host" varchar(512) ,
    "host" varchar(512)
    );

COMMENT ON COLUMN "tb_migration_third_party_software_config"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."zk_port" IS 'zk端口';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."kafka_port" IS 'kafka端口';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."schema_registry_port" IS 'schema registry 端口';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."zk_ip" IS 'zk ip地址';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."kafka_ip" IS 'kafka ip地址';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."schema_registry_ip" IS 'schema registry ip';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."install_dir" IS 'kafka 安装目录';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."bind_portal_host" IS '绑定的portal实例id';
COMMENT ON COLUMN "tb_migration_third_party_software_config"."host" IS '安装的主机地址';
COMMENT ON TABLE "tb_migration_third_party_software_config" IS 'kafka实例配置表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_operate_record" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_id" int8,
    "title" varchar(255) ,
    "oper_time" timestamp,
    "oper_user" varchar(255) ,
    "oper_type" int8
    );

COMMENT ON COLUMN "tb_migration_task_operate_record"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_task_operate_record"."task_id" IS '任务ID';
COMMENT ON COLUMN "tb_migration_task_operate_record"."title" IS '操作标题';
COMMENT ON COLUMN "tb_migration_task_operate_record"."oper_time" IS '操作时间';
COMMENT ON COLUMN "tb_migration_task_operate_record"."oper_user" IS '操作人';
COMMENT ON COLUMN "tb_migration_task_operate_record"."oper_type" IS '操作类型；1：启动；2：停止增量；3：启动反向；100：结束迁移';
COMMENT ON TABLE "tb_migration_task_operate_record" IS '任务操作记录表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_status_record" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "title" varchar(255) ,
    "operate_id" int8,
    "task_id" int8,
    "status_id" int8,
    "create_time" timestamp
    );

COMMENT ON COLUMN "tb_migration_task_status_record"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_task_status_record"."task_id" IS '任务ID';
COMMENT ON COLUMN "tb_migration_task_status_record"."title" IS '状态名称';
COMMENT ON COLUMN "tb_migration_task_status_record"."operate_id" IS '操作ID';
COMMENT ON COLUMN "tb_migration_task_status_record"."status_id" IS '状态ID';
COMMENT ON COLUMN "tb_migration_task_status_record"."create_time" IS '记录时间';
COMMENT ON TABLE "tb_migration_task_status_record" IS '任务状态记录表';

INSERT INTO "tb_migration_task_model" ("id", "model_name", "migration_operations")
VALUES (1, '离线模式', 'start_plan1');
INSERT INTO "tb_migration_task_model" ("id", "model_name", "migration_operations")
VALUES (2, '在线模式', 'start_plan3');


INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (1, 'sink.query-dop', '8', 'sink端数据库并行查询会话配置');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (2, 'sink.minIdle', '10', '默认最小连接数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (3, 'sink.maxActive', '20', '默认激活数据库连接数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (4, 'sink.initialSize', '5', '初始化连接池大小');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (5, 'sink.debezium-time-period', '1', 'Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次。');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (7, 'source.query-dop', '8', 'source端数据库并行查询会话配置');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (8, 'source.minIdle', '10', '默认最小连接数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (9, 'source.maxActive', '20', '默认激活数据库连接数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (10, 'source.initialSize', '5', '默认初始连接池大小');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (12, 'source.debezium-num-period', '1000', 'Debezium增量校验数量的阈值，默认值为1000，应大于100');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (11, 'source.debezium-time-period', '1', 'Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (13, 'rules.enable', 'true', '规则过滤，true代表开启，false代表关闭');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (14, 'rules.table', '0','配置表过滤规则，可通过添加黑白名单，对当前数据库中待校验表进行过滤，黑白名单为互斥规则，配置有白名单时，会忽略配置的黑名单规则。可同时配置多组白名单或者黑名单。如果配置多组白名单或黑名单，那么会依次按照白名单去筛选表。值为table规则的数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (15, 'rules.table.name1', 'white', '配置规则名称，黑名单或者白名单，white|black');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (17, 'rules.table.name2', 'white', '如果有多个黑白名单，就会配置过滤名称2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (18, 'rules.table.text2', NULL, '如果有多个黑白名单，就会配置正则2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (19, 'rules.row', '0','配置行级过滤规则，规则继承table规则类；允许配置多组行过滤规则；行级规则等效于select * from table order by primaryKey asc limit offset,count; 如果多组规则配置的正则表达式过滤出的表产生交集，那么行过滤条件只生效最先匹配到的规则条件。值为row规则的数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (20, 'rules.row.name1', NULL, '配置规则表名过滤正则表达式，用于匹配表名称；name规则不可为空，不可重复');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (22, 'rules.row.name2', NULL, '如果有多个黑白名单，就会配置过滤名称2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (23, 'rules.row.text2', NULL, '如果有多个黑白名单，就会配置过滤规则2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (24, 'rules.column', '0', '列过滤规则，用于对表字段列进行过滤校验。可配置多组规则，name不可重复，重复会进行规则去重。值为column规则的数量。');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (25, 'rules.column.name1', NULL, '待过滤字段的表名称');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (26, 'rules.column.text1', NULL, '配置当前表待过滤的字段名称列表，如果某字段名称不属于当前表，则该字段不生效');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (27, 'rules.column.attribute1', 'exclude','当前表过滤字段模式，include包含text配置的字段，exclude排除text配置的字段；如果为include模式，text默认添加主键字段，不论text是否配置；如果为exclude模式，text默认不添加主键字段，不论是否配置');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (28, 'rules.column.name2', NULL, '如果有多个规则，就会配置过滤名称2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (29, 'rules.column.text2', NULL, '如果有多个规则，就会配置过滤规则2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (31, 'type.override', '1', '全量迁移类型转换数量，值为类型转换规则的数量');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (30, 'rules.column.attribute2', 'include', '如果有多个规则，就会配置过滤模式2');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (32, 'override.type', 'tinyint(1)', '全量迁移类型转换mysql数据类型');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (33, 'override.to', 'boolean', '全量迁移类型转换opengauss数据种类');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (34, 'override.tables', '"*"', '全量迁移类型转换适用的表');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (6, 'sink.debezium-num-period', '1000', 'Debezium增量校验数量的阈值，默认值为1000，应大于100');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (16, 'rules.table.text1', NULL, '配置规则内容，为正则表达式');
INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
VALUES (21, 'rules.row.text1', '0,0', '配置行过滤规则的具体条件，配置格式为[offset,count]，必须为数字，否则该规则无效');

CREATE TABLE IF NOT EXISTS "tb_main_task_env_error_host" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "run_host_id" varchar(64) ,
    "run_host" varchar(64) ,
    "run_port" varchar(64) ,
    "run_user" varchar(64) ,
    "run_pass" varchar(512) ,
    "main_task_id" int8
    );

COMMENT ON COLUMN "tb_main_task_env_error_host"."id" IS '主键ID';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_host_id" IS '机器ID';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_host" IS '机器ip';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_port" IS '机器端口';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_user" IS '机器用户';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_pass" IS '机器密码';
COMMENT ON COLUMN "tb_main_task_env_error_host"."main_task_id" IS '主任务ID';

CREATE TABLE IF NOT EXISTS "tb_migration_host_portal_install" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "run_host_id" varchar(64) ,
    "install_status" int8,
    "install_type" int8,
    "pkg_download_url" text,
    "pkg_name" text,
    "jar_name" text,
    "pkg_upload_path" text,
    "install_path" varchar(512),
    "host" varchar(64),
    "port" int8,
    "run_user" varchar(64),
    "run_password" text,
    "host_user_id" varchar(64)
    );

COMMENT ON COLUMN "tb_migration_host_portal_install"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_host_portal_install"."run_host_id" IS '机器ID';
COMMENT ON COLUMN "tb_migration_host_portal_install"."install_status" IS 'portal安装状态0 ： 未安装  1：安装中；2：已安装；10：安装失败';
COMMENT ON COLUMN "tb_migration_host_portal_install"."install_type" IS '安装类型，0：在线安装1：离线安装';
COMMENT ON COLUMN "tb_migration_host_portal_install"."pkg_download_url" IS '在线下载地址';
COMMENT ON COLUMN "tb_migration_host_portal_install"."pkg_name" IS '安装包名称';
COMMENT ON COLUMN "tb_migration_host_portal_install"."jar_name" IS 'portal的jar名称';
COMMENT ON COLUMN "tb_migration_host_portal_install"."pkg_upload_path" IS '离线包上传路径，JSON字符串，key为name-文件名字，realPath-上传文件夹路径';
COMMENT ON COLUMN "tb_migration_host_portal_install"."install_path" IS '安装路径';
COMMENT ON COLUMN "tb_migration_host_portal_install"."host" IS '机器IP';
COMMENT ON COLUMN "tb_migration_host_portal_install"."port" IS '机器端口';
COMMENT ON COLUMN "tb_migration_host_portal_install"."run_user" IS '安装用户';
COMMENT ON COLUMN "tb_migration_host_portal_install"."run_password" IS '用户密码';
COMMENT ON COLUMN "tb_migration_host_portal_install"."host_user_id" IS '用户ID';
COMMENT ON TABLE "tb_migration_host_portal_install" IS '机器安装portal记录';

delete from "tb_migration_host_portal_install" where host_user_id is null;

DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 15;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 16;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 17;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 18;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 20;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 21;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 22;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 23;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 25;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 26;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 27;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 28;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 29;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 30;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 32;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 33;
DELETE FROM "tb_migration_task_init_global_param" WHERE "id" = 34;

INSERT INTO "tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc", "param_type", "param_extends", "param_rules")
VALUES(0, 'opengauss.database.schema', '', '将数据迁移至openGauss端数据库的对应schema名 | 与mysql数据库名保持一致', 6, '', '[1,64]') ON DUPLICATE KEY UPDATE NOTHING;

UPDATE "tb_migration_task_init_global_param"
SET "param_key" = 'sink.query-dop', "param_value" = '8', "param_desc" = 'sink端数据库并行查询会话配置',
    "param_type" = 2, "param_extends" = NULL, "param_rules" = '[1,64]'
WHERE "id" = 1;

UPDATE "tb_migration_task_init_global_param"
SET "param_key" = 'sink.minIdle', "param_value" = '10', "param_desc" = '默认最小连接数量',
    "param_type" = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 2;

UPDATE "tb_migration_task_init_global_param"
SET "param_key" = 'sink.maxActive', "param_value" = '20', "param_desc" = '默认激活数据库连接数量',
    "param_type" = 2, "param_extends" = NULL, "param_rules" = '[10,300]'
WHERE "id" = 3;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'sink.initialSize', "param_value"   = '5', "param_desc"    = '初始化连接池大小',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 4;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'sink.debezium-time-period', "param_value"   = '1', "param_desc"    = 'Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次。',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[1,99999]'
WHERE "id" = 5;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'sink.debezium-num-period', "param_value"   = '1000', "param_desc"    = 'Debezium增量校验数量的阈值，默认值为1000，应大于100',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[100,10000]'
WHERE "id" = 6;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'source.query-dop', "param_value"   = '8', "param_desc"    = 'source端数据库并行查询会话配置',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[1,64]'
WHERE "id" = 7;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'source.minIdle', "param_value"   = '10', "param_desc"    = '默认最小连接数量',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 8;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'source.maxActive', "param_value"   = '20', "param_desc"    = '默认激活数据库连接数量',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[10,300]'
WHERE "id" = 9;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'source.initialSize', "param_value"   = '5', "param_desc"    = '默认初始连接池大小',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 10;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'source.debezium-time-period', "param_value"   = '1',
    "param_desc"    = 'Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[1,99999]'
WHERE "id" = 11;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'source.debezium-num-period', "param_value"   = '1000',
    "param_desc"    = 'Debezium增量校验数量的阈值，默认值为1000，应大于100',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[100,10000]'
WHERE "id" = 12;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'rules.enable', "param_value"   = 'true',
    "param_desc"    = '规则过滤，true代表开启，false代表关闭',
    "param_type"    = 3, "param_extends" = NULL
WHERE "id" = 13;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'rules.table', "param_value"   = '0',
    "param_desc"    = '配置表过滤规则，可通过添加黑白名单，对当前数据库中待校验表进行过滤，黑白名单为互斥规则，配置有白名单时，会忽略配置的黑名单规则。可同时配置多组白名单或者黑名单。如果配置多组白名单或黑名单，那么会依次按照白名单去筛选表。值为table规则的数量',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "rules.table.name", "paramType": 4,"paramValue":"white","desc": "配置规则名称，黑名单或者白名单，white|black", "paramRules": "[\"white\", \"black\"]"},{"subKeyPrefix":"rules.table.text","paramType": 5, "paramValue":"","desc": "配置规则内容，为正则表达式"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 14;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'rules.row', "param_value"   = '0',
    "param_desc"    = '配置行级过滤规则，规则继承table规则类；允许配置多组行过滤规则；行级规则等效于select * from table order by primaryKey asc limit offset,count; 如果多组规则配置的正则表达式过滤出的表产生交集，那么行过滤条件只生效最先匹配到的规则条件。值为row规则的数量',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "rules.row.name","paramType": 5,"paramValue":"","desc": "配置规则表名过滤正则表达式，用于匹配表名称；name规则不可为空，不可重复"},{"subKeyPrefix":"rules.row.text","paramType": 1,"paramValue":"0,0","desc": "配置行过滤规则的具体条件，配置格式为[offset,count]，必须为数字，否则该规则无效", "paramRules": "[[1, 5000000], [1, 10000]]"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 19;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'rules.column', "param_value"   = '0',
    "param_desc"    = '列过滤规则，用于对表字段列进行过滤校验。可配置多组规则，name不可重复，重复会进行规则去重。值为column规则的数量。',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "rules.column.name","paramType": 1,"paramValue":"","desc": "待过滤字段的表名称", "paramRules": "[1, 512]"},{"subKeyPrefix":"rules.column.text","paramType": 1,"paramValue":"","desc": "配置当前表待过滤的字段名称列表，如果某字段名称不属于当前表，则该字段不生效", "paramRules": "[1, 512]"},{"subKeyPrefix":"rules.column.attribute","paramType": 4,"paramValue":"exclude","desc": "当前表过滤字段模式，include包含text配置的字段，exclude排除text配置的字段；如果为include模式，text默认添加主键字段，不论text是否配置；如果为exclude模式，text默认不添加主键字段，不论是否配置", "paramRules": "[\"include\", \"exclude\"]"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 24;

UPDATE "tb_migration_task_init_global_param"
SET "param_key"     = 'type_override', "param_value"   = '0',
    "param_desc"    = '全量迁移类型转换数量，值为类型转换规则的数量',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "override_type","paramType": 1,"paramValue":"","desc": "全量迁移类型转换mysql数据类型", "paramRules": "[1, 64]"}, {"subKeyPrefix":"override_to","paramType": 1,"paramValue":"","desc": "全量迁移类型转换opengauss数据种类", "paramRules": "[1, 64]"},{"subKeyPrefix":"override_tables","paramType": 1,"paramValue":"''*''","desc": "全量迁移类型转换适用的表", "paramRules": "[1, 512]"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 31;
