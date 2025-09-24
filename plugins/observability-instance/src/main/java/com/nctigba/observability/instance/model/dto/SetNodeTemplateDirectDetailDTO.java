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

import lombok.Data;

/**
 * DTO for details of setting metrics interval for one database node
 *
 * @since 2023/12/1
 */
@Data
public class SetNodeTemplateDirectDetailDTO {
    private String metricKey;
    private String interval;
    private Boolean isEnable;
}
