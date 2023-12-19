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
 *  BlockSession.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/BlockSession.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.BlockSessionDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockSession
 *
 * @author luomeng
 * @since 2023/8/22
 */
@Slf4j
@Service
public class BlockSession implements DiagnosisPointService<Object> {
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private DiagnosisTaskMapper taskMapper;

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
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            DiagnosisTaskDO currentTask;
            List<BlockSessionDTO> list = new ArrayList<>();
            do {
                currentTask = taskMapper.selectById(task.getId());
                String sql = SqlConstants.BLOCK_SESSION;
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    BlockSessionDTO sessionDTO = new BlockSessionDTO();
                    sessionDTO.setStartTime(rs.getString(1));
                    sessionDTO.setWSessionId(rs.getString(2));
                    sessionDTO.setWPid(rs.getString(3));
                    sessionDTO.setLSessionId(rs.getString(4));
                    sessionDTO.setLPid(rs.getString(5));
                    sessionDTO.setLUser(rs.getString(6));
                    sessionDTO.setLockingQuery(rs.getString(7));
                    sessionDTO.setTableName(rs.getString(8));
                    sessionDTO.setApplicationName(rs.getString(9));
                    sessionDTO.setClientAddress(rs.getString(10));
                    sessionDTO.setState(rs.getString(11));
                    sessionDTO.setMode(rs.getString(12));
                    list.add(sessionDTO);
                }
                ThreadUtil.sleep(1000L);
            } while (currentTask != null && currentTask.getTaskEndTime() == null);
            AnalysisDTO analysisDTO = new AnalysisDTO();
            analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
            if (CollectionUtils.isEmpty(list)) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            } else {
                BlockSessionDTO blockData = list.get(0);
                blockData.setEndTime(list.get(list.size() - 1).getStartTime());
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                analysisDTO.setPointData(blockData);
            }
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
