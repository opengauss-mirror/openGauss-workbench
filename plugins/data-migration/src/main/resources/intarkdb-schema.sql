CREATE TABLE IF NOT EXISTS "tb_migration_main_task" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_name" varchar(255),
    "exec_status" int4,
    "create_time" timestamp,
    "finish_time" timestamp,
    "exec_time" timestamp,
    "exec_progress" varchar(10),
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
    "source_node_id" varchar(64),
    "source_db" varchar(255),
    "source_tables" text,
    "target_node_id" varchar(64),
    "target_db" varchar(255),
    "migration_operations" varchar(255),
    "run_host_id" varchar(64),
    "source_db_host" varchar(50),
    "source_db_port" varchar(10),
    "source_db_user" varchar(50),
    "source_db_pass" text,
    "target_db_host" varchar(50),
    "target_db_port" varchar(10),
    "target_db_user" varchar(50),
    "target_db_pass" text,
    "create_time" timestamp,
    "exec_status" int4,
    "run_host" varchar(50),
    "run_port" varchar(50),
    "run_user" varchar(50),
    "run_pass" text,
    "exec_time" timestamp,
    "finish_time" timestamp,
    "main_task_id" int8,
    "migration_model_id" int8,
    "migration_process" varchar(10),
    "run_hostname" varchar(255),
    "target_db_version" varchar(20)
    );

COMMENT ON COLUMN "tb_migration_task"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task"."source_node_id" IS '源端实例ID';

COMMENT ON COLUMN "tb_migration_task"."source_db" IS '源端数据库';

COMMENT ON COLUMN "tb_migration_task"."source_tables" IS '源端表';

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
COMMENT ON TABLE "tb_migration_task" IS '迁移子任务表';

ALTER TABLE tb_migration_task ADD COLUMN is_adjust_kernel_param BOOLEAN;
COMMENT ON COLUMN "tb_migration_task"."is_adjust_kernel_param" IS '是否调整内核参数';

UPDATE sys_menu SET menu_en_name = 'Create Transcribe Replay Task' WHERE menu_name = '创建录制回放';

UPDATE sys_menu SET menu_en_name = 'Transcribe Replay Task Detail' WHERE menu_name = '录制回放详情';

UPDATE sys_menu SET menu_en_name = 'Create Data Migration Task' WHERE menu_name = '创建迁移任务';

ALTER TABLE tb_migration_task ADD COLUMN source_tables text;
COMMENT ON COLUMN "tb_migration_task"."source_tables" IS '源端表';

CREATE TABLE IF NOT EXISTS "tb_migration_task_exec_result_detail" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_id" int8,
    "exec_result_detail" text,
    "create_time" timestamp,
    "process_type" int2
    );

COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."task_id" IS '任务ID';

COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."exec_result_detail" IS '执行结果进度详情';
COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "tb_migration_task_exec_result_detail"."process_type" IS '进度类型；1：全量；2：增量 3: 反向';

COMMENT ON TABLE "tb_migration_task_exec_result_detail" IS '任务执行结果进度详情';

CREATE TABLE IF NOT EXISTS "tb_migration_task_global_param" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "param_key" varchar(255),
    "param_value" varchar(255),
    "param_desc" varchar(512),
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
    "param_key" varchar(255),
    "param_value" varchar(255),
    "param_value_type" int2,
    "param_change_value" varchar(512),
    "config_id" int2,
    "portal_host_id" varchar(255),
    "param_desc" varchar(1024),
    "delete_flag" int2,
    "new_param_flag" int2
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
    "run_host_id" varchar(64),
    "create_time" timestamp
    );

COMMENT ON COLUMN "tb_migration_task_host_ref"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_host_ref"."main_task_id" IS '主任务ID';

COMMENT ON COLUMN "tb_migration_task_host_ref"."run_host_id" IS '运行机器ID';

COMMENT ON COLUMN "tb_migration_task_host_ref"."create_time" IS '创建时间';


CREATE TABLE IF NOT EXISTS "tb_migration_task_init_global_param" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "param_key" varchar(255),
    "param_value" varchar(255),
    "param_desc" varchar(512)
    );

COMMENT ON COLUMN "tb_migration_task_init_global_param"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_key" IS '参数key';

COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_value" IS '参数值';

COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_desc" IS '参数说明';

COMMENT ON TABLE "tb_migration_task_init_global_param" IS '初始全局参数配置表';

ALTER TABLE tb_migration_task_init_global_param ADD COLUMN default_param_value varchar(255);
COMMENT ON COLUMN "tb_migration_task_init_global_param"."default_param_value" IS '默认参数值';

