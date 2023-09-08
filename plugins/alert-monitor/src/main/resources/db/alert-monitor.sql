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

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_rule_item_src_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_rule_item_src_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_rule_item_exp_src_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_rule_item_exp_src_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 20
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_alert_schedule_id'' )
THEN
CREATE SEQUENCE "public"."sq_alert_schedule_id"
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

CREATE TABLE IF NOT EXISTS "public"."alert_rule_item_src" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_rule_item_src_id'::regclass),
    "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "unit" varchar(20) COLLATE "pg_catalog"."default",
    "params" text COLLATE "pg_catalog"."default",
    "params_explanation" text COLLATE "pg_catalog"."default",
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_rule_item_src"."id" IS '主键';
COMMENT ON COLUMN "public"."alert_rule_item_src"."name" IS '规则项名称';
COMMENT ON COLUMN "public"."alert_rule_item_src"."unit" IS '单位';
COMMENT ON COLUMN "public"."alert_rule_item_src"."params" IS '参数，json格式';
COMMENT ON COLUMN "public"."alert_rule_item_src"."params_explanation" IS '参数解释，json格式';
COMMENT ON COLUMN "public"."alert_rule_item_src"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_rule_item_src"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_rule_item_src"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_rule_item_src" IS '告警规则项源';

CREATE TABLE IF NOT EXISTS "public"."alert_rule_item_exp_src" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_rule_item_exp_src_id'::regclass),
    "rule_item_src_id" int8 NOT NULL,
    "action" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "operate" varchar(20) COLLATE "pg_catalog"."default",
    "limit_value" REAL ,
    "show_limit_value" int1 default 1,
    "exp" text COLLATE "pg_catalog"."default" NOT NULL,
    "is_deleted" int1 DEFAULT 0,
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."id" IS '主键';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."rule_item_src_id" IS '告警规则项源ID';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."action" IS 'normal:连续发生，increase:持续增长，decrease：持续减少';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."exp" IS '规则表达式';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."operate" IS '操作符，<、 <=、 = 、>、 >=';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."limit_value" IS '阈值';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."show_limit_value" IS '是否显示阈值，默认是，但是持续增长和持续减少不需要显示';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alert_rule_item_exp_src"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."alert_rule_item_exp_src" IS '告警规则项表达式源';

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
COMMENT ON COLUMN "public"."alert_rule"."notify_duration" IS '统计周期，表示告警持续多久通知';
COMMENT ON COLUMN "public"."alert_rule"."notify_duration_unit" IS '统计周期，s表示秒，m表示分，h表示小时，d表示天';
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

ALTER TABLE "public"."alert_rule" ADD COLUMN "next_repeat" int4;
ALTER TABLE "public"."alert_rule" ADD COLUMN "next_repeat_unit" varchar(20);
ALTER TABLE "public"."alert_rule" ADD COLUMN "max_repeat_count" int4;
COMMENT ON COLUMN "public"."alert_rule"."next_repeat" IS '通知周期';
COMMENT ON COLUMN "public"."alert_rule"."next_repeat_unit" IS '通知周期单位';
COMMENT ON COLUMN "public"."alert_rule"."max_repeat_count" IS '最大通知次数';
ALTER TABLE "public"."alert_rule" ADD COLUMN "check_frequency" int;
ALTER TABLE "public"."alert_rule" ADD COLUMN "check_frequency_unit" char(1);
COMMENT ON COLUMN "public"."alert_rule"."check_frequency" IS '检查频率';
COMMENT ON COLUMN "public"."alert_rule"."check_frequency_unit" IS '检查频率单位';

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

