package com.nctigba.observability.sql.service.diagnosis.caller;

import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.param.DatabaseParamData;
import com.nctigba.observability.sql.model.param.ParamDto;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.sqlite.JDBC;

import javax.script.ScriptEngineManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseParamCaller implements Caller {

    private final DiagnosisTaskResultMapper taskResultMapper;
    private final DiagnosisTaskMapper diagnosisTaskMapper;
    private final ClusterManager clusterManager;


    @Override
    @Async
    public void start(Task task) {
        log.info("paramAnalyze caller start begin");
        if (!task.getConf().isParamAnalysis())
            return;
        task.addRemarks("start get param info:");
        diagnosisTaskMapper.updateById(task);
        Statement statement=null;
        Statement stmt=null;
        ResultSet rs=null;
        ResultSet result=null;
        try {
            Connection connect = connect_sqlite();
             statement = connect.createStatement();
            var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
            String sql = "select name,setting from pg_settings";
             stmt = conn.createStatement();
             rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString(1);
                String value = rs.getString(2);
                DatabaseParamData[] fields = DatabaseParamData.values();
                String nodeName = null;
                for (int j = 0; j < fields.length; j++) {
                    if (fields[j].getParamName().equals(name)) {
                        nodeName = fields[j].toString();
                    }
                }
                if (nodeName == null)
                    continue;
                String selectSql = "select * from param_info where paramName='" + name + "';";
                 result = statement.executeQuery(selectSql);
                if (result.next()) {
                    ParamDto paramDto = new ParamDto();
                    paramDto.setParamName(result.getString("paramName"));
                    paramDto.setCurrentValue(value);
                    paramDto.setUnit(result.getString("unit"));
                    paramDto.setParamDescription(result.getString("paramDetail"));
                    paramDto.setSuggestValue(result.getString("suggestValue"));
                    paramDto.setSuggestReason(result.getString("suggestExplain"));
                    TaskResult taskResult = new TaskResult();
                    taskResult.setTaskid(task.getId());
                    taskResult.setResultType(ResultType.valueOf(nodeName));
                    taskResult.setFrameType(FrameType.Param);
                    if (result.getString(CommonConstants.DIAGNOSIS_RULE) != null || !"".equals(result.getString(CommonConstants.DIAGNOSIS_RULE))) {
                        var manager = new ScriptEngineManager();
                        var t = manager.getEngineByName("javascript");
                        var bindings = t.createBindings();
                        bindings.put("actualValue", value);
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
                    taskResult.setData(paramDto);
                    taskResultMapper.insert(taskResult);
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            task.addRemarks("get param info failed", e);
        } finally {
            diagnosisTaskMapper.updateById(task);

            try{
                if(stmt!=null){
                    stmt.close();
                }
            }catch (Exception e){
                log.error(CommonConstants.DATA_FAIL ,e.getMessage());
            }
            try{
                if(statement!=null){
                    statement.close();
                }
            }catch (Exception e){
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try{
                if(rs!=null){
                    rs.close();
                }
            }catch (Exception e){
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }
            try{
                if(result!=null){
                    result.close();
                }
            }catch (Exception e){
                log.error(CommonConstants.DATA_FAIL, e.getMessage());
            }

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