CREATE TABLE IF NOT EXISTS "tb_migration_task_model" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_name" varchar(255),
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
    "param_key" varchar(255),
    "param_value" varchar(255),
    "param_desc" varchar(512),
    "param_type" int2
    );

COMMENT ON COLUMN "tb_migration_task_param"."id" IS '主键ID';

COMMENT ON COLUMN "tb_migration_task_param"."main_task_id" IS '主任务ID';

COMMENT ON COLUMN "tb_migration_task_param"."task_id" IS '任务ID';

COMMENT ON COLUMN "tb_migration_task_param"."param_key" IS '参数key';

COMMENT ON COLUMN "tb_migration_task_param"."param_value" IS '参数value';

COMMENT ON COLUMN "tb_migration_task_param"."param_desc" IS '参数说明';

COMMENT ON COLUMN "tb_migration_task_param"."param_type" IS '参数类型；1：全局；2：个性化';

COMMENT ON TABLE "tb_migration_task_param" IS '任务参数配置表';


CREATE TABLE IF NOT EXISTS "tb_migration_third_party_software_config" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "zk_port" varchar(50),
    "kafka_port" varchar(50),
    "schema_registry_port" varchar(50),
    "zk_ip" varchar(255),
    "kafka_ip" varchar(255),
    "schema_registry_ip" varchar(255),
    "install_dir" varchar(255),
    "bind_portal_host" varchar(512),
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
    "title" varchar(255),
    "oper_time" timestamp,
    "oper_user" varchar(255),
    "oper_type" int2
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
    "title" varchar(255),
    "operate_id" int8,
    "task_id" int8,
    "status_id" int4,
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
VALUES (5, 'sink.debezium-time-period', '1', 'Debezium增量校验时间段：24*60单位：分钟，即每隔1分钟增量校验一次');
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
VALUES (11, 'source.debezium-time-period', '1', 'Debezium增量校验时间段：24*60单位：分钟，即每隔1分钟增量校验一次');
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

ALTER TABLE tb_migration_task ADD COLUMN status_desc varchar(512);
COMMENT ON COLUMN "tb_migration_task"."status_desc" IS ''状态说明'';

CREATE TABLE IF NOT EXISTS "tb_main_task_env_error_host" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "run_host_id" varchar(64),
    "run_host" varchar(64),
    "run_port" varchar(64),
    "run_user" varchar(64),
    "run_pass" text,
    "main_task_id" int8
    );

COMMENT ON COLUMN "tb_main_task_env_error_host"."id" IS '主键ID';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_host_id" IS '机器ID';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_host" IS '机器ip';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_port" IS '机器端口';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_user" IS '机器用户';
COMMENT ON COLUMN "tb_main_task_env_error_host"."run_pass" IS '机器密码';
COMMENT ON COLUMN "tb_main_task_env_error_host"."main_task_id" IS '主任务ID';

ALTER TABLE "tb_migration_task" ALTER COLUMN "status_desc" type text;

CREATE TABLE IF NOT EXISTS "tb_migration_host_portal_install" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "run_host_id" varchar(64),
    "install_status" int2
    );


ALTER TABLE tb_migration_host_portal_install ADD COLUMN install_type int2;
COMMENT ON COLUMN "tb_migration_host_portal_install"."install_type" IS '安装类型，0：在线安装1：离线安装';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN pkg_download_url text;
COMMENT ON COLUMN "tb_migration_host_portal_install"."pkg_download_url" IS '在线下载地址';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN pkg_name text;
COMMENT ON COLUMN "tb_migration_host_portal_install"."pkg_name" IS '安装包名称';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN jar_name text;
COMMENT ON COLUMN "tb_migration_host_portal_install"."jar_name" IS 'portal的jar名称';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN pkg_upload_path text;
COMMENT ON COLUMN "tb_migration_host_portal_install"."pkg_upload_path" IS '离线包上传路径，JSON字符串，key为name-文件名字，realPath-上传文件夹路径';

COMMENT ON COLUMN "tb_migration_host_portal_install"."id" IS '主键ID';
COMMENT ON COLUMN "tb_migration_host_portal_install"."run_host_id" IS '机器ID';
COMMENT ON COLUMN "tb_migration_host_portal_install"."install_status" IS 'portal安装状态0 ： 未安装  1：安装中；2：已安装；10：安装失败';
COMMENT ON TABLE "tb_migration_host_portal_install" IS '机器安装portal记录';


