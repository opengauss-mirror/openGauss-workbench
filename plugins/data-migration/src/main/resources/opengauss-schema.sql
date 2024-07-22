CREATE OR REPLACE FUNCTION create_sequences_not_exists() RETURNS integer AS 'BEGIN

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_main_task_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_main_task_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_migration_task_id'' )
THEN
CREATE SEQUENCE "public"."sq_migration_task_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_task_exec_result_detail_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_task_exec_result_detail_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_task_global_param_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_task_global_param_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_task_run_host_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_task_run_host_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_init_global_param_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_init_global_param_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_task_param_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_task_param_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_migration_third_party_software_config_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_migration_third_party_software_config_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_migration_task_global_tools_param_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_migration_task_global_tools_param_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_task_operate_record_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_task_operate_record_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS(SELECT 1 FROM information_schema.sequences WHERE sequence_schema = ''public'' AND sequence_name = ''sq_tb_task_status_record_id'')
THEN
CREATE SEQUENCE "public"."sq_tb_task_status_record_id"
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS(SELECT 1 FROM information_schema.sequences WHERE sequence_schema = ''public'' AND sequence_name = ''sq_tb_main_task_env_error_host_id'')
THEN
CREATE SEQUENCE "public"."sq_tb_main_task_env_error_host_id"
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
END IF;

IF NOT EXISTS(SELECT 1
              FROM information_schema.sequences
              WHERE sequence_schema = ''public''
                AND sequence_name = ''sq_tb_host_portal_install_id'')
THEN
    CREATE SEQUENCE "public"."sq_tb_host_portal_install_id"
        INCREMENT 1
        MINVALUE 1
        MAXVALUE 9223372036854775807
        START 1
        CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_tb_migration_tool_portal_download_info_id'' )
THEN
CREATE SEQUENCE "public"."sq_tb_migration_tool_portal_download_info_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT create_sequences_not_exists();
DROP FUNCTION create_sequences_not_exists;

CREATE TABLE IF NOT EXISTS "public"."tb_migration_main_task" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_main_task_id'::regclass),
  "task_name" varchar(255) COLLATE "pg_catalog"."default",
  "exec_status" int4,
  "create_time" timestamp(6),
  "finish_time" timestamp(6),
  "exec_time" timestamp(6),
  "exec_progress" varchar(10) COLLATE "pg_catalog"."default",
  "create_user" varchar(255) COLLATE "pg_catalog"."default",
  CONSTRAINT "sys_task_copy1_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_main_task"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_main_task"."task_name" IS '任务名称';

COMMENT ON COLUMN "public"."tb_migration_main_task"."exec_status" IS '执行状态（0：未执行；1：执行中；2：已完成；）';

COMMENT ON COLUMN "public"."tb_migration_main_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."tb_migration_main_task"."finish_time" IS '完成时间';

COMMENT ON COLUMN "public"."tb_migration_main_task"."exec_time" IS '执行时间';

COMMENT ON COLUMN "public"."tb_migration_main_task"."create_user" IS '创建人';

COMMENT ON COLUMN "public"."tb_migration_main_task"."exec_progress" IS '迁移进度';

COMMENT ON TABLE "public"."tb_migration_main_task" IS '平台任务表';


