/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TaskInfo
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class HisTaskInfo implements HisDiagnosisPointService<HisDiagnosisTask> {
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private ClusterManager clusterManager;

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
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.ROOT);
        return analysisDTO;
    }

    @Override
    public HisDiagnosisTask getShowData(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<OpsClusterVO> clusterVOList = clusterManager.getAllOpsCluster();
        OpsClusterVO nodeInfo = new OpsClusterVO();
        for (OpsClusterVO clusterVO : clusterVOList) {
            List<OpsClusterNodeVO> clusterNodes = clusterVO.getClusterNodes();
            for (OpsClusterNodeVO nodeVO : clusterNodes) {
                if (nodeVO.getNodeId().equals(task.getNodeId())) {
                    nodeInfo = clusterVO;
                    break;
                }
            }
        }
        task.setNodeVOSub(nodeInfo);
        return task;
    }
}
