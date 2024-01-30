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
 */

package com.nctigba.datastudio.config;

import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * DataSourceConfig
 *
 * @author liupengfei
 * @since 2024/1/25
 */
@Configuration
public class DataSourceConfig {
    static final String GS_DRIVER = "org.opengauss.Driver";
    static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    static final String DB_PATH = "data/ds.db";
    static final String SQLITE_URL = "jdbc:sqlite:" + DB_PATH;

    @Autowired
    DataSourceProperties properties;

    /**
     * dataSource
     *
     * @return DataSource
     * @throws IOException e
     */
    @Bean
    @Profile("!dev")
    public DataSource dataSource() throws IOException {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");
        if (GS_DRIVER.equals(driverClassName) && !SQLITE_DRIVER.equals(properties.getDriverClassName())) {
            properties.setDriverClassName(SQLITE_DRIVER);
            properties.setUrl(SQLITE_URL);
            initDbFile();
        }
        return properties.initializeDataSourceBuilder().build();
    }

    private void initDbFile() throws IOException {
        File dbFile = new File(DB_PATH);
        File dbDir = dbFile.getParentFile();
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }
    }
}