CREATE TABLE IF NOT EXISTS "public"."tb_migration_task" (
  "id" int8 NOT NULL DEFAULT nextval('sq_migration_task_id'::regclass),
  "source_node_id" varchar(64) COLLATE "pg_catalog"."default",
  "source_db" varchar(255) COLLATE "pg_catalog"."default",
  "source_tables" varchar(255) COLLATE "pg_catalog"."default",
  "target_node_id" varchar(64) COLLATE "pg_catalog"."default",
  "target_db" varchar(255) COLLATE "pg_catalog"."default",
  "migration_operations" varchar(255) COLLATE "pg_catalog"."default",
  "run_host_id" varchar(64) COLLATE "pg_catalog"."default",
  "source_db_host" varchar(50) COLLATE "pg_catalog"."default",
  "source_db_port" varchar(10) COLLATE "pg_catalog"."default",
  "source_db_user" varchar(50) COLLATE "pg_catalog"."default",
  "source_db_pass" varchar(512) COLLATE "pg_catalog"."default",
  "target_db_host" varchar(50) COLLATE "pg_catalog"."default",
  "target_db_port" varchar(10) COLLATE "pg_catalog"."default",
  "target_db_user" varchar(50) COLLATE "pg_catalog"."default",
  "target_db_pass" varchar(512) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "exec_status" int4,
  "run_host" varchar(50) COLLATE "pg_catalog"."default",
  "run_port" varchar(50) COLLATE "pg_catalog"."default",
  "run_user" varchar(50) COLLATE "pg_catalog"."default",
  "run_pass" varchar(512) COLLATE "pg_catalog"."default",
  "exec_time" timestamp(6),
  "finish_time" timestamp(6),
  "main_task_id" int8,
  "migration_model_id" int8,
  "migration_process" varchar(10) COLLATE "pg_catalog"."default",
  "run_hostname" varchar(255) COLLATE "pg_catalog"."default",
  "target_db_version" varchar(20) COLLATE "pg_catalog"."default",
  CONSTRAINT "tb_migration_task_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task"."source_node_id" IS '源端实例ID';

COMMENT ON COLUMN "public"."tb_migration_task"."source_db" IS '源端数据库';

COMMENT ON COLUMN "public"."tb_migration_task"."source_tables" IS '源端表';

COMMENT ON COLUMN "public"."tb_migration_task"."target_node_id" IS '目标端实例ID';

COMMENT ON COLUMN "public"."tb_migration_task"."target_db" IS '目标端数据库';

COMMENT ON COLUMN "public"."tb_migration_task"."migration_operations" IS '迁移动作，json数组存储';

COMMENT ON COLUMN "public"."tb_migration_task"."run_host_id" IS '运行环境ID';

COMMENT ON COLUMN "public"."tb_migration_task"."source_db_host" IS '源端数据库host';

COMMENT ON COLUMN "public"."tb_migration_task"."source_db_port" IS '源端数据库port';

COMMENT ON COLUMN "public"."tb_migration_task"."source_db_user" IS '源端数据库用户名';

COMMENT ON COLUMN "public"."tb_migration_task"."source_db_pass" IS '源端数据库密码';

COMMENT ON COLUMN "public"."tb_migration_task"."target_db_host" IS '目标端数据库host';

COMMENT ON COLUMN "public"."tb_migration_task"."target_db_port" IS '目标数据库port';

COMMENT ON COLUMN "public"."tb_migration_task"."target_db_user" IS '目标数据库用户名';

COMMENT ON COLUMN "public"."tb_migration_task"."target_db_pass" IS '目标数据库密码';

COMMENT ON COLUMN "public"."tb_migration_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."tb_migration_task"."exec_status" IS '执行状态（0：未执行；1：执行中；2：已完成；3：执行失败）';

COMMENT ON COLUMN "public"."tb_migration_task"."run_host" IS '运行环境host';

COMMENT ON COLUMN "public"."tb_migration_task"."run_port" IS '运行环境port';

COMMENT ON COLUMN "public"."tb_migration_task"."run_user" IS '运行环境user';

COMMENT ON COLUMN "public"."tb_migration_task"."run_pass" IS '运行环境pass';

COMMENT ON COLUMN "public"."tb_migration_task"."exec_time" IS '执行时间';

COMMENT ON COLUMN "public"."tb_migration_task"."finish_time" IS '完成时间';

COMMENT ON COLUMN "public"."tb_migration_task"."main_task_id" IS '平台主任务ID';

COMMENT ON COLUMN "public"."tb_migration_task"."migration_model_id" IS '操作模式ID';
COMMENT ON COLUMN "public"."tb_migration_task"."migration_process" IS '迁移进度';
COMMENT ON COLUMN "public"."tb_migration_task"."run_hostname" IS '运行环境hostname';
COMMENT ON COLUMN "public"."tb_migration_task"."target_db_version" IS '目标数据库版本';
COMMENT ON TABLE "public"."tb_migration_task" IS '迁移子任务表';

CREATE OR REPLACE FUNCTION add_migration_task_is_adjust_kernel_param_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_task'' AND COLUMN_NAME = ''is_adjust_kernel_param'' ) = 0
THEN
ALTER TABLE public.tb_migration_task ADD COLUMN is_adjust_kernel_param BOOLEAN default false;
COMMENT ON COLUMN "public"."tb_migration_task"."is_adjust_kernel_param" IS ''是否调整内核参数'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_migration_task_is_adjust_kernel_param_field_func();

DROP FUNCTION add_migration_task_is_adjust_kernel_param_field_func;

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

CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_exec_result_detail" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_exec_result_detail_id'::regclass),
  "task_id" int8,
  "exec_result_detail" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "process_type" int2,
  CONSTRAINT "tb_task_exec_result_detail_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_exec_result_detail"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_exec_result_detail"."task_id" IS '任务ID';

COMMENT ON COLUMN "public"."tb_migration_task_exec_result_detail"."exec_result_detail" IS '执行结果进度详情';
COMMENT ON COLUMN "public"."tb_migration_task_exec_result_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tb_migration_task_exec_result_detail"."process_type" IS '进度类型；1：全量；2：增量 3: 反向';

COMMENT ON TABLE "public"."tb_migration_task_exec_result_detail" IS '任务执行结果进度详情';

CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_global_param" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_global_param_id'::regclass),
  "param_key" varchar(255) COLLATE "pg_catalog"."default",
  "param_value" varchar(255) COLLATE "pg_catalog"."default",
  "param_desc" varchar(512) COLLATE "pg_catalog"."default",
  "main_task_id" int8,
  CONSTRAINT "tb_task_global_param_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_global_param"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_global_param"."param_key" IS '参数key';

COMMENT ON COLUMN "public"."tb_migration_task_global_param"."param_value" IS '参数值';

COMMENT ON COLUMN "public"."tb_migration_task_global_param"."param_desc" IS '参数说明';

COMMENT ON COLUMN "public"."tb_migration_task_global_param"."main_task_id" IS '主任务ID';

