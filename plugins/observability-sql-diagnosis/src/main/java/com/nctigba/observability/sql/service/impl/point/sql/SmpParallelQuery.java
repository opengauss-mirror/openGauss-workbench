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
 *  SmpParallelQuery.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/SmpParallelQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.model.dto.point.AgentDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.TuningAdvisorDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.collection.AgentVO;
import com.nctigba.observability.sql.model.vo.point.OuterPlanVO;
import com.nctigba.observability.sql.model.vo.point.PlanVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.collection.agent.CurrentCpuUsageItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PointUtils;
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
public class SmpParallelQuery implements DiagnosisPointService<Object> {
    @Autowired
    private CurrentCpuUsageItem item;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private PointUtils util;

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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        Object object = dataStoreService.getData(item).getCollectionData();
        AgentVO agentVO = new AgentVO();
        if (object instanceof AgentVO) {
            agentVO = (AgentVO) object;
        }
        List<AgentDTO> dtoList = agentVO.getSysValue();
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
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        if (currentCpu > 50.0f) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            return new AnalysisDTO();
        }
        TuningAdvisorDTO advisorDTO = new TuningAdvisorDTO();
        advisorDTO.setAdvisor("set query_dop=4;");
        ResultSet rs = null;
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            String preSql = "SET explain_perf_mode TO normal;";
            stmt.execute(preSql);
            String diagnosisSql = task.getSql();
            rs = stmt.executeQuery("explain (format json) " + diagnosisSql);
            OuterPlanVO[] firstPlans = new OuterPlanVO[0];
            while (rs.next()) {
                String line = rs.getString(1);
                ObjectMapper objectMapper = new ObjectMapper();
                firstPlans = objectMapper.readValue(line, OuterPlanVO[].class);
            }
            PlanVO firstPlanVO = new PlanVO();
            if (firstPlans.length > 0) {
                OuterPlanVO firstExplain = firstPlans[0];
                firstPlanVO = firstExplain.getPlanVO();
            }
            advisorDTO.setFirstExplain(util.getExecPlan(firstPlanVO));
            OuterPlanVO[] afterPlans = new OuterPlanVO[0];
            if (isMatch(firstPlanVO)) {
                String tunOnParam = "set query_dop=4;";
                stmt.execute(tunOnParam);
                String afterExplainSql = "explain (format json) " + diagnosisSql;
                rs = stmt.executeQuery(afterExplainSql);
                while (rs.next()) {
                    String line = rs.getString(1);
                    ObjectMapper objectMapper = new ObjectMapper();
                    afterPlans = objectMapper.readValue(line, OuterPlanVO[].class);
                }
            } else {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                return new AnalysisDTO();
            }
            PlanVO afterPlanVO = new PlanVO();
            if (afterPlans.length > 0) {
                OuterPlanVO afterExplain = afterPlans[0];
                afterPlanVO = afterExplain.getPlanVO();
            }
            if (afterPlanVO.getNodeType() != null) {
                double afterCost = afterPlanVO.getTotalCost();
                double firstCost = firstPlanVO.getTotalCost();
                BigDecimal improvement = new BigDecimal(firstCost).subtract(BigDecimal.valueOf(afterCost)).divide(
                        BigDecimal.valueOf(firstCost), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                advisorDTO.setImprovement(
                        LocaleStringUtils.format(
                                "sql.SmpParallelQuery.improvement", currentCpu, firstCost, afterCost, improvement));
                if (afterCost < firstCost) {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                } else {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                }
                advisorDTO.setAfterExplain(util.getExecPlan(afterPlanVO));
            }
            analysisDTO.setPointData(advisorDTO);
            return analysisDTO;
        } catch (SQLException | JsonProcessingException e) {
            log.error("execute sql failed:" + e.getMessage());
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

    private boolean isMatch(PlanVO planVO) {
        String regex = ".*(Scan|HashJoin|NestLoop|HashAgg|SortAgg|PlainAgg|WindowAgg|Local Redistribute"
                + "|Local Broadcast|Result|Subqueryscan|Unique|Material|Setop|Append|VectoRow).*";
        if (planVO.getNodeType() != null && planVO.getNodeType().matches(regex)) {
            return true;
        }
        if (planVO.getPlanVOS() == null) {
            return planVO.getNodeType().matches(regex);
        } else {
            for (PlanVO chilePlanVO : planVO.getPlanVOS()) {
                return isMatch(chilePlanVO);
            }
        }
        return false;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
