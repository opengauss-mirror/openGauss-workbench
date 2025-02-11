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

import lombok.Data;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;
import org.opengauss.admin.plugin.vo.ShellInfoVo;

import java.util.Map;

/**
 * TranscribeReplayWriteFile
 *
 * @since 2025/02/11
 */
@Data
public class TranscribeReplayWriteFile {
    private String remotePath;
    private TranscribeReplayTaskDto tp;
    private Integer id;
    private String fileName;
    private Map<String, Object> context;
    private ShellInfoVo shellInfoVo;
}
