CREATE TABLE IF NOT EXISTS "nctigba_env" (
	id varchar NULL,
	hostid varchar NULL,
	"type" varchar NULL,
	username varchar NULL,
	"path" varchar NULL,
	port int8 NULL
);
ALTER TABLE nctigba_env ADD COLUMN nodeid VARCHAR(255);
ALTER TABLE nctigba_env ADD status varchar NULL;
ALTER TABLE nctigba_env ADD update_time timestamp NULL;
ALTER TABLE nctigba_env ADD param varchar NULL;