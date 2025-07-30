/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TranscribeReplayPwdInfo
 *
 * @since 2025/7/30
 */
@Data
@AllArgsConstructor
public class TranscribeReplayPwdInfo {
    private String remotePassword;
    private String sourceNodePassword;
    private String targetNodePassword;
}
