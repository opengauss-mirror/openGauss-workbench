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
 *  LogTypeTreeDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/dto/LogTypeTreeDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.model.dto;

import java.util.List;

import lombok.Data;

/**
 * <p>
 * Log-Search response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
public class LogTypeTreeDTO {
    private String typeName;
    List<LogTypeTreeDTO> children;

}