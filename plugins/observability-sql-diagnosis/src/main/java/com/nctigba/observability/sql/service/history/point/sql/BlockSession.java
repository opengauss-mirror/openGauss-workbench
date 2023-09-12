/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.BlockSessionDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
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
public class BlockSession implements HisDiagnosisPointService<Object> {
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;

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
            HisDiagnosisTask currentTask;
            List<BlockSessionDTO> list = new ArrayList<>();
            do {
                currentTask = taskMapper.selectById(task.getId());
                String sql = SqlCommon.BLOCK_SESSION;
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    BlockSessionDTO sessionDTO = new BlockSessionDTO();
                    String blockSql = rs.getString(2);
                    sessionDTO.setWaitingQuery(blockSql);
                    sessionDTO.setWPid(rs.getString(3));
                    sessionDTO.setWUser(rs.getString(4));
                    sessionDTO.setLockingQuery(rs.getString(5));
                    sessionDTO.setLPid(rs.getString(6));
                    sessionDTO.setLUser(rs.getString(7));
                    sessionDTO.setTableName(rs.getString(8));
                    String queryTime = rs.getString(1);
                    if (CollectionUtils.isEmpty(list)) {
                        sessionDTO.setStartTime(rs.getString(1));
                        list.add(sessionDTO);
                    } else {
                        list.forEach(f -> {
                            if (f.getWaitingQuery().equals(blockSql)) {
                                f.setEndTime(queryTime);
                            } else {
                                sessionDTO.setStartTime(queryTime);
                                list.add(sessionDTO);
                            }
                        });
                    }
                }
                ThreadUtil.sleep(1000L);
            } while (currentTask != null && currentTask.getTaskEndTime() == null);
            AnalysisDTO analysisDTO = new AnalysisDTO();
            analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
            if (CollectionUtils.isEmpty(list)) {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            }
            analysisDTO.setPointData(list);
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