ALTER TABLE "public"."alert_rule_item" ADD COLUMN "keyword" text;
ALTER TABLE "public"."alert_rule_item" ADD COLUMN "block_word" text;
COMMENT ON COLUMN "public"."alert_rule_item"."keyword" IS '关键字，多个用逗号分隔';
COMMENT ON COLUMN "public"."alert_rule_item"."block_word" IS '屏蔽词，多个用逗号分隔';
ALTER TABLE public.alert_rule_item ALTER COLUMN rule_exp_param TYPE text USING rule_exp_param::text;
alter table "public"."alert_rule_item" ALTER COLUMN rule_exp DROP NOT NULL;


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
COMMENT ON COLUMN "public"."notify_way"."notify_type" IS '通知方式，message、email、WeCom、DingTalk、webhook、SNMP';
COMMENT ON COLUMN "public"."notify_way"."phone" IS '手机号';
COMMENT ON COLUMN "public"."notify_way"."email" IS '邮箱';
COMMENT ON COLUMN "public"."notify_way"."person_id" IS '企业微信或者钉钉用户ID';
COMMENT ON COLUMN "public"."notify_way"."dept_id" IS '企业微信或者钉钉部门ID';
COMMENT ON COLUMN "public"."notify_way"."notify_template_id" IS '通知模板ID';
COMMENT ON COLUMN "public"."notify_way"."is_deleted" IS '是否删除，0未删除，1删除';
COMMENT ON COLUMN "public"."notify_way"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."notify_way"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."notify_way" IS '通知方式';

ALTER TABLE "public"."notify_way" ADD COLUMN "send_way" int1;
COMMENT ON COLUMN "public"."notify_way"."send_way" IS '发送方式：0为正常，1为机器人推送，当通知方式为企业微信或者钉钉时，有效。';
ALTER TABLE "public"."notify_way" ADD COLUMN "webhook" text;
COMMENT ON COLUMN "public"."notify_way"."webhook" IS '当发送方式为1或者通知方式为Webhook时，有效';
ALTER TABLE "public"."notify_way" ADD COLUMN "sign" text;
COMMENT ON COLUMN "public"."notify_way"."sign" IS '加签,当通知方式为钉钉，并且发送方式为机器人推送时，有效';
ALTER TABLE "public"."notify_way" ADD COLUMN "header" text;
COMMENT ON COLUMN "public"."notify_way"."header" IS '请求头，json';
ALTER TABLE "public"."notify_way" ADD COLUMN "params" text;
COMMENT ON COLUMN "public"."notify_way"."params" IS '请求参数，json，用于url上，比如http://127.0.0.1/test?a=x&b=y';
ALTER TABLE "public"."notify_way" ADD COLUMN "body" text;
COMMENT ON COLUMN "public"."notify_way"."body" IS '请求参数,json';
ALTER TABLE "public"."notify_way" ADD COLUMN "result_code" text;
COMMENT ON COLUMN "public"."notify_way"."result_code" IS '请求返回成功码,json，比如 {"errcode": 0}';
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_ip" varchar(20);
COMMENT ON COLUMN "public"."notify_way"."snmp_ip" IS 'ip地址';
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_port" varchar(10);
COMMENT ON COLUMN "public"."notify_way"."snmp_port" IS '端口';
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_community" varchar(100);
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_oid" varchar(100);
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_version" int1;
COMMENT ON COLUMN "public"."notify_way"."snmp_version" IS '0为版本1，1为版本2c，3为版本3';
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_username" varchar(100);
COMMENT ON COLUMN "public"."notify_way"."snmp_username" IS 'snmp用户名，snmp_version为v3有效';
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_auth_passwd" varchar(100);
COMMENT ON COLUMN "public"."notify_way"."snmp_auth_passwd" IS 'snmp鉴权密码，snmp_version为v3有效';
ALTER TABLE "public"."notify_way" ADD COLUMN "snmp_priv_passwd" varchar(100);
COMMENT ON COLUMN "public"."notify_way"."snmp_priv_passwd" IS 'snmp数据加密密码，snmp_version为v3有效';




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

