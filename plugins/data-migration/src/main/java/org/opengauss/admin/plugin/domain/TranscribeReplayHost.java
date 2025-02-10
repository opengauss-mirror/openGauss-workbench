/*
 * Copyright (c)Huawei Technologies Co.,Ltd 2025.
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

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * tb transcribe replay task detail
 *
 * @since 2024/12/28
 */
@Data
@TableName("tb_transcribe_replay_host")
public class TranscribeReplayHost {
    private Integer id;
    private Integer taskId;
    private String ip;
    private Integer port;
    private String userName;
    private String passwd;
    private String dbType;
}
