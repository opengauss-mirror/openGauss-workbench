-- ----------------------------
-- Table structure for oauth_login_sso_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS "oauth_login_sso_user" (
    "id" int8 NOT NULL AUTOINCREMENT,
    "uiid" varchar(255) NOT NULL,
    "name" varchar(255) NOT NULL,
    "nickname" varchar(255) NOT NULL,
    "role" varchar(255) NOT NULL,
    "sso_server_url" varchar(255) NOT NULL,
    CONSTRAINT "oauth_login_sso_user_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "oauth_login_sso_user"."id" IS '主键ID';
COMMENT ON COLUMN "oauth_login_sso_user"."uiid" IS '相同sso服务地址下，sso用户的唯一标识符';
COMMENT ON COLUMN "oauth_login_sso_user"."name" IS 'sso用户名';
COMMENT ON COLUMN "oauth_login_sso_user"."nickname" IS 'sso用户昵称';
COMMENT ON COLUMN "oauth_login_sso_user"."role" IS 'sso用户角色';
COMMENT ON COLUMN "oauth_login_sso_user"."sso_server_url" IS 'sso用户所在的sso服务地址';

-- ----------------------------
-- Table structure for oauth_login_user_mapping
-- ----------------------------

CREATE TABLE IF NOT EXISTS "oauth_login_user_mapping" (
    "id" int8 NOT NULL AUTOINCREMENT,
    "sso_user_id" varchar(255) NOT NULL,
    "sys_user_id" varchar(255) NOT NULL,
    CONSTRAINT "oauth_login_user_mapping_pkey" PRIMARY KEY ("id")
    );

COMMENT ON COLUMN "oauth_login_user_mapping"."id" IS '主键ID';
COMMENT ON COLUMN "oauth_login_user_mapping"."sso_user_id" IS 'sso用户的id';
COMMENT ON COLUMN "oauth_login_user_mapping"."sys_user_id" IS 'sso用户映射的系统用户的id';