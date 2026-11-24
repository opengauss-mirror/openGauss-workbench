/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.collect.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * LinuxConfig
 *
 * @author liu
 * @since 2023-10-15
 */
@Data
public class LinuxConfig {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long configId;
    private String configName;
    private String host;
    private Integer port;
    private String userName;
    private String password;
}
