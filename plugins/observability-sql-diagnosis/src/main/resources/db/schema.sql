/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ObservabilityPluginApplication.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/ObservabilityPluginApplication.java
 *
 *  -------------------------------------------------------------------------
 */

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

CREATE SEQUENCE IF NOT EXISTS his_diagnosis_threshold_seq INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS his_diagnosis_threshold_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    cluster_id TEXT,
    node_id TEXT,
    threshold_type TEXT,
    threshold CLOB,
    threshold_name TEXT,
    threshold_value TEXT,
    threshold_unit TEXT,
    threshold_detail TEXT,
    sort_no VARCHAR,
    diagnosis_type TEXT,
    is_deleted INTEGER,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS param_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    paramType TEXT,
    paramName TEXT,
    paramDetail CLOB,
    suggestValue TEXT,
    defaultValue TEXT,
    unit TEXT,
    suggestExplain CLOB,
    diagnosisRule TEXT
);

CREATE TABLE IF NOT EXISTS diagnosis_task_result (
    taskid INTEGER,
    resultState TEXT,
    resultType TEXT,
    frameType TEXT,
    bearing TEXT,
    data TEXT
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

