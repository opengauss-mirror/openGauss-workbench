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

package com.nctigba.observability.instance.agent.exception;

import com.nctigba.observability.instance.agent.metric.Metric;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception occur when collecting metric value
 *
 * @since 2023/12/1
 */
@Slf4j
public class CollectException extends RuntimeException {
    /**
     * Constructor
     *
     * @param metric which metric occur this exception
     * @param e      Parent exception
     * @since 2023/12/1
     */
    public CollectException(Metric metric, Exception e) {
        super(e);
    }

    /**
     * Constructor
     *
     * @param msg Error message
     * @since 2023/12/1
     */
    public CollectException(String msg) {
        super(msg);
        log.error("CollectException:{}", msg);
    }
}