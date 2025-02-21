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
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;

import java.util.Map;

import static org.opengauss.admin.plugin.enums.TranscribeReplaySqlTransMode.TCPDUMP;

/**
 * ConfigTcpdumpAndAttachSameParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigTcpdumpAndAttachSameParams implements TranscribeReplayConstants {
    private String shouldCheckSystem;
    private String maxCpuThreshold;
    private String maxMemoryThreshold;
    private String maxDiskThreshold;
    private String shouldSendFile;
    private String remoteRetryCount;
    private String remoteReceiverPassword;
    private String remoteFilePath;
    private String remoteReceiverName;
    private String remoteNodeIp;
    private Integer remoteNodePort;

    /**
     * setTcpAndAttachSameConfig
     *
     * @param tp TranscribeReplayTaskDto
     * @param config configParams
     * @param downloadId downloadId
     * @param targetOpsHostEntity targetOpsHostEntity
     * @param targetOpsHostUserEntity targetOpsHostUserEntity
     */
    public void setTcpAndAttachSameConfig(TranscribeReplayTaskDto tp, Map<String, String> config, int downloadId,
        OpsHostEntity targetOpsHostEntity, OpsHostUserEntity targetOpsHostUserEntity) {
        this.shouldCheckSystem = config.get(SHOULD_CHECK_SYSTEM);
        this.maxCpuThreshold = config.get(MAX_CPU_THRESHOLD);
        this.maxMemoryThreshold = config.get(MAX_MEMORY_THRESHOLD);
        this.maxDiskThreshold = config.get(MAX_DISK_THRESHOLD);
        this.shouldSendFile = config.get(SHOULD_SEND_FILE);
        this.remoteRetryCount = config.get(REMOTE_RETRY_COUNT);
        this.remoteReceiverPassword = targetOpsHostUserEntity.getPassword();
        if (TCPDUMP.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            this.remoteFilePath = tp.getTargetInstallPath() + "/" + downloadId + "/" + PCAP_FILE_PATH;
        } else {
            this.remoteFilePath = tp.getTargetInstallPath() + "/" + downloadId + "/" + JSON_PATH;
        }
        this.remoteReceiverName = tp.getTargetUser();
        this.remoteNodeIp = tp.getTargetIp();
        this.remoteNodePort = targetOpsHostEntity.getPort();
    }
}
