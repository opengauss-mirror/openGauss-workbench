CREATE OR REPLACE FUNCTION create_sequences_not_exists() RETURNS integer AS 'BEGIN

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_log_config_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_log_config_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_menu_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_menu_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_log_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_log_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_plugin_config_data_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_plugin_config_data_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_plugin_config_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_plugin_config_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_plugin_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_plugin_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_role_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_role_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 100
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_user_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_user_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 2
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_white_list_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_white_list_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_task_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_task_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_sys_setting_id'' )
THEN
CREATE SEQUENCE "public"."sq_sys_setting_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1000
CACHE 1;
END IF;

IF NOT EXISTS(SELECT 1 FROM information_schema.sequences WHERE sequence_schema = ''public'' AND sequence_name = ''sq_sys_plugin_logo_id'')
THEN
    CREATE SEQUENCE "public"."sq_sys_plugin_logo_id"
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1000
    CACHE 1;
END IF;

RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT create_sequences_not_exists();
DROP FUNCTION create_sequences_not_exists;


CREATE TABLE IF NOT EXISTS "public"."ops_az" (
                                                 "az_id" int8 NOT NULL PRIMARY KEY,
                                                 "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "priority" int2,
    "address" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_az"."az_id" IS 'az主键';
COMMENT ON COLUMN "public"."ops_az"."name" IS 'az名称';
COMMENT ON COLUMN "public"."ops_az"."priority" IS '优先级';
COMMENT ON COLUMN "public"."ops_az"."address" IS '实际地理位置';
COMMENT ON COLUMN "public"."ops_az"."remark" IS '描述';
COMMENT ON COLUMN "public"."ops_az"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_az"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_az"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_az"."update_time" IS '更新时间';

INSERT INTO "public"."ops_az"("az_id", "name", "priority", "address", "remark", "create_by", "create_time", "update_by", "update_time") VALUES (1639451067550920705, 'az1', NULL, '中国', NULL, 'admin', '2023-03-25 10:16:08.69', 'admin', '2023-03-25 10:16:08.69')  ON DUPLICATE KEY
UPDATE NOTHING;;

-- ----------------------------
-- Table structure for ops_check
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."ops_check" (
    "check_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "check_res" text COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_check"."remark" IS '描述';
COMMENT ON COLUMN "public"."ops_check"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_check"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_check"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_check"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_cluster
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."ops_cluster" (
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "version" varchar(255) COLLATE "pg_catalog"."default",
    "version_num" varchar(255) COLLATE "pg_catalog"."default",
    "install_mode" varchar(64) COLLATE "pg_catalog"."default",
    "deploy_type" varchar(64) COLLATE "pg_catalog"."default",
    "cluster_name" varchar(255) COLLATE "pg_catalog"."default",
    "install_package_path" varchar(255) COLLATE "pg_catalog"."default",
    "database_password" varchar(255) COLLATE "pg_catalog"."default",
    "database_username" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "install_path" varchar(255) COLLATE "pg_catalog"."default",
    "log_path" varchar(255) COLLATE "pg_catalog"."default",
    "tmp_path" varchar(255) COLLATE "pg_catalog"."default",
    "om_tools_path" varchar(255) COLLATE "pg_catalog"."default",
    "core_path" varchar(255) COLLATE "pg_catalog"."default",
    "port" varchar(255) COLLATE "pg_catalog"."default",
    "enable_dcf" int2
    )
;
COMMENT ON COLUMN "public"."ops_cluster"."cluster_id" IS '集群标识';
COMMENT ON COLUMN "public"."ops_cluster"."version" IS '版本';
COMMENT ON COLUMN "public"."ops_cluster"."version_num" IS '版本号';
COMMENT ON COLUMN "public"."ops_cluster"."install_mode" IS '安装模式';
COMMENT ON COLUMN "public"."ops_cluster"."deploy_type" IS '部署方式';
COMMENT ON COLUMN "public"."ops_cluster"."cluster_name" IS '集群名称';
COMMENT ON COLUMN "public"."ops_cluster"."install_package_path" IS '安装包路径';
COMMENT ON COLUMN "public"."ops_cluster"."database_password" IS '数据库密码';
COMMENT ON COLUMN "public"."ops_cluster"."database_username" IS '数据库用户名';
COMMENT ON COLUMN "public"."ops_cluster"."remark" IS '描述';
COMMENT ON COLUMN "public"."ops_cluster"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_cluster"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_cluster"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_cluster"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."ops_cluster"."install_path" IS '企业版安装路径';
COMMENT ON COLUMN "public"."ops_cluster"."log_path" IS '企业版日志路径';
COMMENT ON COLUMN "public"."ops_cluster"."tmp_path" IS '企业版临时文件路径';
COMMENT ON COLUMN "public"."ops_cluster"."om_tools_path" IS '企业版om路径';
COMMENT ON COLUMN "public"."ops_cluster"."core_path" IS '企业版核心路径';
COMMENT ON COLUMN "public"."ops_cluster"."port" IS '企业版端口';
COMMENT ON COLUMN "public"."ops_cluster"."enable_dcf" IS '是否开启DCF  0否1是';

-- ----------------------------
-- Table structure for ops_cluster_node
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."ops_cluster_node" (
                                                           "cluster_node_id" int8 NOT NULL PRIMARY KEY,
                                                           "cluster_role" varchar(255) COLLATE "pg_catalog"."default",
    "host_id" int8,
    "install_user_id" int8,
    "install_path" varchar(255) COLLATE "pg_catalog"."default",
    "data_path" varchar(255) COLLATE "pg_catalog"."default",
    "pkg_path" varchar(255) COLLATE "pg_catalog"."default",
    "install_demo_database" int2,
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "is_install_cm" int2,
    "is_cm_master" int2,
    "cm_data_path" varchar(255) COLLATE "pg_catalog"."default",
    "cm_port" varchar(255) COLLATE "pg_catalog"."default",
    "xlog_path" varchar(255) COLLATE "pg_catalog"."default",
    "is_enable_dss" int2,
    "dss_data_lun_link_path" varchar(255) COLLATE "pg_catalog"."default",
    "xlog_lun_link_path" varchar(255) COLLATE "pg_catalog"."default",
    "cm_voting_lun_link_path" varchar(255) COLLATE "pg_catalog"."default",
    "cm_sharing_lun_link_path" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;

CREATE OR REPLACE FUNCTION add_lun_link_path() RETURNS integer AS '
BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_cluster_node'' AND COLUMN_NAME =
''is_enable_dss'' ) = 0
THEN
ALTER TABLE ops_cluster_node ADD COLUMN is_enable_dss int2;
ALTER TABLE ops_cluster_node ADD COLUMN dss_data_lun_link_path VARCHAR(255);
ALTER TABLE ops_cluster_node ADD COLUMN xlog_lun_link_path VARCHAR(255);
ALTER TABLE ops_cluster_node ADD COLUMN cm_voting_lun_link_path VARCHAR(255);
ALTER TABLE ops_cluster_node ADD COLUMN cm_sharing_lun_link_path VARCHAR(255);
END IF;
RETURN 0;
 END;'
 LANGUAGE plpgsql;

SELECT add_lun_link_path();

DROP FUNCTION add_lun_link_path;

COMMENT ON COLUMN "public"."ops_cluster_node"."cluster_role" IS '集群角色';
COMMENT ON COLUMN "public"."ops_cluster_node"."host_id" IS '主机ID';
COMMENT ON COLUMN "public"."ops_cluster_node"."install_user_id" IS '安装用户ID';
COMMENT ON COLUMN "public"."ops_cluster_node"."install_path" IS '安装路径';
COMMENT ON COLUMN "public"."ops_cluster_node"."data_path" IS '数据目录';
COMMENT ON COLUMN "public"."ops_cluster_node"."pkg_path" IS '安装包目录';
COMMENT ON COLUMN "public"."ops_cluster_node"."install_demo_database" IS '是否安装示例数据库';
COMMENT ON COLUMN "public"."ops_cluster_node"."cluster_id" IS '集群ID';
COMMENT
ON COLUMN "public"."ops_cluster_node"."is_install_cm" IS '是否安装 CM';
COMMENT
ON COLUMN "public"."ops_cluster_node"."is_cm_master" IS '是否CM master';
COMMENT
ON COLUMN "public"."ops_cluster_node"."cm_data_path" IS 'cm数据路径';
COMMENT
ON COLUMN "public"."ops_cluster_node"."cm_port" IS 'cm端口';
COMMENT
ON COLUMN "public"."ops_cluster_node"."xlog_path" IS 'xlog路径';
COMMENT
ON COLUMN "public"."ops_cluster_node"."is_enable_dss" IS '是否开启Dss';
COMMENT
ON COLUMN "public"."ops_cluster_node"."dss_data_lun_link_path" IS 'data卷软链接';
COMMENT
ON COLUMN "public"."ops_cluster_node"."xlog_lun_link_path" IS 'xlog卷软链接';
COMMENT
ON COLUMN "public"."ops_cluster_node"."cm_voting_lun_link_path" IS 'cm投票卷软链接';
COMMENT
ON COLUMN "public"."ops_cluster_node"."cm_sharing_lun_link_path" IS 'cm共享卷软链接';
COMMENT
ON COLUMN "public"."ops_cluster_node"."remark" IS '描述';
COMMENT
ON COLUMN "public"."ops_cluster_node"."create_by" IS '创建者';
COMMENT
ON COLUMN "public"."ops_cluster_node"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."ops_cluster_node"."update_by" IS '更新者';
COMMENT
ON COLUMN "public"."ops_cluster_node"."update_time" IS '更新时间';

ALTER TABLE "public"."ops_cluster_node"
ALTER
COLUMN "cluster_node_id" TYPE varchar(255) USING "cluster_node_id"::varchar(255);



-- ----------------------------
-- Table structure for ops_cluster_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."ops_cluster_task" (
                                                           "cluster_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "host_id" varchar(255) COLLATE "pg_catalog"."default",
    "host_user_id" varchar(255) COLLATE "pg_catalog"."default",
    "os" varchar(255) COLLATE "pg_catalog"."default",
    "cpu_arch" varchar(255) COLLATE "pg_catalog"."default",
    "version" varchar(255) COLLATE "pg_catalog"."default",
    "version_num" varchar(255) COLLATE "pg_catalog"."default",
    "package_name" varchar(255) COLLATE "pg_catalog"."default",
    "package_id" varchar(255) COLLATE "pg_catalog"."default",
    "cluster_name" varchar(255) COLLATE "pg_catalog"."default",
    "database_username" varchar(255) COLLATE "pg_catalog"."default",
    "database_password" varchar(255) COLLATE "pg_catalog"."default",
    "database_port" int4 DEFAULT 5432,
    "install_package_path" varchar(255) COLLATE "pg_catalog"."default",
    "install_path" varchar(255) COLLATE "pg_catalog"."default",
    "log_path" varchar(255) COLLATE "pg_catalog"."default",
    "tmp_path" varchar(255) COLLATE "pg_catalog"."default",
    "om_tools_path" varchar(255) COLLATE "pg_catalog"."default",
    "core_path" varchar(255) COLLATE "pg_catalog"."default",
    "env_path" varchar(255) COLLATE "pg_catalog"."default",
    "enable_cm_tool" int4 DEFAULT 0,
    "enable_dcf" int4 DEFAULT 0,
    "enable_generate_environment_variable_file" int4 DEFAULT 0,
    "xml_config_path" varchar(255) COLLATE "pg_catalog"."default",
    "deploy_type" varchar(255) COLLATE "pg_catalog"."default",
    "cluster_node_num" int4 DEFAULT 1,
    "status" varchar(255) COLLATE "pg_catalog"."default",
    "execute_time" timestamp(6),
    "env_check_result" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_cluster_task"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "public"."ops_cluster_task"."host_id" IS '服务器IP';
COMMENT ON COLUMN "public"."ops_cluster_task"."host_user_id" IS '安装服务器用户ID';
COMMENT ON COLUMN "public"."ops_cluster_task"."os" IS '操作系统名称';
COMMENT ON COLUMN "public"."ops_cluster_task"."cpu_arch" IS 'CPU架构';
COMMENT ON COLUMN "public"."ops_cluster_task"."version" IS '数据库版本';
COMMENT ON COLUMN "public"."ops_cluster_task"."version_num" IS '数据库版本号';
COMMENT ON COLUMN "public"."ops_cluster_task"."package_name" IS '安装包名称';
COMMENT ON COLUMN "public"."ops_cluster_task"."package_id" IS '安装包ID';
COMMENT ON COLUMN "public"."ops_cluster_task"."cluster_name" IS '集群名称';
COMMENT ON COLUMN "public"."ops_cluster_task"."database_username" IS '数据库用户名称';
COMMENT ON COLUMN "public"."ops_cluster_task"."database_password" IS '数据库密码';
COMMENT ON COLUMN "public"."ops_cluster_task"."database_port" IS '数据库端口';
COMMENT ON COLUMN "public"."ops_cluster_task"."install_package_path" IS '安装包路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."install_path" IS '安装路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."log_path" IS 'log路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."tmp_path" IS 'tmp路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."om_tools_path" IS 'om工具安装路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."core_path" IS 'core 路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."env_path" IS '环境变量路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."enable_cm_tool" IS '是否安装CM工具';
COMMENT ON COLUMN "public"."ops_cluster_task"."enable_dcf" IS '是否dcf';
COMMENT ON COLUMN "public"."ops_cluster_task"."enable_generate_environment_variable_file" IS '是否分离环境变量文件';
COMMENT ON COLUMN "public"."ops_cluster_task"."xml_config_path" IS 'xml安装配置路径';
COMMENT ON COLUMN "public"."ops_cluster_task"."deploy_type" IS '集群部署类型';
COMMENT ON COLUMN "public"."ops_cluster_task"."cluster_node_num" IS '集群节点数量';
COMMENT ON COLUMN "public"."ops_cluster_task"."status" IS '安装任务执行状态';
COMMENT ON COLUMN "public"."ops_cluster_task"."execute_time" IS '任务执行时间';
COMMENT ON COLUMN "public"."ops_cluster_task"."env_check_result" IS '环境检查结果';
COMMENT ON COLUMN "public"."ops_cluster_task"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_cluster_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_cluster_task"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_cluster_task"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."ops_cluster_task" IS 'openGauss集群安装任务表';



-- ----------------------------
-- Table structure for ops_cluster_task_node
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."ops_cluster_task_node" (
                                                                "cluster_node_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "host_id" varchar(255) COLLATE "pg_catalog"."default",
    "host_user_id" varchar(255) COLLATE "pg_catalog"."default",
    "node_type" varchar(255) COLLATE "pg_catalog"."default",
    "data_path" varchar(255) COLLATE "pg_catalog"."default",
    "az_owner" varchar(255) COLLATE "pg_catalog"."default",
    "az_priority" varchar(255) COLLATE "pg_catalog"."default",
    "env_check_result" varchar(255) COLLATE "pg_catalog"."default",
    "env_check_detail" text COLLATE "pg_catalog"."default",
    "is_cm_master" int4 DEFAULT 0,
    "cm_data_path" varchar(255) COLLATE "pg_catalog"."default",
    "cm_port" int4 DEFAULT 0,
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;

COMMENT ON COLUMN "public"."ops_cluster_task_node"."cluster_node_id" IS '集群节点ID';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."host_id" IS '节点服务器Id';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."host_user_id" IS '节点用户ID';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."node_type" IS '节点类型';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."data_path" IS '数据路径';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."az_owner" IS '所属AZ';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."az_priority" IS 'AZ优先级';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."env_check_result" IS '环境检查结果';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."env_check_detail" IS '环境检查详情';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."is_cm_master" IS '是否CM主节点';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."cm_data_path" IS 'CM数据路径';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."cm_port" IS 'CM端口';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_cluster_task_node"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."ops_cluster_task_node" IS '集群安装任务节点信息';

-- ----------------------------
-- Table structure for ops_encryption
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."ops_encryption"
(
    "encryption_id" varchar
(
    255
) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "remark" varchar
(
    255
) COLLATE "pg_catalog"."default",
    "create_by" varchar
(
    64
) COLLATE "pg_catalog"."default",
    "create_time" timestamp
(
    6
),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "encryption_key" varchar(255) COLLATE "pg_catalog"."default",
    "public_key" varchar(1024) COLLATE "pg_catalog"."default",
    "private_key" varchar(1024) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."ops_encryption"."remark" IS '描述';
COMMENT ON COLUMN "public"."ops_encryption"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_encryption"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_encryption"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_encryption"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_host
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."ops_host" (
                                                   "host_id" int8 NOT NULL PRIMARY KEY,
                                                   "hostname" varchar(255) COLLATE "pg_catalog"."default",
    "private_ip" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "public_ip" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "port" int8 NOT NULL,
    "az_id" int8,
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_host"."host_id" IS '主机id';
COMMENT ON COLUMN "public"."ops_host"."hostname" IS '主机名';
COMMENT ON COLUMN "public"."ops_host"."private_ip" IS '内网ip地址';
COMMENT ON COLUMN "public"."ops_host"."public_ip" IS '公网ip地址';
COMMENT ON COLUMN "public"."ops_host"."port" IS '端口';
COMMENT ON COLUMN "public"."ops_host"."az_id" IS 'az_id';
COMMENT ON COLUMN "public"."ops_host"."remark" IS '描述';
COMMENT ON COLUMN "public"."ops_host"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_host"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_host"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_host"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_host_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."ops_host_user" (
                                                        "host_user_id" int8 NOT NULL PRIMARY KEY,
                                                        "username" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "password" varchar(255) COLLATE "pg_catalog"."default",
    "host_id" int8 NOT NULL,
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_host_user"."host_user_id" IS '主机用户主键';
COMMENT ON COLUMN "public"."ops_host_user"."username" IS '用户名';
COMMENT ON COLUMN "public"."ops_host_user"."password" IS '密码';
COMMENT ON COLUMN "public"."ops_host_user"."host_id" IS '所属主机id';
COMMENT ON COLUMN "public"."ops_host_user"."remark" IS '备注';
COMMENT ON COLUMN "public"."ops_host_user"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_host_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_host_user"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_host_user"."update_time" IS '更新时间';

CREATE TABLE IF NOT EXISTS "public"."ops_host_tag" (
    "host_tag_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "name" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_host_tag"."name" IS '标签名';
COMMENT ON COLUMN "public"."ops_host_tag"."remark" IS '备注';
COMMENT ON COLUMN "public"."ops_host_tag"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_host_tag"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_host_tag"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_host_tag"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_host_tag_rel
-- ----------------------------
CREATE TABLE IF NOT EXISTS  "public"."ops_host_tag_rel" (
    "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "host_id" varchar(255) COLLATE "pg_catalog"."default",
    "tag_id" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_host_tag_rel"."host_id" IS '主机ID';
COMMENT ON COLUMN "public"."ops_host_tag_rel"."tag_id" IS 'tag ID';
COMMENT ON COLUMN "public"."ops_host_tag_rel"."remark" IS '描述';
COMMENT ON COLUMN "public"."ops_host_tag_rel"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_host_tag_rel"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_host_tag_rel"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_host_tag_rel"."update_time" IS '更新时间';


-- ----------------------------
-- Table structure for ops_package_path_dict
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."ops_package_path_dict" (
                                                                "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "os" varchar(255) COLLATE "pg_catalog"."default",
    "cpu_arch" varchar(255) COLLATE "pg_catalog"."default",
    "version" varchar(255) COLLATE "pg_catalog"."default",
    "url_path" varchar(255) COLLATE "pg_catalog"."default",
    "package_name_tmp" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."ops_package_path_dict"."os" IS '操作系统名称';
COMMENT ON COLUMN "public"."ops_package_path_dict"."cpu_arch" IS 'CPU架构';
COMMENT ON COLUMN "public"."ops_package_path_dict"."version" IS 'openGauss版本';
COMMENT ON COLUMN "public"."ops_package_path_dict"."url_path" IS 'openGauss包URI路径';
COMMENT ON COLUMN "public"."ops_package_path_dict"."package_name_tmp" IS 'openGauss包名称模版';
COMMENT ON COLUMN "public"."ops_package_path_dict"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_package_path_dict"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_package_path_dict"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_package_path_dict"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."ops_package_path_dict" IS 'openGauss包名称字典表';

CREATE OR REPLACE FUNCTION add_host_user_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_host_user'' AND COLUMN_NAME = ''sudo'' ) = 0
THEN
ALTER TABLE ops_host_user ADD COLUMN sudo int2 DEFAULT 0;
COMMENT ON COLUMN "public"."ops_host_user"."sudo" IS ''管理员'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_host_user_field_func();

DROP FUNCTION add_host_user_field_func;

-- ----------------------------
-- Table structure for sys_log_config
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_log_config" (
    "key" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "value" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
    "id" int4 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_log_config_id'::regclass)
    )
;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_menu" (
    "menu_id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_menu_id'::regclass),
    "menu_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "parent_id" int8,
    "order_num" int4,
    "path" varchar(200) COLLATE "pg_catalog"."default",
    "component" varchar(255) COLLATE "pg_catalog"."default",
    "query" varchar(255) COLLATE "pg_catalog"."default",
    "is_frame" int4 DEFAULT 1,
    "is_cache" int4 DEFAULT 0,
    "menu_type" char(1) COLLATE "pg_catalog"."default",
    "visible" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "status" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "perms" varchar(100) COLLATE "pg_catalog"."default",
    "icon" varchar(100) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "remark" varchar(500) COLLATE "pg_catalog"."default",
    "open_way" int4 DEFAULT 1,
    "plugin_id" varchar(100) COLLATE "pg_catalog"."default",
    "open_position" int2 DEFAULT 1,
    "query_template" varchar(100) COLLATE "pg_catalog"."default",
    "plugin_theme" varchar(50) COLLATE "pg_catalog"."default",
    "menu_classify" int2 DEFAULT 1,
    "menu_en_name" varchar(50) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."sys_menu"."menu_id" IS '菜单ID';
COMMENT ON COLUMN "public"."sys_menu"."menu_name" IS '菜单名称';
COMMENT ON COLUMN "public"."sys_menu"."parent_id" IS '父菜单ID';
COMMENT ON COLUMN "public"."sys_menu"."order_num" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_menu"."path" IS '路由地址';
COMMENT ON COLUMN "public"."sys_menu"."component" IS '组件路径';
COMMENT ON COLUMN "public"."sys_menu"."query" IS '路由参数';
COMMENT ON COLUMN "public"."sys_menu"."is_frame" IS '是否为外链（0是 1否）';
COMMENT ON COLUMN "public"."sys_menu"."is_cache" IS '是否缓存（0缓存 1不缓存）';
COMMENT ON COLUMN "public"."sys_menu"."menu_type" IS '菜单类型（M目录 C菜单 F按钮）';
COMMENT ON COLUMN "public"."sys_menu"."visible" IS '菜单状态（0显示 1隐藏）';
COMMENT ON COLUMN "public"."sys_menu"."status" IS '菜单状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_menu"."perms" IS '权限标识';
COMMENT ON COLUMN "public"."sys_menu"."icon" IS '菜单图标';
COMMENT ON COLUMN "public"."sys_menu"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_menu"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_menu"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_menu"."remark" IS '备注';
COMMENT ON COLUMN "public"."sys_menu"."open_way" IS '打开方式；1：页面打开；2：弹窗打开';
COMMENT ON COLUMN "public"."sys_menu"."plugin_id" IS '所属插件ID';
COMMENT ON COLUMN "public"."sys_menu"."open_position" IS '打开位置；1：左边菜单；2：首页实例';
COMMENT ON COLUMN "public"."sys_menu"."query_template" IS 'query参数模板';
COMMENT ON COLUMN "public"."sys_menu"."plugin_theme" IS '插件主题';
COMMENT ON COLUMN "public"."sys_menu"."menu_classify" IS '菜单分类；1：业务菜单；2：平台菜单';
COMMENT ON COLUMN "public"."sys_menu"."menu_en_name" IS '英文名称';
COMMENT ON TABLE "public"."sys_menu" IS '菜单权限表';

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_oper_log" (
    "oper_id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_log_id'::regclass),
    "title" varchar(50) COLLATE "pg_catalog"."default",
    "business_type" int4,
    "method" varchar(100) COLLATE "pg_catalog"."default",
    "request_method" varchar(10) COLLATE "pg_catalog"."default",
    "operator_type" int4,
    "oper_name" varchar(50) COLLATE "pg_catalog"."default",
    "oper_url" varchar(255) COLLATE "pg_catalog"."default",
    "oper_ip" varchar(128) COLLATE "pg_catalog"."default",
    "oper_location" varchar(255) COLLATE "pg_catalog"."default",
    "oper_param" varchar(2000) COLLATE "pg_catalog"."default",
    "json_result" varchar(2000) COLLATE "pg_catalog"."default",
    "status" int4,
    "error_msg" varchar(2000) COLLATE "pg_catalog"."default",
    "oper_time" timestamp(6)
    )
;
COMMENT ON COLUMN "public"."sys_oper_log"."oper_id" IS '日志主键';
COMMENT ON COLUMN "public"."sys_oper_log"."title" IS '模块标题';
COMMENT ON COLUMN "public"."sys_oper_log"."business_type" IS '业务类型（0其它 1新增 2修改 3删除）';
COMMENT ON COLUMN "public"."sys_oper_log"."method" IS '方法名称';
COMMENT ON COLUMN "public"."sys_oper_log"."request_method" IS '请求方式';
COMMENT ON COLUMN "public"."sys_oper_log"."operator_type" IS '操作类别（0其它 1后台用户 2手机端用户）';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_name" IS '操作人员';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_url" IS '请求URL';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_ip" IS '主机地址';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_location" IS '操作地点';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_param" IS '请求参数';
COMMENT ON COLUMN "public"."sys_oper_log"."json_result" IS '返回参数';
COMMENT ON COLUMN "public"."sys_oper_log"."status" IS '操作状态（0正常 1异常）';
COMMENT ON COLUMN "public"."sys_oper_log"."error_msg" IS '错误消息';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_time" IS '操作时间';
COMMENT ON TABLE "public"."sys_oper_log" IS '操作日志记录';

-- ----------------------------
-- Table structure for sys_plugin_config
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_plugin_config" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_plugin_config_id'::regclass),
    "plugin_id" varchar(100) COLLATE "pg_catalog"."default",
    "config_json" varchar(1000) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."sys_plugin_config"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_plugin_config"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "public"."sys_plugin_config"."config_json" IS '配置参数';
COMMENT ON TABLE "public"."sys_plugin_config" IS '插件配置结构表';

-- ----------------------------
-- Table structure for sys_plugin_config_data
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_plugin_config_data" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_plugin_config_data_id'::regclass),
    "plugin_id" varchar(100) COLLATE "pg_catalog"."default",
    "config_data" varchar(1000) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."sys_plugin_config_data"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_plugin_config_data"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "public"."sys_plugin_config_data"."config_data" IS '配置数据';
COMMENT ON TABLE "public"."sys_plugin_config_data" IS '插件配置数据';

-- ----------------------------
-- Table structure for sys_plugins
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_plugins" (
    "id" int4 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_plugin_id'::regclass),
    "plugin_id" varchar(100) COLLATE "pg_catalog"."default",
    "bootstrap_class" varchar(255) COLLATE "pg_catalog"."default",
    "plugin_desc" text COLLATE "pg_catalog"."default",
    "logo_path" varchar(255) COLLATE "pg_catalog"."default",
    "plugin_type" int4,
    "plugin_version" varchar(50) COLLATE "pg_catalog"."default",
    "plugin_provider" varchar(50) COLLATE "pg_catalog"."default",
    "plugin_status" int2 DEFAULT 1,
    "is_need_configured" int2,
    "theme" varchar(50) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."sys_plugins"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_plugins"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "public"."sys_plugins"."bootstrap_class" IS '启动类';
COMMENT ON COLUMN "public"."sys_plugins"."plugin_desc" IS '插件描述';
COMMENT ON COLUMN "public"."sys_plugins"."logo_path" IS '插件Logo路径';
COMMENT ON COLUMN "public"."sys_plugins"."plugin_type" IS '插件类别';
COMMENT ON COLUMN "public"."sys_plugins"."plugin_version" IS '插件版本';
COMMENT ON COLUMN "public"."sys_plugins"."plugin_provider" IS '插件提供者';
COMMENT ON COLUMN "public"."sys_plugins"."plugin_status" IS '插件状态：1：启动；2：停用';
COMMENT ON COLUMN "public"."sys_plugins"."is_need_configured" IS '是否需要配置数据';
COMMENT ON COLUMN "public"."sys_plugins"."theme" IS '主题';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_role" (
    "role_id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_role_id'::regclass),
    "role_name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
    "role_key" varchar(100) COLLATE "pg_catalog"."default",
    "role_sort" int4,
    "data_scope" char(1) COLLATE "pg_catalog"."default",
    "menu_check_strictly" int2 DEFAULT 1,
    "dept_check_strictly" int2,
    "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "del_flag" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "remark" varchar(500) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."sys_role"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."sys_role"."role_name" IS '角色名称';
COMMENT ON COLUMN "public"."sys_role"."role_key" IS '角色权限字符串';
COMMENT ON COLUMN "public"."sys_role"."role_sort" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_role"."data_scope" IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';
COMMENT ON COLUMN "public"."sys_role"."menu_check_strictly" IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN "public"."sys_role"."dept_check_strictly" IS '医院树选择项是否关联显示';
COMMENT ON COLUMN "public"."sys_role"."status" IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_role"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "public"."sys_role"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_role"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_role"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_role" IS '角色信息表';

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_role_menu" (
                                                        "role_id" int8 NOT NULL,
                                                        "menu_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."sys_role_menu"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."sys_role_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE "public"."sys_role_menu" IS '角色和菜单关联表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_user" (
    "user_id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_user_id'::regclass),
    "user_name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
    "nick_name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
    "user_type" varchar(2) COLLATE "pg_catalog"."default",
    "email" varchar(50) COLLATE "pg_catalog"."default",
    "phonenumber" varchar(11) COLLATE "pg_catalog"."default",
    "sex" char(1) COLLATE "pg_catalog"."default",
    "avatar" varchar(100) COLLATE "pg_catalog"."default",
    "password" varchar(100) COLLATE "pg_catalog"."default",
    "status" char(1) COLLATE "pg_catalog"."default",
    "del_flag" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "login_ip" varchar(128) COLLATE "pg_catalog"."default",
    "login_date" timestamp(6),
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "remark" varchar(500) COLLATE "pg_catalog"."default"
    )
;
COMMENT ON COLUMN "public"."sys_user"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_user"."user_name" IS '用户账号';
COMMENT ON COLUMN "public"."sys_user"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "public"."sys_user"."user_type" IS '用户类型（00系统用户）';
COMMENT ON COLUMN "public"."sys_user"."email" IS '用户邮箱';
COMMENT ON COLUMN "public"."sys_user"."phonenumber" IS '手机号码';
COMMENT ON COLUMN "public"."sys_user"."sex" IS '用户性别（0男 1女 2未知）';
COMMENT ON COLUMN "public"."sys_user"."avatar" IS '头像地址';
COMMENT ON COLUMN "public"."sys_user"."password" IS '密码';
COMMENT ON COLUMN "public"."sys_user"."status" IS '帐号状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_user"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "public"."sys_user"."login_ip" IS '最后登录IP';
COMMENT ON COLUMN "public"."sys_user"."login_date" IS '最后登录时间';
COMMENT ON COLUMN "public"."sys_user"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_user"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_user"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_user"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_user" IS '用户信息表';

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_user_role" (
    "user_id" int8 NOT NULL,
    "role_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."sys_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_user_role"."role_id" IS '角色ID';
COMMENT ON TABLE "public"."sys_user_role" IS '用户和角色关联表';

-- ----------------------------
-- Table structure for sys_white_list
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."sys_white_list" (
    "id" int8 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_white_list_id'::regclass),
    "title" varchar(255) COLLATE "pg_catalog"."default",
    "ip_list" varchar(800) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)
    )
;
COMMENT
ON COLUMN "public"."sys_white_list"."id" IS '主键ID';
COMMENT
ON COLUMN "public"."sys_white_list"."title" IS '白名单名称';
COMMENT
ON COLUMN "public"."sys_white_list"."ip_list" IS 'ip列表，逗号隔开';
COMMENT
ON COLUMN "public"."sys_white_list"."create_time" IS '创建时间';


CREATE TABLE IF NOT EXISTS "public"."ops_package_manager"(
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
    );


-- ----------------------------
-- Table structure for ops_cluster_operate_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."ops_cluster_operate_log" (
    "operate_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "cluster_node_id" varchar(255) COLLATE "pg_catalog"."default",
    "operate_type" varchar(255) COLLATE "pg_catalog"."default",
    "operate_log" text COLLATE "pg_catalog"."default",
    "operate_time"  timestamp(6),
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    )
;

COMMENT ON COLUMN "public"."ops_cluster_operate_log"."operate_id" IS '操作记录Id';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."cluster_node_id" IS '集群节点ID';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."operate_type" IS '操作类型';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."operate_log" IS '操作记录日志';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."operate_time" IS '操作时间';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."ops_cluster_operate_log"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."ops_cluster_operate_log" IS '集群安装任务操作日志';

CREATE TABLE IF NOT EXISTS "public"."sys_task" (
 "id" int8 NOT NULL DEFAULT nextval('sq_sys_task_id'::regclass),
 "task_name" varchar(255) COLLATE "pg_catalog"."default",
 "task_type" int4,
 "exec_status" int4,
 "exec_params" varchar(512) COLLATE "pg_catalog"."default",
 "exec_progress" float4,
 "exec_host_id" int8,
 "create_time" timestamp(6),
 "finish_time" timestamp(6),
 "exec_time" timestamp(6),
 "plugin_id" varchar(100) COLLATE "pg_catalog"."default",
 "sub_task_count" int8,
 CONSTRAINT "sys_task_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."sys_task"."id" IS '主键ID';

COMMENT ON COLUMN "public"."sys_task"."task_name" IS '任务名称';

COMMENT ON COLUMN "public"."sys_task"."task_type" IS '操作类别（1数据迁移）';

COMMENT ON COLUMN "public"."sys_task"."exec_status" IS '执行状态（0：未执行；1：执行中；2：已完成；3：执行失败）';

COMMENT ON COLUMN "public"."sys_task"."exec_params" IS '执行参数';

COMMENT ON COLUMN "public"."sys_task"."exec_progress" IS '执行进度';

COMMENT ON COLUMN "public"."sys_task"."exec_host_id" IS '执行物理机ID';

COMMENT ON COLUMN "public"."sys_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."sys_task"."finish_time" IS '完成时间';

COMMENT ON COLUMN "public"."sys_task"."exec_time" IS '执行时间';

COMMENT ON COLUMN "public"."sys_task"."plugin_id" IS '插件ID';

COMMENT ON COLUMN "public"."sys_task"."sub_task_count" IS '子任务数量';

COMMENT ON TABLE "public"."sys_task" IS '平台任务表';


CREATE OR REPLACE FUNCTION add_pkg_name() RETURNS integer AS '
BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_package_manager'' AND COLUMN_NAME = ''name'' ) = 0
THEN
ALTER TABLE ops_package_manager ADD COLUMN name VARCHAR(255);
END IF;
RETURN 0;
 END;'
 LANGUAGE plpgsql;

SELECT add_pkg_name();

DROP FUNCTION add_pkg_name;


CREATE
OR REPLACE FUNCTION add_field_ops_package_manager() RETURNS integer AS '
BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_package_manager'' AND (COLUMN_NAME = ''package_path'' OR COLUMN_NAME = ''type'') ) = 0
THEN
ALTER TABLE ops_package_manager ADD COLUMN "package_path" text;
ALTER TABLE ops_package_manager ADD COLUMN "type" varchar(255);
UPDATE ops_package_manager set type = ''openGauss'';
END IF;
RETURN 0;
 END;'
LANGUAGE plpgsql;

SELECT add_field_ops_package_manager();

DROP FUNCTION add_field_ops_package_manager;

INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604735048472268811', 'centos', 'x86_64', 'MINIMAL_LIST', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86/openGauss-3.1.1-CentOS-64bit.tar.bz2', NULL,
        'admin', '2022-12-19 15:07:04.749', 'admin', '2022-12-19 15:07:04.749', 'openGauss', 'offical_3.1.1_mini_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605498723119550126', 'centos', 'x86_64', 'ENTERPRISE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86/openGauss-3.1.1-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2022-12-21 17:41:38.992', 'admin', '2022-12-21 17:41:38.992', 'openGauss', 'offical_3.1.1_enterprise_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605498799611072134', 'centos', 'x86_64', 'LITE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86/openGauss-Lite-3.1.1-CentOS-x86_64.tar.gz', NULL,
        'admin', '2022-12-21 17:41:57.21', 'admin', '2022-12-21 17:41:57.21', 'openGauss', 'offical_3.1.1_lite_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604855665214541845', 'openEuler', 'aarch64', 'ENTERPRISE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/arm/openGauss-3.1.1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2022-12-19 23:06:22.02', 'admin', '2022-12-19 23:06:22.02', 'openGauss', 'offical_3.1.1_enterprise_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604857993288142150', 'openEuler', 'aarch64', 'LITE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/arm/openGauss-Lite-3.1.1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2022-12-19 23:15:37.077', 'admin', '2022-12-19 23:15:37.077', 'openGauss', 'offical_3.1.1_lite_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627277907599162', 'openEuler', 'aarch64', 'MINIMAL_LIST', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/arm/openGauss-3.1.1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2022-12-24 20:26:07.402', 'admin', '2022-12-24 20:26:07.402', 'openGauss', 'offical_3.1.1_mini_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627421516374177', 'openEuler', 'x86_64', 'ENTERPRISE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86_openEuler/openGauss-3.1.1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2022-12-24 20:26:41.641', 'admin', '2022-12-24 20:26:41.641', 'openGauss', 'offical_3.1.1_enterprise_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627515523309180', 'openEuler', 'x86_64', 'MINIMAL_LIST', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86_openEuler/openGauss-3.1.1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2022-12-24 20:27:04.054', 'admin', '2022-12-24 20:27:04.054', 'openGauss', 'offical_3.1.1_mini_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627585895342192', 'openEuler', 'x86_64', 'LITE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86_openEuler/openGauss-Lite-3.1.1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2022-12-24 20:27:20.832', 'admin', '2022-12-24 20:27:20.832', 'openGauss', 'offical_3.1.1_lite_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;

INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605398723119550410', 'centos', 'x86_64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86/openGauss-5.0.0-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2022-12-21 17:41:38.992', 'admin', '2022-12-21 17:41:38.992', 'openGauss', 'offical_5.0.0_enterpirse_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605398799611072511', 'centos', 'x86_64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86/openGauss-Lite-5.0.0-CentOS-x86_64.tar.gz', NULL,
        'admin', '2022-12-21 17:41:57.21', 'admin', '2022-12-21 17:41:57.21', 'openGauss', 'offical_5.0.0_lite_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604335048472268012', 'centos', 'x86_64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86/openGauss-5.0.0-CentOS-64bit.tar.bz2', NULL,
        'admin', '2022-12-19 15:07:04.749', 'admin', '2022-12-19 15:07:04.749', 'openGauss', 'offical_5.0.0_mini_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627421516374013', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler/openGauss-5.0.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2022-12-24 20:26:41.641', 'admin', '2022-12-24 20:26:41.641', 'openGauss', 'offical_5.0.0_enterprise_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627515523309514', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler/openGauss-5.0.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2022-12-24 20:27:04.054', 'admin', '2022-12-24 20:27:04.054', 'openGauss', 'offical_5.0.0_mini_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627585895342015', 'openEuler', 'x86_64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler/openGauss-Lite-5.0.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2022-12-24 20:27:20.832', 'admin', '2022-12-24 20:27:20.832', 'openGauss', 'offical_5.0.0_lite_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604855665214541816', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm/openGauss-5.0.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2022-12-19 23:06:22.02', 'admin', '2022-12-19 23:06:22.02', 'openGauss', 'offical_5.0.0_enterprise_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604857993288142817', 'openEuler', 'aarch64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm/openGauss-Lite-5.0.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2022-12-19 23:15:37.077', 'admin', '2022-12-19 23:15:37.077', 'openGauss', 'offical_5.0.0_lite_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627277907599318', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm/openGauss-5.0.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2022-12-24 20:26:07.402', 'admin', '2022-12-24 20:26:07.402', 'openGauss', 'offical_5.0.0_mini_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;

INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627421516374019', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler_2203/openGauss-5.0.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2022-12-24 20:26:41.641', 'admin', '2022-12-24 20:26:41.641', 'openGauss', 'offical_5.0.0_enterprise_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627515523309520', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler_2203/openGauss-5.0.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2022-12-24 20:27:04.054', 'admin', '2022-12-24 20:27:04.054', 'openGauss', 'offical_5.0.0_mini_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627585895342021', 'openEuler', 'x86_64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler_2203/openGauss-Lite-5.0.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2022-12-24 20:27:20.832', 'admin', '2022-12-24 20:27:20.832', 'openGauss', 'offical_5.0.0_lite_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604855665214541822', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm_2203/openGauss-5.0.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2022-12-19 23:06:22.02', 'admin', '2022-12-19 23:06:22.02', 'openGauss', 'offical_5.0.0_enterprise_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604857993288142823', 'openEuler', 'aarch64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm_2203/openGauss-Lite-5.0.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2022-12-19 23:15:37.077', 'admin', '2022-12-19 23:15:37.077', 'openGauss', 'offical_5.0.0_lite_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627277907599324', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm_2203/openGauss-5.0.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2022-12-24 20:26:07.402', 'admin', '2022-12-24 20:26:07.402', 'openGauss', 'offical_5.0.0_mini_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;

INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813634', 'centos', 'x86_64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-5.1.0-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2023-09-25 17:41:38.992', 'admin', '2023-09-25 17:41:38.992', 'openGauss', 'offical_5.1.0_enterpirse_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813635', 'centos', 'x86_64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-Lite-5.1.0-CentOS-x86_64.tar.gz', NULL,
        'admin', '2023-09-25 17:41:57.21', 'admin', '2023-09-25 17:41:57.21', 'openGauss', 'offical_5.1.0_lite_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813636', 'centos', 'x86_64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-5.1.0-CentOS-64bit.tar.bz2', NULL,
        'admin', '2023-09-25 15:07:04.749', 'admin', '2023-09-25 15:07:04.749', 'openGauss', 'offical_5.1.0_mini_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813637', 'openEuler', 'x86_64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler/openGauss-5.1.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2023-09-25 20:26:41.641', 'admin', '2023-09-25 20:26:41.641', 'openGauss', 'offical_5.1.0_enterprise_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813638', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler/openGauss-5.1.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2023-09-25 20:27:04.054', 'admin', '2023-09-25 20:27:04.054', 'openGauss', 'offical_5.1.0_mini_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813639', 'openEuler', 'x86_64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler/openGauss-Lite-5.1.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2023-09-25 20:27:20.832', 'admin', '2023-09-25 20:27:20.832', 'openGauss', 'offical_5.1.0_lite_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813640', 'openEuler', 'aarch64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm/openGauss-5.1.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2023-09-25 23:06:22.02', 'admin', '2023-09-25 23:06:22.02', 'openGauss', 'offical_5.1.0_enterprise_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813641', 'openEuler', 'aarch64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm/openGauss-Lite-5.1.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2023-09-25 23:15:37.077', 'admin', '2023-09-25 23:15:37.077', 'openGauss', 'offical_5.1.0_lite_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813642', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm/openGauss-5.1.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2023-09-25 20:26:07.402', 'admin', '2023-09-25 20:26:07.402', 'openGauss', 'offical_5.1.0_mini_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;

INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813643', 'openEuler', 'x86_64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler_2203/openGauss-5.1.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2023-09-25 20:26:41.641', 'admin', '2023-09-25 20:26:41.641', 'openGauss', 'offical_5.1.0_enterprise_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813644', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler_2203/openGauss-5.1.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2023-09-25 20:27:04.054', 'admin', '2023-09-25 20:27:04.054', 'openGauss', 'offical_5.1.0_mini_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813645', 'openEuler', 'x86_64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler_2203/openGauss-Lite-5.1.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2023-09-25 20:27:20.832', 'admin', '2023-09-25 20:27:20.832', 'openGauss', 'offical_5.1.0_lite_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813646', 'openEuler', 'aarch64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm_2203/openGauss-5.1.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2023-09-25 23:06:22.02', 'admin', '2023-09-25 23:06:22.02', 'openGauss', 'offical_5.1.0_enterprise_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813647', 'openEuler', 'aarch64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm_2203/openGauss-Lite-5.1.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2023-09-25 23:15:37.077', 'admin', '2023-09-25 23:15:37.077', 'openGauss', 'offical_5.1.0_lite_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813648', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm_2203/openGauss-5.1.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2023-09-25 20:26:07.402', 'admin', '2023-09-25 20:26:07.402', 'openGauss', 'offical_5.1.0_mini_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;

INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813664', 'centos', 'x86_64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86/openGauss-6.0.0-RC1-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2024-04-15 17:41:38.992', 'admin', '2024-04-15 17:41:38.992', 'openGauss', 'offical_6.0.0-RC1_enterpirse_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813665', 'centos', 'x86_64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86/openGauss-Lite-6.0.0-RC1-CentOS-x86_64.tar.gz', NULL,
        'admin', '2024-04-15 17:41:57.21', 'admin', '2024-04-15 17:41:57.21', 'openGauss', 'offical_6.0.0-RC1_lite_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813666', 'centos', 'x86_64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86/openGauss-6.0.0-RC1-CentOS-64bit.tar.bz2', NULL,
        'admin', '2024-04-15 15:07:04.749', 'admin', '2024-04-15 15:07:04.749', 'openGauss', 'offical_6.0.0-RC1_mini_centos_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813667', 'openEuler', 'x86_64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-04-15 20:26:41.641', 'admin', '2024-04-15 20:26:41.641', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813668', 'openEuler', 'x86_64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-04-15 20:27:04.054', 'admin', '2024-04-15 20:27:04.054', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813669', 'openEuler', 'x86_64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler/openGauss-Lite-6.0.0-RC1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-04-15 20:27:20.832', 'admin', '2024-04-15 20:27:20.832', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2003_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813670', 'openEuler', 'aarch64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-04-15 23:06:22.02', 'admin', '2024-04-15 23:06:22.02', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813671', 'openEuler', 'aarch64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm/openGauss-Lite-6.0.0-RC1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-04-15 23:15:37.077', 'admin', '2024-04-15 23:15:37.077', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813672', 'openEuler', 'aarch64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-04-15 20:26:07.402', 'admin', '2024-04-15 20:26:07.402', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2003_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813673', 'openEuler', 'x86_64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler_2203/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-04-15 20:26:41.641', 'admin', '2024-04-15 20:26:41.641', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813674', 'openEuler', 'x86_64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler_2203/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-04-15 20:27:04.054', 'admin', '2024-04-15 20:27:04.054', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813675', 'openEuler', 'x86_64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler_2203/openGauss-Lite-6.0.0-RC1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-04-15 20:27:20.832', 'admin', '2024-04-15 20:27:20.832', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2203_x86_64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813676', 'openEuler', 'aarch64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm_2203/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-04-15 23:06:22.02', 'admin', '2024-04-15 23:06:22.02', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813677', 'openEuler', 'aarch64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm_2203/openGauss-Lite-6.0.0-RC1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-04-15 23:15:37.077', 'admin', '2024-04-15 23:15:37.077', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;
INSERT INTO "public"."ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813678', 'openEuler', 'aarch64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm_2203/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-04-15 20:26:07.402', 'admin', '2024-04-15 20:26:07.402', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2203_aarch64') ON DUPLICATE KEY
UPDATE NOTHING;


INSERT INTO "public"."ops_package_path_dict" VALUES ('01', 'CentOS', 'x86_64', 'LITE', 'x86', 'openGauss-Lite-%s-CentOS-x86_64.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('02', 'CentOS', 'x86_64', 'MINIMAL_LIST', 'x86', 'openGauss-%s-CentOS-64bit.tar.bz2', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('03', 'CentOS', 'x86_64', 'ENTERPRISE', 'x86', 'openGauss-%s-CentOS-64bit-all.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('04', 'openEuler', 'aarch64', 'LITE', 'arm', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('05', 'openEuler', 'aarch64', 'MINIMAL_LIST', 'arm', 'openGauss-%s-openEuler-64bit.tar.bz2', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('06', 'openEuler', 'aarch64', 'ENTERPRISE', 'arm', 'openGauss-%s-openEuler-64bit-all.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('07', 'openEuler', 'x86_64', 'LITE', 'x86_openEuler', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('08', 'openEuler', 'x86_64', 'MINIMAL_LIST', 'x86_openEuler', 'openGauss-%s-openEuler-64bit.tar.bz2', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('09', 'openEuler', 'x86_64', 'ENTERPRISE', 'x86_openEuler', 'openGauss-%s-openEuler-64bit-all.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('10', 'openEuler_2203', 'aarch64', 'LITE', 'arm_2203', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('11', 'openEuler_2203', 'aarch64', 'MINIMAL_LIST', 'arm_2203', 'openGauss-%s-openEuler-64bit.tar.bz2', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('12', 'openEuler_2203', 'aarch64', 'ENTERPRISE', 'arm_2203', 'openGauss-%s-openEuler-64bit-all.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('13', 'openEuler_2203', 'x86_64', 'LITE', 'x86_openEuler_2203', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('14', 'openEuler_2203', 'x86_64', 'MINIMAL_LIST', 'x86_openEuler_2203', 'openGauss-%s-openEuler-64bit.tar.bz2', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('15', 'openEuler_2203', 'x86_64', 'ENTERPRISE', 'x86_openEuler_2203', 'openGauss-%s-openEuler-64bit-all.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;
INSERT INTO "public"."ops_package_path_dict" VALUES ('16', 'KYLIN', 'x86_64', 'ENTERPRISE', 'x86', 'openGauss-%s-openEuler-64bit-all.tar.gz', NULL, 'admin', now(), 'admin', now()) ON DUPLICATE KEY UPDATE NOTHING;

CREATE
OR REPLACE FUNCTION init_data_fuc() RETURNS integer AS 'BEGIN

 IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=''public'' and table_name=''sys_role'') AND
 NOT EXISTS (select 1 from "public"."sys_role")
 THEN
 INSERT INTO "public"."sys_role" ("role_id", "role_name", "role_key", "role_sort", "data_scope", "menu_check_strictly", "dept_check_strictly", "status", "del_flag", "create_by", "create_time", "update_by", "update_time", "remark") VALUES (1, ''超级管理员'', ''admin'', 1, ''1'', 1, 1, ''0'', ''0'', ''admin'', ''2021-11-01 09:38:10'', NULL, NULL, ''超级管理员'');
 INSERT INTO "public"."sys_role" ("role_id", "role_name", "role_key", "role_sort", "data_scope", "menu_check_strictly", "dept_check_strictly", "status", "del_flag", "create_by", "create_time", "update_by", "update_time", "remark") VALUES (2, ''普通角色'', ''common'', 2, ''2'', 1, 1, ''0'', ''1'', ''admin'', ''2021-11-01 09:38:10'', NULL, NULL, ''普通角色'');
 END IF;

 IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=''public'' and table_name=''sys_user'') AND
 NOT EXISTS (select 1 from "public"."sys_user")
 THEN
 INSERT INTO "public"."sys_user" ("user_id", "user_name", "nick_name", "user_type", "email", "phonenumber", "sex", "avatar", "password", "status", "del_flag", "login_ip", "login_date", "create_by", "create_time", "update_by", "update_time", "remark") VALUES (1, ''admin'', ''超级管理员'', ''00'', NULL, NULL, NULL, NULL, ''$2a$10$MeXrFYhTOrDXqVDXPBDwrOnxg7NVe1ADX1qnhQe04m94VFKwq3cRy'', ''0'', ''0'', NULL, NULL, ''admin'', ''2021-11-01 09:38:09'', ''admin'', ''2022-11-23 13:58:14.047'', ''管理员'');
 END IF;

 IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=''public'' and table_name=''sys_user_role'') AND
 NOT EXISTS (select 1 from "public"."sys_user_role")
 THEN
 INSERT INTO "public"."sys_user_role" ("user_id", "role_id") VALUES (1, 1);
 END IF;

 IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=''public'' and table_name=''sys_menu'') AND
 NOT EXISTS (select 1 from "public"."sys_menu")
 THEN
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (2, ''资源中心'', 0, 2, ''/resource'', NULL, NULL, 1, 0, ''M'', ''0'', ''0'', NULL, ''resource'', ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''Resources'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (5, ''插件管理'', 0, 5, ''/plugin/manage'', ''plugin/manage/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, ''plugin'', ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Plugins'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (6, ''安全中心'', 0, 6, ''/security'', NULL, NULL, 1, 0, ''M'', ''0'', ''0'', NULL, ''security'', ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Security'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (7, ''日志中心'', 0, 7, ''/logs'', NULL, NULL, 1, 0, ''M'', ''0'', ''0'', NULL, ''logs'', ''admin'', ''2022-11-23 20:54:36.439'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Logs'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (201, ''数据库资源'', 2, 1, ''/resource/database'', ''resource/database/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''Database'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (202, ''协议资源'', 2, 2, ''/resource/protocol'', ''resource/protocol/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''Protocol'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (203, ''物理机资源'', 2, 3, ''/resource/physical'', ''resource/physical/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''Host'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (204, ''AZ管理'', 2, 4, ''/resource/az'', ''resource/az/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''Az Manager'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (601, ''账号管理'', 6, 1, ''/security/user'', ''security/user/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Accounts'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (602, ''角色与权限'', 6, 2, ''/security/role'', ''security/role/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Roles'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (603, ''菜单管理'', 6, 3, ''/security/menu'', ''security/menu/index'', NULL, 1, 0, ''C'', ''1'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Menus'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (604, ''访问白名单'', 6, 4, ''/security/whitelist'', ''security/whitelist/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-10-10 22:16:07.030737'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Whitelist'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (605, ''个人中心'', 6, 5, ''/security/usercenter'', ''security/user/ucenter/index'', NULL, 1, 0, ''C'', ''1'', ''0'', NULL, NULL, ''admin'', NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''User Center'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (701, ''系统日志'', 7, 1, ''/logs/sys-log'', ''logs/sys-log/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-11-23 20:54:36.439'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''System Log'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (702, ''操作日志'', 7, 2, ''/logs/oper-log'', ''logs/oper-log/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-11-23 20:54:36.439'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, ''Operation Log'');
 INSERT INTO "public"."sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (208, ''安装包管理'', 2, 8, ''/resource/package'', ''resource/package/index'', NULL, 1, 0, ''C'', ''0'', ''0'', NULL, NULL, ''admin'', ''2022-11-23 20:54:36.439'', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, ''Package Management'');
 END IF;

 IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=''public'' and table_name=''sys_log_config'') AND
 NOT EXISTS (select 1 from "public"."sys_log_config")
 THEN
 INSERT INTO "public"."sys_log_config" ("key", "value", "id") VALUES (''log_level'', ''info'', 1);
 INSERT INTO "public"."sys_log_config" ("key", "value", "id") VALUES (''log_max_file_size'', ''5mb'', 3);
 INSERT INTO "public"."sys_log_config" ("key", "value", "id") VALUES (''log_total_size_cap'', ''10gb'', 4);
 INSERT INTO "public"."sys_log_config" ("key", "value", "id") VALUES (''log_max_history'', ''30'', 2);
 END IF;

 RETURN 0;
 END;'
 LANGUAGE plpgsql;

select init_data_fuc();
DROP FUNCTION init_data_fuc;


CREATE TABLE IF NOT EXISTS "public"."ops_jdbcdb_cluster_node"
("cluster_node_id" varchar(25) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default",
    "name" varchar(255) COLLATE "pg_catalog"."default",
    "ip" varchar(255) COLLATE "pg_catalog"."default",
    "port" varchar(255) COLLATE "pg_catalog"."default",
    "username" varchar(255) COLLATE "pg_catalog"."default",
    "password" varchar(255) COLLATE "pg_catalog"."default",
    "url" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    );

CREATE TABLE IF NOT EXISTS "public"."ops_jdbcdb_cluster"
(
    "cluster_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "name" varchar(255) COLLATE "pg_catalog"."default",
    "deploy_type" varchar(255) COLLATE "pg_catalog"."default",
    "db_type" varchar(255) COLLATE "pg_catalog"."default",
    "remark" varchar(255) COLLATE "pg_catalog"."default",
    "create_by" varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "update_by" varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)
    );

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."sys_setting"(
    "id" int4 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_setting_id'::regclass),
    "user_id" int8 NOT NULL,
    "upload_path" text COLLATE "pg_catalog"."default" NOT NULL
);

CREATE OR REPLACE FUNCTION add_sys_setting_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''sys_setting'' AND COLUMN_NAME = ''portal_pkg_download_url'' ) = 0
THEN
ALTER TABLE sys_setting ADD COLUMN portal_pkg_download_url text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."sys_setting"."portal_pkg_download_url" IS ''portal的在线下载地址'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''sys_setting'' AND COLUMN_NAME = ''portal_pkg_name'' ) = 0
THEN
ALTER TABLE sys_setting ADD COLUMN portal_pkg_name text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."sys_setting"."portal_pkg_name" IS ''portal的安装包名称'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''sys_setting'' AND COLUMN_NAME = ''portal_jar_name'' ) = 0
THEN
ALTER TABLE sys_setting ADD COLUMN portal_jar_name text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."sys_setting"."portal_jar_name" IS ''portal的jar名称'';
UPDATE "public"."sys_setting" SET portal_pkg_download_url = ''https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/'';
UPDATE "public"."sys_setting" SET portal_pkg_name = ''PortalControl-6.0.0.tar.gz'';
UPDATE "public"."sys_setting" SET portal_jar_name = ''portalControl-6.0.0-exec.jar'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_sys_setting_field_func();

DROP FUNCTION add_sys_setting_field_func;


COMMENT
ON COLUMN "public"."sys_setting"."id" IS 'ID';
COMMENT
ON COLUMN "public"."sys_setting"."user_id" IS '关联的用户ID';
COMMENT
ON COLUMN "public"."sys_setting"."upload_path" IS '文件上传目录';

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO "public"."sys_setting" VALUES (1, 1, '/ops/files/', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/', 'PortalControl-6.0.0.tar.gz', 'portalControl-6.0.0-exec.jar') ON DUPLICATE KEY UPDATE NOTHING;

CREATE OR REPLACE FUNCTION add_user_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''sys_user'' AND COLUMN_NAME = ''update_pwd'' ) = 0
THEN
ALTER TABLE sys_user ADD COLUMN update_pwd int2 DEFAULT 0;
COMMENT ON COLUMN "public"."sys_user"."update_pwd" IS ''是否修改密码；1：是；0：否'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_user_field_func();

DROP FUNCTION add_user_field_func;

CREATE OR REPLACE FUNCTION add_cluster_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_cluster'' AND COLUMN_NAME = ''env_path'' ) = 0
THEN
ALTER TABLE ops_cluster ADD COLUMN env_path varchar(255);
COMMENT ON COLUMN "public"."ops_cluster"."env_path" IS ''环境变量文件路径'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_cluster'' AND COLUMN_NAME = ''xml_config_path'' ) = 0
THEN
ALTER TABLE ops_cluster ADD COLUMN xml_config_path varchar(255);
COMMENT ON COLUMN "public"."ops_cluster"."xml_config_path" IS ''xml配置路径'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_cluster_field_func();

DROP FUNCTION add_cluster_field_func;

CREATE OR REPLACE FUNCTION add_host_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_host'' AND COLUMN_NAME = ''os'' ) = 0
THEN
ALTER TABLE ops_host ADD COLUMN os varchar(255);
COMMENT ON COLUMN "public"."ops_host"."os" IS ''操作系统'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_host'' AND COLUMN_NAME = ''cpu_arch'' ) = 0
THEN
ALTER TABLE ops_host ADD COLUMN cpu_arch varchar(255);
COMMENT ON COLUMN "public"."ops_host"."cpu_arch" IS ''CPU架构'';
END IF;
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_host'' AND COLUMN_NAME = ''name'' ) = 0
THEN
ALTER TABLE ops_host ADD COLUMN name varchar(255);
COMMENT ON COLUMN "public"."ops_host"."name" IS ''名称'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_host_field_func();

DROP FUNCTION add_host_field_func;

CREATE OR REPLACE FUNCTION add_sys_plugin_desc_en_field_func() RETURNS integer AS 'BEGIN
IF
( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''sys_plugins'' AND COLUMN_NAME = ''plugin_desc_en'' ) = 0
THEN
ALTER TABLE sys_plugins ADD COLUMN plugin_desc_en text COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."sys_plugins"."plugin_desc_en" IS ''插件英文描述'';
END IF;
RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_sys_plugin_desc_en_field_func();

DROP FUNCTION add_sys_plugin_desc_en_field_func;

delete from "public"."sys_menu" where menu_id = 202;
update "public"."sys_menu" set menu_name = '实例管理', order_num = 1 where menu_id = 201;
update "public"."sys_menu" set menu_name = '设备管理', order_num = 2 where menu_id = 203;
update "public"."sys_menu" set order_num = 4 where menu_id = 204;

ALTER TABLE "public"."sys_oper_log" ALTER COLUMN "oper_param" type text;

CREATE TABLE IF NOT EXISTS "public"."sys_setting"(
    "id" int4 NOT NULL PRIMARY KEY DEFAULT nextval('sq_sys_setting_id'::regclass),
    "user_id" int8 NOT NULL,
    "upload_path" text COLLATE "pg_catalog"."default" NOT NULL
    );
COMMENT
ON COLUMN "public"."sys_setting"."id" IS 'ID';
COMMENT
ON COLUMN "public"."sys_setting"."user_id" IS '关联的用户ID';
COMMENT
ON COLUMN "public"."sys_setting"."upload_path" IS '文件上传目录';

CREATE TABLE IF NOT EXISTS "public"."sys_plugin_logo" (
    "id" int8 NOT NULL DEFAULT nextval('sq_sys_plugin_logo_id'::regclass),
    "plugin_id" varchar(100) COLLATE "pg_catalog"."default",
    "logo_path" varchar(255) COLLATE "pg_catalog"."default",
    CONSTRAINT "tb_plugin_logo_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."sys_plugin_logo"."id" IS '主键ID';

COMMENT ON COLUMN "public"."sys_plugin_logo"."plugin_id" IS '插件ID';

COMMENT ON COLUMN "public"."sys_plugin_logo"."logo_path" IS 'logo路径';

update "public"."sys_menu" set menu_name = '服务器管理', menu_en_name = 'server', order_num = 2 where menu_id = 203;

CREATE OR REPLACE FUNCTION add_host_field_func() RETURNS integer AS 'BEGIN
    IF
        ( SELECT COUNT ( * ) AS ct1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ''ops_host'' AND COLUMN_NAME = ''os_version'' ) = 0
    THEN
        ALTER TABLE ops_host ADD COLUMN os_version varchar(255);
        COMMENT ON COLUMN "public"."ops_host"."os_version" IS ''操作系统版本'';
    END IF;
    RETURN 0;
END;'
LANGUAGE plpgsql;

SELECT add_host_field_func();

DROP FUNCTION add_host_field_func;