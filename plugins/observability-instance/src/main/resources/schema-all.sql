CREATE TABLE public.nctigba_env (
	id varchar NULL,
	hostid varchar NULL,
	"type" varchar NULL,
	username varchar NULL,
	"path" varchar NULL,
	port int8 NULL
);
ALTER TABLE public.nctigba_env ADD nodeid varchar NULL;
ALTER TABLE public.nctigba_env ADD status varchar NULL;
ALTER TABLE public.nctigba_env ADD update_time timestamp NULL;
ALTER TABLE public.nctigba_env ADD param varchar NULL;