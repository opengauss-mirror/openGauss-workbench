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
 *  TopDbProcess.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/TopDbProcess.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.collection.AgentVO;
import com.nctigba.observability.sql.model.dto.point.AgentDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.CurrentCpuDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.agent.TopDbProcessItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
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
public class TopDbProcess implements DiagnosisPointService<AgentDTO> {
    @Autowired
    private TopDbProcessItem item;

    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public String getDiagnosisType() {
        return PointTypeConstants.CURRENT;
    }


    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        Object object = dataStoreService.getData(item).getCollectionData();
        AgentVO agentVO = null;
        if (object instanceof AgentVO) {
            agentVO = (AgentVO) object;
        }
        if (agentVO == null || CollectionUtils.isEmpty(agentVO.getDbValue())) {
            return new AnalysisDTO();
        }
        List<AgentDTO> dtoList = agentVO.getDbValue();
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (!CollectionUtils.isEmpty(dtoList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        CurrentCpuDTO cpuDTO = new CurrentCpuDTO();
        cpuDTO.setChartName(LocaleStringUtils.format("history.currentProCpu.agent"));
        if (dtoList.size() > 10) {
            cpuDTO.setDataList(dtoList.subList(0, 10));
        } else {
            cpuDTO.setDataList(dtoList);
        }
        analysisDTO.setPointData(cpuDTO);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DISPLAY);
        return analysisDTO;
    }

    @Override
    public AgentDTO getShowData(int taskId) {
        return null;
    }
}