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

package com.nctigba.observability.sql.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * execution plan model
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/10/08 12:39
 */
@Data
public class ExecutionPlanVO {
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
    List<ExecutionPlanVO> children;

    public ExecutionPlanVO() {
        this.id = UUID.randomUUID().toString();
        this.children = new ArrayList<>();
    }
}
