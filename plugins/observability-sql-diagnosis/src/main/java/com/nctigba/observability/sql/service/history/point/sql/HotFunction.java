/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.nctigba.observability.sql.constants.CommonConstants;
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
import com.nctigba.observability.sql.service.history.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HotFunction
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class HotFunction implements HisDiagnosisPointService<Object> {
    @Autowired
    private ProfileItem item;

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
        TableData table = getTableData(file);
        List<TaskResult> list = getResultData(task, table);
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

    private TableData getTableData(MultipartFile file) {
        String[] keys = {"name", CommonConstants.SAMPLES, "ratio"};
        TableData table = new TableData(keys);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if (line.contains("<title>")) {
                    String functionData = line.substring(line.indexOf("<title>") + 7, line.lastIndexOf("</title>"));
                    String functionName = functionData.substring(
                            0, functionData.lastIndexOf(CommonConstants.LEFT_BRACKET) - 1);
                    if ("all".equals(functionName)) {
                        continue;
                    }
                    String samples = functionData.substring(
                            functionData.lastIndexOf(CommonConstants.LEFT_BRACKET) + 1,
                            functionData.indexOf("samples,") - 1).replace(",", "");
                    String ratio = functionData.substring(
                            functionData.indexOf("samples,") + 9,
                            functionData.lastIndexOf(CommonConstants.RIGHT_BRACKET));
                    String[] datas = {functionName, samples, ratio};
                    var map = new HashMap<String, String>();
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        map.put(keys[i], datas[i]);
                    }
                    table.addData(map);
                }
            }
            return table;
        } catch (IOException e) {
            throw new CustomException("onCpu err", e);
        }
    }

    private List<TaskResult> getResultData(HisDiagnosisTask task, TableData table) {
        List<TaskResult> list = new ArrayList<>();
        if (table.getData().size() > 1) {
            table.getData().sort((o1, o2) -> {
                Integer samples1 = Integer.valueOf(o1.get(CommonConstants.SAMPLES));
                Integer samples2 = Integer.valueOf(o2.get(CommonConstants.SAMPLES));
                return samples2.compareTo(samples1);
            });
            var it = table.getData().iterator();
            int count = 1;
            while (it.hasNext()) {
                it.next();
                if (count > 50) {
                    it.remove();
                }
                count++;
            }
            var center = new TaskResult(
                    task, TaskResult.ResultState.NO_ADVICE, ResultType.HotFunction, FrameType.Table,
                    Frame.bearing.center);
            center.setData(table);
            TaskResult taskResult = new TaskResult();
            taskResult.setTaskId(task.getId());
            taskResult.setResultType(ResultType.HotFunction);
            taskResult.setFrameType(FrameType.Suggestion);
            taskResult.setState(TaskResult.ResultState.SUGGESTION);
            taskResult.setBearing(Frame.bearing.top);
            taskResult.setData(Map.of(CommonConstants.TITLE, LocaleString.format("HotFunction.title"), "suggestions",
                    LocaleString.format("HotFunction.name")));
            list.add(center);
        }
        return list;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }

    /**
     * TableData
     *
     * @author xx
     * @since 2023/6/9
     */
    @Data
    @NoArgsConstructor
    public static class TableData {
        private List<Map<String, String>> columns = new ArrayList<>();
        private List<Map<String, String>> data = new ArrayList<>();

        /**
         * Table data
         *
         * @param keys info
         */
        public TableData(String[] keys) {
            for (String key : keys) {
                columns.add(Map.of("key", key, CommonConstants.TITLE, key));
            }
        }

        /**
         * Add data
         *
         * @param map info
         */
        public void addData(Map<String, String> map) {
            data.add(map);
        }
    }
}