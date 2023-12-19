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
 *  ExecPlan.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/ExecPlan.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.handler.TopSQLHandler;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.ExecPlanDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.DataStoreService;
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
public class ExecPlan implements DiagnosisPointService<ExecPlanDTO> {
    private final DiagnosisTaskMapper taskMapper;
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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public ExecPlanDTO getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
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
             PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.PLAN_DETAIL_SQL)) {
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
