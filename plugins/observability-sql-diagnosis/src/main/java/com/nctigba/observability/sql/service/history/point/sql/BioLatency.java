/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import cn.hutool.core.util.ReUtil;
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
import com.nctigba.observability.sql.service.history.collection.ebpf.BioLatencyItem;
import com.nctigba.observability.sql.util.HistogramToHeatmap;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BioLatency
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class BioLatency implements HisDiagnosisPointService<Object> {
    @Autowired
    private BioLatencyItem item;

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
        var heatMap = HistogramToHeatmap.format(file, LocaleString.format("Biolatency.unit"));
        heatMap.forEach(map -> map.setName(ReUtil.getGroup1("^b'(\\w*)'$", map.getName())));
        var suggestions = new ArrayList<>();
        heatMap.forEach(map -> {
            var source = map.getSource();
            float rate = analysisRate(source);
            if (rate > 1.0f) {
                suggestions.add(LocaleString.format("Biolatency.tip", map.getName(), rate, 5));
            }
        });
        List<TaskResult> list = new ArrayList<>();
        if (!suggestions.isEmpty()) {
            TaskResult result = new TaskResult(task, TaskResult.ResultState.SUGGESTION, ResultType.Biolatency,
                    FrameType.Suggestion,
                    Frame.bearing.top);
            result.setData(Map.of("title", LocaleString.format("Biolatency.title"), "suggestions", suggestions));
            list.add(result);
        }
        var center = new TaskResult(task, TaskResult.ResultState.NO_ADVICE, ResultType.RUNQLEN, FrameType.HeatMap,
                Frame.bearing.center);
        heatMap.forEach(map -> map.setSource(null));
        center.setData(heatMap);
        list.add(center);
        Frame f = new Frame();
        for (TaskResult taskResult : list) {
            f.addChild(taskResult.getBearing(), taskResult.toFrame());
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointData(f);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        return analysisDTO;
    }

    private float analysisRate(List<ArrayList<Integer>> source) {
        ArrayList<Integer> sum = new ArrayList<>();
        for (List<Integer> arrayList : source) {
            for (int i = 0; i < arrayList.size(); i++) {
                if (sum.size() <= i) {
                    sum.add(0);
                }
                sum.set(i, arrayList.get(i) + (sum.size() > i ? sum.get(i) : 0));
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
