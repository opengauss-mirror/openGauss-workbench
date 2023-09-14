/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.result.TaskResult;
import com.nctigba.observability.sql.model.param.OsParamData;
import com.nctigba.observability.sql.model.param.ParamDto;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.OsParamItem;
import com.nctigba.observability.sql.util.LocaleString;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.sqlite.JDBC;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * OsParam
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class OsParam implements HisDiagnosisPointService<Object> {
    @Autowired
    private OsParamItem item;
    @Autowired
    private HisDiagnosisResultMapper resultMapper;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionCommon.IS_BCC));
        option.add(String.valueOf(OptionCommon.IS_PARAM));
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
        HashMap<String, String> paramMap = fileToMap(file);
        String selectSql = "select * from param_info where paramType='OS';";
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(selectSql)) {
            while (result.next()) {
                String paramName = result.getString("paramName");
                OsParamData[] fields = OsParamData.values();
                String nodeName = null;
                for (OsParamData field : fields) {
                    if (field.getParamName().equals(paramName)) {
                        nodeName = field.toString();
                    }
                }
                HisDiagnosisResult resultData;
                AnalysisDTO analysisDTO = new AnalysisDTO();
                analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
                if (paramMap.containsKey(paramName)) {
                    String paramActualData = paramMap.get(paramName);
                    String paramData = paramActualData.replace("\t", "");
                    ParamDto paramDto = new ParamDto();
                    paramDto.setParamName(result.getString("paramName"));
                    paramDto.setCurrentValue(paramActualData);
                    paramDto.setUnit(result.getString("unit"));
                    paramDto.setParamDescription(result.getString("paramDetail"));
                    paramDto.setSuggestValue(result.getString("suggestValue"));
                    paramDto.setSuggestReason(result.getString("suggestExplain"));
                    TaskResult taskResult = new TaskResult();
                    if (result.getString(CommonConstants.DIAGNOSIS_RULE) != null || !"".equals(
                            result.getString(CommonConstants.DIAGNOSIS_RULE))) {
                        var manager = new ScriptEngineManager();
                        var t = manager.getEngineByName("javascript");
                        var bindings = t.createBindings();
                        bindings.put("actualValue", paramData.trim());
                        Object object = t.eval(result.getString(CommonConstants.DIAGNOSIS_RULE), bindings);
                        if (object != null && "true".equals(object.toString())) {
                            taskResult.setState(TaskResult.ResultState.NO_ADVICE);
                            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                        } else {
                            paramDto.setTitle(
                                    LocaleString.format("Param.revise") + LocaleString.format(nodeName + ".title")
                                            + LocaleString.format("Param.define"));
                            taskResult.setState(TaskResult.ResultState.SUGGESTION);
                            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
                        }
                    } else {
                        taskResult.setState(TaskResult.ResultState.NO_ADVICE);
                        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                    }
                    taskResult.setTaskId(task.getId());
                    taskResult.setResultType(ResultType.valueOf(nodeName));
                    taskResult.setFrameType(FrameType.Param);
                    taskResult.setData(paramDto);
                    analysisDTO.setPointData(taskResult);
                    resultData = new HisDiagnosisResult(
                            task, analysisDTO, nodeName, HisDiagnosisResult.PointState.SUCCEED);
                } else {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                    resultData = new HisDiagnosisResult(
                            task, analysisDTO, nodeName, HisDiagnosisResult.PointState.NOT_SATISFIED_DIAGNOSIS);
                }
                resultMapper.update(resultData, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                HisDiagnosisResult::getTaskId, resultData.getTaskId())
                        .eq(HisDiagnosisResult::getPointName, resultData.getPointName()));
            }
        } catch (SQLException | ScriptException e) {
            throw new CustomException("sql error:", e);
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        HisDiagnosisResult resultData = new HisDiagnosisResult(
                task, analysisDTO, "OsParam", HisDiagnosisResult.PointState.SUCCEED);
        resultMapper.update(resultData, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                        HisDiagnosisResult::getTaskId, resultData.getTaskId())
                .eq(HisDiagnosisResult::getPointName, resultData.getPointName()));
        analysisDTO.setPointData("osParam");
        return analysisDTO;
    }

    private HashMap<String, String> fileToMap(MultipartFile file) {
        HashMap<String, String> paramMap = new HashMap<>();
        Charset cs = StandardCharsets.UTF_8;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), cs))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                String name = line.substring(0, line.indexOf(CommonConstants.EQUAL)).trim();
                String paramActualData = line.substring(line.indexOf(CommonConstants.EQUAL) + 1);
                paramMap.put(name, paramActualData);
            }
            return paramMap;
        } catch (IOException e) {
            throw new CustomException("file parse error:", e);
        }
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }

    private static synchronized Connection connectSqlite() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(JDBC.PREFIX + "data/paramDiagnosisInfo.db");
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return conn;
    }
}