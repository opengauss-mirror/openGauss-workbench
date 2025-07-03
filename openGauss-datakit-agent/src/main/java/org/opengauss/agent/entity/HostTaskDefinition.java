/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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

package org.opengauss.agent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * HostTaskDefinition
 *
 * @author: wangchao
 * @Date: 2025/4/8 15:44
 * @Description: HostTaskDefinition
 * @since 7.0.0-RC2
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class HostTaskDefinition extends TaskDefinition {
    private boolean isLocal;

    /**
     * HostTaskDefinition
     *
     * @param isLocal isLocal
     */
    public HostTaskDefinition(boolean isLocal) {
        this.isLocal = isLocal;
    }

    /**
     * HostTaskDefinition
     */
    public HostTaskDefinition() {
        this(true);
    }
}
