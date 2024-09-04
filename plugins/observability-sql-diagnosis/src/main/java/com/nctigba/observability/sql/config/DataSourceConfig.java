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
 *  DataSourceConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/config/DataSourceConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * DataSourceConfig
 *
 * @author liupengfei
 * @since 2023/12/5
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DataSourceConfig {
    static final String GS_DRIVER = "org.opengauss.Driver";
    static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    static final String SQLITE_URL = "jdbc:sqlite:data/";
    static final String SQLITE_INSTANCE_URL = SQLITE_URL + "observability-instance-data.db";

    @Autowired
    DynamicDataSourceProperties properties;
    @Autowired
    DruidDataSourceCreator druidDataSourceCreator;

    /**
     * primary dataSource
     *
     * @return DataSource
     * @throws IOException e
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @Profile("!dev")
    public DataSource dataSource() throws IOException {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        DataSourceProperty primaryProperty = new DataSourceProperty();
        primaryProperty.setUrl(environmentProvider.getString("spring.datasource.url"));
        primaryProperty.setUsername(environmentProvider.getString("spring.datasource.username"));
        primaryProperty.setPassword(environmentProvider.getString("spring.datasource.password"));
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");
        primaryProperty.setDriverClassName(driverClassName);
        DataSource primary = druidDataSourceCreator.doCreateDataSource(primaryProperty);
        var d = new DynamicRoutingDataSource();
        d.addDataSource("primary", primary);
        d.setPrimary("primary");
        if (GS_DRIVER.equals(driverClassName)) {
            for (Map.Entry<String, DataSourceProperty> dataSourceProperty : properties.getDatasource().entrySet()) {
                dataSourceProperty.getValue().setDriverClassName(SQLITE_DRIVER);
                if ("instanceAgent".equals(dataSourceProperty.getKey())) {
                    dataSourceProperty.getValue().setUrl(SQLITE_INSTANCE_URL);
                    continue;
                }
                String url = SQLITE_URL + dataSourceProperty.getKey() + ".db";
                dataSourceProperty.getValue().setUrl(url);
                dataSourceProperty.getValue().getDruid().setMaxActive(1);
                String[] split = url.split(":");
                String dbFile = split[split.length - 1];
                initDbFile(dbFile);
            }
        }
        return d;
    }

    private void initDbFile(String file) throws IOException {
        File dbFile = new File(file);
        File dbDir = dbFile.getParentFile();
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }
    }
}
