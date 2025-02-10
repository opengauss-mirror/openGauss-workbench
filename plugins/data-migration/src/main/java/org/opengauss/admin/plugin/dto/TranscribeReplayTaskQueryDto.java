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

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.util.Date;

/**
 * TranscribeReplayTaskQueryDto
 *
 * @since 2025-02-10
 */

@Data
public class TranscribeReplayTaskQueryDto {
    private Integer taskId;
    private String taskName;
    private Integer executionStatus;
    private long taskDuration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskEndTime;
}