ALTER TABLE "public"."alert_template_rule" ADD COLUMN "next_repeat" int4;
ALTER TABLE "public"."alert_template_rule" ADD COLUMN "next_repeat_unit" varchar(20);
ALTER TABLE "public"."alert_template_rule" ADD COLUMN "max_repeat_count" int4;
COMMENT ON COLUMN "public"."alert_template_rule"."next_repeat" IS '通知周期';
COMMENT ON COLUMN "public"."alert_template_rule"."next_repeat_unit" IS '通知周期单位';
COMMENT ON COLUMN "public"."alert_template_rule"."max_repeat_count" IS '最大通知次数';
ALTER TABLE "public"."alert_template_rule" ADD COLUMN "check_frequency" int;
ALTER TABLE "public"."alert_template_rule" ADD COLUMN "check_frequency_unit" char(1);
COMMENT ON COLUMN "public"."alert_template_rule"."check_frequency" IS '检查频率';
COMMENT ON COLUMN "public"."alert_template_rule"."check_frequency_unit" IS '检查频率单位';
ALTER TABLE "public"."alert_template_rule" ADD COLUMN "enable" int1 NOT NULL default 1;
COMMENT ON COLUMN "public"."alert_template_rule"."enable" IS '启动/停止，0为停止，1为启动，默认启动';

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

ALTER TABLE "public"."alert_template_rule_item" ADD COLUMN "keyword" text;
ALTER TABLE "public"."alert_template_rule_item" ADD COLUMN "block_word" text;
COMMENT ON COLUMN "public"."alert_template_rule_item"."keyword" IS '关键字，多个用逗号分隔';
COMMENT ON COLUMN "public"."alert_template_rule_item"."block_word" IS '屏蔽词，多个用逗号分隔';
ALTER TABLE public.alert_template_rule_item ALTER COLUMN rule_exp_param TYPE text USING rule_exp_param::text;
alter table "public"."alert_template_rule_item" ALTER COLUMN rule_exp DROP NOT NULL;

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

CREATE TABLE IF NOT EXISTS "public"."alert_schedule" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_schedule_id'::regclass),
    "job_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "job_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
    "type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "is_locked" int1 DEFAULT 0,
    "cron" varchar(50) COLLATE "pg_catalog"."default",
    "fixed_rate" int8,
    "fixed_delay" int8,
    "initial_delay" int8,
    "last_time" timestamp(6),
    "create_time" timestamp(6),
    "update_time" timestamp(6)
    );

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

ALTER TABLE "public"."alert_record" ADD COLUMN "send_count" int4;
ALTER TABLE "public"."alert_record" ADD COLUMN "send_time" timestamp(6);
COMMENT ON COLUMN "public"."alert_record"."send_count" IS '发送通知次数';
COMMENT ON COLUMN "public"."alert_record"."send_time" IS '上一次发送通知的时间';

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
ALTER TABLE "public"."notify_message" ADD COLUMN "sign" varchar(100);
COMMENT ON COLUMN "public"."notify_message"."sign" IS '加签，用于钉钉机器人推送';
ALTER TABLE "public"."notify_message" ADD COLUMN "webhook_info" text;
COMMENT ON COLUMN "public"."notify_message"."webhook_info" IS 'json格式，message_type为webhook有用，内容包含header、params、body';
ALTER TABLE "public"."notify_message" ADD COLUMN "snmp_info" text;
COMMENT ON COLUMN "public"."notify_message"."snmp_info" IS 'json格式，message_type为SNMP有用';


CREATE TABLE IF NOT EXISTS "public"."alert_config" (
"id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_alert_config_id'::regclass),
"alert_ip" varchar(20) COLLATE "pg_catalog"."default",
"alert_port" varchar(10) COLLATE "pg_catalog"."default"
);

