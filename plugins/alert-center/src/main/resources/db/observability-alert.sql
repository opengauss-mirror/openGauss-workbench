CREATE OR REPLACE FUNCTION create_sequences_not_exists() RETURNS integer AS 'BEGIN

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_rule_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_rule_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_rule_item_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_rule_item_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_rule_item_param_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_rule_item_param_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_notify_way_id'' )
THEN
CREATE SEQUENCE "public"."sq_notify_way_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_template_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_template_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_template_rule_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_template_rule_id"
INCREMENT 1
MINVALUE  2
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_template_rule_item_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_template_rule_item_id"
INCREMENT 1
MINVALUE  2
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_template_rule_item_param_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_template_rule_item_param_id"
INCREMENT 1
MINVALUE  2
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_host_template_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_host_template_id"
INCREMENT 1
MINVALUE  2
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_record_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_record_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_notify_template_id'' )
THEN
CREATE SEQUENCE "public"."sq_notify_template_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_notify_message_id'' )
THEN
CREATE SEQUENCE "public"."sq_notify_message_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_cluster_node_conf_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_cluster_node_conf_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_template_notify_conf_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_template_notify_conf_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_config_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_config_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT create_sequences_not_exists();
DROP FUNCTION create_sequences_not_exists;

CREATE TABLE IF NOT EXISTS "public"."alert_rule" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_rule_id'::regclass),
    "rule_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "level" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_type" varchar(20) COLLATE "pg_catalog"."default",
    "rule_exp_comb" varchar(100) COLLATE "pg_catalog"."default",
    "rule_content" text COLLATE "pg_catalog"."default",
    "notify_duration" int,
    "notify_duration_unit" char(1) COLLATE "pg_catalog"."default",
    "is_repeat" int1,
    "is_silence" int1,
    "silence_start_time" timestamp(6),
    "silence_end_time" timestamp(6),
    "alert_notify" varchar(20) COLLATE "pg_catalog"."default",
    "notify_way_ids" text COLLATE "pg_catalog"."default",
    "alert_desc" text COLLATE "pg_catalog"."default",
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_rule"."id" IS '规则ID';
COMMENT ON COLUMN "public"."alert_rule"."rule_name" IS '规则名称';
COMMENT ON COLUMN "public"."alert_rule"."level" IS '告警等级，serous:严重，warn:警告，info:提示';
COMMENT ON COLUMN "public"."alert_rule"."rule_type" IS '规则类型，index为指标，log为日志';
COMMENT ON COLUMN "public"."alert_rule"."rule_exp_comb" IS '组合表达式';
COMMENT ON COLUMN "public"."alert_rule"."rule_content" IS '规则内容';
COMMENT ON COLUMN "public"."alert_rule"."notify_duration" IS '通知评估时长，表示告警持续多久通知';
COMMENT ON COLUMN "public"."alert_rule"."notify_duration_unit" IS '通知评估时长单位，s表示秒，m表示分，h表示小时，d表示天';
COMMENT ON COLUMN "public"."alert_rule"."is_repeat" IS '是否重复通知，0否，1是';
COMMENT ON COLUMN "public"."alert_rule"."is_silence" IS '是否静默，0为否，1为是';
COMMENT ON COLUMN "public"."alert_rule"."silence_start_time" IS '静默开始时间';
COMMENT ON COLUMN "public"."alert_rule"."silence_end_time" IS '静默结束时间';
COMMENT ON COLUMN "public"."alert_rule"."alert_notify" IS '告警通知，firing为告警，recover为告警恢复，两个可以同时存在，用逗号分隔';
COMMENT ON COLUMN "public"."alert_rule"."notify_way_ids" IS '通知方式ID集合';
COMMENT ON COLUMN "public"."alert_rule"."alert_desc" IS '告警描述';
COMMENT ON COLUMN "public"."alert_rule"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_rule"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_rule" IS '告警规则表';


