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
import com.nctigba.observability.sql.model.history.point.CurrentCpuDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.OtherProcessCurrentCpuItem;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * OtherProcessCurrentCpuUsage
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class OtherProcessCurrentCpuUsage implements HisDiagnosisPointService<List<AgentDTO>> {
    @Autowired
    private OtherProcessCurrentCpuItem item;

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
    public String getDiagnosisType() {
        return DiagnosisTypeCommon.CURRENT;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        HashMap<String, String> map = new HashMap<>();
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        for (HisDiagnosisThreshold threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdCommon.PRO_CPU_USAGE_RATE)) {
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
        int cpuNum = 0;
        for (AgentDTO dto : dtoList) {
            if (!dto.getCommand().equals("gaussdb")) {
                cpuNum += Float.parseFloat(dto.getCpu());
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (cpuNum / 8 > Integer.parseInt(map.get(ThresholdCommon.PRO_CPU_USAGE_RATE))) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        List<AgentDTO> subList = dtoList.subList(0, 10);
        subList.forEach(f -> f.setCpu(String.valueOf(Float.parseFloat(f.getCpu()) / 8)));
        CurrentCpuDTO cpuDTO = new CurrentCpuDTO();
        cpuDTO.setChartName(LocaleString.format("history.currentCpu.agent"));
        cpuDTO.setDataList(subList);
        analysisDTO.setPointData(cpuDTO);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public List<AgentDTO> getShowData(int taskId) {
        return null;
    }
}