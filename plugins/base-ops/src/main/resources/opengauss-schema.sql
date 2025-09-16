
CREATE OR REPLACE FUNCTION check_sequences() RETURNS integer AS '
        BEGIN
IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_modeling_data_flow_id'' )
THEN
CREATE SEQUENCE "public"."sq_modeling_data_flow_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_modeling_custom_dimension_id'' )
THEN
CREATE SEQUENCE "public"."sq_modeling_custom_dimension_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_modeling_visualization_params_id'' )
THEN
CREATE SEQUENCE "public"."sq_modeling_visualization_params_id"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_modeling_visualization_report_id'' )
THEN
CREATE SEQUENCE "public"."sq_modeling_visualization_report_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_modeling_visualization_geo_id'' )
THEN
CREATE SEQUENCE "public"."sq_modeling_visualization_geo_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_modeling_visualization_snapshot_id'' )
THEN
CREATE SEQUENCE "public"."sq_modeling_visualization_snapshot_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

RETURN 0;
 END;'
 LANGUAGE plpgsql;

SELECT check_sequences();

DROP FUNCTION check_sequences;

-- ----------------------------
-- Table structure for modeling_data_flow
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_data_flow" (
  "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_modeling_data_flow_id'::regclass),
  "name" varchar(512) COLLATE "pg_catalog"."default",
  "u_id" varchar(128) COLLATE "pg_catalog"."default",
  "tags" varchar(512) COLLATE "pg_catalog"."default",
  "remark" text COLLATE "pg_catalog"."default",
  "operator_content" text COLLATE "pg_catalog"."default",
  "type" varchar(128) COLLATE "pg_catalog"."default",
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "db_name" varchar(255) COLLATE "pg_catalog"."default",
  "cluster_node_id" varchar(128) COLLATE "pg_catalog"."default",
  "schema" varchar(255) COLLATE "pg_catalog"."default",
  "cluster_id" varchar(128) COLLATE "pg_catalog"."default",
  "query_count" int4,
  "data_base" varchar(128) COLLATE "pg_catalog"."default",
  "creator" int4[],
  "manager" varchar(1024) COLLATE "pg_catalog"."default",
  "developer" varchar(1024) COLLATE "pg_catalog"."default",
  "visitor" varchar(1024) COLLATE "pg_catalog"."default",
  "input_fileds" varchar(512) COLLATE "pg_catalog"."default",
  "output_fields" varchar(512) COLLATE "pg_catalog"."default",
  "operator_content_auto_save" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_data_flow"."name" IS '数据流名称';
COMMENT ON COLUMN "public"."modeling_data_flow"."u_id" IS '数据流全局唯一ID';
COMMENT ON COLUMN "public"."modeling_data_flow"."tags" IS '标签';
COMMENT ON COLUMN "public"."modeling_data_flow"."remark" IS '备注';
COMMENT ON COLUMN "public"."modeling_data_flow"."operator_content" IS '算子完整内容json';
COMMENT ON COLUMN "public"."modeling_data_flow"."type" IS '查询类型';
COMMENT ON COLUMN "public"."modeling_data_flow"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_data_flow"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_data_flow"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_data_flow"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_data_flow"."data_base" IS '默认访问数据库';
COMMENT ON COLUMN "public"."modeling_data_flow"."creator" IS '创建人';
COMMENT ON COLUMN "public"."modeling_data_flow"."manager" IS '管理权限';
COMMENT ON COLUMN "public"."modeling_data_flow"."developer" IS '开发权限';
COMMENT ON COLUMN "public"."modeling_data_flow"."visitor" IS '访客权限';
COMMENT ON COLUMN "public"."modeling_data_flow"."input_fileds" IS '输入字段';
COMMENT ON COLUMN "public"."modeling_data_flow"."output_fields" IS '输出字段';
COMMENT ON COLUMN "public"."modeling_data_flow"."operator_content_auto_save" IS '算子完整内容json定时自动保存';
COMMENT ON TABLE "public"."modeling_data_flow" IS '数据流表';

