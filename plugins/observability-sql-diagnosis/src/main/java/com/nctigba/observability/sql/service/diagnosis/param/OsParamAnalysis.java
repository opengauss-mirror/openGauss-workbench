package com.nctigba.observability.sql.service.diagnosis.param;

import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.param.OsParamData;
import com.nctigba.observability.sql.model.param.ParamDto;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.sqlite.JDBC;

import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
@Slf4j
@Service
public class OsParamAnalysis implements ResultAnalysis {
    @Autowired
    private DiagnosisTaskResultMapper resultMapper;

    @Override
    public void analysis(GrabType grabType, Task task, MultipartFile file) {
        task.addRemarks("start get osParam info:");



        Connection connect = connect_sqlite();
        try( Statement statement = connect.createStatement();
             BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {


            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line))
                    continue;
                String name = line.substring(0, line.indexOf(CommonConstants.EQUAL)).trim();
                OsParamData[] fields = OsParamData.values();
                String nodeName = null;
                for (int j = 0; j < fields.length; j++) {
                    if (fields[j].getParamName().equals(name)) {
                        nodeName = fields[j].toString();
                    }
                }
                if (nodeName == null)
                    continue;
                String paramData = line.substring(line.indexOf(CommonConstants.EQUAL) + 1).replace("\t", "");
                String selectSql = "select * from param_info where paramName='" + name + "';";
                try(ResultSet result = statement.executeQuery(selectSql);){
                    if (result.next()) {
                        ParamDto paramDto = new ParamDto();
                        paramDto.setParamName(result.getString("paramName"));
                        paramDto.setCurrentValue(paramData);
                        paramDto.setUnit(result.getString("unit"));
                        paramDto.setParamDescription(result.getString("paramDetail"));
                        paramDto.setSuggestValue(result.getString("suggestValue"));
                        paramDto.setSuggestReason(result.getString("suggestExplain"));
                        TaskResult taskResult = new TaskResult();
                        if (result.getString(CommonConstants.DIAGNOSIS_RULE) != null || !"".equals(result.getString(CommonConstants.DIAGNOSIS_RULE))) {
                            var manager = new ScriptEngineManager();
                            var t = manager.getEngineByName("javascript");
                            var bindings = t.createBindings();
                            bindings.put("actualValue", paramData.trim());
                            Object object = t.eval(result.getString(CommonConstants.DIAGNOSIS_RULE), bindings);
                            if (object!=null && "true".equals(object.toString())) {
                                taskResult.setState(TaskResult.ResultState.NoAdvice);
                            } else {
                                paramDto.setTitle(LocaleString.format("Param.revise") + LocaleString.format(nodeName + ".title") + LocaleString.format("Param.define"));
                                taskResult.setState(TaskResult.ResultState.Suggestions);
                            }
                        } else {
                            taskResult.setState(TaskResult.ResultState.NoAdvice);
                        }
                        taskResult.setTaskid(task.getId());
                        taskResult.setResultType(ResultType.valueOf(nodeName));
                        taskResult.setFrameType(FrameType.Param);
                        taskResult.setData(paramDto);
                        resultMapper.insert(taskResult);
                    }
                }catch (Exception e) {
                    throw new CustomException("osParam err", e);
                }
            }

        } catch (Exception e) {
            throw new CustomException("osParam err", e);
        }

    }

    public static synchronized Connection connect_sqlite() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(JDBC.PREFIX + "data/paramDiagnosisInfo.db");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
