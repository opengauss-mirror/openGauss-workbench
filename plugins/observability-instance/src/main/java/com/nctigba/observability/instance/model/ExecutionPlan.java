/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.opengauss.admin.common.exception.CustomException;

import com.nctigba.observability.instance.constants.CommonConstants;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * execution plan model
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/10/08 12:39
 */
@Data
@NoArgsConstructor
public class ExecutionPlan {
    // uuid;
    String id = UUID.randomUUID().toString();
    // operation
    String nodeType;
    // object
    String alias;
    // start cost
    Double startupCost;
    // total cost
    Double totalCost;
    // rows
    Integer planRows = 0;
    // width
    Integer planWidth = 0;
    // condition
    String joinType;
    // sub nodes
    List<ExecutionPlan> children = new ArrayList<>();

    /**
     * ExecutionPlan
     *
     * @param executionPlan executionPlan
     */
    public ExecutionPlan(String executionPlan) {
        var splitLines = Arrays.asList(executionPlan.split("\n"));

        LinkedList<String> plan = new LinkedList<>(splitLines);
        // remove non-operation or non-condition lines
        for (String line : splitLines) {
            if ((line.contains("cost=") && !line.contains("Result")) || line.contains(CommonConstants.HASH_COND)) {
                continue;
            }
            plan.remove(line);
        }
        if (plan.size() == 0) {
            throw new CustomException("failResolveExecutionPlan");
        }
        processExecutionPlanString(plan.get(0));
        processExecutionPlan(plan.subList(1, plan.size()), 0, this, this.getChildren());
    }

    /**
     * generate execution plan tree shape json object
     *
     * @param lines          lines of execution plan and condition
     * @param previousIndent last level indent length
     * @param plan           last level plan
     * @param children       last level plan children
     */
    private void processExecutionPlan(List<String> lines, int previousIndent, ExecutionPlan plan,
            List<ExecutionPlan> children) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // process condition eg. Hash Cond: (s.datid = d.oid)
            if (line.contains(CommonConstants.HASH_COND)) {
                String[] split = line.split(": ");
                plan.setJoinType(split[1]);
                continue;
            }
            // skip processed lines
            if (line.isEmpty()) {
                continue;
            }
            String[] indentPlanSplit = line.split("->");
            int currentIndent = indentPlanSplit[0].length();
            // when current indent greater than previous indent, add new children node and
            // go into next tree level
            if (currentIndent > previousIndent) {
                lines.set(i, "");
                ExecutionPlan subPlan = processExecutionPlanString(line);
                children.add(subPlan);
                processExecutionPlan(lines.subList(i + 1, lines.size()), currentIndent, subPlan, subPlan.getChildren());
            }
            // when current indent less than or equals previous indent, return to last level
            if (currentIndent <= previousIndent) {
                return;
            }
        }
    }

    /**
     * parse execution plan line string into object<br>
     * e.g.
     * {@code Function Scan on pg_show_all_settings a  (cost=0.00..12.50 rows=5 p-time=0 p-rows=0 width=32)}
     *
     * @param line execution plan line string
     * @return execution plan line object
     */
    private ExecutionPlan processExecutionPlanString(String line) {
        if (line.contains(CommonConstants.HASH_COND)) {
            return new ExecutionPlan();
        }
        ExecutionPlan plan = new ExecutionPlan();
        // check if first line
        String planString;
        if (line.contains("->")) {
            String[] indentPlanSplit = line.split("->");
            planString = indentPlanSplit[1];
        } else {
            planString = line;
        }
        String[] operationParameterSplit = planString.split("\\(");
        // set operation name and alias name
        String operation = operationParameterSplit[0];
        if (operation.contains(" on ")) {
            String[] split = operation.split(" on ");
            plan.setNodeType(split[0].trim());
            // only show object name, remove alias name
            String objectName = split[1];
            String aliasName;
            if (objectName.contains(CommonConstants.BLANK)) {
                String[] aliasSplit = objectName.split(CommonConstants.BLANK);
                aliasName = aliasSplit[0];
            } else {
                aliasName = objectName;
            }
            plan.setAlias(aliasName);
        } else {
            plan.setNodeType(operation.trim());
        }
        // set parameters
        String parameters = operationParameterSplit[1].replace(")", "");
        String[] parametersSplit = parameters.split(CommonConstants.BLANK);
        for (String parameterSplit : parametersSplit) {
            String[] split = parameterSplit.split("=");
            switch (split[0].trim()) {
            case "cost":
                // set start cost and total cost
                String[] startCostTotalCostSplit = split[1].split("\\.\\.");
                plan.setStartupCost(Double.parseDouble(startCostTotalCostSplit[0]));
                plan.setTotalCost(Double.parseDouble(startCostTotalCostSplit[1]));
                break;
            case "rows":
                // set plan rows
                int rowsNum = Integer.parseInt(split[1]);
                plan.setPlanRows(rowsNum);
                break;
            case "width":
                // set plan width
                int widthNum = Integer.parseInt(split[1]);
                plan.setPlanWidth(widthNum);
                break;
            default:
            }
        }
        return plan;
    }

    /**
     * totalPlanRows
     *
     * @return int
     */
    public int totalPlanRows() {
        int rows = planRows;
        for (ExecutionPlan executionPlan : children) {
            rows += executionPlan.totalPlanRows();
        }
        return rows;
    }

    /**
     * totalPlanWidth
     *
     * @return int
     */
    public int totalPlanWidth() {
        int width = planWidth;
        for (ExecutionPlan executionPlan : children) {
            width += executionPlan.totalPlanWidth();
        }
        return width;
    }

    /**
     * allAlias
     *
     * @return Set
     */
    public Set<String> allAlias() {
        Set<String> set = new HashSet<>();
        if (alias != null) {
            set.add(alias);
        }
        for (ExecutionPlan executionPlan : children) {
            set.addAll(executionPlan.allAlias());
        }
        return set;
    }
}
