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
 *  ThresholdDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/ThresholdDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ThresholdDTO
 *
 * @author luomeng
 * @since 2024/1/30
 */
@Data
@Accessors(chain = true)
public class ThresholdDTO {
    private String thresholdKey;
    private String thresholdValue;
}