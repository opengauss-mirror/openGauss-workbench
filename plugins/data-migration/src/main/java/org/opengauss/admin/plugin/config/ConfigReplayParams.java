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

import static org.opengauss.admin.plugin.enums.TranscribeReplaySqlTransMode.GENERAL;
import static org.opengauss.admin.plugin.enums.TranscribeReplaySqlTransMode.TCPDUMP;

/**
 * ConfigReplayParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigReplayParams implements TranscribeReplayConstants {
    private String sqlStorageMode;
    private String sqlReplayMultiple;
    private String sqlReplayOnlyQuery;
    private String sqlReplayStrategy;
    private String sqlReplayParallelMaxPoolSize;
    private String replayMaxTime;
    private String sqlReplaySlowSqlRule;
    private String sqlReplaySlowTimeDifferenceThreshold;
    private String sqlReplaySlowSqlDurationThreshold;
    private String sqlReplaySlowTopNumber;
    private String sqlReplayDrawInterval;
    private String sqlReplaySessionWhiteList;
    private String sqlReplaySessionBlackList;
    private String sqlReplayDatabaseUsername;
    private String sqlReplayDatabasePassword;
    private String sqlReplaySlowSqlCsvDir;
    private String sqlReplayDatabaseIp;
    private Integer sqlReplayDatabasePort;
    private String sqlReplayDatabaseSchemaMap;
    private String sqlFilePath;
    private String sqlFileName;

    /**
     * setRepConfig
     *
     * @param tp TranscribeReplayTaskDto
     * @param config configParams
     * @param schemaMap schemaMap
     * @param downloadId downloadId
     */
    public void setRepConfig(TranscribeReplayTaskDto tp, Map<String, String> config, String schemaMap,
                             Integer downloadId) {
        this.sqlStorageMode = config.get(SQL_STORAGE_MODE);
        this.sqlReplayMultiple = config.get(SQL_REPLAY_MULTIPLE);
        this.sqlReplayOnlyQuery = config.get(SQL_REPLAY_ONLY_QUERY);
        if (TCPDUMP.getMode().equals(config.get(SQL_STORAGE_MODE))) {
            this.sqlReplayStrategy = config.get(SQL_REPLAY_STRATEGY);
            this.sqlReplayParallelMaxPoolSize = config.get(SQL_REPLAY_PARALLEL_MAX_POOL_SIZE);
        } else {
            this.sqlReplayStrategy = "serial";
            this.sqlReplayParallelMaxPoolSize = "1";
        }
        this.replayMaxTime = config.get(REPLAY_MAX_TIME);
        this.sqlReplaySlowSqlRule = config.get(SQL_REPLAY_SLOW_SQL_RULE);
        this.sqlReplaySlowTimeDifferenceThreshold = config.get(SQL_REPLAY_SLOW_TIME_DIFFERENCE_THRESHOLD);
        this.sqlReplaySlowSqlDurationThreshold = config.get(SQL_REPLAY_SLOW_SQL_DURATION_THRESHOLD);
        this.sqlReplaySlowTopNumber = config.get(SQL_REPLAY_SLOW_TOP_NUMBER);
        this.sqlReplayDrawInterval = config.get(SQL_REPLAY_DRAW_INTERVAL);
        this.sqlReplaySessionWhiteList = config.get(SQL_REPLAY_SESSION_WHITE_LIST);
        this.sqlReplaySessionBlackList = config.get(SQL_REPLAY_SESSION_BLACK_LIST);
        this.sqlReplayDatabaseUsername = config.get(SQL_REPLAY_DATABASE_USERNAME);
        this.sqlReplayDatabasePassword = config.get(SQL_REPLAY_DATABASE_PASSWORD);
        this.sqlReplaySlowSqlCsvDir = config.get(SQL_REPLAY_SLOW_SQL_CSV_DIR);
        if (GENERAL.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            this.sqlReplaySlowSqlCsvDir = tp.getSourceInstallPath() + "/" + downloadId;
            this.sqlFilePath = tp.getSourceInstallPath() + "/" + downloadId + "/" + JSON_PATH;
        } else {
            this.sqlReplaySlowSqlCsvDir = tp.getTargetInstallPath() + "/" + downloadId;
            this.sqlFilePath = tp.getTargetInstallPath() + "/" + downloadId + "/" + JSON_PATH;
        }
        this.sqlReplayDatabaseIp = tp.getTargetIp();
        this.sqlReplayDatabasePort = tp.getTargetPort();
        this.sqlReplayDatabaseSchemaMap = schemaMap;
        this.sqlFileName = config.get(SQL_FILE_NAME);
    }
}
