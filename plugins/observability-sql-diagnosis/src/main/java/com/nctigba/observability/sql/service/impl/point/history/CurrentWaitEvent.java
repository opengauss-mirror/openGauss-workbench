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
 *  CurrentWaitEvent.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/CurrentWaitEvent.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.WaitEventDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.vo.point.WaitEventInfoVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.ThreadWaitEventItem;
import com.nctigba.observability.sql.util.ComparatorUtils;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CurrentWaitEvent
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class CurrentWaitEvent implements DiagnosisPointService<WaitEventDTO> {
    @Autowired
    ThreadWaitEventItem item;

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
        return PointTypeConstants.CURRENT;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<DatabaseVO> databaseVOList = new ArrayList<>();
        list.forEach(data -> {
            if (data instanceof DatabaseVO) {
                databaseVOList.add((DatabaseVO) data);
            }
        });
        int count = 0;
        List<WaitEventInfoVO> eventList = new ArrayList<>();
        for (DatabaseVO dto : databaseVOList) {
            if (dto.getSqlName() != null && dto.getSqlName().getString("__name__").equals(SqlConstants.WAIT_EVENT)) {
                dto.setTableName(LocaleStringUtils.format("history.WAIT_EVENT.table"));
            }
            List<HashMap<String, String>> jsonArray = (List<HashMap<String, String>>) dto.getValue().get(0);
            for (HashMap<String, String> hashMap : jsonArray) {
                count += Integer.parseInt(String.valueOf(hashMap.get("count")));
                WaitEventInfoVO waitEventInfoVO = new WaitEventInfoVO();
                waitEventInfoVO.setEventName(hashMap.get("wait_status"));
                waitEventInfoVO.setEventCount(Integer.valueOf(String.valueOf(hashMap.get("count"))));
                String fieldName;
                if (hashMap.get("wait_status").contains(":")) {
                    fieldName = hashMap.get("wait_status").substring(
                            0, hashMap.get("wait_status").indexOf(":")).replace(" ", "");
                } else {
                    fieldName = hashMap.get("wait_status").replace(" ", "");
                }
                waitEventInfoVO.setEventDetail(LocaleStringUtils.format("history.WAIT_EVENT." + fieldName + ".detail"));
                waitEventInfoVO.setSuggestion(LocaleStringUtils.format("history.WAIT_EVENT." + fieldName + ".suggest"));
                eventList.add(waitEventInfoVO);
            }
        }
        HashMap<String, String> map = getThreshold(task);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (count > Integer.parseInt(map.get(ThresholdConstants.WAIT_EVENT_NUM))) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        eventList = eventList.stream().sorted(new ComparatorUtils()).collect(Collectors.toList());
        WaitEventDTO waitEventDTO = new WaitEventDTO();
        waitEventDTO.setData(eventList);
        waitEventDTO.setChartName(LocaleStringUtils.format("history.WAIT_EVENT.table"));
        analysisDTO.setPointData(waitEventDTO);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public WaitEventDTO getShowData(int taskId) {
        return null;
    }

    private HashMap<String, String> getThreshold(DiagnosisTaskDO task) {
        HashMap<String, String> map = new HashMap<>();
        List<DiagnosisThresholdDO> thresholds = task.getThresholds();
        for (DiagnosisThresholdDO threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdConstants.WAIT_EVENT_NUM)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        return map;
    }
}