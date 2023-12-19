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
 *  ContextSearchQuery.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/query/ContextSearchQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Log-ContextSearch request dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/02/01 09:05
 */
@Data
public class ContextSearchQuery {
    private List<String> nodeId;
    private String searchPhrase;
    private int rowCount;
    private Date startDate;
    private Date endDate;
    private List<String> logType;
    private List<String> logLevel;
    private String scrollId;
    @NotNull(message = "aboveCount is empty")
    private Integer aboveCount;
    @NotNull(message = "belowCount is empty")
    private Integer belowCount;
    private List<String> sorts;
    private String id;
}
