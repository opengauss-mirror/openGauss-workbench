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

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * tb transcribe replay task
 *
 * @since 2024/12/28
 */
@Data
@TableName("tb_transcribe_replay_task")
public class TranscribeReplayTask {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String taskName;
    private String dbName;
    private String sourceDbType;
    private String sourceInstallPath;
    private String targetInstallPath;
    private String toolVersion;
    private String taskType;
    private Integer executionStatus;
    private int slowSqlCount;
    private int failedSqlCount;
    private long taskDuration;
    private long sourceDuration;
    private long targetDuration;
    private long totalNum;
    private long parseNum;
    private long replayNum;
    private String sourceNodeId;
    private String targetNodeId;
    private String errorMsg;
    @TableField(exist = false)
    private List<String> dbMap;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskEndTime;
}
