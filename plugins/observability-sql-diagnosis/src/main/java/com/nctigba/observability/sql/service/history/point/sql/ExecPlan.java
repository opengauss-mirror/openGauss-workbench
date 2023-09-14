/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.handler.TopSQLHandler;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.ExecPlanDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Explain
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExecPlan implements HisDiagnosisPointService<ExecPlanDTO> {
    private final HisDiagnosisTaskMapper taskMapper;
    private final ClusterManager clusterManager;
    private final TopSQLHandler openGaussTopSQLHandler;

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
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public ExecPlanDTO getShowData(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        try {
            JSONObject plan = openGaussTopSQLHandler.getExecutionPlan(task.getNodeId(),
                    String.valueOf(task.getDebugQueryId()), "");
            Map<String, Object> planDetail = planDetail(task.getNodeId(), String.valueOf(task.getDebugQueryId()));
            ExecPlanDTO execPlanDTO = new ExecPlanDTO();
            execPlanDTO.setPlan(plan);
            execPlanDTO.setDetail(planDetail);
            return execPlanDTO;
        } catch (HisDiagnosisException e) {
            return null;
        }
    }

    private Map<String, Object> planDetail(String nodeId, String sqlId) {
        Map<String, Object> map = new HashMap<>();
        try (Connection connection = clusterManager.getConnectionByNodeId(nodeId);
             PreparedStatement preparedStatement = connection.prepareStatement(SqlCommon.PLAN_DETAIL_SQL)) {
            preparedStatement.setString(1, sqlId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        map.put(columnName, value);
                    }
                }
                return map;
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
    }
}
