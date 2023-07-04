/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.DatabaseData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.TopCpuTimeSqlItem;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * TopCpuTimeSql
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class TopCpuTimeSql implements HisDiagnosisPointService<Object> {
    @Autowired
    TopCpuTimeSqlItem item;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;

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
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<DatabaseData> databaseDataList = new ArrayList<>();
        list.forEach(data -> {
            if (data instanceof DatabaseData) {
                databaseDataList.add((DatabaseData) data);
            }
        });
        if (!CollectionUtils.isEmpty(databaseDataList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DISPLAY);
        return analysisDTO;
    }

    @Override
    public List<?> getShowData(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<DatabaseData> dataList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            Object object = item.queryData(task);
            if (object instanceof String) {
                List<String> message = new ArrayList<>();
                message.add(String.valueOf(object));
                return message;
            } else {
                List<?> list = (List<?>) item.queryData(task);
                List<DatabaseData> databaseDataList = new ArrayList<>();
                list.forEach(data -> {
                    if (data instanceof DatabaseData) {
                        databaseDataList.add((DatabaseData) data);
                    }
                });
                if (CollectionUtils.isEmpty(databaseDataList)) {
                    continue;
                }
                dataList.addAll(databaseDataList);
            }
        }
        for (DatabaseData dto : dataList) {
            if (dto.getSqlName() != null && dto.getSqlName().getString("__name__").equals(SqlCommon.TOP_CPU_TIME_SQL)) {
                dto.setTableName(LocaleString.format("history.TOP_CPU_TIME_SQL.table"));
            }
        }
        return dataList;
    }
}
