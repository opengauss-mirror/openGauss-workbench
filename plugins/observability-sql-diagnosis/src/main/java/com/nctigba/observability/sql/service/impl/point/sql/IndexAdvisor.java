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
 *  IndexAdvisor.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/IndexAdvisor.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.TuningAdvisorDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.point.OuterPlanVO;
import com.nctigba.observability.sql.model.vo.point.PlanVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
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
 * IndexAdvisor
 *
 * @author luomeng
 * @since 2023/8/17
 */
@Slf4j
@Service
public class IndexAdvisor implements DiagnosisPointService<Object> {
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
        return null;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        ResultSet rs = null;
        TuningAdvisorDTO advisorDTO = new TuningAdvisorDTO();
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            String preSql = "SET explain_perf_mode TO normal;";
            stmt.execute(preSql);
            String diagnosisSql = task.getSql();
            rs = stmt.executeQuery("explain (format json) " + diagnosisSql);
            OuterPlanVO[] firstPlans = new OuterPlanVO[0];
            while (rs.next()) {
                String planObj = rs.getString(1);
                ObjectMapper objectMapper = new ObjectMapper();
                firstPlans = objectMapper.readValue(planObj, OuterPlanVO[].class);
            }
            PlanVO firstPlanVO = new PlanVO();
            if (firstPlans.length > 0) {
                OuterPlanVO firstExplain = firstPlans[0];
                firstPlanVO = firstExplain.getPlanVO();
            }
            advisorDTO.setFirstExplain(util.getExecPlan(firstPlanVO));
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
            OuterPlanVO[] afterPlans = new OuterPlanVO[0];
            if (!CollectionUtils.isEmpty(indexAdvisor)) {
                String tunOnParam = "set enable_hypo_index = on;";
                stmt.execute(tunOnParam);
                String afterExplainSql = "explain (format json) " + diagnosisSql;
                rs = stmt.executeQuery(afterExplainSql);
                while (rs.next()) {
                    String planObj = rs.getString(1);
                    ObjectMapper objectMapper = new ObjectMapper();
                    afterPlans = objectMapper.readValue(planObj, OuterPlanVO[].class);
                }
                String tunOffParam = "set enable_hypo_index = off;";
                stmt.execute(tunOffParam);
            }
            PlanVO afterPlanVO = new PlanVO();
            if (afterPlans.length > 0) {
                OuterPlanVO afterExplain = afterPlans[0];
                afterPlanVO = afterExplain.getPlanVO();
            }
            if (!CollectionUtils.isEmpty(virtualIndexId)) {
                for (Integer id : virtualIndexId) {
                    String createVirtualIndex = "select * from hypopg_drop_index(" + id + ");";
                    stmt.executeQuery(createVirtualIndex);
                }
            }
            AnalysisDTO analysisDTO = new AnalysisDTO();
            if (afterPlanVO.getNodeType() != null) {
                advisorDTO.setAdvisor(indexAdvisor);
                advisorDTO.setAfterExplain(util.getExecPlan(afterPlanVO));
                double firstCost = firstPlanVO.getTotalCost();
                double afterCost = afterPlanVO.getTotalCost();
                BigDecimal improvement = new BigDecimal(firstCost).subtract(BigDecimal.valueOf(afterCost)).divide(
                        BigDecimal.valueOf(firstCost), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                advisorDTO.setImprovement(
                        LocaleStringUtils.format("sql.IndexAdvisor.improvement", firstCost, afterCost, improvement));
                if (afterCost < firstCost) {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                } else {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                }
            } else {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            }
            analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
            analysisDTO.setPointData(advisorDTO);
            return analysisDTO;
        } catch (SQLException | JsonProcessingException e) {
            AnalysisDTO analysisDTO = new AnalysisDTO();
            analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            analysisDTO.setPointData(advisorDTO);
            return analysisDTO;
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
