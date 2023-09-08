package com.nctigba.observability.sql.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.ExecutionPlan;
import com.nctigba.observability.sql.service.ClusterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * TopSQL business of openGauss
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 14:39
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TopSQLHandler {
    private static final String PRE_CHECK_SET_SQL = "select name,setting from pg_settings where name in('enable_stmt_track','enable_resource_track','track_stmt_stat_level')";
    private static final String EXECUTION_PLAN_SQL = "select query_plan,query from dbe_perf.statement_history where debug_query_id=?;";
    private static final String WORK_MEM_SQL = "select setting from pg_settings WHERE name = 'work_mem'";
    private final ClusterManager clusterManager;
    private int totalPlanRows = 0;
    private int totalPlanWidth = 0;
    private List<String> objectNameList = new ArrayList<>();

    /**
     * deal Execution Plan list
     * @param rsList Execution Plan list
     * @return JSONObject
     */
    public JSONObject dealExecutionPlan(List<String> rsList) {
        JSONObject parsedResult = new JSONObject();
        try {
            LinkedList<String> modifySplitLines = new LinkedList<>(rsList);
            // remove non-operation or non-condition lines
            for (String line : rsList) {
                if ((line.contains("cost=") && !line.contains("Result")) || line.contains(CommonConstants.HASH_COND)) {
                    continue;
                }
                modifySplitLines.remove(line);
            }
            if (modifySplitLines.size() == 0) {
                throw new CustomException("failResolveExecutionPlan");
            }
            // get base execution plan object
            totalPlanWidth = 0;
            totalPlanRows = 0;
            ExecutionPlan plan = processExecutionPlanString(modifySplitLines.get(0));
            // get tree shape execution plan object
            assert plan != null;
            processExecutionPlan(modifySplitLines.subList(1, modifySplitLines.size()), 0, plan,
                    plan.getChildren());
            // put plan node
            JSONObject result = JSONObject.parseObject(JSON.toJSONString(plan));
            JSONArray data = new JSONArray();
            data.add(result);
            parsedResult.put("data", data);
            // put total rows and width
            JSONObject total = new JSONObject();
            total.put("totalPlanRows", totalPlanRows);
            total.put("totalPlanWidth", totalPlanWidth);
            parsedResult.put("total", total);
        } catch (Exception e) {
            log.error("get TopSQL execute plan fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return parsedResult;
    }

    private String getPeakMemory(String planStr) {
        if (StringUtils.isEmpty(planStr)) {
            return "";
        }
        if (planStr.contains("Peak Memory")) {
            String[] planArr = planStr.split("Peak Memory:");
            if (planArr.length > 1) {
                return planArr[1].substring(0, planArr[1].indexOf("(KB)"));
            }
        }
        return "";
    }

    private String getWorkMem(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(WORK_MEM_SQL)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "";
    }

    private List<JSONObject> getRowsDiff(String planStr) {
        List<JSONObject> result = new ArrayList<>();
        List<String> planStrList = Arrays.asList(planStr.split("\n"));
        LinkedList<String> modifyLines = new LinkedList<>(planStrList);
        try {
            for (String item : planStrList) {
                if ((item.contains("cost=") && !item.contains("Result")) || item.contains(CommonConstants.HASH_COND)
                        || item.contains("->")) {
                    continue;
                }
                modifyLines.remove(item);
            }
            for (String mItem : modifyLines) {
                String[] mItemArr = mItem.split("rows=");
                if (mItem.length() <= 2) {
                    continue;
                }
                // there are two rows
                JSONObject jsonObject = new JSONObject();
                String firstStr = mItemArr[0];
                if (!firstStr.contains(CommonConstants.COST)) {
                    continue;
                }
                if (firstStr.contains("->")) {
                    jsonObject.put(
                            "stepName",
                            firstStr.substring(
                                    firstStr.indexOf("->") + 2, firstStr.indexOf(CommonConstants.COST)).trim());
                } else {
                    jsonObject.put("stepName", firstStr.substring(0, firstStr.indexOf(CommonConstants.COST)).trim());
                }
                jsonObject.put("estimateRows", mItemArr[1].substring(0, mItemArr[1].indexOf(CommonConstants.BLANK)));
                jsonObject.put("actualRows", mItemArr[2].substring(0, mItemArr[2].indexOf(CommonConstants.BLANK)));
                result.add(jsonObject);
            }
            return result;
        } catch (Exception e) {
            log.error("get Rows diff fail:{}", e.getMessage());
            return result;
        }
    }

    public JSONObject getExecutionPlan(String nodeId, String sqlId, String type) {
        // nodeInfo.setDbUserPassword(RSAUtil.decrypt(nodeInfo.getDbUserPassword()));
        // store return result
        JSONObject parsedResult = new JSONObject();
        // get connection then execute query
        try (Connection connection = clusterManager.getConnectionByNodeId(nodeId)) {
            if (connection == null) {
                throw new CustomException("get database connection fail");
            }
            // pre-check track_stmt_stat_leve full sql level at least L1
            executionPlanPreCheck(connection);
            // get prepared statement
            try (PreparedStatement preparedStatement = connection.prepareStatement(EXECUTION_PLAN_SQL)) {
                preparedStatement.setString(1, sqlId);
                // native plan
                if ("native".equals(type)) {
                    JSONArray nativePlanArray = new JSONArray();
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            String planStr = rs.getString(1);
                            jsonObject.put("rowsDiff", getRowsDiff(planStr));
                            jsonObject.put("queryPlan", planStr);
                            jsonObject.put("peakMem", getPeakMemory(planStr));
                            jsonObject.put("workMem", getWorkMem(connection));
                            nativePlanArray.add(jsonObject);
                        }
                    }
                    parsedResult.put("plan", nativePlanArray);
                    return parsedResult;
                }
                // json plan
                // get result from result set
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        List<String> splitLines = new ArrayList<>();
                        String executionPlan = rs.getString(1);
                        if (StringUtils.isBlank(executionPlan)) {
                            throw new CustomException("failGetExecutionPlan");
                        } else {
                            splitLines = Arrays.asList(executionPlan.split("\n"));
                        }
                        parsedResult = dealExecutionPlan(splitLines);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL execute plan fail:{}", e.getMessage());
            return parsedResult;
        }
        return parsedResult;
    }

    /**
     * pre-check execution plan job
     *
     * @param connection connection info
     */
    private void executionPlanPreCheck(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(PRE_CHECK_SET_SQL)) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String setting = rs.getString(2);
                    if ("track_stmt_stat_level".equalsIgnoreCase(name)) {
                        if (StringUtils.startsWith(setting, "OFF") || StringUtils.startsWith(setting, "L0")) {
                            throw new CustomException(
                                    "To perform Execution Plan, you need to set 'track_stmt_stat_level' parameter Full SQL level to at least 'L1'");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("pre check fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
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
            // process condition
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
                assert subPlan != null;
                processExecutionPlan(lines.subList(i + 1, lines.size()), currentIndent, subPlan, subPlan.getChildren());
            }
            // when current indent less than or equals previous indent, return to last level
            if (currentIndent <= previousIndent) {
                return;
            }
        }
    }

    /**
     * parse execution plan line string into object
     *
     * @param line execution plan line string
     * @return execution plan line object
     */
    private ExecutionPlan processExecutionPlanString(String line) {
        if (line.contains(CommonConstants.HASH_COND)) {
            return null;
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
            String alias;
            if (objectName.contains(CommonConstants.BLANK)) {
                String[] aliasSplit = objectName.split(CommonConstants.BLANK);
                alias = aliasSplit[0];
            } else {
                alias = objectName;
            }
            plan.setAlias(alias);
            if (!objectNameList.contains(alias)) {
                objectNameList.add(alias);
            }
        } else {
            plan.setNodeType(operation.trim());
        }
        // set parameters
        String parameters = operationParameterSplit[1].replace(")", "");
        String[] parametersSplit = parameters.split(CommonConstants.BLANK);
        for (String parameterSplit : parametersSplit) {
            if (StringUtils.startsWith(parameterSplit, "cost")) {
                // set start cost and total cost
                String[] costSplit = parameterSplit.split("=");
                String[] startCostTotalCostSplit = costSplit[1].split("\\.\\.");
                plan.setStartupCost(Double.parseDouble(startCostTotalCostSplit[0]));
                plan.setTotalCost(Double.parseDouble(startCostTotalCostSplit[1]));
            } else if (StringUtils.startsWith(parameterSplit, "rows=")) {
                // set plan rows
                String[] rowSplit = parameterSplit.split("=");
                int rowsNum = Integer.parseInt(rowSplit[1]);
                plan.setPlanRows(rowsNum);
                totalPlanRows += rowsNum;
            } else if (StringUtils.startsWith(parameterSplit, "width=")) {
                // set plan width
                String[] widthSplit = parameterSplit.split("=");
                int widthNum = Integer.parseInt(widthSplit[1]);
                plan.setPlanWidth(widthNum);
                totalPlanWidth += widthNum;
            }
        }
        return plan;
    }
}
