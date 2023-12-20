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
 *  CurrentCpuUsage.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/CurrentCpuUsage.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.AgentVO;
import com.nctigba.observability.sql.model.dto.point.AgentDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.CurrentCpuUsageDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.agent.CurrentCpuUsageItem;
import com.nctigba.observability.sql.service.impl.collection.metric.CpuCoreNumItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.util.PointUtils;
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
public class CurrentCpuUsage implements DiagnosisPointService<AgentDTO> {
    @Autowired
    private CurrentCpuUsageItem item;
    @Autowired
    private CpuCoreNumItem cpuCoreNumItem;
    @Autowired
    private PointUtils util;

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
        list.add(cpuCoreNumItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        HashMap<String, String> map = new HashMap<>();
        List<DiagnosisThresholdDO> thresholds = task.getThresholds();
        for (DiagnosisThresholdDO threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdConstants.CPU_USAGE_RATE)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        Object object = dataStoreService.getData(item).getCollectionData();
        AgentVO agentVO = null;
        if (object instanceof AgentVO) {
            agentVO = (AgentVO) object;
        }
        if (agentVO == null || CollectionUtils.isEmpty(agentVO.getDbValue())) {
            return new AnalysisDTO();
        }
        List<AgentDTO> dtoList = agentVO.getSysValue();
        float cpuNum = 0.0f;
        for (AgentDTO dto : dtoList) {
            cpuNum += Float.parseFloat(dto.getCpu());
        }
        List<?> list = (List<?>) dataStoreService.getData(cpuCoreNumItem).getCollectionData();
        int coreNum = util.getCpuCoreNum(list);
        BigDecimal avgCpu = new BigDecimal(cpuNum).divide(BigDecimal.valueOf(coreNum), 2, RoundingMode.HALF_UP);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (avgCpu.floatValue() > Float.parseFloat(map.get(ThresholdConstants.CPU_USAGE_RATE))) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        CurrentCpuUsageDTO usageDTO = new CurrentCpuUsageDTO();
        usageDTO.setAvgUsage(avgCpu.floatValue());
        usageDTO.setTotalUsage(cpuNum);
        analysisDTO.setPointData(usageDTO);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public AgentDTO getShowData(int taskId) {
        return null;
    }
}
