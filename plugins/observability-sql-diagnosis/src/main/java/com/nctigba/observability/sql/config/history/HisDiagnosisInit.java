/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.config.history;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.util.LocaleString;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * HisDiagnosis Init
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Component
@Slf4j
@EnableScheduling
public class HisDiagnosisInit {
    private static final String HIS_DIAGNOSIS_TASK_DB = "data/diagnosisTaskV1.db";
    private static final String HIS_DIAGNOSIS_RESULT_DB = "data/diagnosisResultV1.db";
    private static final String HIS_DIAGNOSIS_THRESHOLD_DB = "data/diagnosisThresholdV1.db";
    private static final String[] TASK_SQL = {"CREATE TABLE \"his_diagnosis_task_info\" ("
            + "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, cluster_id TEXT,"
            + "node_id TEXT,db_name TEXT,task_name TEXT,sql_id TEXT,sql TEXT,topology_map TEXT,"
            + "pid INTEGER,debug_query_id INTEGER,session_id INTEGER,\"his_data_start_time\" DATETIME,"
            + "\"his_data_end_time\" DATETIME,\"task_start_time\" DATETIME,\"task_end_time\" DATETIME,"
            + "state TEXT,span TEXT,remarks TEXT,conf TEXT,threshold TEXT,task_type TEXT,"
            + "diagnosis_type TEXT,node_vo_sub TEXT,is_deleted INTEGER"
            + ",create_time DATETIME,update_time DATETIME);"};
    private static final String[] RESULT_SQL = {"CREATE TABLE \"his_diagnosis_result_info\" ("
            + "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,cluster_id TEXT,node_id TEXT,"
            + "task_id INTEGER,point_name TEXT,point_type TEXT,point_title TEXT,point_suggestion TEXT,"
            + "is_hint TEXT,point_data TEXT,point_detail TEXT,point_state TEXT,diagnosis_type TEXT,"
            + "is_deleted INTEGER,create_time DATETIME,update_time DATETIME);"};
    private static final String[] THRESHOLD_SQL = {
            "CREATE TABLE \"his_diagnosis_threshold_info\" ("
                    + "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,cluster_id TEXT,node_id TEXT,"
                    + "threshold_type TEXT,threshold TEXT,threshold_name TEXT,diagnosis_type TEXT,"
                    + "threshold_value TEXT,threshold_unit TEXT,threshold_detail TEXT,sort_no INTEGER,"
                    + "is_deleted INTEGER,create_time DATETIME,update_time DATETIME);",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"cpuUsageRate\",\"" + LocaleString.format(
                    "history.threshold.cpuUsageRate.title") + "\",\"50\",\"%\",\"" + LocaleString.format(
                    "history.threshold.cpuUsageRate.detail") + "\",\"1\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"dbCpuUsageRate\",\"" + LocaleString.format(
                    "history.threshold.dbCpuUsageRate.title") + "\",\"50\",\"%\",\"" + LocaleString.format(
                    "history.threshold.dbCpuUsageRate.detail") + "\",\"2\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"proCpuUsageRate\",\"" + LocaleString.format(
                    "history.threshold.proCpuUsageRate.title") + "\",\"50\",\"%\",\"" + LocaleString.format(
                    "history.threshold.proCpuUsageRate.detail") + "\",\"3\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"activityNum\",\"" + LocaleString.format(
                    "history.threshold.activityNum.title") + "\",\"10\",\"pcs\",\"" + LocaleString.format(
                    "history.threshold.activityNum.detail") + "\",\"4\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"threadPoolUsageRate\",\"" + LocaleString.format(
                    "history.threshold.threadPoolUsageRate.title") + "\",\"30\",\"%\",\"" + LocaleString.format(
                    "history.threshold.threadPoolUsageRate.detail") + "\",\"5\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"connectionNum\",\"" + LocaleString.format(
                    "history.threshold.connectionNum.title") + "\",\"10\",\"pcs\",\"" + LocaleString.format(
                    "history.threshold.connectionNum.detail") + "\",\"6\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"duration\",\"" + LocaleString.format(
                    "history.threshold.duration.title") + "\",\"60\",\"s\",\"" + LocaleString.format(
                    "history.threshold.duration.detail") + "\",\"7\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"sqlNum\",\"" + LocaleString.format(
                    "history.threshold.sqlNum.title") + "\",\"1\",\"pcs\",\"" + LocaleString.format(
                    "history.threshold.sqlNum.detail") + "\",\"8\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"waitEventNum\",\"" + LocaleString.format(
                    "history.threshold.waitEventNum.title") + "\",\"10\",\"pcs\",\"" + LocaleString.format(
                    "history.threshold.waitEventNum.detail") + "\",\"9\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"IO\",\"diskUtilization\",\"" + LocaleString.format(
                    "history.threshold.diskUtilization.title") + "\",\"50\",\"%\",\"" + LocaleString.format(
                    "history.threshold.diskUtilization.detail") + "\",\"10\",\"history\");",
            ThresholdCommon.INSERT
                    + "values(\"CPU\",\"sysCpu\",\"" + LocaleString.format(
                    "sql.threshold.sysCpu.title") + "\",\"50\",\"%\",\"" + LocaleString.format(
                    "sql.threshold.sysCpu.detail") + "\",\"1\",\"sql\");"};
    public static final String HISTORY_DIAGNOSIS_TASK = "hisDiagnosisTask";
    public static final String HISTORY_DIAGNOSIS_RESULT = "hisDiagnosisResult";
    public static final String HISTORY_DIAGNOSIS_THRESHOLD = "hisDiagnosisThreshold";


