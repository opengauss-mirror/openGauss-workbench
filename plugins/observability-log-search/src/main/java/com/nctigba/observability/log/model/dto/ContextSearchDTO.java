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
 *  ContextSearchDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/dto/ContextSearchDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.model.dto;

import lombok.Data;

/**
 * <p>
 * Log-ContextSearch response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/02/01 09:05
 */
@Data
public class ContextSearchDTO {
    private Object logTime;
    private Object logType;
    private Object logLevel;
    private Object logData;
    private Object logClusterId;
    private Object logNodeId;
    private String id;
}
