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
 *  BusinessConnCountDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/point/BusinessConnCountDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto.point;

import com.nctigba.observability.sql.model.vo.point.ConnCountVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * BusinessConnCountDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class BusinessConnCountDTO {
    List<ConnCountVO> connCount;
    List<AspAnalysisDTO> timeSlot;
}