ALTER TABLE tb_migration_host_portal_install ADD COLUMN install_path varchar(512);
COMMENT ON COLUMN "tb_migration_host_portal_install"."install_path" IS '安装路径';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN host varchar(64);
COMMENT ON COLUMN "tb_migration_host_portal_install"."host" IS '机器IP';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN port int8;
COMMENT ON COLUMN "tb_migration_host_portal_install"."port" IS '机器端口';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN run_user varchar(64);
COMMENT ON COLUMN "tb_migration_host_portal_install"."run_user" IS '安装用户';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN run_password text;
COMMENT ON COLUMN "tb_migration_host_portal_install"."run_password" IS '用户密码';

ALTER TABLE tb_migration_host_portal_install ADD COLUMN host_user_id varchar(64);
COMMENT ON COLUMN "tb_migration_host_portal_install"."host_user_id" IS '用户ID';


delete from "tb_migration_host_portal_install" where host_user_id is null;



ALTER TABLE tb_migration_task_init_global_param ADD COLUMN param_type int2;
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_type" IS '参数类型；1：字符；2：数值；3：布尔；4: 选择；5: 正则表达式；6: 变量；9：对象数组';

ALTER TABLE tb_migration_task_init_global_param ADD COLUMN param_extends varchar(800);
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_extends" IS '参数扩展信息';

ALTER TABLE tb_migration_task_init_global_param ADD COLUMN param_rules TEXT;
COMMENT ON COLUMN "tb_migration_task_init_global_param"."param_rules" IS '参数校验信息';


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
SET "param_key"     = 'sink.debezium-time-period', "param_value"   = '1', "param_desc"    = 'Debezium增量校验时间段：24*60单位：分钟，即每隔1分钟增量校验一次',
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
    "param_desc"    = 'Debezium增量校验时间段：24*60单位：分钟，即每隔1分钟增量校验一次',
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

-- ----------------------------
-- Table structure for tb_migration_tool_portal_download_info
-- ----------------------------

CREATE TABLE IF NOT EXISTS "tb_migration_tool_portal_download_info" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "host_os" varchar(255) NOT NULL,
    "host_os_version" varchar(255) NOT NULL,
    "host_cpu_arch" varchar(255) NOT NULL,
    "portal_pkg_download_url" text NOT NULL,
    "portal_pkg_name" text NOT NULL,
    "portal_jar_name" text NOT NULL
    );

COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."id" IS 'portal下载信息主键';
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."host_os" IS '主机系统类型';
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."host_os_version" IS '主机系统版本';
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."host_cpu_arch" IS '处理器架构';
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."portal_pkg_download_url" IS 'portal安装包下载地址';
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."portal_pkg_name" IS 'portal安装包名';
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."portal_jar_name" IS 'portal jar包名';

-- ----------------------------
-- Records of tb_migration_tool_portal_download_info
-- ----------------------------

DELETE FROM "tb_migration_tool_portal_download_info";

-- ----------------------------
-- Table structure for tb_migration_task_alert
-- ----------------------------

CREATE TABLE IF NOT EXISTS "tb_migration_task_alert" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_id" int8 NOT NULL,
    "migration_phase" int8 NOT NULL,
    "date_time" varchar(24) NOT NULL,
    "thread" varchar(255),
    "class_name" varchar(255),
    "method_name" varchar(255),
    "line_number" int8,
    "cause_cn" text NOT NULL,
    "cause_en" text NOT NULL,
    "log_level" varchar(10),
    "log_code" varchar(4) NOT NULL,
    "log_source" int8 NOT NULL
    );

COMMENT ON COLUMN "tb_migration_task_alert"."id" IS '告警信息主键ID';
COMMENT ON COLUMN "tb_migration_task_alert"."task_id" IS '迁移任务主键ID';
COMMENT ON COLUMN "tb_migration_task_alert"."migration_phase" IS '迁移阶段；1：全量迁移；2：全量校验；3：增量迁移；4：反向迁移';
COMMENT ON COLUMN "tb_migration_task_alert"."date_time" IS '告警时间，格式：yyyy-MM-dd HH:mm:ss.SSS';
COMMENT ON COLUMN "tb_migration_task_alert"."thread" IS '告警所在进程';
COMMENT ON COLUMN "tb_migration_task_alert"."class_name" IS '告警所在类名';
COMMENT ON COLUMN "tb_migration_task_alert"."method_name" IS '告警所在方法名';
COMMENT ON COLUMN "tb_migration_task_alert"."line_number" IS '告警所在代码行数';
COMMENT ON COLUMN "tb_migration_task_alert"."cause_cn" IS '告警中文原因';
COMMENT ON COLUMN "tb_migration_task_alert"."cause_en" IS '告警英文原因';
COMMENT ON COLUMN "tb_migration_task_alert"."log_level" IS '告警对应的日志级别';
COMMENT ON COLUMN "tb_migration_task_alert"."log_code" IS '告警对应的日志状态码';
COMMENT ON COLUMN "tb_migration_task_alert"."log_source" IS '告警对应的日志来源，0：portal_id.log；10：full_migration.log；20：check.log；21：source.log；22：sink.log；31：connect_source.log；32：connect_sink.log；41：reverse_connect_source.log；42：reverse_connect_sink.log';

