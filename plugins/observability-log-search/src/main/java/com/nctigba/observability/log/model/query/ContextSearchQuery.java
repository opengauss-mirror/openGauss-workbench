/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.log.model.query;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
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
