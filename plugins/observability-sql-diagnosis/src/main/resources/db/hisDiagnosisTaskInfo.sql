CREATE TABLE IF NOT EXISTS his_diagnosis_task_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    cluster_id TEXT,
    node_id TEXT,
    db_name TEXT,
    task_name TEXT,
    sql_id TEXT,
    sql CLOB,
    topology_map CLOB,
    pid INTEGER,
    debug_query_id BIGINT,
    session_id BIGINT,
    his_data_start_time DATETIME,
    his_data_end_time DATETIME,
    task_start_time DATETIME,
    task_end_time DATETIME,
    state TEXT,
    span TEXT,
    remarks CLOB,
    conf TEXT,
    threshold CLOB,
    task_type TEXT,
    diagnosis_type TEXT,
    node_vo_sub TEXT,
    is_deleted INTEGER,
    create_time DATETIME,
    update_time DATETIME
);

ALTER TABLE his_diagnosis_task_info ADD collect_pid_status INTEGER NULL;
ALTER TABLE his_diagnosis_task_info ADD schema_name VARCHAR;