-- ----------------------------
-- Table structure for tb_migration_task_alert_detail
-- ----------------------------

CREATE TABLE IF NOT EXISTS "tb_migration_task_alert_detail" (
    "alert_id" int8 NOT NULL PRIMARY KEY,
    "detail" text NOT NULL
    );

COMMENT ON COLUMN "tb_migration_task_alert_detail"."alert_id" IS '告警信息主键ID';
COMMENT ON COLUMN "tb_migration_task_alert_detail"."detail" IS '告警对应的日志文本';


-- ----------------------------
-- Table structure for tb_migration_task_check_progress_detail
-- ----------------------------

CREATE TABLE IF NOT EXISTS "tb_migration_task_check_progress_detail" (
    "id" varchar(255) NOT NULL PRIMARY KEY,
    "task_id" int8 NOT NULL,
    "schema_name" varchar(255),
    "source_name" varchar(255),
    "sink_name" varchar(255),
    "status" varchar(16),
    "failed_rows" int8,
    "repair_file_name" varchar(255),
    "message" text,
    "create_time" timestamp DEFAULT now()
    );
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."id" IS 'ID';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."task_id" IS '迁移任务ID';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."schema_name" IS '迁移任务源端Schema名称';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."source_name" IS '迁移任务源端表名称';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."sink_name" IS '迁移任务目标端表名称';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."status" IS '迁移任务表校验结果';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."failed_rows" IS '迁移任务表校验失败行数';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."repair_file_name" IS '迁移任务表校验失败修复脚本名称';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."message" IS '迁移任务表校验信息';
COMMENT ON COLUMN "tb_migration_task_check_progress_detail"."create_time" IS '迁移任务表记录创建时间';



-- ----------------------------
-- Table structure for tb_migration_task_check_progress_summary
-- ----------------------------

CREATE TABLE IF NOT EXISTS "tb_migration_task_check_progress_summary" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_id" int8 NOT NULL,
    "source_db" varchar(255),
    "sink_db" varchar(255),
    "total" int8,
    "avg_speed" int4,
    "completed" int4,
    "table_count" int4,
    "start_time" timestamp,
    "end_time" timestamp,
    "status" varchar(16)
    );

COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."id" IS '迁移全量校验进度表详情主键ID';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."task_id" IS '迁移任务ID';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."source_db" IS '迁移任务源端表名称';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."sink_db" IS '迁移任务目标端表名称';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."total" IS '迁移任务校验总记录数';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."avg_speed" IS '迁移任务平均校验速率';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."completed" IS '迁移任务完成表数量';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."table_count" IS '迁移任务校验表数量';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."start_time" IS '迁移任务开始时间';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."end_time" IS '迁移任务截止时间';
COMMENT ON COLUMN "tb_migration_task_check_progress_summary"."status" IS '迁移任务是否完成';


CREATE TABLE IF NOT EXISTS "tb_transcribe_replay_task"
(
    id                int8 PRIMARY KEY AUTOINCREMENT,
    task_name          VARCHAR(255) NOT NULL,
    task_type          VARCHAR(255) NOT NULL,
    db_name            text NOT NULL,
    source_db_type        VARCHAR(50)  NOT NULL,
    source_install_path VARCHAR(500),
    target_install_path VARCHAR(500),
    tool_version       VARCHAR(50)  NOT NULL,
    execution_status   INTEGER      NOT NULL,
    slow_sql_count      INTEGER      NOT NULL DEFAULT 0,
    failed_sql_count    INTEGER      NOT NULL DEFAULT 0,
    task_duration      BIGINT       NOT NULL DEFAULT 0,
    source_duration      BIGINT       NOT NULL DEFAULT 0,
    target_duration      BIGINT       NOT NULL DEFAULT 0,
    total_num          BIGINT       NOT NULL,
    parse_num          BIGINT       NOT NULL DEFAULT 0,
    replay_num         BIGINT       NOT NULL DEFAULT 0,
    source_node_id         VARCHAR(50),
    target_node_id         VARCHAR(50),
    error_msg         text,
    task_start_time     TIMESTAMP    ,
    task_end_time       TIMESTAMP
    );

