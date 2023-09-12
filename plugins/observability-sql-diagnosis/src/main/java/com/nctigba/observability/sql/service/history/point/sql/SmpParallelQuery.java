/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.IndexAdvisorDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.CurrentCpuUsageItem;
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
        if (CollectionUtils.isEmpty(dtoList)) {
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
            String diagnosisSql = task.getSql();
            rs = stmt.executeQuery("explain " + diagnosisSql);
            List<String> firstExplain = new ArrayList<>();
            while (rs.next()) {
                firstExplain.add(rs.getString(1));
            }
            String regex = ".*(Scan|HashJoin|NestLoop|HashAgg|SortAgg|PlainAgg|WindowAgg|Local Redistribute"
                    + "|Local Broadcast|Result|Subqueryscan|Unique|Material|Setop|Append|VectoRow).*";
            boolean isExists = firstExplain.stream().anyMatch(f -> f.matches(regex));
            List<String> afterExplain = new ArrayList<>();
            if (isExists) {
                String tunOnParam = "set query_dop=4;";
                stmt.execute(tunOnParam);
                String afterExplainSql = "explain " + diagnosisSql;
                rs = stmt.executeQuery(afterExplainSql);
                while (rs.next()) {
                    afterExplain.add(rs.getString(1));
                }
                String tunOffParam = "set query_dop=1;";
                stmt.execute(tunOffParam);
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                return new AnalysisDTO();
            }
            IndexAdvisorDTO advisorDTO = new IndexAdvisorDTO();
            advisorDTO.setFirstExplain(firstExplain);
            advisorDTO.setIndexAdvisor(null);
            advisorDTO.setAfterExplain(afterExplain);
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            analysisDTO.setPointData(advisorDTO);
            return analysisDTO;
        } catch (SQLException e) {
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
