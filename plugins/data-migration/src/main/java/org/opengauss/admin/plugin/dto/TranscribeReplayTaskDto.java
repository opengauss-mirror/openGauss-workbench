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

package org.opengauss.admin.plugin.dto;

import lombok.Data;

import java.util.List;

/**
 * TranscribeReplayTaskDto
 *
 * @since 2025-02-10
 */

@Data
public class TranscribeReplayTaskDto {
    private String taskName;
    private String sourceDbType;
    private String sourceIp;
    private Integer sourcePort;
    private String sourceInstallPath;
    private String targetIp;
    private Integer targetPort;
    private String targetInstallPath;
    private List<String> dbMap;
    private String sourceUser;
    private String targetUser;
    private String toolVersion;
    private String taskType;
    private String sourceNodeId;
    private String targetNodeId;
}