CREATE TABLE IF NOT EXISTS "public"."alert_rule_item" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_rule_item_id'::regclass),
    "rule_id" int8 NOT NULL,
    "rule_mark" char(1) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_exp_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "action" varchar(50) COLLATE "pg_catalog"."default",
    "operate" varchar(20) COLLATE "pg_catalog"."default",
    "limit_value" REAL ,
    "unit" varchar(20) COLLATE "pg_catalog"."default",
    "rule_exp" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_exp_param" varchar(500) COLLATE "pg_catalog"."default",
    "rule_item_desc" text COLLATE "pg_catalog"."default",
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_rule_item"."id" IS '规则项ID';
COMMENT ON COLUMN "public"."alert_rule_item"."rule_id" IS '规则ID';
COMMENT ON COLUMN "public"."alert_rule_item"."rule_mark" IS '规则标记，A B C ……';
COMMENT ON COLUMN "public"."alert_rule_item"."rule_exp_name" IS '规则表达式名称';
COMMENT ON COLUMN "public"."alert_rule_item"."action" IS 'normal:连续发生，increase:持续增长，decrease：持续减少';
COMMENT ON COLUMN "public"."alert_rule_item"."operate" IS '操作符，<、 <=、 = 、>、 >=';
COMMENT ON COLUMN "public"."alert_rule_item"."limit_value" IS '阈值';
COMMENT ON COLUMN "public"."alert_rule_item"."unit" IS '单位';
COMMENT ON COLUMN "public"."alert_rule_item"."rule_exp" IS '规则表达式';
COMMENT ON COLUMN "public"."alert_rule_item"."rule_exp_param" IS '规则表达式参数，json';
COMMENT ON COLUMN "public"."alert_rule_item"."rule_item_desc" IS '描述';
COMMENT ON COLUMN "public"."alert_rule_item"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_rule_item"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_rule_item"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_rule_item" IS '告警规则项表';

CREATE TABLE IF NOT EXISTS "public"."alert_rule_item_param" (
    id int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_rule_item_param_id'::regclass),
    item_id bigint NOT NULL,
    param_name varchar(20) COLLATE "pg_catalog"."default",
    param_value varchar(100) COLLATE "pg_catalog"."default",
    "param_order" int default 0,
    is_deleted tinyint DEFAULT 0,
    create_time timestamp(6),
    update_time timestamp(6)
);

CREATE TABLE IF NOT EXISTS "public"."notify_way" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_notify_way_id'::regclass),
    "name" varchar(50) COLLATE "pg_catalog"."default",
    "notify_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
    "phone" text COLLATE "pg_catalog"."default",
    "email" text COLLATE "pg_catalog"."default",
    "person_id" text COLLATE "pg_catalog"."default",
    "dept_id" text COLLATE "pg_catalog"."default",
    "notify_template_id" int8 NOT NULL,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."notify_way"."id" IS '通知方式ID';
COMMENT ON COLUMN "public"."notify_way"."name" IS '通知方式名称';
COMMENT ON COLUMN "public"."notify_way"."notify_type" IS '通知方式，message、email、WeCom、DingTalk';
COMMENT ON COLUMN "public"."notify_way"."phone" IS '手机号';
COMMENT ON COLUMN "public"."notify_way"."email" IS '邮箱';
COMMENT ON COLUMN "public"."notify_way"."person_id" IS '企业微信或者钉钉用户ID';
COMMENT ON COLUMN "public"."notify_way"."dept_id" IS '企业微信或者钉钉部门ID';
COMMENT ON COLUMN "public"."notify_way"."notify_template_id" IS '通知模板ID';
COMMENT ON COLUMN "public"."notify_way"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."notify_way"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."notify_way"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."notify_way" IS '通知方式';


CREATE TABLE IF NOT EXISTS "public"."alert_template" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_template_id'::regclass), --
    "template_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_template"."id" IS '告警模板ID';
COMMENT ON COLUMN "public"."alert_template"."template_name" IS 'template_name';
COMMENT ON COLUMN "public"."alert_template"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_template"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_template"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_template" IS '告警模板';