-- ----------------------------
-- Records of modeling_data_flow
-- ----------------------------

-- ----------------------------
-- Table structure for modeling_data_flow_operator
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_data_flow_operator" (
  "id" int4 NOT NULL PRIMARY KEY,
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "package_path" varchar(255) COLLATE "pg_catalog"."default",
  "type" int2,
  "group_id" int2,
  "sort_id" int2,
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."name" IS '算子名称';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."package_path" IS '算子包路径';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."type" IS '可用数据流类型';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."group_id" IS '所属分组';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."sort_id" IS '菜单排序';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_data_flow_operator"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."modeling_data_flow_operator" IS '数据库操作算子表';

-- ----------------------------
-- Records of modeling_data_flow_operator
-- ----------------------------
INSERT INTO "public"."modeling_data_flow_operator" VALUES (202, '条件算子', 'condition', 2, 2, 2, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (102, '条件算子', 'condition', 1, 2, 2, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (108, '映射算子', 'mapping', 1, 2, 8, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (101, '查询算子', 'query', 1, 1, 1, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (103, '排序算子', 'sort', 1, 2, 3, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (104, '限条算子', 'limit', 1, 2, 4, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (105, '分组算子', 'group', 1, 2, 5, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (106, '聚合算子', 'polymerization', 1, 2, 6, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (107, '桥接算子', 'join', 1, 2, 7, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (201, '更新算子', 'update', 2, 1, 1, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (1001, '数据源算子', 'dataSource', 10, 0, 1, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (301, '插入算子', 'insert', 3, 1, 1, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator" VALUES (401, '删除算子', 'delete', 4, 1, 1, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;

-- ----------------------------
-- Table structure for modeling_data_flow_operator_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_data_flow_operator_group" (
  "id" int4 NOT NULL PRIMARY KEY,
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "sort_id" int4,
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."name" IS '分组名称';
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."sort_id" IS '菜单排序';
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_data_flow_operator_group"."remark" IS '描述';
COMMENT ON TABLE "public"."modeling_data_flow_operator_group" IS '数据库算子分类';

-- ----------------------------
-- Records of modeling_data_flow_operator_group
-- ----------------------------
INSERT INTO "public"."modeling_data_flow_operator_group" VALUES (1, '基础算子', 1, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;
INSERT INTO "public"."modeling_data_flow_operator_group" VALUES (2, '组合算子', 2, NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;;

-- ----------------------------
-- Table structure for modeling_data_flow_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_data_flow_type" (
  "id" int4 NOT NULL PRIMARY KEY,
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_data_flow_type"."name" IS '类型名称';
COMMENT ON COLUMN "public"."modeling_data_flow_type"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_data_flow_type"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_data_flow_type"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_data_flow_type"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_data_flow_type"."remark" IS '描述';
COMMENT ON TABLE "public"."modeling_data_flow_type" IS '数据流操作类型';

-- ----------------------------
-- Records of modeling_data_flow_type
-- ----------------------------
INSERT INTO "public"."modeling_data_flow_type" VALUES (1, '查询类', NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."modeling_data_flow_type" VALUES (2, '更新类', NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."modeling_data_flow_type" VALUES (3, '删除类', NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."modeling_data_flow_type" VALUES (4, '插入类', NULL, NULL, NULL, NULL, NULL) ON DUPLICATE KEY UPDATE NOTHING;

-- ----------------------------
-- Table structure for modeling_visualization_custom_dimensions
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_visualization_custom_dimensions" (
  "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_modeling_custom_dimension_id'::regclass),
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "field" varchar(128) COLLATE "pg_catalog"."default",
  "categories_json" text COLLATE "pg_catalog"."default",
  "operator_id" varchar(128) COLLATE "pg_catalog"."default",
  "data_flow_id" int8,
  "sort_id" int4,
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."id" IS 'id';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."name" IS '维度名称';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."field" IS '维度对应字段';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."categories_json" IS '类目完整json';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."operator_id" IS '算子唯一id';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."data_flow_id" IS '所属数据流';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."sort_id" IS '目录排序';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_visualization_custom_dimensions"."remark" IS '描述';

-- ----------------------------
-- Records of modeling_visualization_custom_dimensions
-- ----------------------------

-- ----------------------------
-- Table structure for modeling_visualization_geo_files
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_visualization_geo_files" (
  "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_modeling_visualization_geo_id'::regclass),
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "file_path" varchar(128) COLLATE "pg_catalog"."default",
  "geo_json" text COLLATE "pg_catalog"."default",
  "register_name" varchar(128) COLLATE "pg_catalog"."default",
  "data_flow_id" int8,
  "sort_id" int4,
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."id" IS 'id';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."name" IS '地图中文名称';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."file_path" IS 'http访问路径';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."geo_json" IS 'geo完整json';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."register_name" IS '地图注册名称';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."data_flow_id" IS '所属数据流';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."sort_id" IS '目录排序';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_visualization_geo_files"."remark" IS '描述';

-- ----------------------------
-- Records of modeling_visualization_geo_files
-- ----------------------------

-- ----------------------------
-- Table structure for modeling_visualization_params
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_visualization_params" (
  "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_modeling_visualization_params_id'::regclass),
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "type" varchar(128) COLLATE "pg_catalog"."default",
  "params_json" text COLLATE "pg_catalog"."default",
  "operator_id" varchar(128) COLLATE "pg_catalog"."default",
  "data_flow_id" int8,
  "sort_id" int4,
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_visualization_params"."id" IS 'id';
COMMENT ON COLUMN "public"."modeling_visualization_params"."name" IS '图表名称';
COMMENT ON COLUMN "public"."modeling_visualization_params"."type" IS '图表类型';
COMMENT ON COLUMN "public"."modeling_visualization_params"."params_json" IS '图表参数完整json';
COMMENT ON COLUMN "public"."modeling_visualization_params"."operator_id" IS '算子唯一id';
COMMENT ON COLUMN "public"."modeling_visualization_params"."data_flow_id" IS '所属数据流';
COMMENT ON COLUMN "public"."modeling_visualization_params"."sort_id" IS '目录排序';
COMMENT ON COLUMN "public"."modeling_visualization_params"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_visualization_params"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_visualization_params"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_visualization_params"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_visualization_params"."remark" IS '描述';

-- ----------------------------
-- Records of modeling_visualization_params
-- ----------------------------

-- ----------------------------
-- Table structure for modeling_visualization_reports
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_visualization_reports" (
  "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_modeling_visualization_report_id'::regclass),
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "url" varchar(1024) COLLATE "pg_catalog"."default",
  "intro" text COLLATE "pg_catalog"."default",
  "data_flow_id" int8,
  "params_json" text COLLATE "pg_catalog"."default",
  "type" int8,
  "resource_url" varchar(1024) COLLATE "pg_catalog"."default",
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_visualization_reports"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_visualization_reports"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_visualization_reports"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_visualization_reports"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_visualization_reports"."remark" IS '描述';

-- ----------------------------
-- Records of modeling_visualization_reports
-- ----------------------------

-- ----------------------------
-- Table structure for modeling_visualization_snapshots
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."modeling_visualization_snapshots" (
  "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_modeling_visualization_snapshot_id'::regclass),
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "img_base64" text COLLATE "pg_catalog"."default",
  "chart_data_json" text COLLATE "pg_catalog"."default",
  "data_flow_id" int8,
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."modeling_visualization_snapshots"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."modeling_visualization_snapshots"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."modeling_visualization_snapshots"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."modeling_visualization_snapshots"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."modeling_visualization_snapshots"."remark" IS '描述';

CREATE TABLE IF NOT EXISTS  "public"."ops_backup" (
    "backup_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "host_id" varchar(255) COLLATE "pg_catalog"."default",
    "cluster_node_id" varchar(255) COLLATE "pg_catalog"."default",
    "backup_path" text COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    );
CREATE TABLE IF NOT EXISTS "public"."ops_wdr" (
    "wdr_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "scope" varchar(255) COLLATE "pg_catalog"."default",
    "report_at" timestamp(6),
    "report_type" varchar(255) COLLATE "pg_catalog"."default",
    "report_name" varchar(255) COLLATE "pg_catalog"."default",
    "report_path" varchar(255) COLLATE "pg_catalog"."default",
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "node_id" varchar(255) COLLATE "pg_catalog"."default",
    "host_id" varchar(255) COLLATE "pg_catalog"."default",
    "user_id" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "start_snapshot_id" varchar(255) COLLATE "pg_catalog"."default",
    "end_snapshot_id" varchar(255) COLLATE "pg_catalog"."default"
    )
;

CREATE TABLE IF NOT EXISTS "public"."ops_package_manager" (
    "package_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "os" varchar(255) COLLATE "pg_catalog"."default",
    "cpu_arch" varchar(255) COLLATE "pg_catalog"."default",
    "package_version" varchar(255) COLLATE "pg_catalog"."default",
    "package_version_num" varchar(255) COLLATE "pg_catalog"."default",
    "package_url" varchar(1024) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;

CREATE TABLE IF NOT EXISTS "public"."ops_olk" (
    "id" VARCHAR(64) NOT NULL PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "olk_tar_id" VARCHAR(255) NOT NULL,
    "ss_tar_id" VARCHAR(255) NOT NULL,
    "dad_tar_id" VARCHAR(255) NOT NULL,
    "dad_port" VARCHAR(10) NOT NULL,
    "dad_install_path" TEXT NOT NULL,
    "dad_install_host_id" VARCHAR(255) NOT NULL,
    "dad_install_username" VARCHAR(255) NOT NULL,
    "zk_tar_id" VARCHAR(255) NOT NULL,
    "ss_port" VARCHAR(10) NOT NULL,
    "olk_port" VARCHAR(10) NOT NULL,
    "zk_port" VARCHAR(10) NOT NULL,
    "ss_install_path" TEXT NOT NULL,
    "ss_upload_path" TEXT NOT NULL,
    "olk_install_path" TEXT NOT NULL,
    "olk_upload_path" TEXT NOT NULL,
    "ss_install_host_id" VARCHAR(255) NOT NULL,
    "olk_install_host_id" VARCHAR(255) NOT NULL,
    "ss_install_username" VARCHAR(255) NOT NULL,
    "olk_install_username" VARCHAR(255) NOT NULL,
    "remark" TEXT NULL DEFAULT NULL,
    "rule_yaml" TEXT NOT NULL,
    "table_name" TEXT NULL DEFAULT NULL,
    "columns" TEXT NULL DEFAULT NULL,
    "create_by" VARCHAR(64) NULL DEFAULT NULL,
    "update_by" VARCHAR(64) NULL DEFAULT NULL,
    "update_time" TIMESTAMP NULL DEFAULT NULL,
    "create_time" TIMESTAMP NULL DEFAULT NULL,
    "dad_install_password" TEXT NULL DEFAULT NULL
);



CREATE OR REPLACE FUNCTION add_field_db_name() RETURNS integer AS '
        BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''modeling_data_flow'' AND COLUMN_NAME = ''db_name'' ) = 0
THEN
ALTER TABLE modeling_data_flow ADD COLUMN db_name VARCHAR(255);
END IF;
RETURN 0;
 END;'
 LANGUAGE plpgsql;

SELECT add_field_db_name();

DROP FUNCTION add_field_db_name;

DELETE FROM sys_menu WHERE menu_name IN ('磁阵管理', '容灾集群管理', '容灾集群安装');