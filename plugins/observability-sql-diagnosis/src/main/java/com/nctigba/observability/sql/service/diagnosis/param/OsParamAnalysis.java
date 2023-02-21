package com.nctigba.observability.sql.service.diagnosis.param;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.param.OsParamData;
import com.nctigba.observability.sql.model.param.ParamDto;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class OsParamAnalysis implements ResultAnalysis {
    @Autowired
    private DiagnosisTaskResultMapper resultMapper;

    @Override
    public void analysis(GrabType grabType, Task task, MultipartFile file) {
        try {
            String paramData;
            var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line))
                    continue;
                paramData=line.substring(line.indexOf("=")+1).trim();
                OsParamData[] fields= OsParamData.values();
                for(int j=0;j<fields.length;j++) {
                    if (line.contains(fields[j].getParamName())) {
                        Pattern pattern=Pattern.compile("[0-9]*");
                        ParamDto paramDto=new ParamDto();
                        boolean isEmpty=!"".equals(paramData) && paramData!=null && fields[j].getSuggestValue()!=null && !"".equals(fields[j].getSuggestValue());
                        boolean isMatch=pattern.matcher(paramData).matches() && pattern.matcher(fields[j].getSuggestValue()).matches();
                        if(isEmpty && isMatch ){
                            BigInteger a = new BigInteger(paramData);
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
                        paramDto.setCurrentValue(paramData);
                        paramDto.setUnit(fields[j].getUnit());
                        paramDto.setParamDescription(fields[j].getParamDetail());
                        paramDto.setSuggestValue(fields[j].getSuggestValue());
                        paramDto.setSuggestReason(fields[j].getSuggestExplain());
                        TaskResult taskResult = new TaskResult();
                        taskResult.setTaskid(task.getId());
                        taskResult.setResultType(ResultType.valueOf(fields[j].toString()));
                        taskResult.setFrameType(FrameType.Param);
                        taskResult.setState(TaskResult.ResultState.Suggestions);
                        taskResult.setData(paramDto);
                        resultMapper.insert(taskResult);
                    }
                }
            }
        } catch (IOException e) {
            throw new CustomException("osParam err", e);
        }
    }
}
