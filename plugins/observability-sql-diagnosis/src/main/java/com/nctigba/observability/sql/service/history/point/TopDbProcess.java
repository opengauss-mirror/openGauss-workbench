/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.CurrentCpuDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.TopDbProcessItem;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * TopDbProcess
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class TopDbProcess implements HisDiagnosisPointService<List<AgentDTO>> {
    @Autowired
    private TopDbProcessItem item;

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
        Object object = item.queryData(task);
        AgentData agentData = null;
        if (object instanceof AgentData) {
            agentData = (AgentData) object;
        }
        if (agentData == null || CollectionUtils.isEmpty(agentData.getDbValue())) {
            return new AnalysisDTO();
        }
        List<AgentDTO> dtoList = agentData.getDbValue();
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (!CollectionUtils.isEmpty(dtoList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        CurrentCpuDTO cpuDTO = new CurrentCpuDTO();
        cpuDTO.setChartName(LocaleString.format("history.currentProCpu.agent"));
        cpuDTO.setDataList(dtoList.subList(0, 10));
        analysisDTO.setPointData(cpuDTO);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DISPLAY);
        return analysisDTO;
    }

    @Override
    public List<AgentDTO> getShowData(int taskId) {
        return null;
    }
}