COMMENT ON TABLE "public"."tb_migration_task_global_param" IS '任务全局参数配置表';


CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_global_tools_param" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_migration_task_global_tools_param_id'::regclass),
  "param_key" varchar(255) COLLATE "pg_catalog"."default",
  "param_value" varchar(255) COLLATE "pg_catalog"."default",
  "param_value_type" int2,
  "param_change_value" varchar(512) COLLATE "pg_catalog"."default",
  "config_id" int2,
  "portal_host_id" varchar(255) COLLATE "pg_catalog"."default",
  "param_desc" varchar(1024) COLLATE "pg_catalog"."default",
  "delete_flag" int2,
  "new_param_flag" int2,
  CONSTRAINT "tb_task_global_tools_param_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."param_key" IS '参数key';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."param_value" IS '参数值';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."param_value_type" IS '参数类型';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."param_change_value" IS '参数修改值';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."config_id" IS '工具ID';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."portal_host_id" IS 'portal主机id';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."param_desc" IS '参数描述';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."delete_flag" IS '是否删除标识 0 未删除 1删除';

COMMENT ON COLUMN "public"."tb_migration_task_global_tools_param"."new_param_flag" IS '是否新增参数 0 不是新增参数 1是新增参数';

COMMENT ON TABLE "public"."tb_migration_task_global_tools_param" IS '工具全局参数配置表';

CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_host_ref" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_run_host_id'::regclass),
  "main_task_id" int8,
  "run_host_id" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  CONSTRAINT "tb_task_host_ref_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_host_ref"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_host_ref"."main_task_id" IS '主任务ID';

COMMENT ON COLUMN "public"."tb_migration_task_host_ref"."run_host_id" IS '运行机器ID';

COMMENT ON COLUMN "public"."tb_migration_task_host_ref"."create_time" IS '创建时间';


CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_init_global_param" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_init_global_param_id'::regclass),
  "param_key" varchar(255) COLLATE "pg_catalog"."default",
  "param_value" varchar(255) COLLATE "pg_catalog"."default",
  "param_desc" varchar(512) COLLATE "pg_catalog"."default",
  CONSTRAINT "tb_init_global_param_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."param_key" IS '参数key';

COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."param_value" IS '参数默认值';

COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."param_desc" IS '参数说明';

COMMENT ON TABLE "public"."tb_migration_task_init_global_param" IS '初始全局参数配置表';


CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_model" (
  "id" int8 NOT NULL,
  "model_name" varchar(255) COLLATE "pg_catalog"."default",
  "migration_operations" varchar(255) COLLATE "pg_catalog"."default",
  CONSTRAINT "tb_miration_model_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_model"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_model"."model_name" IS '迁移模式名';

COMMENT ON COLUMN "public"."tb_migration_task_model"."migration_operations" IS '迁移动作，json数组存储';

COMMENT ON TABLE "public"."tb_migration_task_model" IS '迁移模式表';

CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_param" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_param_id'::regclass),
  "main_task_id" int8,
  "task_id" int8,
  "param_key" varchar(255) COLLATE "pg_catalog"."default",
  "param_value" varchar(255) COLLATE "pg_catalog"."default",
  "param_desc" varchar(512) COLLATE "pg_catalog"."default",
  "param_type" int2,
  CONSTRAINT "tb_task_param_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_param"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_param"."main_task_id" IS '主任务ID';

COMMENT ON COLUMN "public"."tb_migration_task_param"."task_id" IS '任务ID';

COMMENT ON COLUMN "public"."tb_migration_task_param"."param_key" IS '参数key';

COMMENT ON COLUMN "public"."tb_migration_task_param"."param_value" IS '参数value';

COMMENT ON COLUMN "public"."tb_migration_task_param"."param_desc" IS '参数说明';

COMMENT ON COLUMN "public"."tb_migration_task_param"."param_type" IS '参数类型；1：全局；2：个性化';

COMMENT ON TABLE "public"."tb_migration_task_param" IS '任务参数配置表';


CREATE TABLE IF NOT EXISTS "public"."tb_migration_third_party_software_config" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_migration_third_party_software_config_id'::regclass),
  "zk_port" varchar(50) COLLATE "pg_catalog"."default",
  "kafka_port" varchar(50) COLLATE "pg_catalog"."default",
  "schema_registry_port" varchar(50) COLLATE "pg_catalog"."default",
  "zk_ip" varchar(255) COLLATE "pg_catalog"."default",
  "kafka_ip" varchar(255) COLLATE "pg_catalog"."default",
  "schema_registry_ip" varchar(255) COLLATE "pg_catalog"."default",
  "install_dir" varchar(255) COLLATE "pg_catalog"."default",
  "bind_portal_host" varchar(512) COLLATE "pg_catalog"."default",
  "host" varchar(512) COLLATE "pg_catalog"."default",
  CONSTRAINT "tb_migration_third_party_software_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."zk_port" IS 'zk端口';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."kafka_port" IS 'kafka端口';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."schema_registry_port" IS 'schema registry 端口';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."zk_ip" IS 'zk ip地址';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."kafka_ip" IS 'kafka ip地址';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."schema_registry_ip" IS 'schema registry ip';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."install_dir" IS 'kafka 安装目录';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."bind_portal_host" IS '绑定的portal实例id';

