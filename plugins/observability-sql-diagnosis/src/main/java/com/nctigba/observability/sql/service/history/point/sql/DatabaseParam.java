/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.result.TaskResult;
import com.nctigba.observability.sql.model.param.DatabaseParamData;
import com.nctigba.observability.sql.model.param.ParamDto;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.DatabaseItem;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sqlite.JDBC;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseParam
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseParam implements HisDiagnosisPointService<Object> {
    @Autowired
    private DatabaseItem item;
    private final ClusterManager clusterManager;
    @Autowired
    private HisDiagnosisResultMapper resultMapper;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
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
        ResultSet result = null;
        ResultSet rs = null;
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            String sql = SqlCommon.DATABASE_PARAM;
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString(1);
                DatabaseParamData[] fields = DatabaseParamData.values();
                String nodeName = null;
                for (DatabaseParamData field : fields) {
                    if (field.getParamName().equals(name)) {
                        nodeName = field.toString();
                    }
                }
                if (nodeName == null) {
                    continue;
                }
                String selectSql = "select * from param_info where paramName='" + name + "';";
                result = statement.executeQuery(selectSql);
                String value = rs.getString(2);
                if (result.next()) {
                    ParamDto paramDto = new ParamDto();
                    paramDto.setParamName(result.getString("paramName"));
                    paramDto.setCurrentValue(value);
                    paramDto.setUnit(result.getString("unit"));
                    paramDto.setParamDescription(result.getString("paramDetail"));
                    paramDto.setSuggestValue(result.getString("suggestValue"));
                    paramDto.setSuggestReason(result.getString("suggestExplain"));
                    TaskResult taskResult = new TaskResult();
                    taskResult.setTaskId(task.getId());
                    taskResult.setResultType(ResultType.valueOf(nodeName));
                    taskResult.setFrameType(FrameType.Param);
                    AnalysisDTO analysisDTO = new AnalysisDTO();
                    if (result.getString(CommonConstants.DIAGNOSIS_RULE) != null || !"".equals(
                            result.getString(CommonConstants.DIAGNOSIS_RULE))) {
                        var manager = new ScriptEngineManager();
                        var t = manager.getEngineByName("javascript");
                        var bindings = t.createBindings();
                        bindings.put("actualValue", value);
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
                    taskResult.setData(paramDto);
                    analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
                    analysisDTO.setPointData(taskResult);
                    DatabaseParamData[] dbFields = DatabaseParamData.values();
                    String pointName = null;
                    for (DatabaseParamData dbField : dbFields) {
                        if (dbField.getParamName().equals(result.getString("paramName"))) {
                            pointName = dbField.toString();
                        }
                    }
                    HisDiagnosisResult resultData = new HisDiagnosisResult(
                            task, analysisDTO, pointName, HisDiagnosisResult.PointState.SUCCEED);
                    resultMapper.update(resultData, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                    HisDiagnosisResult::getTaskId, resultData.getTaskId())
                            .eq(HisDiagnosisResult::getPointName, resultData.getPointName()));
                }
            }
        } catch (SQLException | ScriptException e) {
            task.addRemarks("get param info failed", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error(CommonConstants.DATA_FAIL, e.getMessage());
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.error(CommonConstants.DATA_FAIL, e.getMessage());
                }
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        HisDiagnosisResult resultData = new HisDiagnosisResult(
                task, analysisDTO, "DatabaseParam", HisDiagnosisResult.PointState.SUCCEED);
        resultMapper.update(resultData, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                        HisDiagnosisResult::getTaskId, resultData.getTaskId())
                .eq(HisDiagnosisResult::getPointName, resultData.getPointName()));
        return null;
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
