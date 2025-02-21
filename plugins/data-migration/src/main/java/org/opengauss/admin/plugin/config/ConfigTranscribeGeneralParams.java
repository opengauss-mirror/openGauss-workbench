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
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;

import java.util.Map;

/**
 * ConfigTranscribeGeneralParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigTranscribeGeneralParams implements TranscribeReplayConstants {
    private String sqlTranscribeMode;
    private String databaseUsername;
    private String fileCountLimit;
    private String databasePassword;
    private String sqlBatch;
    private String startTime;
    private String sqlStorageMode;
    private String sqlTableDrop;
    private String sqlFilePath;
    private String databaseIp;
    private Integer databasePort;

    /**
     * setGeneralConfig
     *
     * @param tp TranscribeReplayTaskDto
     * @param config configParams
     * @param downloadId downloadId
     */
    public void setGeneralConfig(TranscribeReplayTaskDto tp, Map<String, String> config, Integer downloadId) {
        this.sqlTranscribeMode = config.get(SQL_TRANSCRIBE_MODE);
        this.databaseUsername = config.get(GENERAL_DATABASE_USERNAME);
        this.fileCountLimit = config.get(FILE_COUNT_LIMIT);
        this.databasePassword = config.get(GENERAL_DATABASE_PASSWORD);
        this.sqlBatch = config.get(GENERAL_SQL_BATCH);
        this.startTime = config.get(GENERAL_START_TIME);
        this.sqlStorageMode = config.get(SQL_STORAGE_MODE);
        this.sqlTableDrop = config.get(SQL_TABLE_DROP);
        this.sqlFilePath = tp.getSourceInstallPath() + "/" + downloadId + "/" + JSON_PATH;
        this.databaseIp = tp.getSourceIp();
        this.databasePort = tp.getSourcePort();
    }
}
