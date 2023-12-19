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
 *  ObjectInfoCheck.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/ObjectInfoCheck.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.handler.TopSQLHandler;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.vo.PartitionDataVO;
import com.nctigba.observability.sql.enums.FrameTypeEnum;
import com.nctigba.observability.sql.enums.ResultTypeEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.TaskResultDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.impl.collection.table.ExplainItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ObjectInfoCheck
 *
 * @author luomeng
 * @since 2023/9/6
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ObjectInfoCheck implements DiagnosisPointService<Object> {
    private final DiagnosisResultMapper resultMapper;
    private final TopSQLHandler openGaussTopSQLHandler;
    private final ClusterManager clusterManager;
    private final ExplainItem item;
    private final DiagnosisTaskMapper taskMapper;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_EXPLAIN));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        ArrayList<String> rsList = (ArrayList<String>) dataStoreService.getData(item).getCollectionData();
        setTaskResultSuggestions(task.getId(), ResultTypeEnum.ExecPlan, null);
        JSONObject result = new JSONObject();
        log.info("analyze caller getExecutionPlanResult start");
        result.put("executionPlan", getExecutionPlanResult(rsList, task.getNodeId(), task));
        // Get data uniformly with ObjectInfoCheck
        setTaskResultSuggestions(task.getId(), ResultTypeEnum.ObjectInfoCheck, result);
        return new AnalysisDTO();
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }

    private void setTaskResultSuggestions(Integer taskId, ResultTypeEnum resultType, JSONObject result) {
        TaskResultDTO taskResultDTO = new TaskResultDTO();
        taskResultDTO.setTaskId(taskId);
        taskResultDTO.setResultType(resultType);
        taskResultDTO.setState(TaskResultDTO.ResultState.SUGGESTION);
        taskResultDTO.setFrameType(FrameTypeEnum.Explain);
        if (result != null) {
            taskResultDTO.setData(result);
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        analysisDTO.setPointData(taskResultDTO);
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        DiagnosisResultDO resultData = new DiagnosisResultDO(
                task, analysisDTO, resultType.toString(), DiagnosisResultDO.PointState.SUCCEED);
        resultMapper.update(resultData, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                        DiagnosisResultDO::getTaskId, resultData.getTaskId())
                .eq(DiagnosisResultDO::getPointName, resultData.getPointName()));
    }

    /**
     * get native execution plan
     *
     * @param rsList execution plan list
     * @param nodeId node id
     * @param task task info
     * @return JSONObject
     */
    private JSONObject getExecutionPlanResult(ArrayList<String> rsList, String nodeId, DiagnosisTaskDO task) {
        JSONObject jsonResult = new JSONObject();
        // get execution analyze native query plan
        jsonResult.put("queryPlan", rsList);
        int peakMem = getPeakOrDiskMemory(rsList);
        // get peak memory
        jsonResult.put("peakMem", peakMem);
        // max cost result
        JSONObject maxCostResult = getMaxCostTableName(rsList);
        String maxCostTableName = maxCostResult.getString("mostCostTableName");
        String maxCostStepName = maxCostResult.getString("mostCostStepName");
        jsonResult.put("maxCostTableName", maxCostTableName);
        jsonResult.put("maxCostStepName", maxCostStepName);
        // get connection
        try (var conn = clusterManager.getConnectionByNodeId(nodeId)) {
            // get rows diff list
            List<JSONObject> rowsDiffList = getRowsDiff(rsList, maxCostTableName, conn);
            jsonResult.put("rowsDiff", rowsDiffList);
            // suggest: ResultType.ObjectRecommendedToUpdateStatistics
            if (rowsDiffList != null && rowsDiffList.size() > 0) {
                log.info("get ObjectRecommendedToUpdateStatistics success");
                setTaskResultSuggestions(task.getId(), ResultTypeEnum.ObjectRecommendedToUpdateStatistics, null);
            }
            // get work memory
            String workMem = getWorkMem(conn);
            jsonResult.put("workMem", workMem);
            // get debug_query_id
            jsonResult.put("debugQueryId", task.getDebugQueryId());
            // get json query plan
            jsonResult.put("jsonQueryPlan", getJsonQueryPlan(task, rsList));
            // suggest: ResultType.PlanRecommendedToOptimizeStatementsOrAddWorkMemSize
            if (peakMem > Integer.parseInt(workMem)) {
                log.info("get PlanRecommendedToOptimizeStatementsOrAddWorkMemSize success");
                setTaskResultSuggestions(task.getId(),
                        ResultTypeEnum.PlanRecommendedToOptimizeStatementsOrAddWorkMemSize, null);
            }
            JSONObject partitionData = getPartitionData(task, rsList, maxCostTableName, conn);
            jsonResult.put(
                    "partitionData", JSON.parse(partitionData.getString(CommonConstants.PARTITION_RESULT_ARRAY)));
            boolean isHasPartKey = Boolean.parseBoolean(partitionData.getString(CommonConstants.IS_HAS_PART_KEY));
            boolean isByPartition = Boolean.parseBoolean(partitionData.getString("isByPartition"));
            jsonResult.put(CommonConstants.IS_HAS_PART_KEY, isHasPartKey);
            // suggest: PlanRecommendedToQueryBasedOnPartition
            if (StringUtils.isNotEmpty(maxCostStepName) && maxCostStepName.contains("Partitioned Seq Scan")
                    && !isByPartition && !isHasPartKey) {
                log.info("get PlanRecommendedToQueryBasedOnPartition success");
                setTaskResultSuggestions(task.getId(), ResultTypeEnum.PlanRecommendedToQueryBasedOnPartition, null);
            }
            JSONObject tableMetaData = getTableData(maxCostTableName, conn, SqlConstants.TABLE_METADATA_SQL, false);
            jsonResult.put("tableMetaData", tableMetaData);
            if (StringUtils.isNotEmpty(maxCostStepName) && maxCostStepName.contains("Seq Scan")) {
                JSONObject curTableMetaData = JSONObject.parseObject(tableMetaData.getString(maxCostTableName));
                int nLiveTup = Integer.parseInt(curTableMetaData.getString("n_live_tup"));
                int nDeadTup = Integer.parseInt(curTableMetaData.getString("n_dead_tup"));

                // suggest: ResultType.PlanChangedToPartitionTable
                if (nLiveTup > 2000 * 10000) {
                    log.info("get PlanChangedToPartitionTable success");
                    setTaskResultSuggestions(task.getId(), ResultTypeEnum.PlanChangedToPartitionTable, null);
                }
                // suggest: ResultType.PlanRecommendedToDoVacuumCleaning
                if (nDeadTup > ((nLiveTup + nDeadTup) * 0.2 + 50)) {
                    log.info("get PlanRecommendedToDoVacuumCleaning success");
                    setTaskResultSuggestions(task.getId(), ResultTypeEnum.PlanRecommendedToDoVacuumCleaning, null);
                }
            }
            // get table structure data
            jsonResult.put(
                    "tableStructureData", getTableData(maxCostTableName, conn, SqlConstants.TABLE_STRUCTURE_SQL, true));

            // this is table index info, not index advice
            JSONObject tableIndexData = getTableData(maxCostTableName, conn, SqlConstants.INDEX_SQL, true);
            jsonResult.put("tableIndexData", tableIndexData);
            // get index advice
            String completeQueryText = "select * from gs_index_advise('"
                    + task.getSql().replace("'", "''") + "')";
            JSONObject indexAdviceJsonResult = getIndexAdvice(conn, completeQueryText);
            JSONArray indexAdvicesData = indexAdviceJsonResult.getJSONArray("indexList");
            JSONArray indexAdvicesTableNameList = indexAdviceJsonResult.getJSONArray("tableNameList");
            jsonResult.put("indexAdvicesData", indexAdvicesData);
            jsonResult.put("indexAdvicesTableNameList", indexAdvicesTableNameList);
            List<JSONObject> otherStructureAndIndexList = new ArrayList<>();
            // suggest: ResultType.PlanRecommendedToCreateIndex
            if (StringUtils.isNotEmpty(maxCostStepName) && maxCostStepName.contains("Seq Scan")
                    && indexAdvicesData != null && !indexAdvicesData.isEmpty()) {
                log.info("get PlanRecommendedToCreateIndex success");
                setTaskResultSuggestions(task.getId(), ResultTypeEnum.PlanRecommendedToCreateIndex, null);
                // get all index advice table's all
                log.info("get indexAdvicesTableNameList : {}", indexAdvicesTableNameList);
                for (Object name : indexAdvicesTableNameList) {
                    String nameStr = String.valueOf(name);
                    if (nameStr.equals(maxCostTableName)) {
                        continue;
                    }
                    JSONObject structureData = getTableData(nameStr, conn, SqlConstants.TABLE_STRUCTURE_SQL, true);
                    JSONObject indexData = getTableData(nameStr, conn, SqlConstants.INDEX_SQL, true);
                    JSONObject itemObj = new JSONObject();
                    itemObj.put(CommonConstants.TABLENAME, nameStr);
                    itemObj.put("structureData", structureData.getJSONArray(nameStr));
                    itemObj.put("indexData", indexData.getJSONArray(nameStr));
                    otherStructureAndIndexList.add(itemObj);
                }
            }
            jsonResult.put("otherStructureAndIndexList", otherStructureAndIndexList);
        } catch (SQLException e) {
            log.error("get execution plan fail:{}", e.getMessage());
        }
        return jsonResult;
    }

    private JSONObject getIndexAdvice(Connection conn, String sql) {
        JSONObject jsonResult = new JSONObject();
        JSONArray indexList = new JSONArray();
        JSONArray tableNameList = new JSONArray();
        String indexTemplate = "It is recommended to create an index for column %c of table %t";
        String multiColumnIndexTemplate = "It is recommended to create a composite index for column %c of table %t";
        log.info("get index advice: {}", sql);
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 1: Schema ; 2: Table ; 3: Column; 4: IndexType
                String tableName = rs.getString(2);
                String columnStr = rs.getString(3);
                if (StringUtils.isNotEmpty(columnStr) && StringUtils.isNotEmpty(tableName)) {
                    String result = columnStr.contains(",") ? multiColumnIndexTemplate : indexTemplate;
                    result = result.replace("%t", tableName);
                    result = result.replace("%c", columnStr);
                    tableNameList.add(tableName);
                    indexList.add(result);
                }
            }
            stmt.close();
            jsonResult.put("indexList", indexList);
            jsonResult.put("tableNameList", tableNameList);
            return jsonResult;
        } catch (SQLException e) {
            log.error(CommonConstants.GET_INDEX_ADVICE_FAIL, e.getMessage());
            return jsonResult;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
        }
    }

    private JSONObject getTableData(String tableName, Connection conn, String sql, boolean isList) {
        JSONObject result = new JSONObject();
        if (StringUtils.isEmpty(tableName)) {
            return result;
        }
        log.info("get table information tableName: {}, sql: {}", tableName, sql);
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.replace(CommonConstants.TABLENAME, tableName));
            if (isList) {
                JSONArray resultArray = new JSONArray();
                while (rs.next()) {
                    resultArray.add(JSONObject.parseObject(rs.getString(1)));
                }
                result.put(tableName, resultArray);
            } else {
                while (rs.next()) {
                    result.put(tableName, JSONObject.parseObject(rs.getString(1)));
                }
            }
            return result;
        } catch (SQLException e) {
            log.error("get {} data fail:{}", sql, e.getMessage());
            return result;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
        }
    }

    private JSONObject getJsonQueryPlan(DiagnosisTaskDO task, ArrayList<String> rsList) {
        JSONObject result = new JSONObject();
        log.info("get Json Query PlanVO begin, debug query id : {}", task.getDebugQueryId());
        if (rsList != null && !rsList.isEmpty()) {
            result = openGaussTopSQLHandler.dealExecutionPlan(rsList);
            log.info("Json Query PlanVO result : {}", result);
        }
        return result;
    }

    private JSONObject getMaxCostTableName(ArrayList<String> rsList) {
        LinkedList<String> modifyLines = new LinkedList<>(rsList);
        for (String rs : rsList) {
            if ((rs.contains("cost=") && !rs.contains("Result")) || rs.contains("Hash Cond") || rs.contains("->")) {
                continue;
            }
            modifyLines.remove(rs);
        }
        double cost = -1D;
        String mostCostTableName = "";
        String mostCostStepName = "";
        for (String mItem : modifyLines) {
            // avoid function, Partition has 'on'
            if (mItem.contains(" on ") && mItem.contains("..") && mItem.contains("rows")) {
                double tempCost = Double
                        .parseDouble(mItem.substring(mItem.indexOf("..") + 2, mItem.indexOf("rows")).trim());
                if (tempCost > cost) {
                    // get mostCostTableName
                    String[] mItemArr = mItem.split(" on ");
                    String text = mItemArr[1].trim();
                    mostCostTableName = text.substring(0, text.indexOf(CommonConstants.BLANK));
                    // get mostCostStepNmae
                    String[] stepArr = mItem.split(mostCostTableName);
                    String step = stepArr[0].trim();
                    if (step.contains("->")) {
                        mostCostStepName =
                                step.substring(step.indexOf("->") + 2) + CommonConstants.BLANK + mostCostTableName;
                    } else {
                        mostCostStepName = step + CommonConstants.BLANK + mostCostTableName;
                    }
                    cost = tempCost;
                }
            }
        }
        JSONObject result = new JSONObject();
        result.put("mostCostTableName", mostCostTableName);
        result.put("mostCostStepName", mostCostStepName.trim());
        log.info("get max cost table name success:{}, step name: {}", mostCostTableName, mostCostStepName);
        return result;
    }

    private String getWorkMem(Connection conn) {
        String workMemValue = "";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SqlConstants.WORK_MEM_SQL);
            while (rs.next()) {
                workMemValue = rs.getString(1);
            }
        } catch (SQLException e) {
            log.error("get work memory fail:{}", e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
        }
        return workMemValue;
    }

    private int getPeakOrDiskMemory(ArrayList<String> rsList) {
        if (rsList == null) {
            return -1;
        }
        int mostPeakMemory = -1;
        for (String rs : rsList) {
            if (rs.contains("MemoryAnalysis") || rs.contains("Disk")) {
                int lastSignPosi = rs.lastIndexOf(CommonConstants.COLON);
                int lastUnitPosi = rs.lastIndexOf("kB");
                if (lastSignPosi > -1 && lastUnitPosi > -1 && (lastUnitPosi > lastSignPosi)) {
                    int curPeakMemory = Integer.parseInt(rs.substring(lastSignPosi + 1, lastUnitPosi).trim());
                    if (curPeakMemory > mostPeakMemory) {
                        mostPeakMemory = curPeakMemory;
                    }
                }
            }
        }
        return mostPeakMemory;
    }

    private List<JSONObject> getRowsDiff(ArrayList<String> rsList, String tableName, Connection conn) {
        log.info("analyze caller getRowsDiff start");
        List<JSONObject> result = new ArrayList<>();
        int allRowsNum = -1;
        // get all rows
        Statement stmt = null;
        ResultSet rst = null;
        try {
            stmt = conn.createStatement();
            rst = stmt.executeQuery(SqlConstants.QUERY_TABLE_ALL_ROWS.replace(CommonConstants.TABLENAME, tableName));
            while (rst.next()) {
                allRowsNum = rst.getInt(1);
            }
        } catch (SQLException e) {
            log.error("get work memory fail:{}", e.getMessage());
            return result;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try {
                if (rst != null) {
                    rst.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
        }
        if (allRowsNum < 0) {
            return result;
        }
        // deal result
        LinkedList<String> modifyLines = new LinkedList<>(rsList);
        BigDecimal diffValue = new BigDecimal(allRowsNum).multiply(new BigDecimal("0.2"))
                .add(new BigDecimal("1000"));
        double diff = diffValue.doubleValue();
        for (String rs : rsList) {
            if ((rs.contains("cost=") && !rs.contains("Result")) || rs.contains("Hash Cond") || rs.contains("->")) {
                continue;
            }
            modifyLines.remove(rs);
        }
        log.info("analyze caller modifyLines : {}", modifyLines);
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
            String estimateRows = mItemArr[1].substring(0, mItemArr[1].indexOf(CommonConstants.BLANK));
            String actualRows = mItemArr[2].substring(0, mItemArr[2].indexOf(CommonConstants.BLANK));
            if (StringUtils.isNotEmpty(estimateRows) && StringUtils.isNotEmpty(actualRows)
                    && ((Integer.parseInt(estimateRows) - Integer.parseInt(actualRows)) >= diff)) {
                jsonObject.put(
                        "estimateRows", mItemArr[1].substring(0, mItemArr[1].indexOf(CommonConstants.BLANK)));
                jsonObject.put("actualRows", mItemArr[2].substring(0, mItemArr[2].indexOf(CommonConstants.BLANK)));
                result.add(jsonObject);
            }
        }
        return result;
    }

    private JSONObject getPartitionData(DiagnosisTaskDO task, ArrayList<String> rsList, String tableName,
            Connection conn) {
        JSONObject result = new JSONObject();
        JSONArray resultArray = new JSONArray();
        if (task.getSql().contains(" partition")) {
            result.put(CommonConstants.PARTITION_RESULT_ARRAY, resultArray);
            result.put(CommonConstants.IS_HAS_PART_KEY, false);
            result.put("isByPartition", true);
            return result;
        }
        List<String> columnNameList = getAllColumnNameList(tableName, conn);
        List<String> partitionFilterList = getPartitionFilterList(rsList);
        log.info("get partition data begin, tableName: {}", tableName);
        Statement stmt = null;
        ResultSet rs = null;
        boolean isHasPartKey = false;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SqlConstants.PARTITION_LIST_SQL.replace(CommonConstants.TABLENAME, tableName));
            while (rs.next()) {
                JSONObject itemObj = JSONObject.parseObject(rs.getString(1));
                // PartitionData
                String partKey = itemObj.getString("partkey");
                if (StringUtils.isNotEmpty(partKey)) {
                    JSONArray partKeyArr = JSONArray.parseArray(itemObj.getString("partkey"));
                    int partKeyPosition = (int) partKeyArr.get(0);
                    String partKeyColumnName = columnNameList.get(partKeyPosition - 1);
                    if (partitionFilterList.contains(partKeyColumnName)) {
                        isHasPartKey = true;
                    }
                    PartitionDataVO partitionDataVO = new PartitionDataVO();
                    partitionDataVO.setPartStrategy(itemObj.getString("partstrategy"));
                    partitionDataVO.setPartKey(partKeyColumnName);
                    partitionDataVO.setRelPages(itemObj.getString("relpages"));
                    partitionDataVO.setRelTuples(itemObj.getString("reltuples"));
                    partitionDataVO.setRelallVisible(itemObj.getString("relallvisible"));
                    partitionDataVO.setInterval(itemObj.getString("interval"));
                    resultArray.add(partitionDataVO);
                }
            }
            result.put(CommonConstants.PARTITION_RESULT_ARRAY, JSON.toJSONString(resultArray));
            result.put(CommonConstants.IS_HAS_PART_KEY, isHasPartKey);
            log.info("get partition data result : {}", result);
            return result;
        } catch (SQLException e) {
            log.error("get partition data fail:{}", e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
        }
        return result;
    }

    private List<String> getPartitionFilterList(ArrayList<String> rsList) {
        List<String> list = new ArrayList<>();
        log.info("get partition filter list begin");
        for (String query : rsList) {
            if (!(query.contains("Filter") && !query.contains("Removed"))) {
                continue;
            }
            log.info("get partition filter query, {}", query);
            if (query.contains("AND")) {
                String[] filterArr = query.split("AND");
                for (String filter : filterArr) {
                    if (filter.contains("::")) {
                        list.add(filter.substring(
                                filter.indexOf("((") + 2,
                                filter.indexOf(CommonConstants.RIGHT_BRACKET)).trim());
                    } else {
                        list.add(filter.substring(
                                filter.indexOf("((") + 2,
                                filter.indexOf(CommonConstants.EQUAL)).trim());
                    }
                }
            } else {
                if (query.contains("((")) {
                    list.add(query.substring(
                            query.indexOf("((") + 2,
                            query.indexOf(CommonConstants.RIGHT_BRACKET)).trim());
                } else {
                    list.add(query.substring(
                            query.indexOf(CommonConstants.LEFT_BRACKET) + 1,
                            query.indexOf(CommonConstants.EQUAL)).trim());
                }
            }
        }
        log.info("get partition filter list, {}", list);
        return list;
    }

    private List<String> getAllColumnNameList(String tableName, Connection conn) {
        log.info("get all column name begin");
        List<String> columnList = new ArrayList<>();
        if (StringUtils.isEmpty(tableName)) {
            return columnList;
        }
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SqlConstants.QUERY_ALL_COLUMN_NAME.replace(CommonConstants.TABLENAME, tableName));
            while (rs.next()) {
                columnList.add(rs.getString(1));
            }
            stmt.close();
        } catch (SQLException e) {
            log.error("get all column name fail:{}", e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
        }
        return columnList;
    }
}
