/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * <p>
 * execution plan model
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/10/08 12:39
 */
@Data
public class ExecutionPlan {
    // uuid;
    String id;
    // operation
    String nodeType;
    // object
    String alias;
    // start cost
    Double startupCost;
    // total cost
    Double totalCost;
    // rows
    Integer planRows;
    // width
    Integer planWidth;
    // condition
    String joinType;
    // sub nodes
    List<ExecutionPlan> children;

    public ExecutionPlan() {
        this.id = UUID.randomUUID().toString();
        this.children = new ArrayList<>();
    }
}
