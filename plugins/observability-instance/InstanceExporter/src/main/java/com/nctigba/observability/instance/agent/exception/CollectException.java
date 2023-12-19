/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  CollectException.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/exception/CollectException.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.exception;

import com.nctigba.observability.instance.agent.metric.Metric;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
    public CollectException(Metric metric, IOException e) {
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