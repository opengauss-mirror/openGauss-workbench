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
import com.nctigba.observability.sql.service.history.collection.ebpf.TcpLifeItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.TableFormatter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TcpLife
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class TcpLife implements HisDiagnosisPointService<Object> {
    private static final String[] TIP = {"TcpLife.TIP0", "TcpLife.TIP1"};

    @Autowired
    private TcpLifeItem item;

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
        List<TaskResult> list = getResultData(task, file);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (!CollectionUtils.isEmpty(list)) {
            Frame f = new Frame();
            for (TaskResult taskResult : list) {
                f.addChild(taskResult.getBearing(), taskResult.toFrame());
            }
            analysisDTO.setPointData(f);
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    private List<TaskResult> getResultData(HisDiagnosisTask task, MultipartFile file) {
        List<TaskResult> list = new ArrayList<>();
        var center = new TaskResult(
                task, TaskResult.ResultState.NO_ADVICE, ResultType.TcplifeTCP, FrameType.Table, Frame.bearing.center);
        list.add(center);
        var table = TableFormatter.format(file, "TIME");
        center.setData(table);
        var counter = new HashMap<String, Set<String>>();
        var thisThread = new HashSet<>();
        if (!CollectionUtils.isEmpty(table.getData())) {
            table.getData().forEach(row -> {
                var key = row.get("LADDR") + ":" + row.get("LPORT");
                var raddr = row.get("RADDR");
                if (NumberUtils.toInt(row.get("PID"), 0) == task.getPid()) {
                    thisThread.add(raddr);
                }
                if (!counter.containsKey(key)) {
                    counter.put(key, new HashSet<>());
                }
                counter.get(key).add(raddr);
            });
            var suggestions = new ArrayList<>();
            if (thisThread.size() > 1) {
                suggestions.add(LocaleString.format(TIP[0], task.getPid(), thisThread.size()));
            }
            counter.forEach((key, set) -> {
                if (set.size() > 1) {
                    suggestions.add(LocaleString.format(TIP[1], key, set.size()));
                }
            });
            if (suggestions.size() > 0) {
                TaskResult top = new TaskResult(
                        task, TaskResult.ResultState.SUGGESTION, ResultType.TcplifeTCP, FrameType.Suggestion,
                        Frame.bearing.top)
                        .setData(Map.of("title", LocaleString.format("TcpLife.title"), "suggestions", suggestions));
                list.add(top);
            }
        }
        return list;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}