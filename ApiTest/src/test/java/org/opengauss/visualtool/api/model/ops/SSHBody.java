/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.visualtool.api.model.ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ops host ssh body
 *
 * @since 2024/10/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSHBody {
    private String ip;
    private Integer sshPort;
    private String sshUsername;
    private String sshPassword;
    private String businessId;
}
