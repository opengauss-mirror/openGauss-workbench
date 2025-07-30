/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 */

package org.opengauss.admin.plugin.config;

import lombok.Data;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;

import java.util.Map;

/**
 * ConfigDbStorageParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigDbStorageParams implements TranscribeReplayConstants {
    private String sqlDatabaseIp;
    private String sqlDatabasePort;
    private String sqlDatabaseUsername;
    private String sqlDatabaseName;
    private String sqlDatabasePassword;
    private String sqlTableName;

    /**
     * setDbStorConfig
     *
     * @param config configParams
     */
    public void setDbStorConfig(Map<String, String> config) {
        this.sqlDatabaseIp = config.get(SQL_DATABASE_IP);
        this.sqlDatabasePort = config.get(SQL_DATABASE_PORT);
        this.sqlDatabaseUsername = config.get(SQL_DATABASE_USERNAME);
        this.sqlDatabaseName = config.get(SQL_DATABASE_NAME);
        this.sqlTableName = config.get(SQL_TABLE_NAME);
    }
}
