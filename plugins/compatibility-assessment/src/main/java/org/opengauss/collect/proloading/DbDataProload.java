/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.proloading;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.collect.config.common.DataBaseType;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * DbDataProload
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Component
public class DbDataProload {
    private static final String DB_PATH = "data/collect.db";

    /**
     * initDb
     *
     * @throws IOException IOException
     */
    @PostConstruct
    private void initDb() throws IOException {
        // 创建数据库文件
        File dbFile1 = new File(DB_PATH);
        File dbDir = dbFile1.getParentFile(); // 获取数据库文件所在目录路径
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        if (!dbFile1.exists()) {
            dbFile1.createNewFile();
            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName(DataBaseType.DRIVER_CLASS_NAME);
            dataSourceBuilder.url(DataBaseType.URL);
            dataSourceBuilder.username("");
            dataSourceBuilder.password("");
            DataSource dataSource = dataSourceBuilder.build();
            initDatabase(dataSource);
        }
    }

    private void initDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // 执行SQL语句
            statement.executeUpdate("DROP TABLE IF EXISTS collect_period");
            statement.executeUpdate("CREATE TABLE collect_period (task_id TEXT, "
                    + "host TEXT, "
                    + "task_name TEXT, "
                    + "start_time TEXT, "
                    + "end_time TEXT, "
                    + "time_interval TEXT, "
                    + "pid INTEGER, "
                    + "file_path TEXT,"
                    + "current_status TEXT not null default waiting, "
                    + "is_switch_status Boolean not null default false, "
                    + "primary key(task_id))");
            statement.executeUpdate("DROP TABLE IF EXISTS assessment");
            statement.executeUpdate("CREATE TABLE assessment ("
                    + "assessment_id TEXT,"
                    + "sql_input_type TEXT,"
                    + "proccess_pid TEXT,"
                    + "assessmenttype TEXT,"
                    + "filedir TEXT,"
                    + "sqltype TEXT,"
                    + "mysql_password TEXT,"
                    + "mysql_user TEXT,"
                    + "mysql_port TEXT,"
                    + "mysql_host TEXT,"
                    + "mysql_dbname TEXT,"
                    + "opengauss_user TEXT,"
                    + "opengauss_password TEXT,"
                    + "opengauss_port TEXT,"
                    + "opengauss_host TEXT,"
                    + "opengauss_dbname TEXT,"
                    + "start_time TEXT,"
                    + "report_file_name TEXT,"
                    + "primary key(assessment_id))");
            log.info("Database initialized.");
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}

