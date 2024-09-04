CREATE TABLE IF NOT EXISTS his_diagnosis_result_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    cluster_id TEXT,
    node_id TEXT,
    task_id INTEGER,
    point_name TEXT,
    point_type TEXT,
    point_title TEXT,
    point_suggestion TEXT,
    is_hint TEXT,
    point_data CLOB,
    point_detail TEXT,
    point_state TEXT,
    diagnosis_type TEXT,
    is_deleted INTEGER,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS diagnosis_resource (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    taskid INTEGER,
    grabType TEXT,
    f CLOB
);

CREATE TABLE IF NOT EXISTS dictionary_config (
    id TEXT NOT NULL PRIMARY KEY,
    nodeId TEXT,
    "key" TEXT,
    "value" TEXT
);