package com.nctigba.observability.sql.service.diagnosis.caller;

import java.math.BigInteger;
import java.util.regex.Pattern;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
        task.addRemarks("start get param info:");
        diagnosisTaskMapper.updateById(task);
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());) {
            String sql="select name,setting from pg_settings";
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
            String value;
            while (rs.next()) {
                DatabaseParamData[] fields= DatabaseParamData.values();
                for(int j=0;j<fields.length;j++) {
                    if (rs.getString(1).equals(fields[j].getParamName())) {
                        value = rs.getString(2);
                        Pattern pattern=Pattern.compile("[0-9]*");
                        ParamDto paramDto=new ParamDto();
                        boolean isEmpty=!"".equals(value) && value!=null && fields[j].getSuggestValue()!=null && !"".equals(fields[j].getSuggestValue());
                        boolean isMatch=pattern.matcher(value).matches() && pattern.matcher(fields[j].getSuggestValue()).matches();
                        if(isEmpty && isMatch ){
                            BigInteger a = new BigInteger(value);
                            BigInteger b = new BigInteger(fields[j].getSuggestValue());
                            if(a.compareTo(b)>0){
                                paramDto.setTitle(LocaleString.format("Param.turnDown")+LocaleString.format(fields[j]+".title")+LocaleString.format("Param.define"));
                            }else if(a.compareTo(b)<0){
                                paramDto.setTitle(LocaleString.format("Param.turnUp")+LocaleString.format(fields[j]+".title")+LocaleString.format("Param.define"));
                            }else{
                                paramDto.setTitle(LocaleString.format(fields[j]+".title"));
                            }
                        }else{
                            paramDto.setTitle(LocaleString.format(fields[j]+".title"));
                        }
                        paramDto.setParamName(fields[j].getParamName());
                        paramDto.setCurrentValue(value);
                        paramDto.setUnit(fields[j].getUnit());
                        paramDto.setParamDescription(fields[j].getParamDetail());
                        paramDto.setSuggestValue(fields[j].getSuggestValue());
                        paramDto.setSuggestReason(fields[j].getSuggestExplain());
                        setTaskResultSuggestions(task.getId(), ResultType.valueOf(fields[j].toString()), paramDto);
                    }
                }
            }
            stmt.close();
        }catch (Exception e) {
            log.info(e.getMessage());
            task.addRemarks("get param info failed");
        } finally {
            diagnosisTaskMapper.updateById(task);
        }
    }

    private void setTaskResultSuggestions(Integer taskId, ResultType resultType, ParamDto paramDto) {
        TaskResult taskResult = new TaskResult();
        taskResult.setTaskid(taskId);
        taskResult.setResultType(resultType);
        taskResult.setState(TaskResult.ResultState.Suggestions);
        taskResult.setFrameType(FrameType.Param);
        taskResult.setData(paramDto);
        taskResultMapper.insert(taskResult);
    }

}
