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
 * ConfigTranscribeAttachParams
 *
 * @author: ybx
 * @since: 2025/02/18
 */
@Data
public class ConfigTranscribeAttachParams implements TranscribeReplayConstants {
    private String sqlTranscribeMode;
    private String attachProcessPid;
    private String attachTargetSchema;
    private String attachCaptureDuration;
    private String fileCountLimit;
    private String sqlFileName;
    private String sqlFileSize;
    private String attachPluginPath;
    private String sqlFilePath;

    /**
     * setAttachConfig
     *
     * @param tp TranscribeReplayTaskDto
     * @param config configParams
     * @param downloadId downloadId
     */
    public void setAttachConfig(TranscribeReplayTaskDto tp, Map<String, String> config, Integer downloadId) {
        this.sqlTranscribeMode = config.get(SQL_TRANSCRIBE_MODE);
        this.attachProcessPid = config.get(ATTACH_PROCESS_PID);
        this.attachTargetSchema = config.get(ATTACH_TARGET_SCHEMA);
        this.attachCaptureDuration = config.get(ATTACH_CAPTURE_DURATION);
        this.fileCountLimit = config.get(FILE_COUNT_LIMIT);
        this.sqlFileName = config.get(SQL_FILE_NAME);
        this.sqlFileSize = config.get(SQL_FILE_SIZE);
        this.attachPluginPath = tp.getSourceInstallPath() + "/" + downloadId + "/transcribe-replay-tool/plugin";
        this.sqlFilePath = tp.getSourceInstallPath() + "/" + downloadId + "/" + JSON_PATH;
    }
}
