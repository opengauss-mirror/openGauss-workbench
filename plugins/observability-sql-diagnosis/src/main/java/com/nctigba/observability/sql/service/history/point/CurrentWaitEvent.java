/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.DatabaseData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.WaitEventInfo;
import com.nctigba.observability.sql.model.history.point.WaitEventDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.ThreadWaitEventItem;
import com.nctigba.observability.sql.util.ComparatorUtil;
import com.nctigba.observability.sql.util.LocaleString;
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
public class CurrentWaitEvent implements HisDiagnosisPointService<WaitEventDTO> {
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
        return DiagnosisTypeCommon.CURRENT;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<DatabaseData> databaseDataList = new ArrayList<>();
        list.forEach(data -> {
            if (data instanceof DatabaseData) {
                databaseDataList.add((DatabaseData) data);
            }
        });
        int count = 0;
        List<WaitEventInfo> eventList = new ArrayList<>();
        for (DatabaseData dto : databaseDataList) {
            if (dto.getSqlName() != null && dto.getSqlName().getString("__name__").equals(SqlCommon.WAIT_EVENT)) {
                dto.setTableName(LocaleString.format("history.WAIT_EVENT.table"));
            }
            List<HashMap<String, String>> jsonArray = (List<HashMap<String, String>>) dto.getValue().get(0);
            for (HashMap<String, String> hashMap : jsonArray) {
                count += Integer.parseInt(String.valueOf(hashMap.get("count")));
                WaitEventInfo waitEventInfo = new WaitEventInfo();
                waitEventInfo.setEventName(hashMap.get("wait_status"));
                waitEventInfo.setEventCount(Integer.valueOf(String.valueOf(hashMap.get("count"))));
                String fieldName;
                if (hashMap.get("wait_status").contains(":")) {
                    fieldName = hashMap.get("wait_status").substring(0,
                            hashMap.get("wait_status").indexOf(":")).replace(" ", "");
                } else {
                    fieldName = hashMap.get("wait_status").replace(" ", "");
                }
                waitEventInfo.setEventDetail(LocaleString.format("history.WAIT_EVENT." + fieldName + ".detail"));
                waitEventInfo.setSuggestion(LocaleString.format("history.WAIT_EVENT." + fieldName + ".suggest"));
                eventList.add(waitEventInfo);
            }
        }
        HashMap<String, String> map = getThreshold(task);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (count > Integer.parseInt(map.get(ThresholdCommon.WAIT_EVENT_NUM))) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        eventList = eventList.stream().sorted(new ComparatorUtil()).collect(Collectors.toList());
        WaitEventDTO waitEventDTO = new WaitEventDTO();
        waitEventDTO.setData(eventList);
        waitEventDTO.setChartName(LocaleString.format("history.WAIT_EVENT.table"));
        analysisDTO.setPointData(waitEventDTO);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public WaitEventDTO getShowData(int taskId) {
        return null;
    }

    private HashMap<String, String> getThreshold(HisDiagnosisTask task) {
        HashMap<String, String> map = new HashMap<>();
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        for (HisDiagnosisThreshold threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdCommon.WAIT_EVENT_NUM)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        return map;
    }
}