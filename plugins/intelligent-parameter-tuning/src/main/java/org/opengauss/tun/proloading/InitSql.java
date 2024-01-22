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

package org.opengauss.tun.proloading;

import com.alibaba.druid.pool.DruidDataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.tun.common.FixedTuning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * LoadCache
 *
 * @author liu
 * @since 2023-12-20
 */
@Component
@Order(3)
@Slf4j
public class InitSql implements CommandLineRunner {
    @Autowired
    private DruidDataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        initDatabase(dataSource, FixedTuning.SQL_PATH);
    }

    private void initDatabase(DataSource dataSource, String sqlFile) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Read and execute SQL statements
            String sqlScript = readSqlScript(sqlFile);
            statement.executeUpdate(sqlScript);
            log.info("Database initialized.");
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private String readSqlScript(String sqlFile) {
        // Reading SQL statements from a file
        StringBuilder sqlScript = new StringBuilder();
        try (InputStream inputStream = InitSql.class.getClassLoader().getResourceAsStream(sqlFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sqlScript.append(line);
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        return sqlScript.toString();
    }
}
