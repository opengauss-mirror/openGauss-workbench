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
import com.nctigba.observability.sql.service.history.collection.ebpf.XfsSlowerItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.TableFormatter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XfsSlower
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class XfsSlower implements HisDiagnosisPointService<Object> {
    @Autowired
    private XfsSlowerItem item;

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
                task, TaskResult.ResultState.NO_ADVICE, ResultType.Xfsslower, FrameType.Table, Frame.bearing.center);
        list.add(center);
        var table = TableFormatter.format(file, "TIME");
        center.setData(table);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (table == null || table.getData() == null) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        } else {
            table.getData().stream().anyMatch(row -> {
                var find = NumberUtils.toInt(row.get("PID"), 0) == task.getPid();
                if (find) {
                    var top = new TaskResult(task, TaskResult.ResultState.SUGGESTION, ResultType.Xfsslower,
                            FrameType.Suggestion, Frame.bearing.top)
                            .setData(Map.of("title", LocaleString.format("XfsslowerAnaly.title"), "suggestions",
                                    List.of(LocaleString.format("XfsslowerAnaly.tip", row.get("FILENAME")))));
                    list.add(top);
                }
                return find;
            });
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        }
        Frame f = new Frame();
        for (TaskResult taskResult : list) {
            f.addChild(taskResult.getBearing(), taskResult.toFrame());
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        analysisDTO.setPointData(f);
        return analysisDTO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}