CREATE TABLE IF NOT EXISTS "public"."alert_template_rule" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_template_rule_id'::regclass),
    "template_id" int8,
    "rule_id" int8 NOT NULL,
    "rule_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "level" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_type" varchar(20) COLLATE "pg_catalog"."default",
    "rule_exp_comb" varchar(100) COLLATE "pg_catalog"."default",
    "rule_content" text COLLATE "pg_catalog"."default",
    "notify_duration" int4,
    "notify_duration_unit" char(1) COLLATE "pg_catalog"."default",
    "is_repeat" int1,
    "is_silence" int1,
    "silence_start_time" timestamp(6),
    "silence_end_time" timestamp(6),
    "alert_notify" varchar(20) COLLATE "pg_catalog"."default",
    "notify_way_ids" text COLLATE "pg_catalog"."default",
    "alert_desc" text COLLATE "pg_catalog"."default",
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_template_rule"."id" IS '模板规则ID';
COMMENT ON COLUMN "public"."alert_template_rule"."template_id" IS '模板ID';
COMMENT ON COLUMN "public"."alert_template_rule"."rule_id" IS '规则ID';
COMMENT ON COLUMN "public"."alert_template_rule"."rule_name" IS '规则名称';
COMMENT ON COLUMN "public"."alert_template_rule"."level" IS '告警等级，serous:严重，warn:警告，info:提示';
COMMENT ON COLUMN "public"."alert_template_rule"."rule_type" IS '规则类型，index为指标，log为日志';
COMMENT ON COLUMN "public"."alert_template_rule"."rule_exp_comb" IS '组合表达式';
COMMENT ON COLUMN "public"."alert_template_rule"."rule_content" IS '规则内容';
COMMENT ON COLUMN "public"."alert_template_rule"."notify_duration" IS '通知评估时长，表示告警持续多久通知';
COMMENT ON COLUMN "public"."alert_template_rule"."notify_duration_unit" IS '通知评估时长单位，s表示秒，m表示分，h表示小时，d表示天';
COMMENT ON COLUMN "public"."alert_template_rule"."is_repeat" IS '是否重复通知，0否，1是';
COMMENT ON COLUMN "public"."alert_template_rule"."is_silence" IS '是否静默，0为否，1为是';
COMMENT ON COLUMN "public"."alert_template_rule"."silence_start_time" IS '静默开始时间';
COMMENT ON COLUMN "public"."alert_template_rule"."silence_end_time" IS '静默结束时间';
COMMENT ON COLUMN "public"."alert_template_rule"."alert_notify" IS '告警通知，firing为告警，recover为告警恢复，两个可以同时存在，用逗号分隔';
COMMENT ON COLUMN "public"."alert_template_rule"."notify_way_ids" IS '通知方式ID集合';
COMMENT ON COLUMN "public"."alert_template_rule"."alert_desc" IS '告警描述';
COMMENT ON COLUMN "public"."alert_template_rule"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_template_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_template_rule"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_template_rule" IS '告警模板规则表';

CREATE TABLE IF NOT EXISTS "public"."alert_template_rule_item" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_template_rule_item_id'::regclass),
    "template_rule_id" int8 NOT NULL,
    "rule_item_id" int8 NOT NULL,
    "rule_mark" char(1) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_exp_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "action" varchar(50) COLLATE "pg_catalog"."default",
    "operate" varchar(20) COLLATE "pg_catalog"."default",
    "limit_value" REAL ,
    "unit" varchar(20) COLLATE "pg_catalog"."default",
    "rule_exp" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_exp_param" varchar(500) COLLATE "pg_catalog"."default",
    "rule_item_desc" text COLLATE "pg_catalog"."default",
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_template_rule_item"."id" IS '告警模板规则项ID';
COMMENT ON COLUMN "public"."alert_template_rule_item"."template_rule_id" IS '告警模板规则ID';
COMMENT ON COLUMN "public"."alert_template_rule_item"."rule_item_id" IS '规则项ID';
COMMENT ON COLUMN "public"."alert_template_rule_item"."rule_mark" IS '规则标记，A B C ……';
COMMENT ON COLUMN "public"."alert_template_rule_item"."rule_exp_name" IS '规则表达式名称';
COMMENT ON COLUMN "public"."alert_rule_item"."action" IS 'normal:连续发生，increase:持续增长，decrease：持续减少';
COMMENT ON COLUMN "public"."alert_template_rule_item"."operate" IS '操作符，<、 <=、 = 、>、 >=';
COMMENT ON COLUMN "public"."alert_template_rule_item"."limit_value" IS '阈值';
COMMENT ON COLUMN "public"."alert_template_rule_item"."unit" IS '单位';
COMMENT ON COLUMN "public"."alert_template_rule_item"."rule_exp" IS '规则表达式';
COMMENT ON COLUMN "public"."alert_template_rule_item"."rule_exp_param" IS '规则表达式参数，json';
COMMENT ON COLUMN "public"."alert_template_rule_item"."rule_item_desc" IS '描述';
COMMENT ON COLUMN "public"."alert_template_rule_item"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_template_rule_item"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_template_rule_item"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_template_rule_item" IS '告警模板规则项表';