COMMENT ON TABLE "tb_transcribe_replay_task" IS '录制回放任务表';
COMMENT ON COLUMN "tb_transcribe_replay_task"."id" IS '录制回放表详情主键ID';
COMMENT ON COLUMN "tb_transcribe_replay_task"."task_name" IS '任务名称';
COMMENT ON COLUMN "tb_transcribe_replay_task"."task_type" IS '任务类型';
COMMENT ON COLUMN "tb_transcribe_replay_task"."db_name" IS '数据库名称';
COMMENT ON COLUMN "tb_transcribe_replay_task"."source_db_type" IS '源数据库类型';
COMMENT ON COLUMN "tb_transcribe_replay_task"."source_install_path" IS '源数据库安装路径';
COMMENT ON COLUMN "tb_transcribe_replay_task"."target_install_path" IS '目标数据库安装路径';
COMMENT ON COLUMN "tb_transcribe_replay_task"."tool_version" IS '工具版本';
COMMENT ON COLUMN "tb_transcribe_replay_task"."execution_status" IS '执行状态';
COMMENT ON COLUMN "tb_transcribe_replay_task"."slow_sql_count" IS '慢SQL数量';
COMMENT ON COLUMN "tb_transcribe_replay_task"."failed_sql_count" IS '失败SQL数量';
COMMENT ON COLUMN "tb_transcribe_replay_task"."task_duration" IS '任务总时长（毫秒）';
COMMENT ON COLUMN "tb_transcribe_replay_task"."source_duration" IS '源数据库操作时长（毫秒）';
COMMENT ON COLUMN "tb_transcribe_replay_task"."target_duration" IS '目标数据库操作时长（毫秒）';
COMMENT ON COLUMN "tb_transcribe_replay_task"."total_num" IS '总任务数量';
COMMENT ON COLUMN "tb_transcribe_replay_task"."parse_num" IS '解析数量';
COMMENT ON COLUMN "tb_transcribe_replay_task"."replay_num" IS '回放数量';
COMMENT ON COLUMN "tb_transcribe_replay_task"."source_node_id" IS '源节点ID';
COMMENT ON COLUMN "tb_transcribe_replay_task"."target_node_id" IS '目标节点ID';
COMMENT ON COLUMN "tb_transcribe_replay_task"."error_msg" IS '错误信息';
COMMENT ON COLUMN "tb_transcribe_replay_task"."task_start_time" IS '任务开始时间';
COMMENT ON COLUMN "tb_transcribe_replay_task"."task_end_time" IS '任务结束时间';

