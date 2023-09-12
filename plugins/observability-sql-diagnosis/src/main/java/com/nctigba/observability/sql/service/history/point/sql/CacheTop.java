/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.result.TaskResult;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.CacheTopItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PercentUtil;
import com.nctigba.observability.sql.util.TableFormatter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CacheTop
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class CacheTop implements HisDiagnosisPointService<Object> {
    @Autowired
    private CacheTopItem item;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionCommon.IS_BCC));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        Object obj = dataStoreService.getData(item).getCollectionData();
        MultipartFile file = null;
        if (obj instanceof MultipartFile) {
            file = (MultipartFile) obj;
        }
        List<TaskResult> list = new ArrayList<>();
        var center = new TaskResult(
                task, TaskResult.ResultState.NO_ADVICE, ResultType.CACHETOP, FrameType.Table, Frame.bearing.center);
        list.add(center);
        var table = TableFormatter.format(file, "PID");
        center.setData(table);
        if (!CollectionUtils.isEmpty(table.getData())) {
            table.getData().stream().anyMatch(row -> {
                if (NumberUtils.toInt(row.get("PID"), 0) == task.getPid()) {
                    var readHit = PercentUtil.parse(row.get("READ_HIT"), 1);
                    var writeHit = PercentUtil.parse(row.get("WRITE_HIT"), 1);
                    if (readHit < 1 || writeHit < 1) {
                        var top = new TaskResult(task, TaskResult.ResultState.SUGGESTION, ResultType.CACHETOP,
                                FrameType.Suggestion, Frame.bearing.top)
                                .setData(Map.of("title", LocaleString.format("Cachetop.title"), "suggestions",
                                        List.of(LocaleString.format(
                                                "Cachetop.TIP", task.getPid(), readHit, writeHit))));
                        list.add(top);
                    }
                    return true;
                }
                return false;
            });
        }
        Frame f = new Frame();
        for (TaskResult taskResult : list) {
            f.addChild(taskResult.getBearing(), taskResult.toFrame());
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        analysisDTO.setPointData(f);
        return analysisDTO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}