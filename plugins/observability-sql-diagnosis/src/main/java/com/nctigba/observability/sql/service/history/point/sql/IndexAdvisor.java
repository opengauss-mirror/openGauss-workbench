/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.OuterPlan;
import com.nctigba.observability.sql.model.history.point.Plan;
import com.nctigba.observability.sql.model.history.point.TuningAdvisorDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PointUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexAdvisor
 *
 * @author luomeng
 * @since 2023/8/17
 */
@Slf4j
@Service
public class IndexAdvisor implements HisDiagnosisPointService<Object> {
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private PointUtil util;

    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        return null;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        ResultSet rs = null;
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            String preSql = "SET explain_perf_mode TO normal;";
            stmt.execute(preSql);
            String diagnosisSql = task.getSql();
            rs = stmt.executeQuery("explain (format json) " + diagnosisSql);
            OuterPlan[] firstPlans = new OuterPlan[0];
            while (rs.next()) {
                String planObj = rs.getString(1);
                ObjectMapper objectMapper = new ObjectMapper();
                firstPlans = objectMapper.readValue(planObj, OuterPlan[].class);
            }
            TuningAdvisorDTO advisorDTO = new TuningAdvisorDTO();
            OuterPlan firstExplain = firstPlans[0];
            Plan firstPlan = firstExplain.getPlan();
            advisorDTO.setFirstExplain(util.getExecPlan(firstPlan));
            String sql = "select \"schema\",\"table\", \"column\" ,\"indextype\" from gs_index_advise('"
                    + diagnosisSql.replace("'", "''") + "');";
            rs = stmt.executeQuery(sql);
            List<String> indexAdvisor = new ArrayList<>();
            while (rs.next()) {
                String schema = rs.getString(1);
                String tableName = rs.getString(2);
                String column = rs.getString(3);
                String indexType = rs.getString(4);
                if (column != null && !"".equals(column)) {
                    String createIndex;
                    if (indexType != null && !"".equals(indexType)) {
                        createIndex =
                                "create index " + tableName + "_" + System.currentTimeMillis() + " on " + schema + "."
                                        + tableName
                                        + " (" + column + ") " + indexType + ";";
                    } else {
                        createIndex =
                                "create index " + tableName + "_" + System.currentTimeMillis() + " on " + schema + "."
                                        + tableName + " (" + column + ");";
                    }
                    indexAdvisor.add(createIndex);
                }
            }
            List<Integer> virtualIndexId = new ArrayList<>();
            if (!CollectionUtils.isEmpty(indexAdvisor)) {
                for (String index : indexAdvisor) {
                    String createVirtualIndex = "select * from hypopg_create_index('" + index + "');";
                    rs = stmt.executeQuery(createVirtualIndex);
                    while (rs.next()) {
                        virtualIndexId.add(rs.getInt(1));
                    }
                }
            }
            OuterPlan[] afterPlans = new OuterPlan[0];
            if (!CollectionUtils.isEmpty(indexAdvisor)) {
                String tunOnParam = "set enable_hypo_index = on;";
                stmt.execute(tunOnParam);
                String afterExplainSql = "explain (format json) " + diagnosisSql;
                rs = stmt.executeQuery(afterExplainSql);
                while (rs.next()) {
                    String planObj = rs.getString(1);
                    ObjectMapper objectMapper = new ObjectMapper();
                    afterPlans = objectMapper.readValue(planObj, OuterPlan[].class);
                }
                String tunOffParam = "set enable_hypo_index = off;";
                stmt.execute(tunOffParam);
            }
            OuterPlan afterExplain = afterPlans[0];
            Plan afterPlan = afterExplain.getPlan();
            if (!CollectionUtils.isEmpty(virtualIndexId)) {
                for (Integer id : virtualIndexId) {
                    String createVirtualIndex = "select * from hypopg_drop_index(" + id + ");";
                    stmt.executeQuery(createVirtualIndex);
                }
            }
            advisorDTO.setAdvisor(indexAdvisor);
            advisorDTO.setAfterExplain(util.getExecPlan(afterPlan));
            AnalysisDTO analysisDTO = new AnalysisDTO();
            double firstCost = firstPlan.getTotalCost();
            double afterCost = afterPlan.getTotalCost();
            BigDecimal improvement = new BigDecimal(firstCost).subtract(BigDecimal.valueOf(afterCost)).divide(
                    BigDecimal.valueOf(firstCost), 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            advisorDTO.setImprovement(
                    LocaleString.format("sql.IndexAdvisor.improvement", firstCost, afterCost, improvement));
            if (afterCost < firstCost) {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            }
            analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
            analysisDTO.setPointData(advisorDTO);
            return analysisDTO;
        } catch (SQLException | JsonProcessingException e) {
            throw new HisDiagnosisException("execute sql failed!");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error(CommonConstants.DATA_FAIL, e.getMessage());
                }
            }
        }
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
