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
 *  DeadTuple.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/DeadTuple.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.util.DbUtils;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DeadTuple
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class DeadTuple implements DiagnosisPointService<AutoShowDataVO> {
    private static final float THRESHOLD_VALUE = 20.0f;

    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private DbUtils dbUtils;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
    @Autowired
    private PointUtils pointUtils;

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
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        if (!task.getSql().contains("from")) {
            return analysisDTO;
        }
        String sql = String.format(SqlConstants.DEAD_TUPLE, pointUtils.getTableName(task.getSql()));
        try (Connection conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             java.sql.Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                if (rs.getFloat(6) > THRESHOLD_VALUE) {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                }
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return analysisDTO;
    }

    @Override
    public AutoShowDataVO getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        String sql = String.format(SqlConstants.DEAD_TUPLE, pointUtils.getTableName(task.getSql()));
        List<?> resultList = (List<?>) dbUtils.rangQuery(sql, null, null, task.getNodeId());
        List<ShowData> tableList = pointUtils.getTableData(resultList, "DeadTuple");
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        return dataVO;
    }
}