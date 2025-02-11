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

package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TranscribeReplayStatus
 *
 * @since 2025-02-10
 */

@Getter
@AllArgsConstructor
public enum TranscribeReplayStatus {
    DOWNLOADING(-1, "downloading"),
    DOWNLOAD_FAIL(-2, "download fail"),
    NOT_RUN(0, "not run"),
    RUNNING(1, "running"),
    FINISH(2, "finish"),
    RUN_FAIL(3, "run fail");

    private final Integer code;
    private final String command;
}
