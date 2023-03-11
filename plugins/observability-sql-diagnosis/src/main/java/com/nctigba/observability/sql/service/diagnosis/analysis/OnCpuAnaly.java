package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Resource;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.Frame.bearing;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;

@Service
public class OnCpuAnaly implements ResultAnalysis {
    @Autowired
    private DiagnosisTaskResultMapper resultMapper;
    @Autowired
    private DiagnosisResourceMapper resourceMapper;

    @Override
    public void analysis(GrabType grabType, Task task, MultipartFile file) {
        try {
            Resource resource = new Resource(task, grabType).setF(file.getInputStream().readAllBytes());
            resourceMapper.insert(resource);
            TaskResult resultSvg = new TaskResult(task,
                    task.getConf().isOnCpu() ? ResultState.Suggestions : ResultState.NoAdvice, ResultType.OnCpu,
                    FrameType.Flamefigure, bearing.top).setData(Map.of("title", LocaleString.format("OnCpuAnaly.title"), "id", resource.getId()));
            resultMapper.insert(resultSvg);
            tableData table;
            String[] keys = {"name", "samples", "ratio"};
            table = new tableData(keys);
            var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line))
                    continue;
                if (line.contains("<title>")) {
                    String functionData = line.substring(line.indexOf("<title>") + 7, line.lastIndexOf("</title>"));
                    String functionName = functionData.substring(0, functionData.lastIndexOf("(") - 1);
                    if ("all".equals(functionName)) {
                        continue;
                    }
                    String samples = functionData.substring(functionData.lastIndexOf("(") + 1, functionData.indexOf("samples,") - 1).replace(",", "");
                    String ratio = functionData.substring(functionData.indexOf("samples,") + 9, functionData.lastIndexOf(")"));
                    String[] datas = {functionName, samples, ratio};
                    var map = new HashMap<String, String>();
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        map.put(keys[i], datas[i]);
                    }
                    table.addData(map);
                }
            }
            Collections.sort(table.getData(), (o1, o2) -> {
                Integer samples1 = Integer.valueOf(o1.get("samples"));
                Integer samples2 = Integer.valueOf(o2.get("samples"));
                return samples2.compareTo(samples1);
            });
            Iterator it = table.getData().iterator();
            String comValue = table.getData().get(50).get("samples");
            while (it.hasNext()) {
                Map<String, String> map = (Map<String, String>) it.next();
                if (Integer.valueOf(map.get("samples")) <= Integer.valueOf(comValue)) {
                    it.remove();
                }
            }
            var center = new TaskResult(task, ResultState.NoAdvice, ResultType.HotFunction, FrameType.Table, bearing.center);
            center.setData(table);
            resultMapper.insert(center);
            TaskResult taskResult = new TaskResult();
            taskResult.setTaskid(task.getId());
            taskResult.setResultType(ResultType.HotFunction);
            taskResult.setFrameType(FrameType.Suggestion);
            taskResult.setState(TaskResult.ResultState.Suggestions);
            taskResult.setBearing(bearing.top);
            taskResult.setData(Map.of("title", LocaleString.format("HotFunction.title"), "suggestions", LocaleString.format("HotFunction.name")));
            resultMapper.insert(taskResult);
        } catch (IOException e) {
            throw new CustomException("onCpu err", e);
        }
    }

    @Data
    @NoArgsConstructor
    public static class tableData {
        private List<Map<String, String>> columns = new ArrayList<>();
        private List<Map<String, String>> data = new ArrayList<>();

        public tableData(String[] keys) {
            for (String key : keys) {
                columns.add(Map.of("key", key, "title", key));
            }
        }

        public void addData(Map<String, String> map) {
            data.add(map);
        }
    }
}