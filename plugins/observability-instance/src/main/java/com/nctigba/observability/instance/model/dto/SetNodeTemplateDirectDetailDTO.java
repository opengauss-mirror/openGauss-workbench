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
 *  SetNodeTemplateDirectDetailDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/SetNodeTemplateDirectDetailDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * DTO for details of setting metrics interval for one database node
 *
 * @since 2023/12/1
 */
@Data
@ApiModel("DTO for details of setting metrics interval for one database node")
public class SetNodeTemplateDirectDetailDTO {
    @ApiModelProperty(value = "Metric group key", required = true)
    private String metricKey;
    @ApiModelProperty(value = "Scrape interval for this metric group", required = true)
    private String interval;
    @ApiModelProperty(value = "is the metric open", required = true)
    private Boolean isEnable;
}
