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
 *  PrometheusDataDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/point/PrometheusDataDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto.point;

import com.nctigba.observability.sql.model.dto.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * PrometheusDataDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class PrometheusDataDTO {
    private String chartName;
    private String unit;
    private List<String> time;
    private List<MetricDataDTO> datas;
    private List<AspAnalysisDTO> data;
}