CREATE TABLE alert_template_rule_item_param (
    id int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_template_rule_item_param_id'::regclass),
    item_id bigint NOT NULL,
    param_name varchar(20) COLLATE "pg_catalog"."default",
        param_value varchar(100) COLLATE "pg_catalog"."default",
        "param_order" int default 0,
        is_deleted tinyint DEFAULT 0,
        create_time timestamp(6),
        update_time timestamp(6)
);

CREATE TABLE IF NOT EXISTS "public"."alert_cluster_node_conf" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_cluster_node_conf_id'::regclass),
    "cluster_node_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
--    "host_id" int8 NOT NULL,
    "template_id" int8 NOT NULL,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_cluster_node_conf"."id" IS '告警配置ID';
COMMENT ON COLUMN "public"."alert_cluster_node_conf"."cluster_node_id" IS '实例ID';
COMMENT ON COLUMN "public"."alert_cluster_node_conf"."template_id" IS '告警模板ID';
COMMENT ON COLUMN "public"."alert_cluster_node_conf"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_cluster_node_conf"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_cluster_node_conf"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_cluster_node_conf" IS '实例告警配置';

CREATE TABLE IF NOT EXISTS "public"."alert_record" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_record_id'::regclass),
    "cluster_node_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
--    "host_id" int8 NOT NULL,
    "template_id" int8 NOT NULL,
    "template_name" varchar(100) COLLATE "pg_catalog"."default",
    "template_rule_id" int8 NOT NULL,
    "template_rule_name" varchar(100) COLLATE "pg_catalog"."default",
    "template_rule_type" varchar(50) COLLATE "pg_catalog"."default",
    "level" varchar(50) COLLATE "pg_catalog"."default",
    "alert_content" text COLLATE "pg_catalog"."default" NOT NULL,
    "notify_way_ids" text COLLATE "pg_catalog"."default",
    "notify_way_names" text COLLATE "pg_catalog"."default",
    "start_time" timestamp(6),
    "end_time" timestamp(6),
    "duration" int8,
    "alert_status" int1 DEFAULT 0,
    "record_status" int1 DEFAULT 0,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_record"."id" IS '告警记录ID';
COMMENT ON COLUMN "public"."alert_record"."cluster_node_id" IS '实例ID';
COMMENT ON COLUMN "public"."alert_record"."template_id" IS '告警模板ID';
COMMENT ON COLUMN "public"."alert_record"."template_rule_id" IS '告警模板规则ID';
COMMENT ON COLUMN "public"."alert_record"."alert_content" IS '告警内容';
COMMENT ON COLUMN "public"."alert_record"."start_time" IS '告警开始时间';
COMMENT ON COLUMN "public"."alert_record"."end_time" IS '告警结束时间';
COMMENT ON COLUMN "public"."alert_record"."duration" IS '持续时间';
COMMENT ON COLUMN "public"."alert_record"."alert_status" IS '告警状态，告警中：0,已结束：1';
COMMENT ON COLUMN "public"."alert_record"."record_status" IS '信息状态，未读：0,已读：1';
COMMENT ON COLUMN "public"."alert_record"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_record"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_record"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_record" IS '告警记录';

