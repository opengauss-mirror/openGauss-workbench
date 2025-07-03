CREATE TABLE IF NOT EXISTS time_config (
    id integer primary key,
    peroid integer default 30, --慢sql保留天数
    frequency integer default 300 --慢sql采集频率，单位秒
);

insert or ignore into time_config values (1, 30, 300);

CREATE TABLE IF NOT EXISTS tb_max_finish_time (
    node_tablename text primary key,
    max_finish_time timestamp with time zone
);