COMMENT ON COLUMN "public"."tb_migration_third_party_software_config"."host" IS '安装的主机地址';

COMMENT ON TABLE "public"."tb_migration_third_party_software_config" IS 'kafka实例配置表';



CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_operate_record" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_operate_record_id'::regclass),
  "task_id" int8,
  "title" varchar(255) COLLATE "pg_catalog"."default",
  "oper_time" timestamp(6),
  "oper_user" varchar(255) COLLATE "pg_catalog"."default",
  "oper_type" int2,
  CONSTRAINT "tb_task_process_record_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."task_id" IS '任务ID';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."title" IS '操作标题';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."oper_time" IS '操作时间';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."oper_user" IS '操作人';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."oper_type" IS '操作类型；1：启动；2：停止增量；3：启动反向；100：结束迁移';

COMMENT ON TABLE "public"."tb_migration_task_operate_record" IS '任务操作记录表';

CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_status_record" (
    "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_status_record_id'::regclass),
    "title" varchar(255) COLLATE "pg_catalog"."default",
    "operate_id" int8,
    "task_id" int8,
    "status_id" int4,
    "create_time" timestamptz(6),
    CONSTRAINT "tb_migration_task_status_record_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_status_record"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_status_record"."task_id" IS '任务ID';
COMMENT ON COLUMN "public"."tb_migration_task_status_record"."title" IS '状态名称';

COMMENT ON COLUMN "public"."tb_migration_task_status_record"."operate_id" IS '操作ID';

COMMENT ON COLUMN "public"."tb_migration_task_status_record"."status_id" IS '状态ID';

COMMENT ON COLUMN "public"."tb_migration_task_status_record"."create_time" IS '记录时间';

COMMENT ON TABLE "public"."tb_migration_task_status_record" IS '任务状态记录表';

