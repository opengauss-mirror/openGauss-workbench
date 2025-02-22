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
 * ConfigTranscribeTcpdumpParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigTranscribeTcpdumpParams implements TranscribeReplayConstants {
    private String sqlTranscribeMode;
    private String tcpdumpNetworkInterface;
    private String tcpdumpCaptureDuration;
    private String tcpdumpFileName;
    private String tcpdumpFileSize;
    private String fileCountLimit;
    private String tcpdumpPluginPath;
    private int tcpdumpDatabasePort;
    private String tcpdumpFilePath;

    /**
     * setParseConfig
     *
     * @param tp TranscribeReplayTaskDto
     * @param config configParams
     * @param tcpdumpPath tcpdumpPath
     * @param downloadId downloadId
     */
    public void setTcpDumpConfig(TranscribeReplayTaskDto tp, Map<String, String> config, String tcpdumpPath,
                                 int downloadId) {
        this.sqlTranscribeMode = config.get(SQL_TRANSCRIBE_MODE);
        this.tcpdumpNetworkInterface = config.get(TCPDUMP_NETWORK_INTERFACE);
        this.tcpdumpCaptureDuration = config.get(TCPDUMP_CAPTURE_DURATION);
        this.tcpdumpFileName = config.get(TCPDUMP_FILE_NAME);
        this.tcpdumpFileSize = config.get(TCPDUMP_FILE_SIZE);
        this.fileCountLimit = config.get(FILE_COUNT_LIMIT);
        this.tcpdumpPluginPath = tcpdumpPath;
        this.tcpdumpDatabasePort = tp.getSourcePort();
        this.tcpdumpFilePath = tp.getSourceInstallPath() + "/" + downloadId + "/" + PCAP_FILE_PATH;
    }
}
