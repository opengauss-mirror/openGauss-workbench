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
import com.nctigba.observability.sql.service.history.collection.ebpf.RunQLatItem;
import com.nctigba.observability.sql.util.HistogramToHeatmap;
import com.nctigba.observability.sql.util.LocaleString;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RunQLat
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class RunQLat implements HisDiagnosisPointService<Object> {
    @Autowired
    private RunQLatItem item;

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
        var heatMap = HistogramToHeatmap.format(file, LocaleString.format("RunqlatAnaly.unit"));
        var map = heatMap.stream().filter(
                data -> NumberUtils.toInt(data.getName(), 0) == task.getPid()).findFirst().orElse(null);
        heatMap.clear();
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        if (map == null) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            return analysisDTO;
        }
        heatMap.add(map);
        map.setName(LocaleString.format("RunqlatAnaly.name"));
        List<TaskResult> list = new ArrayList<>();
        List<ArrayList<Integer>> source = map.getSource();
        float rate = analysisRate(source);
        if (rate > 5.0f) {
            TaskResult result = new TaskResult(
                    task, TaskResult.ResultState.SUGGESTION, ResultType.runqlat, FrameType.Suggestion,
                    Frame.bearing.top);
            result.setData(Map.of("title", LocaleString.format("RunqlatAnaly.title"), "suggestions",
                    List.of(LocaleString.format("RunqlatAnaly.TIP", rate, 5))));
            list.add(result);
        }
        var center = new TaskResult(task, TaskResult.ResultState.NO_ADVICE, ResultType.runqlat, FrameType.HeatMap,
                Frame.bearing.center);
        heatMap.forEach(m -> m.setSource(null));
        center.setData(heatMap);
        list.add(center);
        Frame f = new Frame();
        for (TaskResult taskResult : list) {
            f.addChild(taskResult.getBearing(), taskResult.toFrame());
        }
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        analysisDTO.setPointData(f);
        return analysisDTO;
    }

    private float analysisRate(List<ArrayList<Integer>> source) {
        ArrayList<Integer> sum = new ArrayList<>();
        for (ArrayList<Integer> arrayList : source) {
            for (int i = 0; i < arrayList.size(); i++) {
                if (sum.size() <= i) {
                    sum.add(0);
                }
                sum.set(i, arrayList.get(i) + sum.get(i));
            }
        }
        int r10 = 0;
        int ra = 0;
        for (int i = 0; i < sum.size(); i++) {
            if (i >= 10) {
                r10 += sum.get(i);
            }
            ra += sum.get(i);
        }
        float rate = 0.0f;
        if (ra != 0) {
            rate = (float) (r10 / ra * 100);
        }
        return rate;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}