CREATE TABLE IF NOT EXISTS "ops_az" (
    "az_id" int8 NOT NULL PRIMARY KEY,
    "name" varchar(255) NOT NULL,
    "priority" int2,
    "address" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_az"."az_id" IS 'az主键';
COMMENT ON COLUMN "ops_az"."name" IS 'az名称';
COMMENT ON COLUMN "ops_az"."priority" IS '优先级';
COMMENT ON COLUMN "ops_az"."address" IS '实际地理位置';
COMMENT ON COLUMN "ops_az"."remark" IS '描述';
COMMENT ON COLUMN "ops_az"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_az"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_az"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_az"."update_time" IS '更新时间';

INSERT INTO "ops_az"("az_id", "name", "priority", "address", "remark", "create_by", "create_time", "update_by", "update_time") VALUES (1639451067550920705, 'az1', NULL, '中国', NULL, 'admin', '2023-03-25 10:16:08.69', 'admin', '2023-03-25 10:16:08.69');

-- ----------------------------
-- Table structure for ops_check
-- ----------------------------

CREATE TABLE IF NOT EXISTS "ops_check" (
    "check_id" varchar(255)  NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) ,
    "check_res" text ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_check"."remark" IS '描述';
COMMENT ON COLUMN "ops_check"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_check"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_check"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_check"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_cluster
-- ----------------------------

CREATE TABLE IF NOT EXISTS "ops_cluster" (
    "cluster_id" varchar(255) NOT NULL PRIMARY KEY,
    "version" varchar(255) ,
    "version_num" varchar(255) ,
    "install_mode" varchar(64) ,
    "deploy_type" varchar(64) ,
    "cluster_name" varchar(255) ,
    "install_package_path" varchar(255) ,
    "database_password" text ,
    "database_username" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp,
    "install_path" varchar(255) ,
    "log_path" varchar(255) ,
    "tmp_path" varchar(255) ,
    "om_tools_path" varchar(255) ,
    "core_path" varchar(255) ,
    "port" varchar(255) ,
    "enable_dcf" int2,
    "dcf_port" varchar(255)
    )
;
COMMENT ON COLUMN "ops_cluster"."cluster_id" IS '集群标识';
COMMENT ON COLUMN "ops_cluster"."version" IS '版本';
COMMENT ON COLUMN "ops_cluster"."version_num" IS '版本号';
COMMENT ON COLUMN "ops_cluster"."install_mode" IS '安装模式';
COMMENT ON COLUMN "ops_cluster"."deploy_type" IS '部署方式';
COMMENT ON COLUMN "ops_cluster"."cluster_name" IS '集群名称';
COMMENT ON COLUMN "ops_cluster"."install_package_path" IS '安装包路径';
COMMENT ON COLUMN "ops_cluster"."database_password" IS '数据库密码';
COMMENT ON COLUMN "ops_cluster"."database_username" IS '数据库用户名';
COMMENT ON COLUMN "ops_cluster"."remark" IS '描述';
COMMENT ON COLUMN "ops_cluster"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_cluster"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_cluster"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_cluster"."update_time" IS '更新时间';
COMMENT ON COLUMN "ops_cluster"."install_path" IS '企业版安装路径';
COMMENT ON COLUMN "ops_cluster"."log_path" IS '企业版日志路径';
COMMENT ON COLUMN "ops_cluster"."tmp_path" IS '企业版临时文件路径';
COMMENT ON COLUMN "ops_cluster"."om_tools_path" IS '企业版om路径';
COMMENT ON COLUMN "ops_cluster"."core_path" IS '企业版核心路径';
COMMENT ON COLUMN "ops_cluster"."port" IS '企业版端口';
COMMENT ON COLUMN "ops_cluster"."enable_dcf" IS '是否开启DCF  0否1是';
COMMENT ON COLUMN "ops_cluster"."dcf_port" IS 'DCF端口';

-- ----------------------------
-- Table structure for ops_cluster_node
-- ----------------------------

CREATE TABLE IF NOT EXISTS "ops_cluster_node" (
    "cluster_node_id" int8 NOT NULL PRIMARY KEY,
    "cluster_role" varchar(255) ,
    "host_id" int8,
    "install_user_id" int8,
    "install_path" varchar(255) ,
    "data_path" varchar(255) ,
    "pkg_path" varchar(255) ,
    "install_demo_database" int2,
    "cluster_id" varchar(255) ,
    "is_install_cm" int2,
    "is_cm_master" int2,
    "cm_data_path" varchar(255) ,
    "cm_port" varchar(255) ,
    "xlog_path" varchar(255) ,
    "is_enable_dss" int2,
    "dss_data_lun_link_path" varchar(255) ,
    "xlog_lun_link_path" varchar(255) ,
    "cm_voting_lun_link_path" varchar(255) ,
    "cm_sharing_lun_link_path" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;

COMMENT ON COLUMN "ops_cluster_node"."cluster_role" IS '集群角色';
COMMENT ON COLUMN "ops_cluster_node"."host_id" IS '主机ID';
COMMENT ON COLUMN "ops_cluster_node"."install_user_id" IS '安装用户ID';
COMMENT ON COLUMN "ops_cluster_node"."install_path" IS '安装路径';
COMMENT ON COLUMN "ops_cluster_node"."data_path" IS '数据目录';
COMMENT ON COLUMN "ops_cluster_node"."pkg_path" IS '安装包目录';
COMMENT ON COLUMN "ops_cluster_node"."install_demo_database" IS '是否安装示例数据库';
COMMENT ON COLUMN "ops_cluster_node"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "ops_cluster_node"."is_install_cm" IS '是否安装 CM';
COMMENT ON COLUMN "ops_cluster_node"."is_cm_master" IS '是否CM master';
COMMENT ON COLUMN "ops_cluster_node"."cm_data_path" IS 'cm数据路径';
COMMENT ON COLUMN "ops_cluster_node"."cm_port" IS 'cm端口';
COMMENT ON COLUMN "ops_cluster_node"."xlog_path" IS 'xlog路径';
COMMENT ON COLUMN "ops_cluster_node"."is_enable_dss" IS '是否开启Dss';
COMMENT ON COLUMN "ops_cluster_node"."dss_data_lun_link_path" IS 'data卷软链接';
COMMENT ON COLUMN "ops_cluster_node"."xlog_lun_link_path" IS 'xlog卷软链接';
COMMENT ON COLUMN "ops_cluster_node"."cm_voting_lun_link_path" IS 'cm投票卷软链接';
COMMENT ON COLUMN "ops_cluster_node"."cm_sharing_lun_link_path" IS 'cm共享卷软链接';
COMMENT ON COLUMN "ops_cluster_node"."remark" IS '描述';
COMMENT ON COLUMN "ops_cluster_node"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_cluster_node"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_cluster_node"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_cluster_node"."update_time" IS '更新时间';

ALTER TABLE "ops_cluster_node" ALTER COLUMN "cluster_node_id" TYPE varchar(255);



-- ----------------------------
-- Table structure for ops_cluster_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS "ops_cluster_task" (
    "cluster_id" varchar(255)  NOT NULL PRIMARY KEY,
    "host_id" varchar(255) ,
    "host_user_id" varchar(255) ,
    "os" varchar(255) ,
    "cpu_arch" varchar(255) ,
    "version" varchar(255) ,
    "version_num" varchar(255) ,
    "package_name" varchar(255) ,
    "package_id" varchar(255) ,
    "cluster_name" varchar(255) ,
    "database_username" varchar(255) ,
    "database_password" text ,
    "database_port" int4 DEFAULT 5432,
    "install_package_path" varchar(255) ,
    "install_path" varchar(255) ,
    "log_path" varchar(255) ,
    "tmp_path" varchar(255) ,
    "om_tools_path" varchar(255) ,
    "core_path" varchar(255) ,
    "env_path" varchar(255) ,
    "enable_cm_tool" int4 DEFAULT 0,
    "enable_dcf" int4 DEFAULT 0,
    "enable_generate_environment_variable_file" int4 DEFAULT 0,
    "xml_config_path" varchar(255) ,
    "deploy_type" varchar(255) ,
    "cluster_node_num" int4 DEFAULT 0,
    "status" varchar(255) ,
    "execute_time" timestamp,
    "env_check_result" varchar(255) ,
    "remark" text ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_cluster_task"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "ops_cluster_task"."host_id" IS '服务器IP';
COMMENT ON COLUMN "ops_cluster_task"."host_user_id" IS '安装服务器用户ID';
COMMENT ON COLUMN "ops_cluster_task"."os" IS '操作系统名称';
COMMENT ON COLUMN "ops_cluster_task"."cpu_arch" IS 'CPU架构';
COMMENT ON COLUMN "ops_cluster_task"."version" IS '数据库版本';
COMMENT ON COLUMN "ops_cluster_task"."version_num" IS '数据库版本号';
COMMENT ON COLUMN "ops_cluster_task"."package_name" IS '安装包名称';
COMMENT ON COLUMN "ops_cluster_task"."package_id" IS '安装包ID';
COMMENT ON COLUMN "ops_cluster_task"."cluster_name" IS '集群名称';
COMMENT ON COLUMN "ops_cluster_task"."database_username" IS '数据库用户名称';
COMMENT ON COLUMN "ops_cluster_task"."database_password" IS '数据库密码';
COMMENT ON COLUMN "ops_cluster_task"."database_port" IS '数据库端口';
COMMENT ON COLUMN "ops_cluster_task"."install_package_path" IS '安装包路径';
COMMENT ON COLUMN "ops_cluster_task"."install_path" IS '安装路径';
COMMENT ON COLUMN "ops_cluster_task"."log_path" IS 'log路径';
COMMENT ON COLUMN "ops_cluster_task"."tmp_path" IS 'tmp路径';
COMMENT ON COLUMN "ops_cluster_task"."om_tools_path" IS 'om工具安装路径';
COMMENT ON COLUMN "ops_cluster_task"."core_path" IS 'core 路径';
COMMENT ON COLUMN "ops_cluster_task"."env_path" IS '环境变量路径';
COMMENT ON COLUMN "ops_cluster_task"."enable_cm_tool" IS '是否安装CM工具';
COMMENT ON COLUMN "ops_cluster_task"."enable_dcf" IS '是否dcf';
COMMENT ON COLUMN "ops_cluster_task"."enable_generate_environment_variable_file" IS '是否分离环境变量文件';
COMMENT ON COLUMN "ops_cluster_task"."xml_config_path" IS 'xml安装配置路径';
COMMENT ON COLUMN "ops_cluster_task"."deploy_type" IS '集群部署类型';
COMMENT ON COLUMN "ops_cluster_task"."cluster_node_num" IS '集群节点数量';
COMMENT ON COLUMN "ops_cluster_task"."status" IS '安装任务执行状态';
COMMENT ON COLUMN "ops_cluster_task"."execute_time" IS '任务执行时间';
COMMENT ON COLUMN "ops_cluster_task"."env_check_result" IS '环境检查结果';
COMMENT ON COLUMN "ops_cluster_task"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_cluster_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_cluster_task"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_cluster_task"."update_time" IS '更新时间';
COMMENT ON TABLE "ops_cluster_task" IS 'openGauss集群安装任务表';



-- ----------------------------
-- Table structure for ops_cluster_task_node
-- ----------------------------
CREATE TABLE IF NOT EXISTS "ops_cluster_task_node" (
    "cluster_node_id" varchar(255)  NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) ,
    "host_id" varchar(255) ,
    "host_user_id" varchar(255) ,
    "node_type" varchar(255) ,
    "data_path" varchar(255) ,
    "az_owner" varchar(255) ,
    "az_priority" varchar(255) ,
    "env_check_result" varchar(255) ,
    "env_check_detail" text ,
    "is_cm_master" int4 DEFAULT 0,
    "cm_data_path" varchar(255) ,
    "cm_port" int4 DEFAULT 0,
    "remark" text ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;

COMMENT ON COLUMN "ops_cluster_task_node"."cluster_node_id" IS '集群节点ID';
COMMENT ON COLUMN "ops_cluster_task_node"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "ops_cluster_task_node"."host_id" IS '节点服务器Id';
COMMENT ON COLUMN "ops_cluster_task_node"."host_user_id" IS '节点用户ID';
COMMENT ON COLUMN "ops_cluster_task_node"."node_type" IS '节点类型';
COMMENT ON COLUMN "ops_cluster_task_node"."data_path" IS '数据路径';
COMMENT ON COLUMN "ops_cluster_task_node"."az_owner" IS '所属AZ';
COMMENT ON COLUMN "ops_cluster_task_node"."az_priority" IS 'AZ优先级';
COMMENT ON COLUMN "ops_cluster_task_node"."env_check_result" IS '环境检查结果';
COMMENT ON COLUMN "ops_cluster_task_node"."env_check_detail" IS '环境检查详情';
COMMENT ON COLUMN "ops_cluster_task_node"."is_cm_master" IS '是否CM主节点';
COMMENT ON COLUMN "ops_cluster_task_node"."cm_data_path" IS 'CM数据路径';
COMMENT ON COLUMN "ops_cluster_task_node"."cm_port" IS 'CM端口';
COMMENT ON COLUMN "ops_cluster_task_node"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_cluster_task_node"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_cluster_task_node"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_cluster_task_node"."update_time" IS '更新时间';
COMMENT ON TABLE "ops_cluster_task_node" IS '集群安装任务节点信息';

-- ----------------------------
-- Table structure for ops_encryption
-- ----------------------------

CREATE TABLE IF NOT EXISTS "ops_encryption"
(
    "encryption_id" varchar(255) NOT NULL PRIMARY KEY,
    "remark" varchar(255),
    "create_by" varchar(64),
    "create_time" timestamp,
    "update_by" varchar(64),
    "update_time" timestamp,
    "key_security" varchar(64),
    "encryption_key" text,
    "public_key" text,
    "private_key" text
);
COMMENT ON COLUMN "ops_encryption"."remark" IS '描述';
COMMENT ON COLUMN "ops_encryption"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_encryption"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_encryption"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_encryption"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_host
-- ----------------------------

CREATE TABLE IF NOT EXISTS "ops_host" (
    "host_id" int8 NOT NULL PRIMARY KEY,
    "hostname" varchar(255) ,
    "private_ip" varchar(64)  NOT NULL,
    "public_ip" varchar(64)  NOT NULL,
    "port" int8 NOT NULL,
    "az_id" int8,
    "os_build" varchar(255) ,
    "cpu_model" varchar(255) ,
    "cpu_arch" varchar(64) ,
    "cpu_freq" varchar(64) ,
    "physical_cores" int4 ,
    "logical_cores" int4 ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_host"."host_id" IS '主机id';
COMMENT ON COLUMN "ops_host"."hostname" IS '主机名';
COMMENT ON COLUMN "ops_host"."private_ip" IS '内网ip地址';
COMMENT ON COLUMN "ops_host"."public_ip" IS '公网ip地址';
COMMENT ON COLUMN "ops_host"."port" IS '端口';
COMMENT ON COLUMN "ops_host"."az_id" IS 'az_id';
COMMENT ON COLUMN "ops_host"."remark" IS '描述';
COMMENT ON COLUMN "ops_host"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_host"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_host"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_host"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_host_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS "ops_host_user" (
    "host_user_id" int8 NOT NULL PRIMARY KEY,
    "username" varchar(255)  NOT NULL,
    "password" text ,
    "host_id" int8 NOT NULL,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_host_user"."host_user_id" IS '主机用户主键';
COMMENT ON COLUMN "ops_host_user"."username" IS '用户名';
COMMENT ON COLUMN "ops_host_user"."password" IS '密码';
COMMENT ON COLUMN "ops_host_user"."host_id" IS '所属主机id';
COMMENT ON COLUMN "ops_host_user"."remark" IS '备注';
COMMENT ON COLUMN "ops_host_user"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_host_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_host_user"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_host_user"."update_time" IS '更新时间';

CREATE TABLE IF NOT EXISTS "ops_host_tag" (
    "host_tag_id" varchar(255)  NOT NULL PRIMARY KEY,
    "name" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_host_tag"."name" IS '标签名';
COMMENT ON COLUMN "ops_host_tag"."remark" IS '备注';
COMMENT ON COLUMN "ops_host_tag"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_host_tag"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_host_tag"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_host_tag"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for ops_host_tag_rel
-- ----------------------------
CREATE TABLE IF NOT EXISTS  "ops_host_tag_rel" (
    "id" varchar(255)  NOT NULL PRIMARY KEY,
    "host_id" varchar(255) ,
    "tag_id" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_host_tag_rel"."host_id" IS '主机ID';
COMMENT ON COLUMN "ops_host_tag_rel"."tag_id" IS 'tag ID';
COMMENT ON COLUMN "ops_host_tag_rel"."remark" IS '描述';
COMMENT ON COLUMN "ops_host_tag_rel"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_host_tag_rel"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_host_tag_rel"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_host_tag_rel"."update_time" IS '更新时间';


-- ----------------------------
-- Table structure for ops_package_path_dict
-- ----------------------------
CREATE TABLE IF NOT EXISTS "ops_package_path_dict" (
    "id" varchar(255)  NOT NULL PRIMARY KEY,
    "os" varchar(255) ,
    "os_version" varchar(255) ,
    "cpu_arch" varchar(255) ,
    "version" varchar(255) ,
    "url_path" varchar(255) ,
    "package_name_tmp" varchar(255) ,
    "pkg_tmp_use_version" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;
COMMENT ON COLUMN "ops_package_path_dict"."os" IS '操作系统名称';
COMMENT ON COLUMN "ops_package_path_dict"."os_version" IS '操作系统版本';
COMMENT ON COLUMN "ops_package_path_dict"."cpu_arch" IS 'CPU架构';
COMMENT ON COLUMN "ops_package_path_dict"."version" IS 'openGauss版本';
COMMENT ON COLUMN "ops_package_path_dict"."url_path" IS 'openGauss包URI路径';
COMMENT ON COLUMN "ops_package_path_dict"."package_name_tmp" IS 'openGauss包名称模版';
COMMENT ON COLUMN "ops_package_path_dict"."pkg_tmp_use_version" IS 'openGauss模版使用版本号';
COMMENT ON COLUMN "ops_package_path_dict"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_package_path_dict"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_package_path_dict"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_package_path_dict"."update_time" IS '更新时间';
COMMENT ON TABLE "ops_package_path_dict" IS 'openGauss包名称字典表';



UPDATE ops_package_path_dict SET os_version = '7' WHERE (os_version IS NULL OR os_version = '') AND os='CentOS';
UPDATE ops_package_path_dict SET os_version = '20.03' WHERE (os_version IS NULL OR os_version = '') AND os = 'openEuler' AND url_path NOT LIKE '%2203%';
UPDATE ops_package_path_dict SET os_version = '22.03', os = 'openEuler' WHERE (os_version IS NULL OR os_version = '') AND os LIKE '%%openEuler%%' AND url_path LIKE '%2203%' ;


UPDATE ops_package_path_dict SET pkg_tmp_use_version = '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1' WHERE (pkg_tmp_use_version IS NULL OR pkg_tmp_use_version = '') ;


ALTER TABLE ops_host_user ADD COLUMN sudo int2 DEFAULT 0;
COMMENT ON COLUMN "ops_host_user"."sudo" IS '管理员';

-- ----------------------------
-- Table structure for sys_log_config
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_log_config" (
    "key" varchar(100)  NOT NULL,
    "value" varchar(500)  NOT NULL,
    "id" int4 NOT NULL PRIMARY KEY
    )
;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_menu" (
    "menu_id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "menu_name" varchar(50)  NOT NULL,
    "parent_id" int8,
    "order_num" int4,
    "path" varchar(200) ,
    "component" varchar(255) ,
    "query" varchar(255) ,
    "is_frame" int4 DEFAULT 1,
    "is_cache" int4 DEFAULT 0,
    "menu_type" char(1) ,
    "visible" char(1)  DEFAULT 0,
    "status" char(1)  DEFAULT 0,
    "perms" varchar(100) ,
    "icon" varchar(100) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp,
    "remark" varchar(500) ,
    "open_way" int4 DEFAULT 1,
    "plugin_id" varchar(100) ,
    "open_position" int2 DEFAULT 1,
    "query_template" varchar(100) ,
    "plugin_theme" varchar(50) ,
    "menu_classify" int2 DEFAULT 1,
    "menu_en_name" varchar(50)
    )
;
COMMENT ON COLUMN "sys_menu"."menu_id" IS '菜单ID';
COMMENT ON COLUMN "sys_menu"."menu_name" IS '菜单名称';
COMMENT ON COLUMN "sys_menu"."parent_id" IS '父菜单ID';
COMMENT ON COLUMN "sys_menu"."order_num" IS '显示顺序';
COMMENT ON COLUMN "sys_menu"."path" IS '路由地址';
COMMENT ON COLUMN "sys_menu"."component" IS '组件路径';
COMMENT ON COLUMN "sys_menu"."query" IS '路由参数';
COMMENT ON COLUMN "sys_menu"."is_frame" IS '是否为外链（0是 1否）';
COMMENT ON COLUMN "sys_menu"."is_cache" IS '是否缓存（0缓存 1不缓存）';
COMMENT ON COLUMN "sys_menu"."menu_type" IS '菜单类型（M目录 C菜单 F按钮）';
COMMENT ON COLUMN "sys_menu"."visible" IS '菜单状态（0显示 1隐藏）';
COMMENT ON COLUMN "sys_menu"."status" IS '菜单状态（0正常 1停用）';
COMMENT ON COLUMN "sys_menu"."perms" IS '权限标识';
COMMENT ON COLUMN "sys_menu"."icon" IS '菜单图标';
COMMENT ON COLUMN "sys_menu"."create_by" IS '创建者';
COMMENT ON COLUMN "sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_menu"."update_by" IS '更新者';
COMMENT ON COLUMN "sys_menu"."update_time" IS '更新时间';
COMMENT ON COLUMN "sys_menu"."remark" IS '备注';
COMMENT ON COLUMN "sys_menu"."open_way" IS '打开方式；1：页面打开；2：弹窗打开';
COMMENT ON COLUMN "sys_menu"."plugin_id" IS '所属插件ID';
COMMENT ON COLUMN "sys_menu"."open_position" IS '打开位置；1：左边菜单；2：首页实例';
COMMENT ON COLUMN "sys_menu"."query_template" IS 'query参数模板';
COMMENT ON COLUMN "sys_menu"."plugin_theme" IS '插件主题';
COMMENT ON COLUMN "sys_menu"."menu_classify" IS '菜单分类；1：业务菜单；2：平台菜单';
COMMENT ON COLUMN "sys_menu"."menu_en_name" IS '英文名称';
COMMENT ON TABLE "sys_menu" IS '菜单权限表';

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_oper_log" (
    "oper_id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "title" varchar(50),
    "business_type" int4,
    "method" varchar(100) ,
    "request_method" varchar(10) ,
    "operator_type" int4,
    "oper_name" varchar(50) ,
    "oper_url" varchar(255) ,
    "oper_ip" varchar(128) ,
    "oper_location" varchar(255) ,
    "oper_param" varchar(2000) ,
    "json_result" varchar(2000) ,
    "status" int4,
    "error_msg" varchar(2000) ,
    "oper_time" timestamp
    )
;
COMMENT ON COLUMN "sys_oper_log"."oper_id" IS '日志主键';
COMMENT ON COLUMN "sys_oper_log"."title" IS '模块标题';
COMMENT ON COLUMN "sys_oper_log"."business_type" IS '业务类型（0其它 1新增 2修改 3删除）';
COMMENT ON COLUMN "sys_oper_log"."method" IS '方法名称';
COMMENT ON COLUMN "sys_oper_log"."request_method" IS '请求方式';
COMMENT ON COLUMN "sys_oper_log"."operator_type" IS '操作类别（0其它 1后台用户 2手机端用户）';
COMMENT ON COLUMN "sys_oper_log"."oper_name" IS '操作人员';
COMMENT ON COLUMN "sys_oper_log"."oper_url" IS '请求URL';
COMMENT ON COLUMN "sys_oper_log"."oper_ip" IS '主机地址';
COMMENT ON COLUMN "sys_oper_log"."oper_location" IS '操作地点';
COMMENT ON COLUMN "sys_oper_log"."oper_param" IS '请求参数';
COMMENT ON COLUMN "sys_oper_log"."json_result" IS '返回参数';
COMMENT ON COLUMN "sys_oper_log"."status" IS '操作状态（0正常 1异常）';
COMMENT ON COLUMN "sys_oper_log"."error_msg" IS '错误消息';
COMMENT ON COLUMN "sys_oper_log"."oper_time" IS '操作时间';
COMMENT ON TABLE "sys_oper_log" IS '操作日志记录';

-- ----------------------------
-- Table structure for sys_plugin_config
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_plugin_config" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "plugin_id" varchar(100) ,
    "config_json" varchar(1000)
    )
;
COMMENT ON COLUMN "sys_plugin_config"."id" IS '主键ID';
COMMENT ON COLUMN "sys_plugin_config"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "sys_plugin_config"."config_json" IS '配置参数';
COMMENT ON TABLE "sys_plugin_config" IS '插件配置结构表';

-- ----------------------------
-- Table structure for sys_plugin_config_data
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_plugin_config_data" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "plugin_id" varchar(100) ,
    "config_data" varchar(1000)
    )
;
COMMENT ON COLUMN "sys_plugin_config_data"."id" IS '主键ID';
COMMENT ON COLUMN "sys_plugin_config_data"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "sys_plugin_config_data"."config_data" IS '配置数据';
COMMENT ON TABLE "sys_plugin_config_data" IS '插件配置数据';

-- ----------------------------
-- Table structure for sys_plugins
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_plugins" (
    "id" int4 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "plugin_id" varchar(100) ,
    "bootstrap_class" varchar(255) ,
    "plugin_desc" text ,
    "logo_path" varchar(255) ,
    "plugin_type" int4,
    "plugin_version" varchar(50) ,
    "plugin_provider" varchar(50) ,
    "plugin_status" int2 DEFAULT 1,
    "is_need_configured" int2,
    "theme" varchar(50)
    )
;
COMMENT ON COLUMN "sys_plugins"."id" IS '主键ID';
COMMENT ON COLUMN "sys_plugins"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "sys_plugins"."bootstrap_class" IS '启动类';
COMMENT ON COLUMN "sys_plugins"."plugin_desc" IS '插件描述';
COMMENT ON COLUMN "sys_plugins"."logo_path" IS '插件Logo路径';
COMMENT ON COLUMN "sys_plugins"."plugin_type" IS '插件类别';
COMMENT ON COLUMN "sys_plugins"."plugin_version" IS '插件版本';
COMMENT ON COLUMN "sys_plugins"."plugin_provider" IS '插件提供者';
COMMENT ON COLUMN "sys_plugins"."plugin_status" IS '插件状态：1：启动；2：停用';
COMMENT ON COLUMN "sys_plugins"."is_need_configured" IS '是否需要配置数据';
COMMENT ON COLUMN "sys_plugins"."theme" IS '主题';

-- ----------------------------
-- Table structure for sys_plugins_repository
-- ----------------------------

CREATE TABLE IF NOT EXISTS sys_plugins_repository (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "plugin_id" varchar(255),
    "is_downloaded" int8 DEFAULT 0,
    "plugin_version" varchar(50),
    "download_url" varchar(255),
    "plugin_desc" text,
    "plugin_desc_en" text
)
;
COMMENT ON COLUMN "sys_plugins_repository"."id" IS '主键ID';
COMMENT ON COLUMN "sys_plugins_repository"."plugin_id" IS '插件ID';
COMMENT ON COLUMN "sys_plugins_repository"."is_downloaded" IS '插件是否已下载：0：未下载；1：已下载';
COMMENT ON COLUMN "sys_plugins_repository"."plugin_version" IS '插件版本';
COMMENT ON COLUMN "sys_plugins_repository"."download_url" IS '插件下载地址';
COMMENT ON COLUMN "sys_plugins_repository"."plugin_desc" IS '插件描述';
COMMENT ON COLUMN "sys_plugins_repository"."plugin_desc_en" IS '插件英文描述';

--pluginsURL-(7.0.0-RC2)
INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (1, 'alert-monitor', 0, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/alert-monitor-7.0.0-RC2-repackage.jar', '告警插件', 'Alarm Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (2, 'webds-plugin', 1, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/webds-plugin-7.0.0-RC2-repackage.jar', '业务开发', 'Data Studio Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (4, 'observability-log-search', 0, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-log-search-7.0.0-RC2-repackage.jar', '日志检索插件', 'Log Retrieval Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (6, 'observability-instance', 0, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-instance-7.0.0-RC2-repackage.jar', '实例监控插件', 'Instance Monitoring Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (7, 'data-migration', 0, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/data-migration-7.0.0-RC2-repackage.jar', '数据迁移插件', 'Data Migration Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (8, 'base-ops', 1, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/base-ops-7.0.0-RC2-repackage.jar', '基础运维插件', 'Basic Operation Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (9, 'observability-sql-diagnosis', 0, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-sql-diagnosis-7.0.0-RC2-repackage.jar', '智能诊断插件', 'Intelligent Diagnosis Plugin');

INSERT INTO sys_plugins_repository (id, plugin_id, is_downloaded, plugin_version, download_url, plugin_desc, plugin_desc_en)
VALUES (11, 'compatibility-assessment', 0, '7.0.0-RC2', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/compatibility-assessment-7.0.0-RC2-repackage.jar', 'openGauss兼容性评估工具', 'The openGauss compatibility evaluation tool');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_role" (
    "role_id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "role_name" varchar(30)  NOT NULL,
    "role_key" varchar(100) ,
    "role_sort" int4,
    "data_scope" char(1) ,
    "menu_check_strictly" int2 DEFAULT 1,
    "dept_check_strictly" int2,
    "status" char(1)  NOT NULL DEFAULT 0,
    "del_flag" char(1)  DEFAULT 0,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp,
    "remark" varchar(500)
    )
;
COMMENT ON COLUMN "sys_role"."role_id" IS '角色ID';
COMMENT ON COLUMN "sys_role"."role_name" IS '角色名称';
COMMENT ON COLUMN "sys_role"."role_key" IS '角色权限字符串';
COMMENT ON COLUMN "sys_role"."role_sort" IS '显示顺序';
COMMENT ON COLUMN "sys_role"."data_scope" IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';
COMMENT ON COLUMN "sys_role"."menu_check_strictly" IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN "sys_role"."dept_check_strictly" IS '医院树选择项是否关联显示';
COMMENT ON COLUMN "sys_role"."status" IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN "sys_role"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "sys_role"."create_by" IS '创建者';
COMMENT ON COLUMN "sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_role"."update_by" IS '更新者';
COMMENT ON COLUMN "sys_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "sys_role"."remark" IS '备注';
COMMENT ON TABLE "sys_role" IS '角色信息表';

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_role_menu" (
    "role_id" int8 NOT NULL,
    "menu_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "sys_role_menu"."role_id" IS '角色ID';
COMMENT ON COLUMN "sys_role_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE "sys_role_menu" IS '角色和菜单关联表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_user" (
    "user_id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_name" varchar(30)  NOT NULL,
    "nick_name" varchar(30)  NOT NULL,
    "user_type" varchar(2) ,
    "email" varchar(50) ,
    "phonenumber" varchar(11) ,
    "sex" char(1) ,
    "avatar" varchar(100) ,
    "password" varchar(100) ,
    "status" char(1) ,
    "del_flag" char(1)  DEFAULT 0,
    "login_ip" varchar(128) ,
    "login_date" timestamp,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp,
    "remark" varchar(500)
    )
;
COMMENT ON COLUMN "sys_user"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_user"."user_name" IS '用户账号';
COMMENT ON COLUMN "sys_user"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "sys_user"."user_type" IS '用户类型（00系统用户）';
COMMENT ON COLUMN "sys_user"."email" IS '用户邮箱';
COMMENT ON COLUMN "sys_user"."phonenumber" IS '手机号码';
COMMENT ON COLUMN "sys_user"."sex" IS '用户性别（0男 1女 2未知）';
COMMENT ON COLUMN "sys_user"."avatar" IS '头像地址';
COMMENT ON COLUMN "sys_user"."password" IS '密码';
COMMENT ON COLUMN "sys_user"."status" IS '帐号状态（0正常 1停用）';
COMMENT ON COLUMN "sys_user"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "sys_user"."login_ip" IS '最后登录IP';
COMMENT ON COLUMN "sys_user"."login_date" IS '最后登录时间';
COMMENT ON COLUMN "sys_user"."create_by" IS '创建者';
COMMENT ON COLUMN "sys_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_user"."update_by" IS '更新者';
COMMENT ON COLUMN "sys_user"."update_time" IS '更新时间';
COMMENT ON COLUMN "sys_user"."remark" IS '备注';
COMMENT ON TABLE "sys_user" IS '用户信息表';

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_user_role" (
    "user_id" int8 NOT NULL PRIMARY KEY,
    "role_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "sys_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_user_role"."role_id" IS '角色ID';
COMMENT ON TABLE "sys_user_role" IS '用户和角色关联表';

-- ----------------------------
-- Table structure for sys_white_list
-- ----------------------------

CREATE TABLE IF NOT EXISTS "sys_white_list" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "title" varchar(255) ,
    "ip_list" varchar(800) ,
    "create_time" timestamp
    )
;
COMMENT
ON COLUMN "sys_white_list"."id" IS '主键ID';
COMMENT
ON COLUMN "sys_white_list"."title" IS '白名单名称';
COMMENT
ON COLUMN "sys_white_list"."ip_list" IS 'ip列表，逗号隔开';
COMMENT
ON COLUMN "sys_white_list"."create_time" IS '创建时间';


CREATE TABLE IF NOT EXISTS "ops_package_manager"(
    "package_id" varchar(255)  NOT NULL PRIMARY KEY,
    "os" varchar(255) ,
    "cpu_arch" varchar(255) ,
    "package_version" varchar(255) ,
    "package_version_num" varchar(255) ,
    "package_url" varchar(1024) ,
    "remark" varchar(255) ,
    "source" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    );


-- ----------------------------
-- Table structure for ops_cluster_operate_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS "ops_cluster_operate_log" (
    "operate_id" varchar(255)  NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) ,
    "cluster_node_id" varchar(255) ,
    "operate_type" varchar(255) ,
    "operate_log" text ,
    "operate_time"  timestamp,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    )
;

COMMENT ON COLUMN "ops_cluster_operate_log"."operate_id" IS '操作记录Id';
COMMENT ON COLUMN "ops_cluster_operate_log"."cluster_id" IS '集群ID';
COMMENT ON COLUMN "ops_cluster_operate_log"."cluster_node_id" IS '集群节点ID';
COMMENT ON COLUMN "ops_cluster_operate_log"."operate_type" IS '操作类型';
COMMENT ON COLUMN "ops_cluster_operate_log"."operate_log" IS '操作记录日志';
COMMENT ON COLUMN "ops_cluster_operate_log"."operate_time" IS '操作时间';
COMMENT ON COLUMN "ops_cluster_operate_log"."create_by" IS '创建者';
COMMENT ON COLUMN "ops_cluster_operate_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "ops_cluster_operate_log"."update_by" IS '更新者';
COMMENT ON COLUMN "ops_cluster_operate_log"."update_time" IS '更新时间';
COMMENT ON TABLE "ops_cluster_operate_log" IS '集群安装任务操作日志';

CREATE TABLE IF NOT EXISTS "sys_task" (
    "id" int8 NOT NULL AUTOINCREMENT,
    "task_name" varchar(255) ,
    "task_type" int4,
    "exec_status" int4,
    "exec_params" varchar(512) ,
    "exec_progress" float4,
    "exec_host_id" int8,
    "create_time" timestamp,
    "finish_time" timestamp,
    "exec_time" timestamp,
    "plugin_id" varchar(100) ,
    "sub_task_count" int8,
    CONSTRAINT "sys_task_pkey" PRIMARY KEY ("id")
    );

COMMENT ON COLUMN "sys_task"."id" IS '主键ID';

COMMENT ON COLUMN "sys_task"."task_name" IS '任务名称';

COMMENT ON COLUMN "sys_task"."task_type" IS '操作类别（1数据迁移）';

COMMENT ON COLUMN "sys_task"."exec_status" IS '执行状态（0：未执行；1：执行中；2：已完成；3：执行失败）';

COMMENT ON COLUMN "sys_task"."exec_params" IS '执行参数';

COMMENT ON COLUMN "sys_task"."exec_progress" IS '执行进度';

COMMENT ON COLUMN "sys_task"."exec_host_id" IS '执行物理机ID';

COMMENT ON COLUMN "sys_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "sys_task"."finish_time" IS '完成时间';

COMMENT ON COLUMN "sys_task"."exec_time" IS '执行时间';

COMMENT ON COLUMN "sys_task"."plugin_id" IS '插件ID';

COMMENT ON COLUMN "sys_task"."sub_task_count" IS '子任务数量';

COMMENT ON TABLE "sys_task" IS '平台任务表';

ALTER TABLE ops_package_manager ADD COLUMN name VARCHAR(255);

ALTER TABLE ops_package_manager ADD COLUMN "package_path" text;
ALTER TABLE ops_package_manager ADD COLUMN "type" varchar(255);
UPDATE ops_package_manager set type = 'openGauss';

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604735048472268811', 'centos', 'x86_64', 'MINIMAL_LIST', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86/openGauss-3.1.1-CentOS-64bit.tar.bz2', NULL,
        'admin', '2022-12-19 15:07:04.749', 'admin', '2022-12-19 15:07:04.749', 'openGauss', 'offical_3.1.1_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605498723119550126', 'centos', 'x86_64', 'ENTERPRISE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86/openGauss-3.1.1-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2022-12-21 17:41:38.992', 'admin', '2022-12-21 17:41:38.992', 'openGauss', 'offical_3.1.1_enterprise_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605498799611072134', 'centos', 'x86_64', 'LITE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86/openGauss-Lite-3.1.1-CentOS-x86_64.tar.gz', NULL,
        'admin', '2022-12-21 17:41:57.21', 'admin', '2022-12-21 17:41:57.21', 'openGauss', 'offical_3.1.1_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604855665214541845', 'openEuler', 'aarch64', 'ENTERPRISE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/arm/openGauss-3.1.1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2022-12-19 23:06:22.02', 'admin', '2022-12-19 23:06:22.02', 'openGauss', 'offical_3.1.1_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604857993288142150', 'openEuler', 'aarch64', 'LITE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/arm/openGauss-Lite-3.1.1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2022-12-19 23:15:37.077', 'admin', '2022-12-19 23:15:37.077', 'openGauss', 'offical_3.1.1_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627277907599162', 'openEuler', 'aarch64', 'MINIMAL_LIST', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/arm/openGauss-3.1.1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2022-12-24 20:26:07.402', 'admin', '2022-12-24 20:26:07.402', 'openGauss', 'offical_3.1.1_mini_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627421516374177', 'openEuler', 'x86_64', 'ENTERPRISE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86_openEuler/openGauss-3.1.1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2022-12-24 20:26:41.641', 'admin', '2022-12-24 20:26:41.641', 'openGauss', 'offical_3.1.1_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627515523309180', 'openEuler', 'x86_64', 'MINIMAL_LIST', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86_openEuler/openGauss-3.1.1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2022-12-24 20:27:04.054', 'admin', '2022-12-24 20:27:04.054', 'openGauss', 'offical_3.1.1_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627585895342192', 'openEuler', 'x86_64', 'LITE', '3.1.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.1.1/x86_openEuler/openGauss-Lite-3.1.1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2022-12-24 20:27:20.832', 'admin', '2022-12-24 20:27:20.832', 'openGauss', 'offical_3.1.1_lite_openEuler2003_x86_64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605398723119550410', 'centos', 'x86_64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86/openGauss-5.0.0-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2022-12-21 17:41:38.992', 'admin', '2022-12-21 17:41:38.992', 'openGauss', 'offical_5.0.0_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1605398799611072511', 'centos', 'x86_64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86/openGauss-Lite-5.0.0-CentOS-x86_64.tar.gz', NULL,
        'admin', '2022-12-21 17:41:57.21', 'admin', '2022-12-21 17:41:57.21', 'openGauss', 'offical_5.0.0_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604335048472268012', 'centos', 'x86_64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86/openGauss-5.0.0-CentOS-64bit.tar.bz2', NULL,
        'admin', '2022-12-19 15:07:04.749', 'admin', '2022-12-19 15:07:04.749', 'openGauss', 'offical_5.0.0_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627421516374013', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler/openGauss-5.0.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2022-12-24 20:26:41.641', 'admin', '2022-12-24 20:26:41.641', 'openGauss', 'offical_5.0.0_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627515523309514', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler/openGauss-5.0.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2022-12-24 20:27:04.054', 'admin', '2022-12-24 20:27:04.054', 'openGauss', 'offical_5.0.0_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627585895342015', 'openEuler', 'x86_64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler/openGauss-Lite-5.0.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2022-12-24 20:27:20.832', 'admin', '2022-12-24 20:27:20.832', 'openGauss', 'offical_5.0.0_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604855665214541816', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm/openGauss-5.0.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2022-12-19 23:06:22.02', 'admin', '2022-12-19 23:06:22.02', 'openGauss', 'offical_5.0.0_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604857993288142817', 'openEuler', 'aarch64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm/openGauss-Lite-5.0.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2022-12-19 23:15:37.077', 'admin', '2022-12-19 23:15:37.077', 'openGauss', 'offical_5.0.0_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627277907599318', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm/openGauss-5.0.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2022-12-24 20:26:07.402', 'admin', '2022-12-24 20:26:07.402', 'openGauss', 'offical_5.0.0_mini_openEuler2003_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627421516374019', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler_2203/openGauss-5.0.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2022-12-24 20:26:41.641', 'admin', '2022-12-24 20:26:41.641', 'openGauss', 'offical_5.0.0_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627515523309520', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler_2203/openGauss-5.0.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2022-12-24 20:27:04.054', 'admin', '2022-12-24 20:27:04.054', 'openGauss', 'offical_5.0.0_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627585895342021', 'openEuler', 'x86_64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/x86_openEuler_2203/openGauss-Lite-5.0.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2022-12-24 20:27:20.832', 'admin', '2022-12-24 20:27:20.832', 'openGauss', 'offical_5.0.0_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604855665214541822', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm_2203/openGauss-5.0.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2022-12-19 23:06:22.02', 'admin', '2022-12-19 23:06:22.02', 'openGauss', 'offical_5.0.0_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1604857993288142823', 'openEuler', 'aarch64', 'LITE', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm_2203/openGauss-Lite-5.0.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2022-12-19 23:15:37.077', 'admin', '2022-12-19 23:15:37.077', 'openGauss', 'offical_5.0.0_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1606627277907599324', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.0/arm_2203/openGauss-5.0.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2022-12-24 20:26:07.402', 'admin', '2022-12-24 20:26:07.402', 'openGauss', 'offical_5.0.0_mini_openEuler2203_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813634', 'centos', 'x86_64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-5.1.0-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2023-09-25 17:41:38.992', 'admin', '2023-09-25 17:41:38.992', 'openGauss', 'offical_5.1.0_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813635', 'centos', 'x86_64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-Lite-5.1.0-CentOS-x86_64.tar.gz', NULL,
        'admin', '2023-09-25 17:41:57.21', 'admin', '2023-09-25 17:41:57.21', 'openGauss', 'offical_5.1.0_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813636', 'centos', 'x86_64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-5.1.0-CentOS-64bit.tar.bz2', NULL,
        'admin', '2023-09-25 15:07:04.749', 'admin', '2023-09-25 15:07:04.749', 'openGauss', 'offical_5.1.0_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813637', 'openEuler', 'x86_64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler/openGauss-5.1.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2023-09-25 20:26:41.641', 'admin', '2023-09-25 20:26:41.641', 'openGauss', 'offical_5.1.0_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813638', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler/openGauss-5.1.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2023-09-25 20:27:04.054', 'admin', '2023-09-25 20:27:04.054', 'openGauss', 'offical_5.1.0_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813639', 'openEuler', 'x86_64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler/openGauss-Lite-5.1.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2023-09-25 20:27:20.832', 'admin', '2023-09-25 20:27:20.832', 'openGauss', 'offical_5.1.0_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813640', 'openEuler', 'aarch64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm/openGauss-5.1.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2023-09-25 23:06:22.02', 'admin', '2023-09-25 23:06:22.02', 'openGauss', 'offical_5.1.0_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813641', 'openEuler', 'aarch64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm/openGauss-Lite-5.1.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2023-09-25 23:15:37.077', 'admin', '2023-09-25 23:15:37.077', 'openGauss', 'offical_5.1.0_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813642', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm/openGauss-5.1.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2023-09-25 20:26:07.402', 'admin', '2023-09-25 20:26:07.402', 'openGauss', 'offical_5.1.0_mini_openEuler2003_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813643', 'openEuler', 'x86_64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler_2203/openGauss-5.1.0-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2023-09-25 20:26:41.641', 'admin', '2023-09-25 20:26:41.641', 'openGauss', 'offical_5.1.0_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813644', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler_2203/openGauss-5.1.0-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2023-09-25 20:27:04.054', 'admin', '2023-09-25 20:27:04.054', 'openGauss', 'offical_5.1.0_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813645', 'openEuler', 'x86_64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86_openEuler_2203/openGauss-Lite-5.1.0-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2023-09-25 20:27:20.832', 'admin', '2023-09-25 20:27:20.832', 'openGauss', 'offical_5.1.0_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813646', 'openEuler', 'aarch64', 'ENTERPRISE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm_2203/openGauss-5.1.0-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2023-09-25 23:06:22.02', 'admin', '2023-09-25 23:06:22.02', 'openGauss', 'offical_5.1.0_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813647', 'openEuler', 'aarch64', 'LITE', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm_2203/openGauss-Lite-5.1.0-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2023-09-25 23:15:37.077', 'admin', '2023-09-25 23:15:37.077', 'openGauss', 'offical_5.1.0_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813648', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.1.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/arm_2203/openGauss-5.1.0-openEuler-64bit.tar.bz2', NULL,
        'admin', '2023-09-25 20:26:07.402', 'admin', '2023-09-25 20:26:07.402', 'openGauss', 'offical_5.1.0_mini_openEuler2203_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813664', 'centos', 'x86_64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86/openGauss-6.0.0-RC1-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2024-04-15 17:41:38.992', 'admin', '2024-04-15 17:41:38.992', 'openGauss', 'offical_6.0.0-RC1_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813665', 'centos', 'x86_64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86/openGauss-Lite-6.0.0-RC1-CentOS-x86_64.tar.gz', NULL,
        'admin', '2024-04-15 17:41:57.21', 'admin', '2024-04-15 17:41:57.21', 'openGauss', 'offical_6.0.0-RC1_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813666', 'centos', 'x86_64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86/openGauss-6.0.0-RC1-CentOS-64bit.tar.bz2', NULL,
        'admin', '2024-04-15 15:07:04.749', 'admin', '2024-04-15 15:07:04.749', 'openGauss', 'offical_6.0.0-RC1_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813667', 'openEuler', 'x86_64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-04-15 20:26:41.641', 'admin', '2024-04-15 20:26:41.641', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813668', 'openEuler', 'x86_64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-04-15 20:27:04.054', 'admin', '2024-04-15 20:27:04.054', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813669', 'openEuler', 'x86_64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler/openGauss-Lite-6.0.0-RC1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-04-15 20:27:20.832', 'admin', '2024-04-15 20:27:20.832', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813670', 'openEuler', 'aarch64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-04-15 23:06:22.02', 'admin', '2024-04-15 23:06:22.02', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813671', 'openEuler', 'aarch64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm/openGauss-Lite-6.0.0-RC1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-04-15 23:15:37.077', 'admin', '2024-04-15 23:15:37.077', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813672', 'openEuler', 'aarch64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-04-15 20:26:07.402', 'admin', '2024-04-15 20:26:07.402', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813673', 'openEuler', 'x86_64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler_2203/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-04-15 20:26:41.641', 'admin', '2024-04-15 20:26:41.641', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813674', 'openEuler', 'x86_64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler_2203/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-04-15 20:27:04.054', 'admin', '2024-04-15 20:27:04.054', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813675', 'openEuler', 'x86_64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/x86_openEuler_2203/openGauss-Lite-6.0.0-RC1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-04-15 20:27:20.832', 'admin', '2024-04-15 20:27:20.832', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813676', 'openEuler', 'aarch64', 'ENTERPRISE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm_2203/openGauss-6.0.0-RC1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-04-15 23:06:22.02', 'admin', '2024-04-15 23:06:22.02', 'openGauss', 'offical_6.0.0-RC1_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813677', 'openEuler', 'aarch64', 'LITE', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm_2203/openGauss-Lite-6.0.0-RC1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-04-15 23:15:37.077', 'admin', '2024-04-15 23:15:37.077', 'openGauss', 'offical_6.0.0-RC1_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813678', 'openEuler', 'aarch64', 'MINIMAL_LIST', '6.0.0-RC1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0-RC1/arm_2203/openGauss-6.0.0-RC1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-04-15 20:26:07.402', 'admin', '2024-04-15 20:26:07.402', 'openGauss', 'offical_6.0.0-RC1_mini_openEuler2203_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813679', 'centos', 'x86_64', 'ENTERPRISE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86/openGauss-5.0.1-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 17:41:38.992', 'admin', '2024-08-29 17:41:38.992', 'openGauss', 'offical_5.0.1_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813680', 'centos', 'x86_64', 'LITE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86/openGauss-Lite-5.0.1-CentOS-x86_64.tar.gz', NULL,
        'admin', '2024-08-29 17:41:57.21', 'admin', '2024-08-29 17:41:57.21', 'openGauss', 'offical_5.0.1_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813681', 'centos', 'x86_64', 'MINIMAL_LIST', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86/openGauss-5.0.1-CentOS-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 15:07:04.749', 'admin', '2024-08-29 15:07:04.749', 'openGauss', 'offical_5.0.1_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813682', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86_openEuler/openGauss-5.0.1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-08-29 20:26:41.641', 'admin', '2024-08-29 20:26:41.641', 'openGauss', 'offical_5.0.1_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813683', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86_openEuler/openGauss-5.0.1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-08-29 20:27:04.054', 'admin', '2024-08-29 20:27:04.054', 'openGauss', 'offical_5.0.1_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813684', 'openEuler', 'x86_64', 'LITE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86_openEuler/openGauss-Lite-5.0.1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-08-29 20:27:20.832', 'admin', '2024-08-29 20:27:20.832', 'openGauss', 'offical_5.0.1_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813685', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/arm/openGauss-5.0.1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 23:06:22.02', 'admin', '2024-08-29 23:06:22.02', 'openGauss', 'offical_5.0.1_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813686', 'openEuler', 'aarch64', 'LITE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/arm/openGauss-Lite-5.0.1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-08-29 23:15:37.077', 'admin', '2024-08-29 23:15:37.077', 'openGauss', 'offical_5.0.1_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813687', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/arm/openGauss-5.0.1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 20:26:07.402', 'admin', '2024-08-29 20:26:07.402', 'openGauss', 'offical_5.0.1_mini_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813688', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86_openEuler_2203/openGauss-5.0.1-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-08-29 20:26:41.641', 'admin', '2024-08-29 20:26:41.641', 'openGauss', 'offical_5.0.1_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813689', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86_openEuler_2203/openGauss-5.0.1-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-08-29 20:27:04.054', 'admin', '2024-08-29 20:27:04.054', 'openGauss', 'offical_5.0.1_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813690', 'openEuler', 'x86_64', 'LITE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/x86_openEuler_2203/openGauss-Lite-5.0.1-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-08-29 20:27:20.832', 'admin', '2024-08-29 20:27:20.832', 'openGauss', 'offical_5.0.1_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813691', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/arm_2203/openGauss-5.0.1-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 23:06:22.02', 'admin', '2024-08-29 23:06:22.02', 'openGauss', 'offical_5.0.1_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813692', 'openEuler', 'aarch64', 'LITE', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/arm_2203/openGauss-Lite-5.0.1-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-08-29 23:15:37.077', 'admin', '2024-08-29 23:15:37.077', 'openGauss', 'offical_5.0.1_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813693', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.1',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.1/arm_2203/openGauss-5.0.1-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 20:26:07.402', 'admin', '2024-08-29 20:26:07.402', 'openGauss', 'offical_5.0.1_mini_openEuler2203_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813694', 'centos', 'x86_64', 'ENTERPRISE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86/openGauss-5.0.2-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 17:41:38.992', 'admin', '2024-08-29 17:41:38.992', 'openGauss', 'offical_5.0.2_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813695', 'centos', 'x86_64', 'LITE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86/openGauss-Lite-5.0.2-CentOS-x86_64.tar.gz', NULL,
        'admin', '2024-08-29 17:41:57.21', 'admin', '2024-08-29 17:41:57.21', 'openGauss', 'offical_5.0.2_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813696', 'centos', 'x86_64', 'MINIMAL_LIST', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86/openGauss-5.0.2-CentOS-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 15:07:04.749', 'admin', '2024-08-29 15:07:04.749', 'openGauss', 'offical_5.0.2_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813697', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86_openEuler/openGauss-5.0.2-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-08-29 20:26:41.641', 'admin', '2024-08-29 20:26:41.641', 'openGauss', 'offical_5.0.2_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813698', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86_openEuler/openGauss-5.0.2-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-08-29 20:27:04.054', 'admin', '2024-08-29 20:27:04.054', 'openGauss', 'offical_5.0.2_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813699', 'openEuler', 'x86_64', 'LITE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86_openEuler/openGauss-Lite-5.0.2-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-08-29 20:27:20.832', 'admin', '2024-08-29 20:27:20.832', 'openGauss', 'offical_5.0.2_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813700', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/arm/openGauss-5.0.2-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 23:06:22.02', 'admin', '2024-08-29 23:06:22.02', 'openGauss', 'offical_5.0.2_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813701', 'openEuler', 'aarch64', 'LITE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/arm/openGauss-Lite-5.0.2-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-08-29 23:15:37.077', 'admin', '2024-08-29 23:15:37.077', 'openGauss', 'offical_5.0.2_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813702', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/arm/openGauss-5.0.2-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 20:26:07.402', 'admin', '2024-08-29 20:26:07.402', 'openGauss', 'offical_5.0.2_mini_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813703', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86_openEuler_2203/openGauss-5.0.2-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-08-29 20:26:41.641', 'admin', '2024-08-29 20:26:41.641', 'openGauss', 'offical_5.0.2_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813704', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86_openEuler_2203/openGauss-5.0.2-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-08-29 20:27:04.054', 'admin', '2024-08-29 20:27:04.054', 'openGauss', 'offical_5.0.2_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813705', 'openEuler', 'x86_64', 'LITE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86_openEuler_2203/openGauss-Lite-5.0.2-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-08-29 20:27:20.832', 'admin', '2024-08-29 20:27:20.832', 'openGauss', 'offical_5.0.2_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813706', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/arm_2203/openGauss-5.0.2-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 23:06:22.02', 'admin', '2024-08-29 23:06:22.02', 'openGauss', 'offical_5.0.2_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813707', 'openEuler', 'aarch64', 'LITE', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/arm_2203/openGauss-Lite-5.0.2-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-08-29 23:15:37.077', 'admin', '2024-08-29 23:15:37.077', 'openGauss', 'offical_5.0.2_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813708', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.2',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/arm_2203/openGauss-5.0.2-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 20:26:07.402', 'admin', '2024-08-29 20:26:07.402', 'openGauss', 'offical_5.0.2_mini_openEuler2203_aarch64') ;

INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813709', 'centos', 'x86_64', 'ENTERPRISE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86/openGauss-5.0.3-CentOS-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 17:41:38.992', 'admin', '2024-08-29 17:41:38.992', 'openGauss', 'offical_5.0.3_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813710', 'centos', 'x86_64', 'LITE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86/openGauss-Lite-5.0.3-CentOS-x86_64.tar.gz', NULL,
        'admin', '2024-08-29 17:41:57.21', 'admin', '2024-08-29 17:41:57.21', 'openGauss', 'offical_5.0.3_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813711', 'centos', 'x86_64', 'MINIMAL_LIST', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86/openGauss-5.0.3-CentOS-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 15:07:04.749', 'admin', '2024-08-29 15:07:04.749', 'openGauss', 'offical_5.0.3_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813712', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86_openEuler/openGauss-5.0.3-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-08-29 20:26:41.641', 'admin', '2024-08-29 20:26:41.641', 'openGauss', 'offical_5.0.3_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813713', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86_openEuler/openGauss-5.0.3-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-08-29 20:27:04.054', 'admin', '2024-08-29 20:27:04.054', 'openGauss', 'offical_5.0.3_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813714', 'openEuler', 'x86_64', 'LITE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86_openEuler/openGauss-Lite-5.0.3-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-08-29 20:27:20.832', 'admin', '2024-08-29 20:27:20.832', 'openGauss', 'offical_5.0.3_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813715', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/arm/openGauss-5.0.3-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 23:06:22.02', 'admin', '2024-08-29 23:06:22.02', 'openGauss', 'offical_5.0.3_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813716', 'openEuler', 'aarch64', 'LITE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/arm/openGauss-Lite-5.0.3-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-08-29 23:15:37.077', 'admin', '2024-08-29 23:15:37.077', 'openGauss', 'offical_5.0.3_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813717', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/arm/openGauss-5.0.3-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 20:26:07.402', 'admin', '2024-08-29 20:26:07.402', 'openGauss', 'offical_5.0.3_mini_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813718', 'openEuler', 'x86_64', 'ENTERPRISE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86_openEuler_2203/openGauss-5.0.3-openEuler-64bit-all.tar.gz',
        NULL, 'admin', '2024-08-29 20:26:41.641', 'admin', '2024-08-29 20:26:41.641', 'openGauss', 'offical_5.0.3_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813719', 'openEuler', 'x86_64', 'MINIMAL_LIST', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86_openEuler_2203/openGauss-5.0.3-openEuler-64bit.tar.bz2',
        NULL, 'admin', '2024-08-29 20:27:04.054', 'admin', '2024-08-29 20:27:04.054', 'openGauss', 'offical_5.0.3_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813720', 'openEuler', 'x86_64', 'LITE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/x86_openEuler_2203/openGauss-Lite-5.0.3-openEuler-x86_64.tar.gz',
        NULL, 'admin', '2024-08-29 20:27:20.832', 'admin', '2024-08-29 20:27:20.832', 'openGauss', 'offical_5.0.3_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813721', 'openEuler', 'aarch64', 'ENTERPRISE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/arm_2203/openGauss-5.0.3-openEuler-64bit-all.tar.gz', NULL,
        'admin', '2024-08-29 23:06:22.02', 'admin', '2024-08-29 23:06:22.02', 'openGauss', 'offical_5.0.3_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813722', 'openEuler', 'aarch64', 'LITE', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/arm_2203/openGauss-Lite-5.0.3-openEuler-aarch64.tar.gz',
        NULL, 'admin', '2024-08-29 23:15:37.077', 'admin', '2024-08-29 23:15:37.077', 'openGauss', 'offical_5.0.3_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813723', 'openEuler', 'aarch64', 'MINIMAL_LIST', '5.0.3',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.3/arm_2203/openGauss-5.0.3-openEuler-64bit.tar.bz2', NULL,
        'admin', '2024-08-29 20:26:07.402', 'admin', '2024-08-29 20:26:07.402', 'openGauss', 'offical_5.0.3_mini_openEuler2203_aarch64') ;


ALTER TABLE ops_package_manager ADD COLUMN os_version VARCHAR(20);
UPDATE ops_package_manager SET os_version = '7' WHERE (os_version IS NULL OR os_version = '''') AND os='centos' and package_url LIKE '%CentOS%';
UPDATE ops_package_manager SET os_version = '22.03' WHERE (os_version IS NULL OR os_version = '''') AND os = 'openEuler' AND package_url LIKE '%2203%';
UPDATE ops_package_manager SET os_version = '20.03' WHERE (os_version IS NULL OR os_version = '''') AND os = 'openEuler' AND package_url not LIKE '%2203%';

UPDATE ops_package_manager SET os_version = '7' WHERE (os_version IS NULL OR os_version = '''') AND os='centos' and package_url LIKE '%CentOS%';
UPDATE ops_package_manager SET os_version = '22.03' WHERE (os_version IS NULL OR os_version = '''') AND os = 'openEuler' AND package_url LIKE '%2203%';
UPDATE ops_package_manager SET os_version = '20.03' WHERE (os_version IS NULL OR os_version = '''') AND os = 'openEuler' AND package_url not LIKE '%2203%';


INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813724', 'centos', '7', 'x86_64', 'ENTERPRISE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/CentOS7/x86/openGauss-All-6.0.0-CentOS7-x86_64.tar.gz', NULL,
        'admin', '2024-09-24 17:41:38.992', 'admin', '2024-09-24 17:41:38.992', 'openGauss', 'offical_6.0.0_enterpirse_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813725', 'centos', '7', 'x86_64', 'LITE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/CentOS7/x86/openGauss-Lite-6.0.0-CentOS7-x86_64.tar.gz', NULL,
        'admin', '2024-09-24 17:41:57.21', 'admin', '2024-09-24 17:41:57.21', 'openGauss', 'offical_6.0.0_lite_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813726', 'centos', '7', 'x86_64', 'MINIMAL_LIST', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/CentOS7/x86/openGauss-Server-6.0.0-CentOS7-x86_64.tar.bz2', NULL,
        'admin', '2024-09-24 15:07:04.749', 'admin', '2024-09-24 15:07:04.749', 'openGauss', 'offical_6.0.0_mini_centos_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813727', 'openEuler', '20.03', 'x86_64', 'ENTERPRISE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler20.03/x86/openGauss-All-6.0.0-openEuler20.03-x86_64.tar.gz',
        NULL, 'admin', '2024-09-24 20:26:41.641', 'admin', '2024-09-24 20:26:41.641', 'openGauss', 'offical_6.0.0_enterprise_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813728', 'openEuler', '20.03', 'x86_64', 'LITE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler20.03/x86/openGauss-Lite-6.0.0-openEuler20.03-x86_64.tar.gz',
        NULL, 'admin', '2024-09-24 20:27:20.832', 'admin', '2024-09-24 20:27:20.832', 'openGauss', 'offical_6.0.0_lite_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813729', 'openEuler', '20.03', 'x86_64', 'MINIMAL_LIST', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler20.03/x86/openGauss-Server-6.0.0-openEuler20.03-x86_64.tar.bz2',
        NULL, 'admin', '2024-09-24 20:27:04.054', 'admin', '2024-09-24 20:27:04.054', 'openGauss', 'offical_6.0.0_mini_openEuler2003_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813730', 'openEuler', '20.03', 'aarch64', 'ENTERPRISE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler20.03/arm/openGauss-All-6.0.0-openEuler20.03-aarch64.tar.gz', NULL,
        'admin', '2024-09-24 23:06:22.02', 'admin', '2024-09-24 23:06:22.02', 'openGauss', 'offical_6.0.0_enterprise_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813731', 'openEuler', '20.03', 'aarch64', 'LITE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler20.03/arm/openGauss-Lite-6.0.0-openEuler20.03-aarch64.tar.gz',
        NULL, 'admin', '2024-09-24 23:15:37.077', 'admin', '2024-09-24 23:15:37.077', 'openGauss', 'offical_6.0.0_lite_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813732', 'openEuler', '20.03', 'aarch64', 'MINIMAL_LIST', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler20.03/arm/openGauss-Server-6.0.0-openEuler20.03-aarch64.tar.bz2', NULL,
        'admin', '2024-09-24 20:26:07.402', 'admin', '2024-09-24 20:26:07.402', 'openGauss', 'offical_6.0.0_mini_openEuler2003_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813733', 'openEuler', '22.03', 'x86_64', 'ENTERPRISE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler22.03/x86/openGauss-All-6.0.0-openEuler22.03-x86_64.tar.gz',
        NULL, 'admin', '2024-09-24 20:26:41.641', 'admin', '2024-09-24 20:26:41.641', 'openGauss', 'offical_6.0.0_enterprise_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813734', 'openEuler', '22.03', 'x86_64', 'LITE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler22.03/x86/openGauss-Lite-6.0.0-openEuler22.03-x86_64.tar.gz',
        NULL, 'admin', '2024-09-24 20:27:20.832', 'admin', '2024-09-24 20:27:20.832', 'openGauss', 'offical_6.0.0_lite_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813735', 'openEuler', '22.03', 'x86_64', 'MINIMAL_LIST', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler22.03/x86/openGauss-Server-6.0.0-openEuler22.03-x86_64.tar.bz2',
        NULL, 'admin', '2024-09-24 20:27:04.054', 'admin', '2024-09-24 20:27:04.054', 'openGauss', 'offical_6.0.0_mini_openEuler2203_x86_64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813736', 'openEuler', '22.03', 'aarch64', 'ENTERPRISE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler22.03/arm/openGauss-All-6.0.0-openEuler22.03-aarch64.tar.gz', NULL,
        'admin', '2024-09-24 23:06:22.02', 'admin', '2024-09-24 23:06:22.02', 'openGauss', 'offical_6.0.0_enterprise_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813737', 'openEuler', '22.03', 'aarch64', 'LITE', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler22.03/arm/openGauss-Lite-6.0.0-openEuler22.03-aarch64.tar.gz',
        NULL, 'admin', '2024-09-24 23:15:37.077', 'admin', '2024-09-24 23:15:37.077', 'openGauss', 'offical_6.0.0_lite_openEuler2203_aarch64') ;
INSERT INTO "ops_package_manager"("package_id", "os", "os_version", "cpu_arch", "package_version", "package_version_num",
                                           "package_url", "remark", "create_by", "create_time", "update_by",
                                           "update_time", "type", "name")
VALUES ('1706224187103813738', 'openEuler', '22.03', 'aarch64', 'MINIMAL_LIST', '6.0.0',
        'https://opengauss.obs.cn-south-1.myhuaweicloud.com/6.0.0/openEuler22.03/arm/openGauss-Server-6.0.0-openEuler22.03-aarch64.tar.bz2', NULL,
        'admin', '2024-09-24 20:26:07.402', 'admin', '2024-09-24 20:26:07.402', 'openGauss', 'offical_6.0.0_mini_openEuler2203_aarch64');


INSERT INTO "ops_package_path_dict" VALUES ('01', 'CentOS','7', 'x86_64', 'LITE', 'x86', 'openGauss-Lite-%s-CentOS-x86_64.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('02', 'CentOS','7', 'x86_64', 'MINIMAL_LIST', 'x86', 'openGauss-%s-CentOS-64bit.tar.bz2', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('03', 'CentOS', '7','x86_64', 'ENTERPRISE', 'x86', 'openGauss-%s-CentOS-64bit-all.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('04', 'openEuler','20.03', 'aarch64', 'LITE', 'arm', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('05', 'openEuler', '20.03','aarch64', 'MINIMAL_LIST', 'arm', 'openGauss-%s-openEuler-64bit.tar.bz2','3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('06', 'openEuler','20.03', 'aarch64', 'ENTERPRISE', 'arm', 'openGauss-%s-openEuler-64bit-all.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('07', 'openEuler','20.03', 'x86_64', 'LITE', 'x86_openEuler', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('08', 'openEuler','20.03', 'x86_64', 'MINIMAL_LIST', 'x86_openEuler', 'openGauss-%s-openEuler-64bit.tar.bz2', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('09', 'openEuler', '20.03','x86_64', 'ENTERPRISE', 'x86_openEuler', 'openGauss-%s-openEuler-64bit-all.tar.gz','3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('10', 'openEuler','22.03', 'aarch64', 'LITE', 'arm_2203', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('11', 'openEuler', '22.03','aarch64', 'MINIMAL_LIST', 'arm_2203', 'openGauss-%s-openEuler-64bit.tar.bz2','3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('12', 'openEuler','22.03', 'aarch64', 'ENTERPRISE', 'arm_2203', 'openGauss-%s-openEuler-64bit-all.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('13', 'openEuler','22.03', 'x86_64', 'LITE', 'x86_openEuler_2203', 'openGauss-Lite-%s-openEuler-aarch64.tar.gz', '3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1',NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('14', 'openEuler','22.03', 'x86_64', 'MINIMAL_LIST', 'x86_openEuler_2203', 'openGauss-%s-openEuler-64bit.tar.bz2','3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('15', 'openEuler','22.03', 'x86_64', 'ENTERPRISE', 'x86_openEuler_2203', 'openGauss-%s-openEuler-64bit-all.tar.gz','3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('16', 'KYLIN', '','x86_64', 'ENTERPRISE', 'x86', 'openGauss-%s-openEuler-64bit-all.tar.gz','3.0.0;3.0.3;3.0.5;3.1.0;3.1.1;5.0.0;5.0.1;5.0.2;5.0.3;5.1.0;6.0.0-RC1' ,NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('17', 'CentOS', '7','x86_64', 'LITE', 'CentOS7/x86', 'openGauss-Lite-%s-CentOS7-x86_64.tar.gz','6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('18', 'CentOS', '7', 'x86_64', 'MINIMAL_LIST', 'CentOS7/x86', 'openGauss-Server-%s-CentOS7-x86_64.tar.gz','6.0.0',  NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('19', 'CentOS', '7', 'x86_64', 'ENTERPRISE', 'CentOS7/x86', 'openGauss-All-%s-CentOS7-x86_64.tar.gz', '6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('20', 'openEuler', '20.03','x86_64', 'LITE', 'openEuler20.03/x86', 'openGauss-Lite-%s-openEuler20.03-x86_64.tar.gz','6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('21', 'openEuler', '20.03', 'x86_64', 'MINIMAL_LIST', 'openEuler20.03/x86', 'openGauss-Server-%s-openEuler20.03-x86_64.tar.gz','6.0.0',  NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('22', 'openEuler', '20.03', 'x86_64', 'ENTERPRISE', 'openEuler20.03/x86', 'openGauss-All-%s-openEuler20.03-x86_64.tar.gz', '6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('23', 'openEuler', '22.03','x86_64', 'LITE', 'openEuler22.03/x86', 'openGauss-Lite-%s-openEuler22.03-x86_64.tar.gz','6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('24', 'openEuler', '22.03', 'x86_64', 'MINIMAL_LIST', 'openEuler22.03/x86', 'openGauss-Server-%s-openEuler22.03-x86_64.tar.gz','6.0.0',  NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('25', 'openEuler', '22.03', 'x86_64', 'ENTERPRISE', 'openEuler22.03/x86', 'openGauss-All-%s-openEuler22.03-x86_64.tar.gz', '6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('26', 'openEuler', '20.03','aarch64', 'LITE', 'openEuler20.03/arm', 'openGauss-Lite-%s-openEuler20.03-aarch64.tar.gz','6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('27', 'openEuler', '20.03', 'aarch64', 'MINIMAL_LIST', 'openEuler20.03/arm', 'openGauss-Server-%s-openEuler20.03-aarch64.tar.gz','6.0.0',  NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('28', 'openEuler', '20.03', 'aarch64', 'ENTERPRISE', 'openEuler20.03/arm', 'openGauss-All-%s-openEuler20.03-aarch64.tar.gz', '6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('29', 'openEuler', '22.03','aarch64', 'LITE', 'openEuler22.03/arm', 'openGauss-Lite-%s-openEuler22.03-aarch64.tar.gz','6.0.0', NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('30', 'openEuler', '22.03', 'aarch64', 'MINIMAL_LIST', 'openEuler22.03/arm', 'openGauss-Server-%s-openEuler22.03-aarch64.tar.gz','6.0.0',  NULL, 'admin', now(), 'admin', now());
INSERT INTO "ops_package_path_dict" VALUES ('31', 'openEuler', '22.03', 'aarch64', 'ENTERPRISE', 'openEuler22.03/arm', 'openGauss-All-%s-openEuler22.03-aarch64.tar.gz', '6.0.0', NULL, 'admin', now(), 'admin', now());

update "ops_package_path_dict" set package_name_tmp= replace(package_name_tmp,'gz','bz2')  where id>17 and version='MINIMAL_LIST';

 INSERT INTO "sys_role" ("role_id", "role_name", "role_key", "role_sort", "data_scope", "menu_check_strictly", "dept_check_strictly", "status", "del_flag", "create_by", "create_time", "update_by", "update_time", "remark") VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2021-11-01 09:38:10', NULL, NULL, '超级管理员');
 INSERT INTO "sys_role" ("role_id", "role_name", "role_key", "role_sort", "data_scope", "menu_check_strictly", "dept_check_strictly", "status", "del_flag", "create_by", "create_time", "update_by", "update_time", "remark") VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '1', 'admin', '2021-11-01 09:38:10', NULL, NULL, '普通角色');



 INSERT INTO "sys_user" ("user_id", "user_name", "nick_name", "user_type", "email", "phonenumber", "sex", "avatar", "password", "status", "del_flag", "login_ip", "login_date", "create_by", "create_time", "update_by", "update_time", "remark") VALUES (1, 'admin', '超级管理员', '00', NULL, NULL, NULL, NULL, '$2a$10$MeXrFYhTOrDXqVDXPBDwrOnxg7NVe1ADX1qnhQe04m94VFKwq3cRy', '0', '0', NULL, NULL, 'admin', '2021-11-01 09:38:09', 'admin', '2022-11-23 13:58:14.047', '管理员');


 INSERT INTO "sys_user_role" ("user_id", "role_id") VALUES (1, 1);


INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (2, '资源中心', 0, 2, '/resource', NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'resource', 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Resources');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (5, '插件管理', 0, 5, '/plugin/manage', 'plugin/manage/index', NULL, 1, 0, 'C', '0', '0', NULL, 'plugin', 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Plugins');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (6, '安全中心', 0, 6, '/security', NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'security', 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Security');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (7, '日志中心', 0, 7, '/logs', NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'logs', 'admin', '2022-11-23 20:54:36.439', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Logs');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (201, '数据库资源', 2, 1, '/resource/database', 'resource/database/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Database');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (202, '协议资源', 2, 2, '/resource/protocol', 'resource/protocol/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Protocol');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (203, '物理机资源', 2, 3, '/resource/physical', 'resource/physical/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Host');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (204, 'AZ管理', 2, 4, '/resource/az', 'resource/az/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Az Manager');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (601, '账号管理', 6, 1, '/security/user', 'security/user/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Accounts');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (602, '角色与权限', 6, 2, '/security/role', 'security/role/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Roles');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (603, '菜单管理', 6, 3, '/security/menu', 'security/menu/index', NULL, 1, 0, 'C', '1', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Menus');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (604, '访问白名单', 6, 4, '/security/whitelist', 'security/whitelist/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-10-10 22:16:07.030737', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Whitelist');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (605, '个人中心', 6, 5, '/security/usercenter', 'security/user/ucenter/index', NULL, 1, 0, 'C', '1', '0', NULL, NULL, 'admin', NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'User Center');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (701, '系统日志', 7, 1, '/logs/sys-log', 'logs/sys-log/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-11-23 20:54:36.439', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'System Log');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (702, '操作日志', 7, 2, '/logs/oper-log', 'logs/oper-log/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-11-23 20:54:36.439', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 2, 'Operation Log');
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "order_num", "path", "component", "query", "is_frame", "is_cache", "menu_type", "visible", "status", "perms", "icon", "create_by", "create_time", "update_by", "update_time", "remark", "open_way", "plugin_id", "open_position", "query_template", "plugin_theme", "menu_classify", "menu_en_name") VALUES (208, '安装包管理', 2, 8, '/resource/package', 'resource/package/index', NULL, 1, 0, 'C', '0', '0', NULL, NULL, 'admin', '2022-11-23 20:54:36.439', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Package Management');

INSERT INTO "sys_log_config" ("key", "value", "id") VALUES ('log_level', 'info', 1);
INSERT INTO "sys_log_config" ("key", "value", "id") VALUES ('log_max_file_size', '5mb', 3);
INSERT INTO "sys_log_config" ("key", "value", "id") VALUES ('log_total_size_cap', '10gb', 4);
INSERT INTO "sys_log_config" ("key", "value", "id") VALUES ('log_max_history', '30', 2);



CREATE TABLE IF NOT EXISTS "ops_jdbcdb_cluster_node"
("cluster_node_id" varchar(25)  NOT NULL PRIMARY KEY,
    "cluster_id" varchar(255) ,
    "name" varchar(255) ,
    "ip" varchar(255) ,
    "port" varchar(255) ,
    "username" varchar(255) ,
    "password" text ,
    "url" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    );

CREATE TABLE IF NOT EXISTS "ops_jdbcdb_cluster"
(
    "cluster_id" varchar(255)  NOT NULL PRIMARY KEY,
    "name" varchar(255) ,
    "deploy_type" varchar(255) ,
    "db_type" varchar(255) ,
    "remark" varchar(255) ,
    "create_by" varchar(64) ,
    "create_time" timestamp,
    "update_by" varchar(64) ,
    "update_time" timestamp
    );

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_setting"(
    "id" int4 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_id" int8 NOT NULL,
    "upload_path" text  NOT NULL
    );


ALTER TABLE sys_setting ADD COLUMN portal_pkg_download_url text ;
COMMENT ON COLUMN "sys_setting"."portal_pkg_download_url" IS 'portal的在线下载地址';

ALTER TABLE sys_setting ADD COLUMN portal_pkg_name text ;
COMMENT ON COLUMN "sys_setting"."portal_pkg_name" IS 'portal的安装包名称';

ALTER TABLE sys_setting ADD COLUMN portal_jar_name text ;
COMMENT ON COLUMN "sys_setting"."portal_jar_name" IS 'portal的jar名称';
UPDATE "sys_setting" SET portal_pkg_download_url = 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/';
UPDATE "sys_setting" SET portal_pkg_name = 'PortalControl-7.0.0rc1.tar.gz';
UPDATE "sys_setting" SET portal_jar_name = 'portalControl-7.0.0rc1-exec.jar';

COMMENT ON COLUMN "sys_setting"."id" IS 'ID';
COMMENT ON COLUMN "sys_setting"."user_id" IS '关联的用户ID';
COMMENT ON COLUMN "sys_setting"."upload_path" IS '文件上传目录';

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO "sys_setting" VALUES (1, 1, '/ops/files/', 'https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/', 'PortalControl-7.0.0rc3.tar.gz', 'portalControl-7.0.0rc3-exec.jar');
INSERT INTO "sys_menu" VALUES (209, '创建服务器', 2, 9, '/resource/physical/createserver', 'resource/physical/components/CreateServerPage', NULL, 1, 0, 'C', '1', '0', NULL, NULL, 'admin', '2025-07-15 20:11:27.003', NULL, NULL, NULL, 1, NULL, 1, NULL, NULL, 1, 'Create Server');
ALTER TABLE sys_user ADD COLUMN update_pwd int2 DEFAULT 0;
COMMENT ON COLUMN "sys_user"."update_pwd" IS '是否修改密码；1：是；0：否';

ALTER TABLE ops_cluster ADD COLUMN env_path varchar(255);
COMMENT ON COLUMN "ops_cluster"."env_path" IS '环境变量文件路径';

ALTER TABLE ops_cluster ADD COLUMN xml_config_path varchar(255);
COMMENT ON COLUMN "ops_cluster"."xml_config_path" IS 'xml配置路径';

ALTER TABLE ops_host ADD COLUMN os varchar(255);
COMMENT ON COLUMN "ops_host"."os" IS '操作系统';

ALTER TABLE ops_host ADD COLUMN cpu_arch varchar(255);
COMMENT ON COLUMN "ops_host"."cpu_arch" IS 'CPU架构';

ALTER TABLE ops_host ADD COLUMN name varchar(255);
COMMENT ON COLUMN "ops_host"."name" IS '名称';


ALTER TABLE sys_plugins ADD COLUMN plugin_desc_en text ;
COMMENT ON COLUMN "sys_plugins"."plugin_desc_en" IS '插件英文描述';

delete from "sys_menu" where menu_id = 202;
update "sys_menu" set menu_name = '实例管理', order_num = 1 where menu_id = 201;
update "sys_menu" set menu_name = '设备管理', order_num = 2 where menu_id = 203;
update "sys_menu" set order_num = 4 where menu_id = 204;

ALTER TABLE "sys_oper_log" ALTER COLUMN "oper_param" type text;

CREATE TABLE IF NOT EXISTS "sys_setting"(
    "id" int4 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_id" int8 NOT NULL,
    "upload_path" text  NOT NULL
    );
COMMENT ON COLUMN "sys_setting"."id" IS 'ID';
COMMENT ON COLUMN "sys_setting"."user_id" IS '关联的用户ID';
COMMENT ON COLUMN "sys_setting"."upload_path" IS '文件上传目录';

CREATE TABLE IF NOT EXISTS "sys_plugin_logo" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "plugin_id" varchar(100),
    "logo_path" varchar(255),
    );

COMMENT ON COLUMN "sys_plugin_logo"."id" IS '主键ID';

COMMENT ON COLUMN "sys_plugin_logo"."plugin_id" IS '插件ID';

COMMENT ON COLUMN "sys_plugin_logo"."logo_path" IS 'logo路径';

update "sys_menu" set menu_name = '服务器管理', menu_en_name = 'server', order_num = 2 where menu_id = 203;

ALTER TABLE ops_host ADD COLUMN os_version varchar(255);
COMMENT ON COLUMN "ops_host"."os_version" IS '操作系统版本';


CREATE TABLE IF NOT EXISTS   "agent_install" (
    "agent_id" varchar(100)  NOT NULL PRIMARY KEY,
    "agent_ip" varchar(100),
    "agent_name" varchar(100),
    "agent_port" int4 DEFAULT 1025,
    "install_path" varchar(255),
    "install_user" varchar(100),
    "install_user_id" varchar(100),
    "status" varchar(100),
    "create_time" timestamp,
    "update_time" timestamp
    );

CREATE TABLE IF NOT EXISTS   "agent_task_template_definition" (
    "name" varchar(100) NOT NULL PRIMARY KEY,
    "type" varchar(100),
    "group_tag" varchar(100),
    "plugins_tag" varchar(100),
    "operate_obj_type" varchar(100),
    "operate_obj" text,
    "period" varchar(100),
    "collect_metric" varchar(100),
    "storage_policy" varchar(100),
    "keep_period" varchar(100),
    "receive_api" varchar(100),
    "create_time" timestamp
    );
COMMENT ON COLUMN   "agent_task_template_definition"."name" IS '主键ID 采集任务名称';
COMMENT ON COLUMN   "agent_task_template_definition"."type" IS '采集任务类型 实时任务 统计历史任务';
COMMENT ON COLUMN   "agent_task_template_definition"."group_tag" IS '采集任务所属分组-用户自定义标签';
COMMENT ON COLUMN   "agent_task_template_definition"."plugins_tag" IS '采集任务所属插件-插件标签';
COMMENT ON COLUMN   "agent_task_template_definition"."operate_obj_type" IS '采集任务操作对象类型OS,DB,OSHI,HTTP';
COMMENT ON COLUMN   "agent_task_template_definition"."operate_obj" IS '采集任务操作对象类';
COMMENT ON COLUMN   "agent_task_template_definition"."period" IS '采集任务周期';
COMMENT ON COLUMN   "agent_task_template_definition"."collect_metric" IS '采集任务收集指标';
COMMENT ON COLUMN   "agent_task_template_definition"."storage_policy" IS '采集任务收集数据存储策略';
COMMENT ON COLUMN   "agent_task_template_definition"."keep_period" IS '采集任务收集数据保存周期';
COMMENT ON COLUMN   "agent_task_template_definition"."receive_api" IS '采集任务收集数据接收地址';
COMMENT ON TABLE   "agent_task_template_definition" IS '采集任务模版定义表';


INSERT INTO "agent_task_template_definition" VALUES ('base_host_info', 'OSHI_FIXED_METRIC', 'datakit', NULL, 'OSHI', NULL, 'PT0S', 'base_host_info', 'CUSTOM', NULL, '/receive/fixed/host/info', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('host_history_dynamic_metrics', 'OSHI_DYNAMIC_METRIC', 'datakit', NULL, 'OSHI', NULL, 'PT10S', 'host_dynamic_metrics', 'HISTORY', 'PT1H', '/receive/metrics', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('opengauss_instance_monitor_history', 'DB_METRIC', 'datakit', NULL, 'DB', NULL, 'PT10S', 'opengauss_instance_monitor', 'HISTORY', 'PT1H', '/receive/metrics', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('mysql_instance_monitor_history', 'DB_METRIC', 'datakit', NULL, 'DB', NULL, 'PT10S', 'mysql_instance_monitor', 'HISTORY', 'PT1H', '/receive/metrics', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('opengauss_instance_memory_top10', 'DB_PIPE', 'datakit', NULL, 'DB', 'SELECT * FROM pv_session_memory_detail() ORDER BY usedsize desc limit 10;', 'PT10S', 'opengauss_instance_memory_top10', 'HISTORY', 'PT1H', '/receive/pipeline', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('opengauss_sql_trace', 'DB_PIPE', 'datakit', NULL, 'HTTP', NULL, 'PT10S', 'opengauss_sql_trace', 'TREE', 'P7D', '/receive/pipeline', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('opengauss_alarm_notification', 'DB_PIPE', 'datakit', NULL, 'HTTP', NULL, 'PT10S', 'opengauss_alarm_notification', 'HISTORY', 'P7D', '/receive/pipeline', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('opengauss_slow_sql_query', 'DB_PIPE', 'datakit', NULL, 'DB', NULL, 'PT10S', 'opengauss_slow_sql_query', 'FINGERPRINT', 'P7D', '/receive/pipeline', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('host_dynamic_metrics', 'OSHI_DYNAMIC_METRIC', 'datakit', NULL, 'OSHI', NULL, 'PT10S', 'host_dynamic_metrics', 'REAL_TIME', NULL, '/receive/metrics', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('opengauss_instance_monitor', 'DB_METRIC', 'datakit', NULL, 'DB', NULL, 'PT10S', 'opengauss_instance_monitor', 'REAL_TIME', NULL, '/receive/metrics', '2025-04-16 16:34:16');
INSERT INTO "agent_task_template_definition" VALUES ('mysql_instance_monitor', 'DB_METRIC', 'datakit', NULL, 'DB', NULL, 'PT10S', 'mysql_instance_monitor', 'REAL_TIME', NULL, '/receive/metrics', '2025-04-16 16:34:16');


CREATE TABLE IF NOT EXISTS   "agent_task_schema_definition" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" varchar(100) NOT NULL   ,
    "metric" varchar(100)   ,
    "create_time" timestamp
    );
COMMENT ON COLUMN   "agent_task_schema_definition"."name" IS '采集任务名称';
COMMENT ON COLUMN   "agent_task_schema_definition"."metric" IS '采集任务绑定指标名称';
COMMENT ON TABLE   "agent_task_schema_definition" IS '采集任务数据结构定义表';

INSERT INTO "agent_task_schema_definition" VALUES (1, 'base_host_info', 'system.hostname', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (2, 'base_host_info', 'system.os.name', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (3, 'base_host_info', 'system.os.version', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (4, 'base_host_info', 'system.cpu.name', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (5, 'base_host_info', 'system.cpu.max.freq', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (6, 'base_host_info', 'system.cpu.arch', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (7, 'base_host_info', 'system.cpu.core.count', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (8, 'host_dynamic_metrics', 'system.memory.total', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (9, 'host_dynamic_metrics', 'system.cpu.usage', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (10, 'host_dynamic_metrics', 'system.memory.available', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (11, 'host_dynamic_metrics', 'system.memory.usage', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (12, 'host_dynamic_metrics', 'system.net.bytes.received', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (13, 'host_dynamic_metrics', 'system.net.bytes.sent', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (14, 'host_dynamic_metrics', 'system.disk.total', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (15, 'host_dynamic_metrics', 'system.disk.bytes.write', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (16, 'host_dynamic_metrics', 'system.disk.bytes.read', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (17, 'host_dynamic_metrics', 'system.disk.fs.total', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (18, 'host_dynamic_metrics', 'system.disk.fs.used', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (19, 'host_dynamic_metrics', 'system.disk.fs.free', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (24, 'opengauss_instance_monitor', 'opengauss.pg.locks.count', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (25, 'opengauss_instance_monitor', 'opengauss.pg.session.count', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (26, 'opengauss_instance_monitor', 'opengauss.pg.connect.count', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (28, 'mysql_instance_monitor', 'mysql.jdbc.monitor.conn.num', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (29, 'mysql_instance_monitor', 'mysql.jdbc.monitor.qps', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (30, 'mysql_instance_monitor', 'mysql.jdbc.monitor.tps', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (31, 'mysql_instance_monitor', 'mysql.jdbc.monitor.memory.used', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (32, 'mysql_instance_monitor', 'mysql.jdbc.monitor.table.space.used', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_schema_definition" VALUES (33, 'opengauss_instance_memory_top10', 'opengauss.pg.session.memory.top10.sessid', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (34, 'opengauss_instance_memory_top10', 'opengauss.pg.session.memory.top10.usedsize', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (35, 'opengauss_instance_memory_top10', 'opengauss.pg.session.memory.top10.freesize', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (36, 'opengauss_instance_memory_top10', 'opengauss.pg.session.memory.top10.totalsize', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (37, 'opengauss_instance_memory_top10', 'opengauss.pg.session.memory.top10.contextname', '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_schema_definition" VALUES (201,'opengauss_alarm_notification', 'opengauss.alarm.id', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (202,'opengauss_alarm_notification','opengauss.alarm.name', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (203,'opengauss_alarm_notification','opengauss.alarm.level', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (204,'opengauss_alarm_notification','opengauss.alarm.scope', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (205,'opengauss_alarm_notification','opengauss.alarm.source_tag', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (206,'opengauss_alarm_notification','opengauss.alarm.details', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (207,'opengauss_alarm_notification','opengauss.alarm.op_type', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (208,'opengauss_alarm_notification','opengauss.alarm.clear_type', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (209,'opengauss_alarm_notification','opengauss.alarm.start_timestamp', '2025-04-16 16:34:16');
INSERT INTO "agent_task_schema_definition" VALUES (210,'opengauss_alarm_notification','opengauss.alarm.end_timestamp', '2025-04-16 16:34:16');


CREATE TABLE IF NOT EXISTS   "agent_task_metrics_definition" (
    "name" varchar(100) NOT NULL PRIMARY KEY ,
    "description" varchar(100),
    "field_name" varchar(100),
    "unit" varchar(100),
    "data_type" varchar(100),
    "prop" varchar(100),
    "collect_cmd" varchar(1024),
    "create_time" timestamp
    );
COMMENT ON COLUMN   "agent_task_metrics_definition"."name" IS '采集任务指标名称';
COMMENT ON COLUMN   "agent_task_metrics_definition"."description" IS '采集任务指标描述';
COMMENT ON COLUMN   "agent_task_metrics_definition"."field_name" IS '采集任务指标字段名称';
COMMENT ON COLUMN   "agent_task_metrics_definition"."unit" IS '采集任务指标数据展示单位';
COMMENT ON COLUMN   "agent_task_metrics_definition"."data_type" IS '采集任务指标数据类型';
COMMENT ON COLUMN   "agent_task_metrics_definition"."prop" IS '采集任务指标属性';
COMMENT ON COLUMN   "agent_task_metrics_definition"."collect_cmd" IS '指标采集指令-SQL';
COMMENT ON TABLE   "agent_task_metrics_definition" IS '采集任务指标定义表';

INSERT INTO "agent_task_metrics_definition" VALUES ('system.hostname', '服务器Host名称', 'hostname', NULL, 'String', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.os.name', '操作系统名称', 'osName', NULL, 'String', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.os.version', '操作系统版本', 'osVersion', NULL, 'String', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.cpu.name', 'cpu名称', 'cpuName', NULL, 'String', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.cpu.max.freq', 'cpu最大主频', 'cpuMaxFreq', 'GHz', 'long', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.cpu.arch', 'cpu架构', 'cpuArch', NULL, 'String', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.cpu.core.count', 'cpu核数', 'cpuCoreCount', NULL, 'long', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.memory.total', '内存总量', 'memoryTotal', 'GB', 'long', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.cpu.usage', 'CPU使用率', 'cpuUsage', '%', 'double', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.memory.available', '剩余内存', 'memoryAvailable', 'GB', 'long', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.memory.usage', '内存使用率', 'memoryUsage', '%', 'double', NULL, NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.net.bytes.received', '网卡下行速率', 'netRecv', 'kb/s', 'long', 'faceName', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.net.bytes.sent', '网卡上行速率', 'netSent', 'kb/s', 'long', 'faceName', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.disk.total', '磁盘总大小', 'diskTotal', 'GB', 'long', 'diskName', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.disk.bytes.write', '磁盘写入速率', 'diskWrite', 'GB/s', 'long', 'diskName', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.disk.bytes.read', '磁盘读取速率', 'diskRead', 'GB/s', 'long', 'diskName', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.disk.fs.total', '磁盘分区大小', 'diskFsTotal', 'GB', 'long', 'mountPoint', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.disk.fs.used', '磁盘分区已使用大小', 'diskFsUsed', 'GB', 'long', 'mountPoint', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('system.disk.fs.free', '磁盘分区剩余大小', 'diskFsFree', 'GB', 'long', 'mountPoint', NULL, '2025-04-16 16:34:16') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.status.cm.state', 'openGauss集群CM状态', 'cmState', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.status.cluster.state', 'openGauss集群Cluster状态', 'cluster_state', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.status.node.state', 'openGauss集群节点状态', 'nodeState', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.status.node.role', 'openGauss集群节点角色', 'nodeRole', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.locks.count', 'openGauss锁数量', 'count', NULL, 'Long', NULL, 'SELECT count(*) count FROM pg_locks', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.session.count', 'openGauss会话数量', 'count', NULL, 'Long', NULL, 'SELECT count(*) count FROM pg_stat_activity', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.connect.count', 'openGauss连接数量', 'count', NULL, 'Long', NULL, 'SELECT count(*) count FROM (SELECT pg_stat_get_backend_idset() AS backendid) AS s', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('mysql.jdbc.monitor.role', 'mysql主备role', 'role', NULL, 'String', NULL, 'SHOW SLAVE STATUS', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('mysql.jdbc.monitor.conn.num', 'mysql连接数', 'value', NULL, 'Long', NULL, 'SHOW STATUS LIKE ''Threads_connected''', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('mysql.jdbc.monitor.qps', 'mysql QPS', 'qps', NULL, 'double', NULL, 'SELECT ROUND(CAST(questions AS UNSIGNED) / IF(CAST(uptime AS UNSIGNED) = 0, 1, CAST(uptime AS UNSIGNED)),2) AS qps FROM ( SELECT MAX(CASE WHEN VARIABLE_NAME = ''Questions'' THEN VARIABLE_VALUE END) AS questions, MAX(CASE WHEN VARIABLE_NAME = ''Uptime'' THEN VARIABLE_VALUE END) AS uptime FROM performance_schema.global_status WHERE VARIABLE_NAME IN (''Questions'', ''Uptime'')) AS status;', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('mysql.jdbc.monitor.tps', 'mysql TPS', 'tps', NULL, 'double', NULL, 'SELECT ROUND(((COALESCE(CAST(com_commit AS UNSIGNED), 0) + COALESCE(CAST(com_rollback AS UNSIGNED), 0)) / IF(COALESCE(CAST(uptime AS UNSIGNED), 0) = 0, 1, COALESCE(CAST(uptime AS UNSIGNED), 0))), 2) AS tps FROM (SELECT MAX(CASE WHEN VARIABLE_NAME = ''Com_commit'' THEN VARIABLE_VALUE END) AS com_commit,MAX(CASE WHEN VARIABLE_NAME = ''Com_rollback'' THEN VARIABLE_VALUE END) AS com_rollback,MAX(CASE WHEN VARIABLE_NAME = ''Uptime'' THEN VARIABLE_VALUE END) AS uptime FROM performance_schema.global_status WHERE VARIABLE_NAME IN (''Com_commit'', ''Com_rollback'', ''Uptime'')) AS status;', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('mysql.jdbc.monitor.memory.used', 'mysql 内存使用量', 'memoryUsed', NULL, 'double', NULL, 'SELECT (@@key_buffer_size + @@innodb_buffer_pool_size + @@innodb_log_buffer_size + @@max_connections * ( @@read_buffer_size + @@read_rnd_buffer_size + @@sort_buffer_size + @@join_buffer_size + @@binlog_cache_size + @@thread_stack + @@tmp_table_size )) AS ''memoryUsed''', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('mysql.jdbc.monitor.table.space.used', 'mysql 表空间使用量', 'tableSpaceUsed', NULL, 'double', NULL, 'SELECT SUM( table_schema_size.table_schema_size ) AS ''tableSpaceUsed'' FROM ( SELECT table_schema, SUM( data_length + index_length ) AS table_schema_size FROM information_schema.TABLES GROUP BY table_schema ) table_schema_size', '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.session.memory.top10.sessid', 'openGauss会话内存使用Top10', 'sessid', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.session.memory.top10.usedsize', 'openGauss会话内存使用Top10', 'usedsize', NULL, 'Long', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.session.memory.top10.freesize', 'openGauss会话内存使用Top10', 'freesize', NULL, 'Long', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.session.memory.top10.totalsize', 'openGauss会话内存使用Top10', 'totalsize', NULL, 'Long', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.pg.session.memory.top10.contextname', 'openGauss会话内存使用Top10', 'contextname', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28') ;
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.id', 'openGauss告警信息ID', 'id', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.name', 'openGauss告警信息名称', 'name', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.level', 'openGauss告警信息级别', 'level', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.scope', 'openGauss告警信息范围', 'scope', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.source_tag', 'openGauss告警信息标签', 'source_tag', '', 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.details', 'openGauss告警信息详情', 'details', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.op_type', 'openGauss告警信息操作类型', 'op_type', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.clear_type', 'openGauss告警信息清除类型', 'clear_type', NULL, 'String', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.start_timestamp', 'openGauss告警信息开始时间', 'start_timestamp', NULL, 'long', NULL, NULL, '2025-04-18 15:36:28.000');
INSERT INTO "agent_task_metrics_definition" VALUES ('opengauss.alarm.end_timestamp', 'openGauss告警信息结束时间', 'end_timestamp', NULL, 'long', NULL, NULL, '2025-04-18 15:36:28.000');

CREATE TABLE IF NOT EXISTS   "agent_task_instance" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "task_template_id" varchar(100) NOT NULL,
    "task_name" varchar(100) NOT NULL,
    "agent_id" varchar(100) NOT NULL,
    "cluster_id" varchar(100),
    "cluster_original" varchar(100),
    "task_status" varchar(100) NOT NULL,
    "start_time" timestamp,
    "end_time" timestamp,
    "error_msg" text,
    "task_params" text,
    "create_time" timestamp,
    "update_time" timestamp
    );
COMMENT ON COLUMN   "agent_task_instance"."id" IS '采集任务ID';
COMMENT ON COLUMN   "agent_task_instance"."task_template_id" IS '采集任务绑定模版ID';
COMMENT ON COLUMN   "agent_task_instance"."task_name" IS '采集任务名称';
COMMENT ON COLUMN   "agent_task_instance"."agent_id" IS '采集任务执行AgentID';
COMMENT ON COLUMN   "agent_task_instance"."cluster_id" IS '采集任务关联数据库集群实例ID';
COMMENT ON COLUMN   "agent_task_instance"."cluster_original" IS '采集任务关联集群来源:OPS_CLUSTER;OPS_JDBC_CLUSTER';
COMMENT ON COLUMN   "agent_task_instance"."task_status" IS '采集任务状态';
COMMENT ON COLUMN   "agent_task_instance"."start_time" IS '采集任务开始时间';
COMMENT ON COLUMN   "agent_task_instance"."end_time" IS '采集任务结束时间';
COMMENT ON COLUMN   "agent_task_instance"."error_msg" IS '采集任务异常信息';
COMMENT ON COLUMN   "agent_task_instance"."task_params" IS '采集任务执行参数';
COMMENT ON TABLE   "agent_task_instance" IS 'Agent任务实例表';


CREATE TABLE IF NOT EXISTS   "agent_cluster_relation" (
    "cluster_node_id" varchar(100) NOT NULL PRIMARY KEY,
    "cluster_original" varchar(100) NOT NULL
    );
COMMENT ON COLUMN   "agent_cluster_relation"."cluster_node_id" IS '集群ID';
COMMENT ON COLUMN   "agent_cluster_relation"."cluster_original" IS '采集任务关联集群来源:OPS_CLUSTER;OPS_JDBC_CLUSTER';
COMMENT ON TABLE   "agent_cluster_relation" IS 'Agent任务集群关联表';


CREATE TABLE IF NOT EXISTS   "agent_metric_real_time" (
    "multi_id" varchar(300) NOT NULL   ,
    "agent_id" int8 NOT NULL,
    "cluster_node_id" varchar(100)    ,
    "metric" varchar(100) NOT NULL   ,
    "property" varchar(100)    ,
    "value" varchar(100) NOT NULL   ,
    "start_time" timestamp,
    "end_time" timestamp,
    "create_time" timestamp,
    PRIMARY KEY ("agent_id","cluster_node_id","metric","property")
    );
COMMENT ON COLUMN   "agent_metric_real_time"."multi_id" IS 'Agent 联合主键值';
COMMENT ON COLUMN   "agent_metric_real_time"."agent_id" IS 'AgentID';
COMMENT ON COLUMN   "agent_metric_real_time"."cluster_node_id" IS '集群ID';
COMMENT ON COLUMN   "agent_metric_real_time"."metric" IS '指标ID,指标名称';
COMMENT ON COLUMN   "agent_metric_real_time"."property" IS '指标属性对应值';
COMMENT ON COLUMN   "agent_metric_real_time"."value" IS '指标值';
COMMENT ON COLUMN   "agent_metric_real_time"."start_time" IS '指标采集开始时间';
COMMENT ON COLUMN   "agent_metric_real_time"."end_time" IS '指标采集结束时间';
COMMENT ON COLUMN   "agent_metric_real_time"."create_time" IS '指标保存时间';
COMMENT ON TABLE   "agent_metric_real_time" IS 'Agent实时指标数据表';

CREATE TABLE IF NOT EXISTS   "agent_metric_historical" (
    "id" int8 NOT NULL PRIMARY KEY,
    "task_id" int8 NOT NULL,
    "agent_id" int8 NOT NULL,
    "cluster_node_id" varchar(100)    ,
    "metric" varchar(100) NOT NULL   ,
    "property" varchar(100)    ,
    "value" varchar(100) NOT NULL   ,
    "start_time" timestamp,
    "end_time" timestamp,
    "create_time" timestamp
    );
COMMENT ON COLUMN   "agent_metric_historical"."id" IS 'Agent 主键值';
COMMENT ON COLUMN   "agent_metric_historical"."task_id" IS '数据采集任务ID';
COMMENT ON COLUMN   "agent_metric_historical"."agent_id" IS 'AgentID';
COMMENT ON COLUMN   "agent_metric_historical"."cluster_node_id" IS '集群ID';
COMMENT ON COLUMN   "agent_metric_historical"."metric" IS '指标ID,指标名称';
COMMENT ON COLUMN   "agent_metric_historical"."property" IS '指标属性对应值';
COMMENT ON COLUMN   "agent_metric_historical"."value" IS '指标值';
COMMENT ON COLUMN   "agent_metric_historical"."start_time" IS '指标采集开始时间';
COMMENT ON COLUMN   "agent_metric_historical"."end_time" IS '指标采集结束时间';
COMMENT ON COLUMN   "agent_metric_historical"."create_time" IS '指标保存时间';
COMMENT ON TABLE   "agent_metric_historical" IS 'Agent历史指标数据表';

CREATE TABLE IF NOT EXISTS   "agent_task_table_relation" (
    "task_id" varchar(100) NOT NULL  PRIMARY KEY ,
    "task_table_name" varchar(100) NOT NULL,
    );
COMMENT ON COLUMN   "agent_task_table_relation"."task_id" IS 'Agent任务集群关联关系-任务ID';
COMMENT ON COLUMN   "agent_task_table_relation"."task_table_name" IS '集群ID';
COMMENT ON TABLE   "agent_task_table_relation" IS 'Agent任务集群关联表';

ALTER TABLE "ops_encryption" ALTER COLUMN "public_key" TYPE text;
ALTER TABLE "ops_encryption" ALTER COLUMN "encryption_key" TYPE text;
ALTER TABLE "ops_encryption" ALTER COLUMN "private_key" TYPE text;
ALTER TABLE "ops_jdbcdb_cluster_node" ALTER COLUMN "password" TYPE text;
ALTER TABLE "ops_host_user" ALTER COLUMN "password" TYPE text;
ALTER TABLE "ops_cluster_task" ALTER COLUMN "database_password" TYPE text;
ALTER TABLE "ops_cluster" ALTER COLUMN "database_password" TYPE text;


CREATE TABLE IF NOT EXISTS "sys_setting_ext" (
    "id" int8 NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_id" int8 NOT NULL,
    "param_name" varchar(64)  NOT NULL,
    "param_value" varchar(64),
    "remark" varchar(128)
);
insert into "sys_setting_ext" values(1,1,'datakit_server_host','','');