/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.CurrentCpuUsageDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.CurrentCpuUsageItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CurrentCpuUsage
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class CurrentCpuUsage implements HisDiagnosisPointService<AgentDTO> {
    @Autowired
    private CurrentCpuUsageItem item;

    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public String getDiagnosisType() {
        return DiagnosisTypeCommon.CURRENT;
    }


    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        HashMap<String, String> map = new HashMap<>();
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        for (HisDiagnosisThreshold threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdCommon.CPU_USAGE_RATE)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        Object object = item.queryData(task);
        AgentData agentData = null;
        if (object instanceof AgentData) {
            agentData = (AgentData) object;
        }
        if (agentData == null || CollectionUtils.isEmpty(agentData.getDbValue())) {
            return new AnalysisDTO();
        }
        List<AgentDTO> dtoList = agentData.getSysValue();
        float cpuNum = 0.0f;
        for (AgentDTO dto : dtoList) {
            cpuNum += Float.parseFloat(dto.getCpu());
        }
        BigDecimal avgCpu = new BigDecimal(cpuNum).divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (avgCpu.floatValue() > Float.parseFloat(map.get(ThresholdCommon.CPU_USAGE_RATE))) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        CurrentCpuUsageDTO usageDTO = new CurrentCpuUsageDTO();
        usageDTO.setAvgUsage(avgCpu.floatValue());
        usageDTO.setTotalUsage(cpuNum);
        analysisDTO.setPointData(usageDTO);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public AgentDTO getShowData(int taskId) {
        return null;
    }
}
