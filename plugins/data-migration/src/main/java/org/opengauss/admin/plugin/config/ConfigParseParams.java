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
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;

import java.util.Map;

/**
 * ConfigParseParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigParseParams implements TranscribeReplayConstants {
    private String parseSelectResult;
    private String selectResultPath;
    private String resultFileName;
    private String resultFileSize;
    private String sqlStorageMode;
    private String tcpdumpDatabaseType;
    private String queueSizeLimit;
    private String packetBatchSize;
    private String tcpdumpFilePath;
    private String tcpdumpDatabaseIp;
    private Integer tcpdumpDatabasePort;
    private String sqlFilePath;
    private String sqlFileSize;
    private String sqlFileName;
    private String sqlTableDrop;

    /**
     * setParseConfig
     *
     * @param tp TranscribeReplayTaskDto
     * @param config configParams
     * @param taskPath taskPath
     */
    public void setParseConfig(TranscribeReplayTaskDto tp, Map<String, String> config, String taskPath) {
        this.parseSelectResult = config.get(PARSE_SELECT_RESULT);
        this.selectResultPath = taskPath;
        this.resultFileName = config.get(RESULT_FILE_NAME);
        this.resultFileSize = config.get(RESULT_FILE_SIZE);
        this.sqlStorageMode = config.get(SQL_STORAGE_MODE);
        this.tcpdumpDatabaseType = tp.getSourceDbType().toLowerCase();
        this.queueSizeLimit = config.get(QUEUE_SIZE_LIMIT);
        this.packetBatchSize = config.get(PACKET_BATCH_SIZE);
        this.tcpdumpFilePath = taskPath + "/" + PCAP_FILE_PATH;
        this.tcpdumpDatabaseIp = tp.getSourceIp();
        this.tcpdumpDatabasePort = tp.getSourcePort();
        this.sqlFilePath = taskPath + "/" + JSON_PATH;
        this.sqlFileSize = config.get(SQL_FILE_SIZE);
        this.sqlFileName = config.get(SQL_FILE_NAME);
        this.sqlTableDrop = config.get(SQL_TABLE_DROP);
    }
}