CREATE TABLE IF NOT EXISTS "public"."nctigba_env" (
	id varchar NULL,
	hostid varchar NULL,
	"type" varchar NULL,
	username varchar NULL,
	"path" varchar NULL,
	port int8 NULL,
	nodeid varchar NULL
);

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (1,'CPU使用率过高','warn','index','A','$'||'{nodeName}的CPU使用率超过90%',2,'m',1,0,null,null,'firing,recover','1',null,0,'2023-04-26 08:30:22.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (1,1,'A','cpuUsage','>=',90,'%','(1 - sum(increase(agent_cpu_seconds_total{mode=~"idle",instance=~"$'||'{instances}"}[5m])) by (instance) / sum(increase(agent_cpu_seconds_total{instance=~"$' || '{instances}"}[5m])) by (instance)) * 100','CPU使用率大于等于90%',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (2,'内存使用率过高','serious','index','A','$'||'{nodeName}的内存使用率超过90%',2,'m',1,0,null,null,'firing,recover','1','内存使用率过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (2,2,'A','memoryUsage','>=',90,'%','100 * (1 - (agent_memory_MemFree_bytes + agent_memory_Cached_bytes + agent_memory_Buffers_bytes) / agent_memory_MemTotal_bytes{instance=~"$'||'{instances}"})','内存使用率大于等于90%',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (3,'磁盘使用量过高','serious','index','A','$'||'{nodeName}的磁盘（/dev/vda1）使用量超过15GB',2,'m',1,0,null,null,'firing,recover','1','磁盘使用量过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (3,3,'A','diskUsage','>=',15360,'MB','(agent_filesystem_size_bytes{device=~"/dev/vda1",instance=~"$' ||
 '{instances}"} - agent_filesystem_free_bytes) /1024/1024','内存使用率大于等于90%',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_param(id,item_id,param_name,param_value,param_order,is_deleted,create_time) values(1,3,'filesystemPath','/dev/vda1',1,0,'2023-06-05 15:45:20.02');

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (4,'磁盘写速率过高','serious','index','A','$'||'{nodeName}的磁盘写速率大于等于100MB/s',2,'m',1,0,null,null,'firing','1','磁盘写速率过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (4,4,'A','diskWriteRate','>=',100,'MB/s','sum(rate(agent_disk_written_bytes_total{instance=~"$' || '{instances}"}[2m])) by (instance) /1024/1024','磁盘写速率大于等于100MB/s',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (5,'磁盘读速率过高','serious','index','A','$'||'{nodeName}的磁盘读速率大于等于100MB/s',2,'m',1,0,null,null,'firing','1','磁盘读速率过高',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (5,5,'A','diskReadRate','>=',100,'MB/s','sum(rate(agent_disk_read_bytes_total{instance=~"$'||'{instances}"}[2m])) by (instance) /1024/1024','磁盘读速率大于等于100MB/s',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.notify_template (id,notify_template_name,notify_template_desc,notify_title,notify_content,notify_template_type,is_deleted,create_time,update_time)
VALUES (1,'通用告警模板',null,'告警信息','告警时间：$'||'{alertTime}'||chr(10)||'告警等级：$'||'{level}'||chr(10)||'告警实例：$'||'{nodeName}'||chr(10)||'主机IP：$'||'{hostIp}'||chr(10)||'告警内容：$'||'{content}'||chr(10),'email',0,'2023-04-26 08:30:22.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.notify_way (id,name,notify_type,phone,email,person_id,dept_id,notify_template_id,is_deleted,create_time,update_time)
VALUES (1,'通用告警方式','email',null,'xxxx@xxx.com',null,null,1,0,'2023-05-24 11:25:38.073319',null) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (1,'cpuUsage','%','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(1,1,'normal','',null,'(1 - sum(increase(agent_cpu_seconds_total{mode=~"idle",instance=~"$'||'{instances}"}[5m])) by (instance) / sum(increase(agent_cpu_seconds_total{instance=~"$' || '{instances}"}[5m])) by (instance)) * 100',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (2,'memoryUsage','%','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(4,2,'normal','',null,
'100 * (1 - (agent_memory_MemFree_bytes + agent_memory_Cached_bytes + agent_memory_Buffers_bytes) / agent_memory_MemTotal_bytes{instance=~"$'||'{instances}"})',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,params_explanation,create_time) values (3,'diskUsage',
'MB','{"filesystemPath":""}','{"filesystemPath":{"tip": "filesystemPathTip","required": false}}',now()) ON DUPLICATE KEY
 UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(7,3,'normal','',null,
'(agent_filesystem_size_bytes{device=~"$' || '{filesystemPath}",instance=~"$' ||
'{instances}"} - agent_filesystem_free_bytes)/1024/1024',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (4,'diskWriteRate',
'MB/s','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(10,4,'normal','',null,
'sum(rate(agent_disk_written_bytes_total{instance=~"$' || '{instances}"}[2m])) by (instance)/1024/1024',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (5,'diskReadRate',
'MB/s','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(13,5,'normal','',null,
'sum(rate(agent_disk_read_bytes_total{instance=~"$'||'{instances}"}[2m])) by (instance)/1024/1024',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (6,'networkReceiveRate',
'MB/s','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(16,6,'normal','',null,
'max(rate(agent_network_receive_bytes_total{instance=~"$'||'{instances}"}[2m]))) by (instance)/1024/1024',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (7,'networkTransmitRate',
'MB/s','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(19,7,'normal','',null,
'max(rate(agent_network_transmit_bytes_total{instance=~"$'||'{instances}"}[2m]))) by (instance)/1024/1024',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (15,'lockCount',
'','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(30,15,'normal','',null,
'sum(pg_lock_count{instance =~"$'||'{instances}"}) by (instance)',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (16,'lockTime',
's','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(35,16,'normal','',null,
'pg_lock_detail_locked_time{instance =~"$'||'{instances}"}',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (17,'connectionCount',
'','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(40,17,'normal','',null,
'pg_connections_used_conn{instance =~"$'||'{instances}"}',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,params_explanation,create_time) values (18,
'connectionIncrease','','{"calcCycle":"10m"}',
'{"calcCycle": {"tip": "calcCycleTip","required": true}}',
now())ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(45,18,'normal','',null,
'increase(pg_connections_used_conn{instance =~"$' || '{instances}"}[$' || '{calcCycle}]) by (instance)',
1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (19,'slowsqlRunTime',
's','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(50,19,'normal','',null,
'pg_active_slowsql_query_runtime{instance=~"$'||'{instances}"}',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,params_explanation,create_time) values (20,'tablespaceSize',
'MB','{"name": ""}','{"name": {"tip": "tablespaceNameTip","required": true}}',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(55,20,'normal','',null,
'pg_tablespace_size{instance=~"$'||'{instances}"}',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (21,'waitingCount',
'','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(60,21,'normal','',null,
'pg_state_activity_group_count{state="waiting",instance=~"$'||'{instances}"}',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

insert into public.alert_rule_item_src(id,name,unit,params,create_time) values (22,'pgDbStatus',
'','',now()) ON DUPLICATE KEY UPDATE NOTHING;
insert into public.alert_rule_item_exp_src(id,rule_item_src_id,action,operate,limit_value,exp,show_limit_value,
create_time) values(65,22,'normal','',null,
'pg_db_status{instance=~"$'||'{instances}"}',1,now()) ON DUPLICATE KEY UPDATE NOTHING;

-- add rule
INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (6,'数据库状态监控','serious','index','A','$'||'{nodeName}处于离线状态',1,'m',1,0,null,null,'firing','1',
 '数据库状态监控，数据库状态为0表示数据库处于离线状态',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (6,6,'A','pgDbStatus','=',0,'','pg_db_status{instance=~"$'||'{instances}"}','数据库处于离线状态',0,'2023-08-07 15:45:20.02',
 null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (7,'阻塞会话监控','serious','index','A','$'||'{nodeName}的阻塞会话数超过50',1,'m',1,0,null,null,'firing','1',
  '阻塞会话监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
  VALUES (7,7,'A','waitingCount','>',50,'','pg_state_activity_group_count{state="waiting",instance=~"$'||'{instances}"}',
  '阻塞会话数超过50',0,'2023-08-07 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (8,'表空间容量监控','warn','index','A','$'||'{nodeName}的表空间容量超过500MB',1,'m',1,0,null,null,'firing','1',
  '表空间容量监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,
 rule_item_desc,is_deleted,create_time,update_time,action, ,rule_exp_param)
  VALUES (8,8,'A','tablespaceSize','>',500,'MB','pg_tablespace_size{name=~"$'||'{name}",instance=~"$'||'{instances}"}',
  '表空间容量超过500MB',0,'2023-08-07 15:45:20.02',null,'normal','{"name":""}') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (9,'慢sql运行时间监控','serious','index','A','$'||'{nodeName}的一些sql运行时间超过10秒',0,'s',1,0,null,null,'firing','1',
  '慢sql运行时间监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
  VALUES (9,9,'A','slowsqlRunTime','>',10,'s','pg_active_slowsql_query_runtime{instance=~"$'||'{instances}"}',
  'sql执行时间超过10秒',0,'2023-08-07 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (10,'数据库连接数监控','warn','index','A','$'||'{nodeName}的连接数超过500',1,'m',1,0,null,null,'firing','1',
  '数据库连接数监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
  VALUES (10,10,'A','connectionCount','>',500,'','pg_connections_used_conn{instance=~"$'||'{instances}"}',
  '数据库连接数超过500',0,'2023-08-07 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,
 notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (10,'数据库连接数异常监控','warn','index','A and B','$'||'{nodeName}的连接数超过300，并且每10分钟新增的连接数超过10',5,'m',1,0,null,null,
  'firing','1','数据库连接数异常监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
  VALUES (10,10,'A','connectionCount','>',300,'','pg_connections_used_conn{instance=~"$'||'{instances}"}',
  '数据库连接数超过300',0,'2023-08-07 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,
 rule_item_desc,is_deleted,create_time,update_time,action,rule_exp_param)
   VALUES (11,10,'B','connectionIncrease','>',10,'','increase(pg_connections_used_conn{instance=~"$'||'{instances}"}[$'
   || '{calcCycle}]) by (instance)',
   '数据库连接数每10分钟增长超过10',0,'2023-08-07 15:45:20.02',null,'normal','{"calcCycle":"10m"}') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,
 notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (12,'数据库锁数量监控','warn','index','A','$'||'{nodeName}的锁数量超过50',1,'m',1,0,null,null,
  'firing','1','数据库锁数量监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
  VALUES (12,12,'A','lockCount','>',50,'','sum(pg_lock_count{instance =~"$'||'{instances}"}) by (instance)',
  '数据库锁数量超过50',0,'2023-08-07 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

 INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,
 notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
  VALUES (13,'数据库锁时间监控','serious','index','A','$'||'{nodeName}的锁时间超过10秒',1,'m',1,0,null,null,
  'firing','1','数据库锁时间监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
 INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
  VALUES (13,13,'A','lockTime','>',10,'s','pg_lock_detail_locked_time{instance =~"$'||'{instances}"}',
  '数据库锁时间超过10秒',0,'2023-08-07 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (14,'网络输入速率监控','warn','index','A','$'||'{nodeName}的网络输入速率大于等于100MB/s',1,'m',1,0,null,null,'firing','1',
 '网络输入速率监控',0,'2023-08-07 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (14,14,'A','networkReceiveRate','>=',100,'MB/s','max(rate(agent_network_receive_bytes_total{instance=~"$' ||
 '{instances}"}[2m])) by (instance) /1024/1024','网络输入速率大于等于100MB/s',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;

INSERT INTO public.alert_rule (id,rule_name,level,rule_type,rule_exp_comb,rule_content,notify_duration,notify_duration_unit,is_repeat,is_silence,silence_start_time,silence_end_time,alert_notify,notify_way_ids,alert_desc,is_deleted,create_time,update_time)
 VALUES (15,'网络输出速率监控','warn','index','A','$'||'{nodeName}的网络输出速率大于等于100MB/s',1,'m',1,0,null,null,'firing','1',
 '网络输出速率监控',0,'2023-06-05 15:45:20.02',null) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO public.alert_rule_item (id,rule_id,rule_mark,rule_exp_name,operate,limit_value,unit,rule_exp,rule_item_desc,is_deleted,create_time,update_time,action)
 VALUES (15,15,'A','diskReadRate','>=',100,'MB/s',
 'max(rate(agent_network_transmit_bytes_total{instance=~"$'||'{instances}"}[2m])) by (instance) /1024/1024','网络输出速率大于等于100MB/s',0,'2023-06-05 15:45:20.02',null,'normal') ON DUPLICATE KEY UPDATE NOTHING;