CREATE OR REPLACE FUNCTION init_migration_data_fuc() RETURNS integer AS 'BEGIN

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=''public'' and table_name=''tb_migration_task_model'') AND
       NOT EXISTS (select 1 from "public"."tb_migration_task_model")
    THEN
        INSERT INTO "public"."tb_migration_task_model" ("id", "model_name", "migration_operations")
        VALUES (1, ''离线模式'', ''start_plan1'');
        INSERT INTO "public"."tb_migration_task_model" ("id", "model_name", "migration_operations")
        VALUES (2, ''在线模式'', ''start_plan3'');
    END IF;

    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = ''public'' and table_name = ''tb_migration_task_init_global_param'') AND
       NOT EXISTS(select 1 from "public"."tb_migration_task_init_global_param")
    THEN
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (1, ''sink.query-dop'', ''8'', ''sink端数据库并行查询会话配置'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (2, ''sink.minIdle'', ''10'', ''默认最小连接数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (3, ''sink.maxActive'', ''20'', ''默认激活数据库连接数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (4, ''sink.initialSize'', ''5'', ''初始化连接池大小'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (5, ''sink.debezium-time-period'', ''1'', ''Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次。'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (7, ''source.query-dop'', ''8'', ''source端数据库并行查询会话配置'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (8, ''source.minIdle'', ''10'', ''默认最小连接数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (9, ''source.maxActive'', ''20'', ''默认激活数据库连接数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (10, ''source.initialSize'', ''5'', ''默认初始连接池大小'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (12, ''source.debezium-num-period'', ''1000'', ''Debezium增量校验数量的阈值，默认值为1000，应大于100'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (11, ''source.debezium-time-period'', ''1'', ''Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (13, ''rules.enable'', ''true'', ''规则过滤，true代表开启，false代表关闭'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (14, ''rules.table'', ''0'',''配置表过滤规则，可通过添加黑白名单，对当前数据库中待校验表进行过滤，黑白名单为互斥规则，配置有白名单时，会忽略配置的黑名单规则。可同时配置多组白名单或者黑名单。如果配置多组白名单或黑名单，那么会依次按照白名单去筛选表。值为table规则的数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (15, ''rules.table.name1'', ''white'', ''配置规则名称，黑名单或者白名单，white|black'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (17, ''rules.table.name2'', ''white'', ''如果有多个黑白名单，就会配置过滤名称2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (18, ''rules.table.text2'', NULL, ''如果有多个黑白名单，就会配置正则2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (19, ''rules.row'', ''0'',''配置行级过滤规则，规则继承table规则类；允许配置多组行过滤规则；行级规则等效于select * from table order by primaryKey asc limit offset,count; 如果多组规则配置的正则表达式过滤出的表产生交集，那么行过滤条件只生效最先匹配到的规则条件。值为row规则的数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (20, ''rules.row.name1'', NULL, ''配置规则表名过滤正则表达式，用于匹配表名称；name规则不可为空，不可重复'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (22, ''rules.row.name2'', NULL, ''如果有多个黑白名单，就会配置过滤名称2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (23, ''rules.row.text2'', NULL, ''如果有多个黑白名单，就会配置过滤规则2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (24, ''rules.column'', ''0'', ''列过滤规则，用于对表字段列进行过滤校验。可配置多组规则，name不可重复，重复会进行规则去重。值为column规则的数量。'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (25, ''rules.column.name1'', NULL, ''待过滤字段的表名称'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (26, ''rules.column.text1'', NULL, ''配置当前表待过滤的字段名称列表，如果某字段名称不属于当前表，则该字段不生效'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (27, ''rules.column.attribute1'', ''exclude'',''当前表过滤字段模式，include包含text配置的字段，exclude排除text配置的字段；如果为include模式，text默认添加主键字段，不论text是否配置；如果为exclude模式，text默认不添加主键字段，不论是否配置'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (28, ''rules.column.name2'', NULL, ''如果有多个规则，就会配置过滤名称2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (29, ''rules.column.text2'', NULL, ''如果有多个规则，就会配置过滤规则2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (31, ''type.override'', ''1'', ''全量迁移类型转换数量，值为类型转换规则的数量'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (30, ''rules.column.attribute2'', ''include'', ''如果有多个规则，就会配置过滤模式2'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (32, ''override.type'', ''tinyint(1)'', ''全量迁移类型转换mysql数据类型'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (33, ''override.to'', ''boolean'', ''全量迁移类型转换opengauss数据种类'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (34, ''override.tables'', ''"*"'', ''全量迁移类型转换适用的表'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (6, ''sink.debezium-num-period'', ''1000'', ''Debezium增量校验数量的阈值，默认值为1000，应大于100'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (16, ''rules.table.text1'', NULL, ''配置规则内容，为正则表达式'');
        INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc")
        VALUES (21, ''rules.row.text1'', ''0,0'', ''配置行过滤规则的具体条件，配置格式为[offset,count]，必须为数字，否则该规则无效'');
    END IF;


    RETURN 0;
END;'
    LANGUAGE plpgsql;
select init_migration_data_fuc();
DROP FUNCTION init_migration_data_fuc;


CREATE OR REPLACE FUNCTION add_migration_task_status_desc_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_task'' AND COLUMN_NAME = ''status_desc'' ) = 0
THEN
ALTER TABLE tb_migration_task ADD COLUMN status_desc varchar(512) COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."tb_migration_task"."status_desc" IS ''状态说明'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_migration_task_status_desc_field_func();

DROP FUNCTION add_migration_task_status_desc_field_func;


CREATE TABLE IF NOT EXISTS "public"."tb_main_task_env_error_host" (
    "id" int8 NOT NULL DEFAULT nextval('sq_tb_main_task_env_error_host_id'::regclass),
    "run_host_id" varchar(64) COLLATE "pg_catalog"."default",
    "run_host" varchar(64) COLLATE "pg_catalog"."default",
    "run_port" varchar(64) COLLATE "pg_catalog"."default",
    "run_user" varchar(64) COLLATE "pg_catalog"."default",
    "run_pass" varchar(512) COLLATE "pg_catalog"."default",
    "main_task_id" int8,
    CONSTRAINT "tb_main_task_env_error_host_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."id" IS '主键ID';
COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."run_host_id" IS '机器ID';
COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."run_host" IS '机器ip';
COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."run_port" IS '机器端口';
COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."run_user" IS '机器用户';
COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."run_pass" IS '机器密码';
COMMENT ON COLUMN "public"."tb_main_task_env_error_host"."main_task_id" IS '主任务ID';

ALTER TABLE "public"."tb_migration_task" ALTER COLUMN "status_desc" type text;

CREATE TABLE IF NOT EXISTS "public"."tb_migration_host_portal_install" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_host_portal_install_id'::regclass),
  "run_host_id" varchar(64) COLLATE "pg_catalog"."default",
  "install_status" int2,
  CONSTRAINT "tb_migration_host_portal_install_pkey" PRIMARY KEY ("id")
);

CREATE OR REPLACE FUNCTION add_migration_host_portal_install_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_host_portal_install'' AND COLUMN_NAME = ''install_type'' ) = 0
THEN
ALTER TABLE tb_migration_host_portal_install ADD COLUMN install_type int2;
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."install_type" IS ''安装类型，0：在线安装1：离线安装'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_host_portal_install'' AND COLUMN_NAME = ''pkg_download_url'' ) = 0
THEN
ALTER TABLE tb_migration_host_portal_install ADD COLUMN pkg_download_url text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."pkg_download_url" IS ''在线下载地址'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_host_portal_install'' AND COLUMN_NAME = ''pkg_name'' ) = 0
THEN
ALTER TABLE tb_migration_host_portal_install ADD COLUMN pkg_name text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."pkg_name" IS ''安装包名称'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_host_portal_install'' AND COLUMN_NAME = ''jar_name'' ) = 0
THEN
ALTER TABLE tb_migration_host_portal_install ADD COLUMN jar_name text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."jar_name" IS ''portal的jar名称'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''tb_migration_host_portal_install'' AND COLUMN_NAME = ''pkg_upload_path'' ) = 0
THEN
ALTER TABLE tb_migration_host_portal_install ADD COLUMN pkg_upload_path text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."pkg_upload_path" IS ''离线包上传路径，JSON字符串，key为name-文件名字，realPath-上传文件夹路径'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_migration_host_portal_install_field_func();

DROP FUNCTION add_migration_host_portal_install_field_func;

COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."id" IS '主键ID';
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."run_host_id" IS '机器ID';
COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."install_status" IS 'portal安装状态0 ： 未安装  1：安装中；2：已安装；10：安装失败';
COMMENT ON TABLE "public"."tb_migration_host_portal_install" IS '机器安装portal记录';

CREATE OR REPLACE FUNCTION add_host_portal_install_field_func() RETURNS integer AS 'BEGIN
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_host_portal_install''
               AND COLUMN_NAME = ''install_path'') = 0
    THEN
        ALTER TABLE tb_migration_host_portal_install
            ADD COLUMN install_path varchar(512) COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."install_path" IS ''安装路径'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_host_portal_install''
               AND COLUMN_NAME = ''host'') = 0
    THEN
        ALTER TABLE tb_migration_host_portal_install
            ADD COLUMN host varchar(64) COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."host" IS ''机器IP'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_host_portal_install''
               AND COLUMN_NAME = ''port'') = 0
    THEN
        ALTER TABLE tb_migration_host_portal_install
            ADD COLUMN port int8;
        COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."port" IS ''机器端口'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_host_portal_install''
               AND COLUMN_NAME = ''run_user'') = 0
    THEN
        ALTER TABLE tb_migration_host_portal_install
            ADD COLUMN run_user varchar(64) COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."run_user" IS ''安装用户'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_host_portal_install''
               AND COLUMN_NAME = ''run_password'') = 0
    THEN
        ALTER TABLE tb_migration_host_portal_install
            ADD COLUMN run_password text COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."run_password" IS ''用户密码'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_host_portal_install''
               AND COLUMN_NAME = ''host_user_id'') = 0
    THEN
        ALTER TABLE tb_migration_host_portal_install
            ADD COLUMN host_user_id varchar(64) COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_host_portal_install"."host_user_id" IS ''用户ID'';
    END IF;
    RETURN 0;
END;'
    LANGUAGE plpgsql;

SELECT add_host_portal_install_field_func();

DROP FUNCTION add_host_portal_install_field_func;

delete from "public"."tb_migration_host_portal_install" where host_user_id is null;


CREATE OR REPLACE FUNCTION add_migration_task_init_global_param_field_func() RETURNS integer AS 'BEGIN
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_task_init_global_param''
               AND COLUMN_NAME = ''param_type'') = 0
    THEN
        ALTER TABLE tb_migration_task_init_global_param
            ADD COLUMN param_type int2;
        COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."param_type" IS ''参数类型；1：字符；2：数值；3：布尔；4: 选择；5: 正则表达式；6: 变量；9：对象数组'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_task_init_global_param''
               AND COLUMN_NAME = ''param_extends'') = 0
    THEN
        ALTER TABLE tb_migration_task_init_global_param
            ADD COLUMN param_extends varchar(800) COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."param_extends" IS ''参数扩展信息'';
    END IF;
    IF
            (SELECT COUNT(*) AS ct1
             FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = ''tb_migration_task_init_global_param''
               AND COLUMN_NAME = ''param_rules'') = 0
    THEN
        ALTER TABLE tb_migration_task_init_global_param
            ADD COLUMN param_rules TEXT COLLATE "pg_catalog"."default";
        COMMENT ON COLUMN "public"."tb_migration_task_init_global_param"."param_rules" IS ''参数校验信息'';
    END IF;
    RETURN 0;
END;'
    LANGUAGE plpgsql;

SELECT add_migration_task_init_global_param_field_func();

DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 15;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 16;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 17;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 18;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 20;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 21;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 22;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 23;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 25;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 26;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 27;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 28;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 29;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 30;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 32;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 33;
DELETE FROM "public"."tb_migration_task_init_global_param" WHERE "id" = 34;

INSERT INTO "public"."tb_migration_task_init_global_param" ("id", "param_key", "param_value", "param_desc", "param_type", "param_extends", "param_rules") 
VALUES(0, 'opengauss.database.schema', '', '将数据迁移至openGauss端数据库的对应schema名 | 与mysql数据库名保持一致', 6, '', '[1,64]') ON DUPLICATE KEY UPDATE NOTHING;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key" = 'sink.query-dop', "param_value" = '8', "param_desc" = 'sink端数据库并行查询会话配置',
    "param_type" = 2, "param_extends" = NULL, "param_rules" = '[1,64]'
WHERE "id" = 1;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key" = 'sink.minIdle', "param_value" = '10', "param_desc" = '默认最小连接数量',
    "param_type" = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 2;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key" = 'sink.maxActive', "param_value" = '20', "param_desc" = '默认激活数据库连接数量',
    "param_type" = 2, "param_extends" = NULL, "param_rules" = '[10,300]'
WHERE "id" = 3;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'sink.initialSize', "param_value"   = '5', "param_desc"    = '初始化连接池大小',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 4;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'sink.debezium-time-period', "param_value"   = '1', "param_desc"    = 'Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次。',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[1,99999]'
WHERE "id" = 5;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'sink.debezium-num-period', "param_value"   = '1000', "param_desc"    = 'Debezium增量校验数量的阈值，默认值为1000，应大于100',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[100,10000]'
WHERE "id" = 6;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'source.query-dop', "param_value"   = '8', "param_desc"    = 'source端数据库并行查询会话配置',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[1,64]'
WHERE "id" = 7;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'source.minIdle', "param_value"   = '10', "param_desc"    = '默认最小连接数量',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 8;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'source.maxActive', "param_value"   = '20', "param_desc"    = '默认激活数据库连接数量',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[10,300]'
WHERE "id" = 9;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'source.initialSize', "param_value"   = '5', "param_desc"    = '默认初始连接池大小',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[5,10]'
WHERE "id" = 10;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'source.debezium-time-period', "param_value"   = '1',
    "param_desc"    = 'Debezium增量校验时间段：24*60单位：分钟，即每隔1小时增量校验一次',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[1,99999]'
WHERE "id" = 11;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'source.debezium-num-period', "param_value"   = '1000',
    "param_desc"    = 'Debezium增量校验数量的阈值，默认值为1000，应大于100',
    "param_type"    = 2, "param_extends" = NULL, "param_rules" = '[100,10000]'
WHERE "id" = 12;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'rules.enable', "param_value"   = 'true',
    "param_desc"    = '规则过滤，true代表开启，false代表关闭',
    "param_type"    = 3, "param_extends" = NULL
WHERE "id" = 13;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'rules.table', "param_value"   = '0',
    "param_desc"    = '配置表过滤规则，可通过添加黑白名单，对当前数据库中待校验表进行过滤，黑白名单为互斥规则，配置有白名单时，会忽略配置的黑名单规则。可同时配置多组白名单或者黑名单。如果配置多组白名单或黑名单，那么会依次按照白名单去筛选表。值为table规则的数量',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "rules.table.name", "paramType": 4,"paramValue":"white","desc": "配置规则名称，黑名单或者白名单，white|black", "paramRules": "[\"white\", \"black\"]"},{"subKeyPrefix":"rules.table.text","paramType": 5, "paramValue":"","desc": "配置规则内容，为正则表达式"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 14;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'rules.row', "param_value"   = '0',
    "param_desc"    = '配置行级过滤规则，规则继承table规则类；允许配置多组行过滤规则；行级规则等效于select * from table order by primaryKey asc limit offset,count; 如果多组规则配置的正则表达式过滤出的表产生交集，那么行过滤条件只生效最先匹配到的规则条件。值为row规则的数量',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "rules.row.name","paramType": 5,"paramValue":"","desc": "配置规则表名过滤正则表达式，用于匹配表名称；name规则不可为空，不可重复"},{"subKeyPrefix":"rules.row.text","paramType": 1,"paramValue":"0,0","desc": "配置行过滤规则的具体条件，配置格式为[offset,count]，必须为数字，否则该规则无效", "paramRules": "[[1, 5000000], [1, 10000]]"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 19;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'rules.column', "param_value"   = '0',
    "param_desc"    = '列过滤规则，用于对表字段列进行过滤校验。可配置多组规则，name不可重复，重复会进行规则去重。值为column规则的数量。',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "rules.column.name","paramType": 1,"paramValue":"","desc": "待过滤字段的表名称", "paramRules": "[1, 512]"},{"subKeyPrefix":"rules.column.text","paramType": 1,"paramValue":"","desc": "配置当前表待过滤的字段名称列表，如果某字段名称不属于当前表，则该字段不生效", "paramRules": "[1, 512]"},{"subKeyPrefix":"rules.column.attribute","paramType": 4,"paramValue":"exclude","desc": "当前表过滤字段模式，include包含text配置的字段，exclude排除text配置的字段；如果为include模式，text默认添加主键字段，不论text是否配置；如果为exclude模式，text默认不添加主键字段，不论是否配置", "paramRules": "[\"include\", \"exclude\"]"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 24;

UPDATE "public"."tb_migration_task_init_global_param"
SET "param_key"     = 'type_override', "param_value"   = '0',
    "param_desc"    = '全量迁移类型转换数量，值为类型转换规则的数量',
    "param_type"    = 9,
    "param_extends" = '[{"subKeyPrefix": "override_type","paramType": 1,"paramValue":"","desc": "全量迁移类型转换mysql数据类型", "paramRules": "[1, 64]"}, {"subKeyPrefix":"override_to","paramType": 1,"paramValue":"","desc": "全量迁移类型转换opengauss数据种类", "paramRules": "[1, 64]"},{"subKeyPrefix":"override_tables","paramType": 1,"paramValue":"''*''","desc": "全量迁移类型转换适用的表", "paramRules": "[1, 512]"}]',
    "param_rules" = '[0,9]'
WHERE "id" = 31;

-- ----------------------------
-- Table structure for tb_migration_tool_portal_download_info
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."tb_migration_tool_portal_download_info" (
    "id" int8 NOT NULL DEFAULT nextval('sq_tb_migration_tool_portal_download_info_id'::regclass),
    "host_os" varchar(255) NOT NULL,
    "host_os_version" varchar(255) NOT NULL,
    "host_cpu_arch" varchar(255) NOT NULL,
    "portal_pkg_download_url" text NOT NULL,
    "portal_pkg_name" text NOT NULL,
    "portal_jar_name" text NOT NULL,
    CONSTRAINT "tb_migration_tool_portal_download_info_pkey" PRIMARY KEY ("id")
    );

COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."id" IS 'portal下载信息主键';
COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."host_os" IS '主机系统类型';
COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."host_os_version" IS '主机系统版本';
COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."host_cpu_arch" IS '处理器架构';
COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."portal_pkg_download_url" IS 'portal安装包下载地址';
COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."portal_pkg_name" IS 'portal安装包名';
COMMENT ON COLUMN "public"."tb_migration_tool_portal_download_info"."portal_jar_name" IS 'portal jar包名';

-- ----------------------------
-- Records of tb_migration_tool_portal_download_info
-- ----------------------------

DELETE FROM "public"."tb_migration_tool_portal_download_info";

CREATE OR REPLACE FUNCTION init_tb_migration_tool_portal_download_info_data_fuc(pkg_version varchar(255)[], latest_version varchar) RETURNS void AS '
DECLARE
host_os_info varchar(255)[][] := ARRAY[[''centos'', ''7''],
                                     [''openEuler'', ''20.03''],
                                     [''openEuler'', ''20.03''],
                                     [''openEuler'', ''22.03''],
                                     [''openEuler'', ''22.03'']];
	host_cpu_arch varchar(255)[] := ARRAY[''x86_64'', ''x86_64'', ''aarch64'', ''x86_64'', ''aarch64''];
	row_i integer;
	i integer;
	num integer := 1;
BEGIN
    FOR i IN 1..array_length(pkg_version, 1) LOOP
        FOR row_i IN 1..array_length(host_os_info, 1) LOOP
            IF i = 1 THEN
                INSERT INTO "public"."tb_migration_tool_portal_download_info" ("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name")
                    VALUES (num, host_os_info[row_i][1], host_os_info[row_i][2], host_cpu_arch[row_i], concat(''https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/'', host_os_info[row_i][1], host_os_info[row_i][2], ''/''), concat(''latest_PortalControl-'', latest_version, ''-'', host_cpu_arch[row_i], ''.tar.gz''), concat(''portalControl-'', latest_version, ''-exec.jar''))
                    ON DUPLICATE KEY UPDATE NOTHING;
                num := num + 1;
            END IF;

            IF pkg_version[i] = ''6.0.0rc1'' THEN
                INSERT INTO "public"."tb_migration_tool_portal_download_info" ("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name")
                    VALUES (num, host_os_info[row_i][1], host_os_info[row_i][2], host_cpu_arch[row_i], concat(''https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/tools/'', host_os_info[row_i][1], host_os_info[row_i][2], ''/''), concat(''PortalControl-'', pkg_version[i], ''-'', host_cpu_arch[row_i], ''.tar.gz''), concat(''portalControl-'', pkg_version[i], ''-exec.jar''))
                    ON DUPLICATE KEY UPDATE NOTHING;
            ELSIF pkg_version[i] = ''5.0.0'' THEN
                INSERT INTO "public"."tb_migration_tool_portal_download_info" ("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name")
                    VALUES (num, host_os_info[row_i][1], host_os_info[row_i][2], host_cpu_arch[row_i], ''https://opengauss.obs.cn-south-1.myhuaweicloud.com/tools/portal/'', ''PortalControl-5.0.0.tar.gz'', ''portalControl-1.0-SNAPSHOT-exec.jar'')
                    ON DUPLICATE KEY UPDATE NOTHING;
            ELSE
                INSERT INTO "public"."tb_migration_tool_portal_download_info" ("id", "host_os", "host_os_version", "host_cpu_arch", "portal_pkg_download_url", "portal_pkg_name", "portal_jar_name")
                    VALUES (num, host_os_info[row_i][1], host_os_info[row_i][2], host_cpu_arch[row_i], concat(''https://opengauss.obs.cn-south-1.myhuaweicloud.com/'', pkg_version[i], ''/tools/'', host_os_info[row_i][1], host_os_info[row_i][2], ''/''), concat(''PortalControl-'', pkg_version[i], ''-'', host_cpu_arch[row_i], ''.tar.gz''), ''portalControl-1.0-SNAPSHOT-exec.jar'')
                    ON DUPLICATE KEY UPDATE NOTHING;
            END IF;
            num := num + 1;
        END LOOP;
    END LOOP;
END;
'
LANGUAGE plpgsql;

SELECT init_tb_migration_tool_portal_download_info_data_fuc(ARRAY['6.0.0rc1', '5.1.0', '5.0.0'], '6.0.0');

DROP FUNCTION init_tb_migration_tool_portal_download_info_data_fuc;