package com.nctigba.observability.sql.service.diagnosis.param;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.mapper.NctigbaParamInfoMapper;
import com.nctigba.observability.sql.model.param.NctigbaParamInfo;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

@Service
public class OsParamAnalysis implements ResultAnalysis {
    @Autowired
    private DiagnosisTaskResultMapper resultMapper;

    @Autowired
    private NctigbaParamInfoMapper mapper;

    @Override
    public void analysis(GrabType grabType, Task task, MultipartFile file) {
        try {
            var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line))
                    continue;
                String name=line.substring(0,line.indexOf("=")).trim();
                OsParamData[] fields=OsParamData.values();
                String nodeName = null;
                for(int j=0;j<fields.length;j++){
                    if(fields[j].getParamName().equals(name)){
                        nodeName=fields[j].toString();
                    }
                }
                if(nodeName==null)
                    continue;
                String paramData=line.substring(line.indexOf("=")+1).replace("\t","").trim();
                NctigbaParamInfo nctigbaParamInfo=mapper.selectOne(Wrappers.<NctigbaParamInfo>lambdaQuery().eq(NctigbaParamInfo::getParamName,name));
                //Pattern pattern=Pattern.compile("[0-9]*");
                ParamDto paramDto=new ParamDto();
                boolean isEmpty="".equals(paramData) && paramData!=null && nctigbaParamInfo.getSuggestValue()!=null && !"".equals(nctigbaParamInfo.getSuggestValue());
                //boolean isMatch=pattern.matcher(paramData).matches() && pattern.matcher(nctigbaParamInfo.getSuggestValue()).matches();
                if(!isEmpty && "compare".equals(nctigbaParamInfo.getDiagnosis())){
                    BigInteger a = new BigInteger(paramData);
                    BigInteger b = new BigInteger(nctigbaParamInfo.getSuggestValue());
                    if(a.compareTo(b)>0){
                        paramDto.setTitle(LocaleString.format("Param.turnDown")+LocaleString.format(nodeName+".title")+LocaleString.format("Param.define"));
                    }else if(a.compareTo(b)<0){
                        paramDto.setTitle(LocaleString.format("Param.turnUp")+LocaleString.format(nodeName+".title")+LocaleString.format("Param.define"));
                    }else{
                        paramDto.setTitle(LocaleString.format(nodeName+".title"));
                    }
                }else if(!isEmpty && "equal".equals(nctigbaParamInfo.getDiagnosis())){
                    if(nctigbaParamInfo.getDiagnosis().equals(paramData)){
                        paramDto.setTitle(LocaleString.format(nodeName+".title"));
                    }else{
                        paramDto.setTitle(LocaleString.format("Param.revise")+LocaleString.format(nodeName+".title")+LocaleString.format("Param.define"));
                    }
                }else if(!isEmpty && "range".equals(nctigbaParamInfo.getDiagnosis())){
                    String startRang=nctigbaParamInfo.getDiagnosis().substring(0,nctigbaParamInfo.getDiagnosis().indexOf("-"));
                    String endRang=nctigbaParamInfo.getDiagnosis().substring(nctigbaParamInfo.getDiagnosis().indexOf("-")+1);
                    if(Integer.valueOf(startRang)<=Integer.valueOf(paramData) && Integer.valueOf(paramData)<=Integer.valueOf(endRang)){
                        paramDto.setTitle(LocaleString.format(nodeName+".title"));
                    }else{
                        paramDto.setTitle(LocaleString.format("Param.revise")+LocaleString.format(nodeName+".title")+LocaleString.format("Param.define"));
                    }
                }else{
                    paramDto.setTitle(LocaleString.format(nodeName+".title"));
                }
                paramDto.setParamName(nctigbaParamInfo.getParamName());
                paramDto.setCurrentValue(paramData);
                paramDto.setUnit(nctigbaParamInfo.getUnit());
                paramDto.setParamDescription(nctigbaParamInfo.getParamDetail());
                paramDto.setSuggestValue(nctigbaParamInfo.getSuggestValue());
                paramDto.setSuggestReason(nctigbaParamInfo.getSuggestExplain());
                TaskResult taskResult = new TaskResult();
                taskResult.setTaskid(task.getId());
                taskResult.setResultType(ResultType.valueOf(nodeName));
                taskResult.setFrameType(FrameType.Param);
                taskResult.setState(TaskResult.ResultState.Suggestions);
                taskResult.setData(paramDto);
                resultMapper.insert(taskResult);

            }
        } catch (IOException e) {
            throw new CustomException("osParam err", e);
        }
    }
}
