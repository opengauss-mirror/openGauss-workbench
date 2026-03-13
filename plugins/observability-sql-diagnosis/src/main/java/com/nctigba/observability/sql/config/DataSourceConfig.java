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
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import com.nctigba.observability.sql.enums.DbDataLocationEnum;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * DataSourceConfig
 *
 * @author liupengfei
 * @since 2023/12/5
 */
@Configuration
@Slf4j
public class DataSourceConfig {
    static final String GS_DRIVER = "org.opengauss.Driver";
    static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    static final String SQLITE_URL = "jdbc:sqlite:";
    static final String HIS_SLOW_SQL_INFO_DS = "hisSlowSqlInfo";
    static final String HIS_SLOW_SQL_INFO_SQLITE_URL = "jdbc:sqlite:data/history-slowsql-info.db";

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
        DataSource primary = druidDataSourceCreator.createDataSource(primaryProperty);
        DynamicRoutingDataSource d = new DynamicRoutingDataSource(new ArrayList<>());
        d.addDataSource("primary", primary);
        d.setPrimary("primary");
        for (Map.Entry<String, DataSourceProperty> dataSourceProperty : properties.getDatasource().entrySet()) {
            String dsName = dataSourceProperty.getKey();
            DataSourceProperty dsProperty = dataSourceProperty.getValue();
            // Force historical slow SQL metadata storage to sqlite for compatibility with sqlite-specific SQL.
            if (HIS_SLOW_SQL_INFO_DS.equals(dsName)) {
                dsProperty.setDriverClassName(SQLITE_DRIVER);
                dsProperty.setUrl(HIS_SLOW_SQL_INFO_SQLITE_URL);
            }
            DataSource emDataSource = druidDataSourceCreator.createDataSource(dsProperty);
            if (SQLITE_DRIVER.equals(dsProperty.getDriverClassName())) {
                String url = dsProperty.getUrl();
                String dbFile = url.substring(SQLITE_URL.length());
                initDbFile(dbFile);
            }
            d.addDataSource(dsName, emDataSource);
        }
        return d;
    }

    /**
     * DataSourceScriptDatabaseInitializer
     *
     * @param dataSource DataSource
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean("diagnosisDataSourceScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource) {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");
        Optional<DbDataLocationEnum> optional = DbDataLocationEnum.of(driverClassName);
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource primary = drds.getDataSource("primary");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(true);
        settings.setSeparator(";");
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        if (optional.isEmpty()) {
            return new DataSourceScriptDatabaseInitializer(primary, new DatabaseInitializationSettings());
        }
        DbDataLocationEnum dataLocationEnum = optional.get();
        settings.setDataLocations(dataLocationEnum.getLocations());
        return new DataSourceScriptDatabaseInitializer(primary, settings);
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

    /**
     * diagnosisSourcesScriptDatabaseInitializer
     *
     * @param dataSource DataSource
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean("diagnosisSourcesScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer diagnosisSourcesScriptDatabaseInitializer(DataSource dataSource) {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource embedded = drds.getDataSource("diagnosisSources");
        DataSourceProperty property = properties.getDatasource().get("diagnosisSources");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(property.getInit().isContinueOnError());
        settings.setSeparator(property.getInit().getSeparator());
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setDataLocations(Arrays.asList(property.getInit().getData()));
        return new DataSourceScriptDatabaseInitializer(embedded, settings);
    }

    /**
     * hisDiagnosisTaskInfoScriptDatabaseInitializer
     *
     * @param dataSource DataSource
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean("hisDiagnosisTaskInfoScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer hisDiagnosisTaskInfoScriptDatabaseInitializer(DataSource dataSource) {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource embedded = drds.getDataSource("hisDiagnosisTaskInfo");
        DataSourceProperty property = properties.getDatasource().get("hisDiagnosisTaskInfo");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(property.getInit().isContinueOnError());
        settings.setSeparator(property.getInit().getSeparator());
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setDataLocations(Arrays.asList(property.getInit().getData()));
        return new DataSourceScriptDatabaseInitializer(embedded, settings);
    }

    /**
     * hisDiagnosisThresholdInfoScriptDatabaseInitializer
     *
     * @param dataSource DataSource
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean("hisDiagnosisThresholdInfoScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer hisDiagnosisThresholdInfoScriptDatabaseInitializer(DataSource dataSource) {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource embedded = drds.getDataSource("hisDiagnosisThresholdInfo");
        DataSourceProperty property = properties.getDatasource().get("hisDiagnosisThresholdInfo");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(property.getInit().isContinueOnError());
        settings.setSeparator(property.getInit().getSeparator());
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setDataLocations(Arrays.asList(property.getInit().getData()));
        return new DataSourceScriptDatabaseInitializer(embedded, settings);
    }

    @Bean("paramInfoScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer paramInfoScriptDatabaseInitializer(DataSource dataSource) {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource embedded = drds.getDataSource("paramInfo");
        DataSourceProperty property = properties.getDatasource().get("paramInfo");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(property.getInit().isContinueOnError());
        settings.setSeparator(property.getInit().getSeparator());
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setDataLocations(Arrays.asList(property.getInit().getData()));
        return new DataSourceScriptDatabaseInitializer(embedded, settings);
    }

    /**
     * hisSlowSqlInfoScriptDatabaseInitializer
     *
     * @param dataSource DataSource
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean("hisSlowSqlInfoScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer hisSlowSqlInfoScriptDatabaseInitializer(DataSource dataSource) {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource embedded = drds.getDataSource("hisSlowSqlInfo");
        DataSourceProperty property = properties.getDatasource().get("hisSlowSqlInfo");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(property.getInit().isContinueOnError());
        settings.setSeparator(property.getInit().getSeparator());
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setDataLocations(Arrays.asList(property.getInit().getData()));
        return new DataSourceScriptDatabaseInitializer(embedded, settings);
    }
}
