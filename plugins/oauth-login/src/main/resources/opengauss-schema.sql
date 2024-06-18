CREATE OR REPLACE FUNCTION create_sequences_not_exists() RETURNS integer AS 'BEGIN

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_oauth_login_sso_user_id'' )
THEN
CREATE SEQUENCE "public"."sq_oauth_login_sso_user_id"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
END IF;

IF NOT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_schema=''public'' AND sequence_name=''sq_oauth_login_user_mapping_id'' )
THEN
CREATE SEQUENCE "public"."sq_oauth_login_user_mapping_id"
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

-- ----------------------------
-- Table structure for oauth_login_sso_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."oauth_login_sso_user" (
    "id" int8 NOT NULL DEFAULT nextval('sq_oauth_login_sso_user_id'::regclass),
    "uiid" varchar(255) NOT NULL,
    "name" varchar(255) NOT NULL,
    "nickname" varchar(255) NOT NULL,
    "role" varchar(255) NOT NULL,
    "sso_server_url" varchar(255) NOT NULL,
    CONSTRAINT "oauth_login_sso_user_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."oauth_login_sso_user"."id" IS '主键ID';
COMMENT ON COLUMN "public"."oauth_login_sso_user"."uiid" IS '相同sso服务地址下，sso用户的唯一标识符';
COMMENT ON COLUMN "public"."oauth_login_sso_user"."name" IS 'sso用户名';
COMMENT ON COLUMN "public"."oauth_login_sso_user"."nickname" IS 'sso用户昵称';
COMMENT ON COLUMN "public"."oauth_login_sso_user"."role" IS 'sso用户角色';
COMMENT ON COLUMN "public"."oauth_login_sso_user"."sso_server_url" IS 'sso用户所在的sso服务地址';

-- ----------------------------
-- Table structure for oauth_login_user_mapping
-- ----------------------------

CREATE TABLE IF NOT EXISTS "public"."oauth_login_user_mapping" (
    "id" int8 NOT NULL DEFAULT nextval('sq_oauth_login_user_mapping_id'::regclass),
    "sso_user_id" varchar(255) NOT NULL,
    "sys_user_id" varchar(255) NOT NULL,
    CONSTRAINT "oauth_login_user_mapping_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."oauth_login_user_mapping"."id" IS '主键ID';
COMMENT ON COLUMN "public"."oauth_login_user_mapping"."sso_user_id" IS 'sso用户的id';
COMMENT ON COLUMN "public"."oauth_login_user_mapping"."sys_user_id" IS 'sso用户映射的系统用户的id';