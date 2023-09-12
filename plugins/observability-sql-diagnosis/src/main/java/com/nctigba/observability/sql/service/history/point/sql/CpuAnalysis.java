/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.chart.LineChart;
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
import com.nctigba.observability.sql.service.history.collection.ebpf.MpStatPItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PercentUtil;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CpuAnalysis
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class CpuAnalysis implements HisDiagnosisPointService<Object> {
    private static final String[] KEYS = {"CPU", "%usr", "%nice", "%sys", "%iowait", "%irq", "%soft", "%steal",
            "%guest", "%gnice", "%idle"};

    private static final int[] INDEX = {1, 3, 4, 5, 6, 10};

    @Autowired
    private MpStatPItem item;

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
        try {
            LineChart all = new LineChart().setTitle(LocaleString.format("MpstatP.title"));
            LineChart idle = new LineChart().setTitle(LocaleString.format("MpstatP.idle"));
            try (var reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    var line = reader.readLine();
                    if (line.isBlank()) {
                        continue;
                    }
                    if (line.startsWith("Average")) {
                        break;
                    }
                    var data = line.substring(11).trim().split("\\s+");
                    if (data[0].equals("CPU")) {
                        continue;
                    }
                    var time = line.substring(0, 11);
                    if (data[0].equals("all")) {
                        all.addX(time);
                        for (int i : INDEX) {
                            all.addPoint(KEYS[i], PercentUtil.parse(data[i]));
                        }
                        idle.addX(time);
                        idle.addPoint(KEYS[10], PercentUtil.parse(data[10]));
                    }
                }
            }
            all.parse();
            idle.parse();
            TaskResult result = new TaskResult(task, TaskResult.ResultState.NO_ADVICE, ResultType.CPU,
                    FrameType.LineChart,
                    Frame.bearing.center);
            result.setData(all);
            List<TaskResult> list = new ArrayList<>();
            list.add(result);
            return list;
        } catch (IOException e) {
            throw new CustomException("MpstatP", e);
        }
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
