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
  CONSTRAINT "tb_migration_task_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task"."source_node_id" IS '源端实例ID';

COMMENT ON COLUMN "public"."tb_migration_task"."source_db" IS '源端数据库';

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
COMMENT ON TABLE "public"."tb_migration_task" IS '迁移子任务表';



CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_exec_result_detail" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_exec_result_detail_id'::regclass),
  "task_id" int8,
  "exec_result_detail" varchar(1000) COLLATE "pg_catalog"."default",
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



CREATE TABLE IF NOT EXISTS "public"."tb_migration_task_operate_record" (
  "id" int8 NOT NULL DEFAULT nextval('sq_tb_task_operate_record_id'::regclass),
  "task_id" int8,
  "title" varchar(255) COLLATE "pg_catalog"."default",
  "oper_time" timestamp(6),
  "oper_user" varchar(255) COLLATE "pg_catalog"."default",
  CONSTRAINT "tb_task_process_record_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."id" IS '主键ID';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."task_id" IS '任务ID';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."title" IS '操作标题';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."oper_time" IS '操作时间';

COMMENT ON COLUMN "public"."tb_migration_task_operate_record"."oper_user" IS '操作人';

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

    RETURN 0;
END;'
    LANGUAGE plpgsql;

select init_migration_data_fuc();
DROP FUNCTION init_migration_data_fuc;
