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
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.OuterPlan;
import com.nctigba.observability.sql.model.history.point.Plan;
import com.nctigba.observability.sql.model.history.point.TuningAdvisorDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.CurrentCpuUsageItem;
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
 * SmpParallelQuery
 *
 * @author luomeng
 * @since 2023/8/20
 */
@Slf4j
@Service
public class SmpParallelQuery implements HisDiagnosisPointService<Object> {
    @Autowired
    private CurrentCpuUsageItem item;
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
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        Object object = dataStoreService.getData(item).getCollectionData();
        AgentData agentData = new AgentData();
        if (object instanceof AgentData) {
            agentData = (AgentData) object;
        }
        List<AgentDTO> dtoList = agentData.getSysValue();
        float currentCpu = 0.0f;
        if (!CollectionUtils.isEmpty(dtoList)) {
            float cpuNum = 0.0f;
            for (AgentDTO dto : dtoList) {
                cpuNum += Float.parseFloat(dto.getCpu());
            }
            BigDecimal avgCpu = new BigDecimal(cpuNum).divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
            currentCpu = avgCpu.floatValue();
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        if (currentCpu > 50.0f) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            return new AnalysisDTO();
        }
        ResultSet rs = null;
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            String preSql = "SET explain_perf_mode TO normal;";
            stmt.execute(preSql);
            String diagnosisSql = task.getSql();
            rs = stmt.executeQuery("explain (format json) " + diagnosisSql);
            OuterPlan[] firstPlans = new OuterPlan[0];
            while (rs.next()) {
                String line = rs.getString(1);
                ObjectMapper objectMapper = new ObjectMapper();
                firstPlans = objectMapper.readValue(line, OuterPlan[].class);
            }
            OuterPlan firstExplain = firstPlans[0];
            Plan firstPlan = firstExplain.getPlan();
            OuterPlan[] afterPlans = new OuterPlan[0];
            if (isMatch(firstPlan)) {
                String tunOnParam = "set query_dop=4;";
                stmt.execute(tunOnParam);
                String afterExplainSql = "explain (format json) " + diagnosisSql;
                rs = stmt.executeQuery(afterExplainSql);
                while (rs.next()) {
                    String line = rs.getString(1);
                    ObjectMapper objectMapper = new ObjectMapper();
                    afterPlans = objectMapper.readValue(line, OuterPlan[].class);
                }
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                return new AnalysisDTO();
            }
            OuterPlan afterExplain = afterPlans[0];
            Plan afterPlan = afterExplain.getPlan();
            double afterCost = afterPlan.getTotalCost();
            double firstCost = firstPlan.getTotalCost();
            TuningAdvisorDTO advisorDTO = new TuningAdvisorDTO();
            BigDecimal improvement = new BigDecimal(firstCost).subtract(BigDecimal.valueOf(afterCost)).divide(
                    BigDecimal.valueOf(firstCost), 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            advisorDTO.setImprovement(
                    LocaleString.format(
                            "sql.SmpParallelQuery.improvement", currentCpu, firstCost, afterCost, improvement));
            if (afterCost < firstCost) {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            }
            advisorDTO.setFirstExplain(util.getExecPlan(firstPlan));
            advisorDTO.setAdvisor("set query_dop=4;");
            advisorDTO.setAfterExplain(util.getExecPlan(afterPlan));
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

    private boolean isMatch(Plan plan) {
        String regex = ".*(Scan|HashJoin|NestLoop|HashAgg|SortAgg|PlainAgg|WindowAgg|Local Redistribute"
                + "|Local Broadcast|Result|Subqueryscan|Unique|Material|Setop|Append|VectoRow).*";
        if (plan.getNodeType().matches(regex)) {
            return true;
        }
        if (plan.getPlans() == null) {
            return plan.getNodeType().matches(regex);
        } else {
            for (Plan chilePlan : plan.getPlans()) {
                return isMatch(chilePlan);
            }
        }
        return false;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
