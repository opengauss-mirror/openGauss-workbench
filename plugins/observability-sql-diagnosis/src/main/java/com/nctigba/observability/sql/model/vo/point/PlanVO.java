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
 *  PlanVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/point/PlanVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo.point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * PlanVO
 *
 * @author luomeng
 * @since 2023/9/4
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanVO {
    @JsonProperty("Node Type")
    private String nodeType;
    @JsonProperty("Relation Name")
    private String relationName;
    @JsonProperty("Alias")
    private String alias;
    @JsonProperty("Strategy")
    private String strategy;
    @JsonProperty("Parent Relationship")
    private String parentRelationship;
    @JsonProperty("Startup Cost")
    private double startupCost;
    @JsonProperty("Total Cost")
    private double totalCost;
    @JsonProperty("Plan Rows")
    private int planRows;
    @JsonProperty("Plan Width")
    private int planWidth;
    @JsonProperty("Group By Key")
    private String[] groupByKey;
    @JsonProperty("Sort Key")
    private String[] sortKey;
    @JsonProperty("Filter")
    private String filter;
    @JsonProperty("Plans")
    private PlanVO[] planVOS;
}
