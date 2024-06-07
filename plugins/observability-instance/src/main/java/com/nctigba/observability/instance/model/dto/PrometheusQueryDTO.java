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
 *  PrometheusQueryDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/PrometheusQueryDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto;

import lombok.Data;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

/**
 * PrometheusQueryDTO
 *
 * @author luomeng
 * @since 2024/4/23
 */
@Data
public class PrometheusQueryDTO {
    private OpsClusterNodeVO node;
    private Long start;
    private Long end;
    private Integer step;
    private Object metric;
    private String dbName;
}