    @PostConstruct
    public void init() throws IOException {
        initSqlite(HIS_DIAGNOSIS_TASK_DB, TASK_SQL);
        initSqlite(HIS_DIAGNOSIS_RESULT_DB, RESULT_SQL);
        initSqlite(HIS_DIAGNOSIS_THRESHOLD_DB, THRESHOLD_SQL);
    }

    private void initSqlite(String path, String[] sqls) throws IOException {
        File file = new File(path);
        log.info("sqlite:" + file.getCanonicalPath());
        if (!file.exists()) {
            var parent = file.getParentFile();
            if (!parent.exists()) {
                boolean isCreate = parent.mkdirs();
                if (!isCreate) {
                    throw new HisDiagnosisException("Create fail!");
                }
            }
            boolean isCreate = file.createNewFile();
            if (!isCreate) {
                log.error("DataSourceIniter initSqlite createNewFile fail");
            }
            var sqLiteDataSource = new SQLiteDataSource();
            sqLiteDataSource.setUrl(JDBC.PREFIX + file.getCanonicalPath());
            try (var conn = sqLiteDataSource.getConnection()) {
                for (String sql : sqls) {
                    try {
                        conn.createStatement().execute(sql);
                    } catch (SQLException e) {
                        throw new CustomException(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                throw new HisDiagnosisException("error:", e);
            }
        }
    }

    @Bean
    public DynamicDataSourceProvider sqliteDataSourceInit() {
        return new DynamicDataSourceProvider() {
            @Override
            public Map<String, DataSource> loadDataSources() {
                var map = new HashMap<String, DataSource>();
                map.put(HISTORY_DIAGNOSIS_TASK, sqliteDatasource(HIS_DIAGNOSIS_TASK_DB));
                map.put(HISTORY_DIAGNOSIS_RESULT, sqliteDatasource(HIS_DIAGNOSIS_RESULT_DB));
                map.put(HISTORY_DIAGNOSIS_THRESHOLD, sqliteDatasource(HIS_DIAGNOSIS_THRESHOLD_DB));
                return map;
            }

            private DataSource sqliteDatasource(String path) {
                File taskdb = new File(path);
                var sqLiteDataSource = new SQLiteDataSource();
                try {
                    sqLiteDataSource.setUrl(
                            JDBC.PREFIX + taskdb.getCanonicalPath() + "?date_string_format=yyyy-MM-dd HH:mm:ss");
                } catch (IOException e) {
                    throw new CustomException(e.getMessage());
                }
                var datasource = new HikariDataSource();
                datasource.setMaximumPoolSize(1);
                datasource.setDataSource(sqLiteDataSource);
                return datasource;
            }
        };
    }
}