CREATE TABLE IF NOT EXISTS "tb_transcribe_replay_host"
(
    id     INTEGER PRIMARY KEY,
    task_id INTEGER      NOT NULL,
    ip   VARCHAR(255) NOT NULL,
    port   INTEGER      NOT NULL,
    user_name   VARCHAR(255) NOT NULL,
    passwd TEXT NOT NULL,
    db_type VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE "tb_transcribe_replay_host" IS '录制回放主机信息表';
COMMENT ON COLUMN "tb_transcribe_replay_host"."id" IS '录制回放主机信息ID';
COMMENT ON COLUMN "tb_transcribe_replay_host"."task_id" IS '关联的任务ID';
COMMENT ON COLUMN "tb_transcribe_replay_host"."ip" IS '主机IP地址';
COMMENT ON COLUMN "tb_transcribe_replay_host"."port" IS '主机端口号';
COMMENT ON COLUMN "tb_transcribe_replay_host"."user_name" IS '主机用户名';
COMMENT ON COLUMN "tb_transcribe_replay_host"."passwd" IS '主机密码';
COMMENT ON COLUMN "tb_transcribe_replay_host"."db_type" IS '数据库类型';

CREATE TABLE IF NOT EXISTS "tb_transcribe_replay_slow_sql"
(
    unique_code BIGINT PRIMARY KEY,
    task_id INT NOT NULL,
    sql_str TEXT,
    source_duration BIGINT,
    target_duration BIGINT,
    explain_str TEXT,
    count_str BIGINT
);

COMMENT ON TABLE "tb_transcribe_replay_slow_sql" IS '录制回放慢SQL记录表';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."unique_code" IS '唯一标识码，主键';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."task_id" IS '关联的任务ID';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."sql_str" IS '慢SQL语句';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."source_duration" IS '源端执行该SQL的时长（微妙）';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."target_duration" IS '目的端执行该SQL的时长（微妙）';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."explain_str" IS 'SQL执行计划';
COMMENT ON COLUMN "tb_transcribe_replay_slow_sql"."count_str" IS '该SQL的执行次数';

CREATE TABLE IF NOT EXISTS  "tb_transcribe_replay_fail_sql"
(
    id       int8 PRIMARY KEY AUTOINCREMENT,
    task_id  INT  NOT NULL,
    sql TEXT NOT NULL,
    message  TEXT
);

COMMENT ON TABLE tb_transcribe_replay_fail_sql IS '记录失败 SQL 的表';
COMMENT ON COLUMN tb_transcribe_replay_fail_sql.id IS '主键';
COMMENT ON COLUMN tb_transcribe_replay_fail_sql.task_id IS '任务 ID';
COMMENT ON COLUMN tb_transcribe_replay_fail_sql.sql_text IS '失败的 SQL 语句';
COMMENT ON COLUMN tb_transcribe_replay_fail_sql.message IS '错误消息或日志';

CREATE TABLE IF NOT EXISTS  "tb_transcribe_replay_param"
(
    id int8 PRIMARY KEY AUTOINCREMENT,
    fail_sql_id INT NOT NULL,
    type VARCHAR(255) NOT NULL,
    value TEXT
    );

COMMENT ON TABLE "tb_transcribe_replay_param" IS '录制回放参数表';
COMMENT ON COLUMN "tb_transcribe_replay_param"."id" IS '参数表主键ID';
COMMENT ON COLUMN "tb_transcribe_replay_param"."fail_sql_id" IS '关联的失败SQL记录ID';
COMMENT ON COLUMN "tb_transcribe_replay_param"."type" IS '参数类型';
COMMENT ON COLUMN "tb_transcribe_replay_param"."value" IS '参数值';

-- ----------------------------
-- Update tb_migration_task_init_global_param
-- ----------------------------

UPDATE "tb_migration_task_init_global_param"
SET "param_value" = '20',
    "param_rules" = '[5,100]'
WHERE "id" = 2;

UPDATE "tb_migration_task_init_global_param"
SET "param_value" = '100'
WHERE "id" = 3;

UPDATE "tb_migration_task_init_global_param"
SET "param_value" = '20',
    "param_rules" = '[5,100]'
WHERE "id" = 4;

UPDATE "tb_migration_task_init_global_param"
SET "param_value" = '20',
    "param_rules" = '[5,100]'
WHERE "id" = 8;

UPDATE "tb_migration_task_init_global_param"
SET "param_value" = '100'
WHERE "id" = 9;

UPDATE "tb_migration_task_init_global_param"
SET "param_value" = '20',
    "param_rules" = '[5,100]'
WHERE "id" = 10;

-------------------------------------------
-- ALTER TABLE tb_migration_task_init_global_param
-------------------------------------------

ALTER TABLE tb_migration_task_init_global_param ADD COLUMN db_type varchar(255);
COMMENT ON COLUMN "tb_migration_task_init_global_param"."db_type" IS '参数所属数据库类型';

UPDATE "tb_migration_task_init_global_param"
SET "db_type" = 'MYSQL'
WHERE "db_type" IS NULL;

INSERT INTO "tb_migration_task_init_global_param"
("id", "param_key", "param_value", "param_desc", "param_type", "db_type")
VALUES(32, 'is.migration.object', 'true', '是否迁移对象（view, trigger, function, procedure）', 3, 'POSTGRESQL');

INSERT INTO "tb_migration_task_init_global_param"
("id", "param_key", "param_value", "param_desc", "param_type", "db_type")
VALUES(33, 'schema.mappings', '', '源端到目标端的schema映射，不配置时，默认迁移至目标端的schema与源端schema同名，配置格式为：public:public,schema1:schema1,schema2:schema2，注意分隔符均为英文的冒号和逗号', 6, 'POSTGRESQL');

-------------------------------------------
-- ALTER TABLE tb_migration_host_portal_install
-------------------------------------------

ALTER TABLE tb_migration_host_portal_install ADD COLUMN portal_type varchar(255);
COMMENT ON COLUMN "tb_migration_host_portal_install"."portal_type" IS 'portal类型：MYSQL_ONLY，MULTI_DB';

UPDATE "tb_migration_host_portal_install"
SET "portal_type" = 'MYSQL_ONLY'
WHERE "portal_type" IS NULL;

DELETE FROM tb_migration_host_portal_install WHERE "install_status" = 0;

-------------------------------------------
-- ALTER TABLE tb_migration_tool_portal_download_info
-------------------------------------------

ALTER TABLE tb_migration_tool_portal_download_info ADD COLUMN portal_type varchar(255);
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."portal_type" IS 'portal类型：MYSQL_ONLY，MULTI_DB';

ALTER TABLE tb_migration_tool_portal_download_info ADD COLUMN portal_version varchar(255);
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."portal_version" IS 'portal版本：STABLE，EXPERIMENTAL';

INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(1, 'centos', '7', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/centos7/', 'PortalControl-7.0.0rc1-x86_64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(2, 'openEuler', '20.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/openEuler20.03/', 'PortalControl-7.0.0rc1-x86_64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(3, 'openEuler', '20.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/openEuler20.03/', 'PortalControl-7.0.0rc1-aarch64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(4, 'openEuler', '22.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/openEuler22.03/', 'PortalControl-7.0.0rc1-x86_64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(5, 'openEuler', '22.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/openEuler22.03/', 'PortalControl-7.0.0rc1-aarch64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(6, 'openEuler', '24.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/openEuler24.03/', 'PortalControl-7.0.0rc1-x86_64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(7, 'openEuler', '24.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/openEuler24.03/', 'PortalControl-7.0.0rc1-aarch64.tar.gz', 'portalControl-7.0.0rc1-exec.jar', 'MYSQL_ONLY', 'STABLE');

INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(8, 'centos', '7', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/centos7/', 'PortalControl-7.0.0rc2-x86_64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(9, 'openEuler', '20.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler20.03/', 'PortalControl-7.0.0rc2-x86_64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(10, 'openEuler', '20.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler20.03/', 'PortalControl-7.0.0rc2-aarch64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(11, 'openEuler', '22.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler22.03/', 'PortalControl-7.0.0rc2-x86_64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(12, 'openEuler', '22.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler22.03/', 'PortalControl-7.0.0rc2-aarch64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(13, 'openEuler', '24.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler24.03/', 'PortalControl-7.0.0rc2-x86_64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(14, 'openEuler', '24.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler24.03/', 'PortalControl-7.0.0rc2-aarch64.tar.gz', 'portalControl-7.0.0rc2-exec.jar', 'MYSQL_ONLY', 'EXPERIMENTAL');

INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(15, 'centos', '7', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/centos7/', 'openGauss-portal-7.0.0rc2-CentOS7-x86_64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(16, 'openEuler', '20.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler20.03/', 'openGauss-portal-7.0.0rc2-openEuler20.03-x86_64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(17, 'openEuler', '20.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler20.03/', 'openGauss-portal-7.0.0rc2-openEuler20.03-aarch64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(18, 'openEuler', '22.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler22.03/', 'openGauss-portal-7.0.0rc2-openEuler22.03-x86_64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(19, 'openEuler', '22.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler22.03/', 'openGauss-portal-7.0.0rc2-openEuler22.03-aarch64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(20, 'openEuler', '24.03', 'x86_64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler24.03/', 'openGauss-portal-7.0.0rc2-openEuler24.03-x86_64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');
INSERT INTO "tb_migration_tool_portal_download_info"
("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name", "portal_type", "portal_version")
VALUES(21, 'openEuler', '24.03', 'aarch64', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/openEuler24.03/', 'openGauss-portal-7.0.0rc2-openEuler24.03-aarch64.tar.gz', 'openGauss-portal-7.0.0rc2.jar', 'MULTI_DB', 'EXPERIMENTAL');

-------------------------------------------
-- ALTER TABLE tb_migration_task
-------------------------------------------

ALTER TABLE tb_migration_task ADD COLUMN source_schemas text;
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."source_schemas" IS '源端schema列表，多个schema以英文逗号分隔';

ALTER TABLE tb_migration_task ADD COLUMN source_db_type varchar(255);
COMMENT ON COLUMN "tb_migration_tool_portal_download_info"."source_db_type" IS '源端数据库类型，如：MYSQL, POSTGRESQL';

UPDATE "tb_migration_task"
SET "source_db_type" = 'MYSQL'
WHERE "source_db_type" IS NULL;

---------------------------------
-- CREATE TABLE tb_migration_full_migration_progress
---------------------------------

CREATE TABLE IF NOT EXISTS tb_migration_full_migration_progress
(
    id      BIGINT PRIMARY KEY AUTOINCREMENT,
    task_id INT              NOT NULL,
    object_type VARCHAR(255) NOT NULL,
    name    VARCHAR(255)     NOT NULL,
    schema  VARCHAR(255)     NOT NULL,
    status  INT              NOT NULL,
    percent DOUBLE PRECISION NOT NULL,
    error   TEXT
    );

COMMENT
ON COLUMN "tb_migration_full_migration_progress"."id" IS '主键ID';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."task_id" IS '任务ID';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."object_type" IS '对象类型，如：table, view, trigger, function, procedure';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."name" IS '对象名';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."schema" IS 'schema名';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."status" IS '迁移状态，1未开始，2迁移中，3迁移成功，6迁移失败';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."percent" IS '迁移进度%';
COMMENT
ON COLUMN "tb_migration_full_migration_progress"."error" IS '异常信息';

---------------------------------
-- CREATE TABLE tb_migration_full_migration_summary_data
---------------------------------

CREATE TABLE IF NOT EXISTS tb_migration_full_migration_summary_data
(
    id             INT PRIMARY KEY AUTOINCREMENT,
    task_id        INT              NOT NULL,
    data           DOUBLE PRECISION NOT NULL,
    record         BIGINT           NOT NULL,
    speed          DOUBLE PRECISION NOT NULL,
    time           INT              NOT NULL
);

COMMENT
ON COLUMN "tb_migration_full_migration_summary_data"."id" IS '主键ID';
COMMENT
ON COLUMN "tb_migration_full_migration_summary_data"."task_id" IS '任务ID';
COMMENT
ON COLUMN "tb_migration_full_migration_summary_data"."data" IS '已迁移数据总量，MB';
COMMENT
ON COLUMN "tb_migration_full_migration_summary_data"."record" IS '已迁移记录总条数';
COMMENT
ON COLUMN "tb_migration_full_migration_summary_data"."speed" IS '当前迁移速率，MB/s';
COMMENT
ON COLUMN "tb_migration_full_migration_summary_data"."time" IS '迁移总耗时，s';

---------------------------------
-- CREATE TABLE tb_migration_incremental_migration_progress
---------------------------------

CREATE TABLE IF NOT EXISTS tb_migration_incremental_migration_progress
(
    id             INT PRIMARY KEY AUTOINCREMENT,
    task_id        INT    NOT NULL,
    total_count    BIGINT NOT NULL,
    failed_count   BIGINT NOT NULL,
    replayed_count BIGINT NOT NULL,
    success_count  BIGINT NOT NULL,
    skipped_count  BIGINT NOT NULL,
    rest           BIGINT NOT NULL,
    sink_speed     INT    NOT NULL,
    source_speed   INT    NOT NULL
);
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."id" IS '主键ID';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."task_id" IS '任务ID';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."total_count" IS '总迁移条数';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."failed_count" IS '迁移失败条数';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."replayed_count" IS '已回放条数';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."success_count" IS '迁移成功条数';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."skipped_count" IS '跳过条数';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."rest" IS '剩余待写入条数';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."sink_speed" IS '写入速度，条/s';
COMMENT
ON COLUMN "tb_migration_incremental_migration_progress"."source_speed" IS '抽取速度，条/s';

---------------------------------
-- CREATE TABLE tb_migration_reverse_migration_progress
---------------------------------

CREATE TABLE IF NOT EXISTS tb_migration_reverse_migration_progress
(
    id             INT PRIMARY KEY AUTOINCREMENT,
    task_id        INT    NOT NULL,
    total_count    BIGINT NOT NULL,
    failed_count   BIGINT NOT NULL,
    replayed_count BIGINT NOT NULL,
    success_count  BIGINT NOT NULL,
    skipped_count  BIGINT NOT NULL,
    rest           BIGINT NOT NULL,
    sink_speed     INT    NOT NULL,
    source_speed   INT    NOT NULL
);

COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."id" IS '主键ID';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."task_id" IS '任务ID';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."total_count" IS '总迁移条数';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."failed_count" IS '迁移失败条数';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."replayed_count" IS '已回放条数';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."success_count" IS '迁移成功条数';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."skipped_count" IS '跳过条数';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."rest" IS '剩余待写入条数';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."sink_speed" IS '写入速度，条/s';
COMMENT
ON COLUMN "tb_migration_reverse_migration_progress"."source_speed" IS '抽取速度，条/s';

ALTER TABLE "tb_transcribe_replay_host" ALTER COLUMN "passwd" TYPE text;
ALTER TABLE "tb_main_task_env_error_host" ALTER COLUMN "run_pass" TYPE text;
ALTER TABLE "tb_migration_task" ALTER COLUMN "source_db_pass" TYPE text;
ALTER TABLE "tb_migration_task" ALTER COLUMN "target_db_pass" TYPE text;
ALTER TABLE "tb_migration_task" ALTER COLUMN "run_pass" TYPE text;