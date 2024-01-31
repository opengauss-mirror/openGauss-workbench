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

package org.opengauss.tun.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.opengauss.tun.common.FixedTuning;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * DataSourceConfig
 *
 * @author liu
 * @since 2023-09-17
 */
@Configuration
@Order(2)
public class DataSourceConfig {
    /**
     * dataSource
     *
     * @return DataSource dataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource dataSource() {
        // read config from dataKit platform
        String username = "";
        String secret = "";
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(FixedTuning.DRIVER_CLASS_NAME);
        dataSource.setUrl(FixedTuning.URL);
        dataSource.setUsername(username);
        dataSource.setPassword(secret);
        return dataSource;
    }
}
