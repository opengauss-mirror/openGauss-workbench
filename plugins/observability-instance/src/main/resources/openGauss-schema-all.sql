CREATE TABLE IF NOT EXISTS public.nctigba_env (
	id varchar NULL,
    hostid varchar NULL,
    "type" varchar NULL,
    username varchar NULL,
    "path" varchar NULL,
    port int8 NULL
);
CREATE OR REPLACE FUNCTION alter_nctigba_env() RETURNS integer AS '
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name=''nctigba_env'' AND column_name=''nodeid''
    ) THEN
        ALTER TABLE public.nctigba_env ADD COLUMN nodeid VARCHAR NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name=''nctigba_env'' AND column_name=''status''
    ) THEN
        ALTER TABLE public.nctigba_env ADD COLUMN status VARCHAR NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name=''nctigba_env'' AND column_name=''update_time''
    ) THEN
        ALTER TABLE public.nctigba_env ADD COLUMN update_time TIMESTAMP NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name=''nctigba_env'' AND column_name=''param''
    ) THEN
        ALTER TABLE public.nctigba_env ADD COLUMN param VARCHAR NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name=''nctigba_env'' AND column_name=''err_status_msg''
    ) THEN
        ALTER TABLE public.nctigba_env ADD COLUMN err_status_msg VARCHAR NULL;
    END IF;
    RETURN 0;
END'
LANGUAGE plpgsql;

SELECT alter_nctigba_env();
DROP FUNCTION alter_nctigba_env();

CREATE TABLE IF NOT EXISTS nctigba_env (
    id VARCHAR(255),
    hostid VARCHAR(255),
    "type" VARCHAR(255),
    username VARCHAR(255),
    "path" VARCHAR(255),
    port int8
    );

ALTER TABLE nctigba_env ADD COLUMN nodeid VARCHAR(255);

ALTER TABLE nctigba_env ADD COLUMN status VARCHAR(255);

ALTER TABLE nctigba_env ADD COLUMN update_time TIMESTAMP;

ALTER TABLE nctigba_env ADD COLUMN param VARCHAR(255);

ALTER TABLE nctigba_env ADD COLUMN err_status_msg VARCHAR(255);