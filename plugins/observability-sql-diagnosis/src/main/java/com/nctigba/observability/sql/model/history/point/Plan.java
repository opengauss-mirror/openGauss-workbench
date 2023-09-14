/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Plan
 *
 * @author luomeng
 * @since 2023/9/4
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Plan {
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
    private String[] filter;
    @JsonProperty("Plans")
    private Plan[] plans;
}
