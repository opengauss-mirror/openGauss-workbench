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
import com.nctigba.observability.sql.service.history.collection.ebpf.XfsDistItem;
import com.nctigba.observability.sql.util.HistogramToHeatmap;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XfsDist
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class XfsDist implements HisDiagnosisPointService<Object> {
    private static final Map<String, String> TIP = Map.of("read",
            "XfsdistAnaly.read", "write",
            "XfsdistAnaly.write", "open",
            "XfsdistAnaly.open", "fsync",
            "XfsdistAnaly.fsync");

    @Autowired
    private XfsDistItem item;

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
        var heatMap = HistogramToHeatmap.format(file, LocaleString.format("XfsdistAnaly.unit"));
        var suggestions = new ArrayList<>();
        heatMap.forEach(map -> {
            if (!TIP.containsKey(map.getName())) {
                return;
            }
            var source = map.getSource();
            float rate = analysisRate(source);
            if (rate > 1.0f) {
                suggestions.add(LocaleString.format(TIP.get(map.getName()), rate, 5));
            }
        });
        List<TaskResult> list = new ArrayList<>();
        var center = new TaskResult(task, TaskResult.ResultState.NO_ADVICE, ResultType.RUNQLEN, FrameType.HeatMap,
                Frame.bearing.center);
        heatMap.forEach(map -> map.setSource(null));
        center.setData(heatMap);
        list.add(center);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (!suggestions.isEmpty()) {
            TaskResult result = new TaskResult(task, TaskResult.ResultState.SUGGESTION, ResultType.Xfsdist,
                    FrameType.Suggestion,
                    Frame.bearing.top);
            result.setData(Map.of("title", LocaleString.format("XfsdistAnaly.title"), "suggestions", suggestions));
            list.add(result);
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        Frame f = new Frame();
        for (TaskResult taskResult : list) {
            f.addChild(taskResult.getBearing(), taskResult.toFrame());
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        analysisDTO.setPointData(f);
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
            BigDecimal improvement = new BigDecimal(r10).divide(
                    BigDecimal.valueOf(ra), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            rate = improvement.floatValue();
        }
        return rate;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}