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

package com.nctigba.observability.log.model.vo;

import lombok.Data;

/**
 * AgentExceptionVO
 *
 * @author luomeng
 * @since 2024/1/17
 */
@Data
public class AgentExceptionVO {
    String exceptionInfo;
    boolean isStatus;

    /**
     * Set data
     *
     * @param isStatus Boolean value
     * @param exceptionInfo Exception info
     * @return AgentExceptionVO
     */
    public AgentExceptionVO setAgentStatus(boolean isStatus, String exceptionInfo) {
        this.isStatus = isStatus;
        this.exceptionInfo = exceptionInfo;
        return this;
    }
}
