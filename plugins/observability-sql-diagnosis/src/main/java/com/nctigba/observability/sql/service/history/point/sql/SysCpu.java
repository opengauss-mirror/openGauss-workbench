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
import com.nctigba.observability.sql.service.history.collection.ebpf.PidStatItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PercentUtil;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SysCpu
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class SysCpu implements HisDiagnosisPointService<Object> {
    @Autowired
    private PidStatItem item;

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
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (file != null) {
            TaskResult sysResult = new TaskResult(
                    task, TaskResult.ResultState.NO_ADVICE, ResultType.system, FrameType.LineChart,
                    Frame.bearing.center);
            sysResult.setData(generateLineChart(file));
            List<TaskResult> list = new ArrayList<>();
            list.add(sysResult);
            Frame f = new Frame();
            for (TaskResult taskResult : list) {
                f.addChild(taskResult.getBearing(), taskResult.toFrame());
            }
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            analysisDTO.setPointData(f);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    private LineChart generateLineChart(MultipartFile file) {
        LineChart system = new LineChart().setTitle(LocaleString.format("Pidstat1.system"));
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("Average")) {
                    continue;
                }
                var data = line.substring(11).trim().split("\\s+");
                if (data.length < 9 || data[8].equals("Command")) {
                    continue;
                }
                var time = line.substring(0, 11);
                system.addX(time);
                system.addPoint(data[8], PercentUtil.parse(data[3]));
            }
        } catch (IOException e) {
            throw new CustomException("Pidstat1", e);
        }
        system.parse();
        return system;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}