CREATE TABLE IF NOT EXISTS "public"."notify_template" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_notify_template_id'::regclass),
    "notify_template_name" varchar(100) COLLATE "pg_catalog"."default",
    "notify_template_desc" varchar(200) COLLATE "pg_catalog"."default",
    "notify_title" varchar(100) COLLATE "pg_catalog"."default",
    "notify_content" text COLLATE "pg_catalog"."default" NOT NULL,
    "notify_template_type" varchar(50) COLLATE "pg_catalog"."default",
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."notify_template"."id" IS '通知模板ID';
COMMENT ON COLUMN "public"."notify_template"."notify_template_name" IS '模板名称';
COMMENT ON COLUMN "public"."notify_template"."notify_template_desc" IS '模板描述';
COMMENT ON COLUMN "public"."notify_template"."notify_title" IS '消息主题';
COMMENT ON COLUMN "public"."notify_template"."notify_content" IS '消息内容';
COMMENT ON COLUMN "public"."notify_template"."notify_template_type" IS 'email、WeCom、DingTalk';
COMMENT ON COLUMN "public"."notify_template"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."notify_template"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."notify_template"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."notify_template" IS '通知模板';


CREATE TABLE IF NOT EXISTS "public"."notify_config" (
    "id" int8 NOT NULL PRIMARY KEY,
    "type" varchar(20) COLLATE "pg_catalog"."default",
    "email" varchar(100) COLLATE "pg_catalog"."default",
    "sender" varchar(50) COLLATE "pg_catalog"."default",
    "sever" varchar(100) COLLATE "pg_catalog"."default",
    "port" int4,
    "account" varchar(50) COLLATE "pg_catalog"."default",
    "passwd" varchar(100) COLLATE "pg_catalog"."default",
    "agent_id" varchar(100) COLLATE "pg_catalog"."default",
    "app_key" varchar(100) COLLATE "pg_catalog"."default",
    "secret" text COLLATE "pg_catalog"."default",
    "enable" int1 DEFAULT 0,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."notify_config"."type" IS '发送方式';
COMMENT ON COLUMN "public"."notify_config"."email" IS '发信邮箱';
COMMENT ON COLUMN "public"."notify_config"."sender" IS '发信人';
COMMENT ON COLUMN "public"."notify_config"."sever" IS '服务器';
COMMENT ON COLUMN "public"."notify_config"."port" IS 'SMTP端口号';
COMMENT ON COLUMN "public"."notify_config"."account" IS 'SMTP账号';
COMMENT ON COLUMN "public"."notify_config"."passwd" IS 'SMTP密码';
COMMENT ON COLUMN "public"."notify_config"."agent_id" IS '应用ID';
COMMENT ON COLUMN "public"."notify_config"."app_key" IS '企业微信的企业ID或者钉钉的appKey';
COMMENT ON COLUMN "public"."notify_config"."enable" IS '是否启动，0未启动，1启动';
COMMENT ON COLUMN "public"."notify_config"."secret" IS '密钥';
COMMENT ON TABLE "public"."notify_config" IS '通知渠道';


CREATE TABLE IF NOT EXISTS "public"."notify_message" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_notify_message_id'::regclass),
    "record_id" int8 NOT NULL,
    "message_type" varchar(50) COLLATE "pg_catalog"."default",
    "title" varchar(100) COLLATE "pg_catalog"."default",
    "content" text COLLATE "pg_catalog"."default" NOT NULL,
    "email" varchar(100) COLLATE "pg_catalog"."default",
    "person_id" varchar(100) COLLATE "pg_catalog"."default",
    "dept_id" varchar(100) COLLATE "pg_catalog"."default",
    "webhook" text COLLATE "pg_catalog"."default",
    "status" int1 DEFAULT 0,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."notify_message"."id" IS '消息ID';
COMMENT ON COLUMN "public"."notify_message"."record_id" IS '告警记录ID';
COMMENT ON COLUMN "public"."notify_message"."message_type" IS 'email  WeCom DingTalk';
COMMENT ON COLUMN "public"."notify_message"."title" IS '消息主题';
COMMENT ON COLUMN "public"."notify_message"."content" IS '消息内容';
COMMENT ON COLUMN "public"."notify_message"."email" IS '收件邮箱，当message_type为email时，不为空';
COMMENT ON COLUMN "public"."notify_message"."person_id" IS '用户ID,当message_type为WeCom或DingTalk时，有用';
COMMENT ON COLUMN "public"."notify_message"."dept_id" IS '部门ID,当message_type为WeCom或DingTalk时，有用';
COMMENT ON COLUMN "public"."notify_message"."webhook" IS 'WeCom或者DingTalk提供webhook';
COMMENT ON COLUMN "public"."notify_message"."status" IS '消息发送状态，0为未发送，1为发送成功，2为发送失败';
COMMENT ON COLUMN "public"."notify_message"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."notify_message"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."notify_message"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."notify_message" IS '通知消息';

CREATE TABLE IF NOT EXISTS "public"."alert_config" (
"id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_config_id'::regclass),
"alert_ip" varchar(20) COLLATE "pg_catalog"."default",
"alert_port" varchar(10) COLLATE "pg_catalog"."default"
);

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (1,'CPU使用率过高','warn','index','A','$'||'{nodeName}的CPU使用率超过90%',2,'m',1,0,null,null,'firing,recover','1',null,0,'2023-04-26 08:30:22.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (1,1,'A','cpuUsage','>=',90,'%','100 - avg(rate(node_cpu_seconds_total{mode="idle",instance=~"$'||'{instances}"}[5m])) by(instance)  * 100','CPU使用率大于等于90%',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (2,'内存使用率过高','serious','index','A','$'||'{nodeName}的内存使用率超过90%',2,'m',1,0,null,null,'firing,recover','1','内存使用率过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (2,2,'A','memoryUsage','>=',90,'%','100 * (1 - (node_memory_MemFree_bytes + node_memory_Cached_bytes + node_memory_Buffers_bytes) / node_memory_MemTotal_bytes{instance=~"$'||'{instances}"})','内存使用率大于等于90%',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (3,'磁盘使用量过高','serious','index','A','$'||'{nodeName}的磁盘（/dev/vda1）使用量超过15GB',2,'m',1,0,null,null,'firing,recover','1','磁盘使用量过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (3,3,'A','diskUsage','>=',15360,'MB','(node_filesystem_size_bytes{device=~"/dev/vda1",instance="$' || '{instances}"} - node_filesystem_free_bytes) /1024/1024','内存使用率大于等于90%',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_param(id,item_id,param_name,param_value,param_order,is_deleted,create_time) values(1,3,'filesystemPath','/dev/vda1',1,0,'2023-06-05 15:45:20.02');

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (4,'磁盘写速率过高','serious','index','A','$'||'{nodeName}的磁盘写速率大于等于100MB/s',2,'m',1,0,null,null,'firing','1','磁盘写速率过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (4,4,'A','diskWriteRate','>=',100,'MB/s','sum(rate(node_disk_written_bytes_total{instance=~"$' || '{instances}"}[2m])) by (instance) /1024/1024','磁盘写速率大于等于100MB/s',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (5,'磁盘读速率过高','serious','index','A','$'||'{nodeName}的磁盘读速率大于等于100MB/s',2,'m',1,0,null,null,'firing','1','磁盘读速率过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (5,5,'A','diskReadRate','>=',100,'MB/s','sum(rate(node_disk_read_bytes_total{instance=~"$'||'{instances}"}[2m])) by (instance) /1024/1024','磁盘读速率大于等于100MB/s',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.notify_template (id,notify_template_name,notify_template_desc,notify_title,notify_content,notify_template_type,is_deleted,create_time,update_time)
VALUES (1,'通用告警模板',null,'告警信息','告警时间：$'||'{alertTime}'||chr(10)||'告警等级：$'||'{level}'||chr(10)||'告警实例：$'||'{nodeName}'||chr(10)||'主机IP：$'||'{hostIp}'||chr(10)||'告警内容：$'||'{content}'||chr(10),'email',0,'2023-04-26 08:30:22.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.notify_way (id,name,notify_type,phone,email,person_id,dept_id,notify_template_id,is_deleted,create_time,update_time)
VALUES (1,'通用告警方式','email',null,'xxxx@xxx.com',null,null,1,0,'2023-05-24 11:25:38.073319',null) ON DUPLICATE KEY UPDATE NOTHING;
