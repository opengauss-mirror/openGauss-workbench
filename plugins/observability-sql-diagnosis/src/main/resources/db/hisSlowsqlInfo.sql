CREATE TABLE IF NOT EXISTS tb_time_config (
    id integer primary key,
    peroid integer default 30, --慢sql保留天数
    frequency integer default 300 --慢sql采集频率，单位秒
);

insert or ignore into tb_time_config values (1, 30, 300);

CREATE TABLE IF NOT EXISTS tb_time_point (
    node_tablename text primary key,
    start_time_point timestamp with time zone,
    finish_time_point timestamp with time zone
);