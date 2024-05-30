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
 *  MetricQueryDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/dto/MetricQueryDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto;

import lombok.Data;

/**
 * MetricQueryDTO
 *
 * @author luomeng
 * @since 2024/4/23
 */
@Data
public class MetricQueryDTO {
    private Enum<?>[] metricsArr;
    private String nodeId;
    private Long start;
    private Long end;
    private Integer step;
    private String dbName;

    public MetricQueryDTO(Enum<?>[] metricsArr, String nodeId, Long start, Long end, Integer step, String dbName) {
        this.metricsArr = metricsArr;
        this.nodeId = nodeId;
        this.start = start;
        this.end = end;
        this.step = step;
        this.dbName = dbName;
    }
}
