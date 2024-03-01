-- 2023-11-02 v5.1.1 collect config feature
CREATE TABLE IF NOT EXISTS collect_template (
   id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
   name VARCHAR(255),
   description VARCHAR(255),
   create_by VARCHAR(64),
   create_time TIMESTAMP,
   update_by VARCHAR(64),
   update_time TIMESTAMP
);
CREATE TABLE IF NOT EXISTS collect_template_node (
   id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
   template_id INTEGER,
   node_id VARCHAR(255),
   create_by VARCHAR(64),
   create_time TIMESTAMP,
   update_by VARCHAR(64),
   update_time TIMESTAMP
);
CREATE TABLE IF NOT EXISTS collect_template_metrics (
   id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
   template_id INTEGER NOT NULL,
   metrics_key VARCHAR(255) NOT NULL,
   interval VARCHAR(255),
   create_by VARCHAR(64),
   create_time TIMESTAMP,
   update_by VARCHAR(64),
   update_time TIMESTAMP
);
ALTER TABLE collect_template_metrics ADD COLUMN is_enable BOOLEAN;
UPDATE collect_template_metrics SET is_enable=true WHERE is_enable is null;
CREATE TABLE IF NOT EXISTS cluster_switchover_record (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    cluster_id varchar(255),
    switchover_time varchar(64),
    primary_ip varchar(64),
    switchover_reason varchar(255),
    update_time DATETIME
);
CREATE TABLE IF NOT EXISTS cluster_switchover_log_read (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    cluster_id varchar(255),
    cluster_node_id varchar(255),
    log_name varchar(255),
    log_last_read bigint,
    update_time DATETIME
);

-- 2023-11-02 v5.1.1 agent 1 to n feature
CREATE TABLE IF NOT EXISTS agent_node_relation (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    env_id varchar(100),
    node_id varchar(100),
    create_by VARCHAR(64),
    create_time TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP
);

-- 2023-11-30 v5.1.1 IntarkDB
CREATE TABLE IF NOT EXISTS param_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    param_type TEXT,
    param_name TEXT,
    parameter_category TEXT,
    value_range TEXT,
    param_detail TEXT,
    suggest_value TEXT,
    default_value TEXT,
    unit TEXT,
    suggest_explain TEXT,
    diagnosis_rule TEXT
);

CREATE TABLE IF NOT EXISTS param_value_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    sid INTEGER,
    instance TEXT,
    actual_value TEXT
);
