package com.nctigba.observability.instance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.dto.param.DatabaseParamDTO;
import com.nctigba.observability.instance.dto.param.OsParamDTO;
import com.nctigba.observability.instance.entity.NctigbaParamInfo;
import com.nctigba.observability.instance.mapper.NctigbaParamInfoMapper;
import com.nctigba.observability.instance.model.param.DatabaseParamData;
import com.nctigba.observability.instance.model.param.OsParamData;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.pool.SSHPoolManager;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ParamInfoService;
import com.nctigba.observability.instance.util.SSHOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParamInfoServiceImpl implements ParamInfoService {

    private final ClusterManager opsFacade;

    @Autowired
    private NctigbaParamInfoMapper mapper;

    @Override
    public List<NctigbaParamInfo> getParamInfo(ParamQuery paramQuery) {
        this.updateOsParamInfo(paramQuery);
        this.updateDatabaseParamInfo(paramQuery);
        return mapper.selectList(Wrappers.emptyWrapper());
    }

    private void updateDatabaseParamInfo(ParamQuery paramQuery){
        List<NctigbaParamInfo> paramInfoList=mapper.selectList(Wrappers.emptyWrapper());
        StringBuffer sb= new StringBuffer();
        for(NctigbaParamInfo paramInfo:paramInfoList){
            sb.append(paramInfo.getParamName());
        }
        try(var conn = opsFacade.getConnectionByNodeId(paramQuery.getNodeId());) {
            String sql="select name,setting from pg_settings";
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString(1);
                if(sb!=null && sb.toString().contains(name)){
                    String value = rs.getString(2);
                    NctigbaParamInfo nctigbaParamInfo=new NctigbaParamInfo();
                    nctigbaParamInfo.setActualValue(value);
                    mapper.update(nctigbaParamInfo,Wrappers.<NctigbaParamInfo>lambdaUpdate().eq(NctigbaParamInfo::getParamName,name));
                }
            }
            stmt.close();
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    private void updateOsParamInfo(ParamQuery paramQuery){
        List<NctigbaParamInfo> paramInfoList=mapper.selectList(Wrappers.emptyWrapper());
        StringBuffer sb = new StringBuffer();
        for(NctigbaParamInfo paramInfo:paramInfoList){
            sb.append(paramInfo.getParamName());
        }
        var node = opsFacade.getOpsNodeById(paramQuery.getNodeId());
        SSHOperator ssh = SSHPoolManager.getSSHOperator(node.getPublicIp(), node.getHostPort(), "root",
                paramQuery.getPassword());
        String paramValues=ssh.executeCommandReturnStr("sysctl -a");
        String[] values=paramValues.split("\n");
        for(int n=0;n<values.length;n++){
            String name = values[n].substring(0,values[n].lastIndexOf("=")).trim();
            if(sb!=null && sb.toString().contains(name)){
                String paramData=values[n].substring(values[n].indexOf("=")+1).trim();
                NctigbaParamInfo nctigbaParamInfo=new NctigbaParamInfo();
                nctigbaParamInfo.setActualValue(paramData);
                mapper.update(nctigbaParamInfo,Wrappers.<NctigbaParamInfo>lambdaUpdate().eq(NctigbaParamInfo::getParamName,name));
            }
        }
    }

    /*@Override
    public List<DatabaseParamDTO> getDatabaseParamInfo(ParamQuery paramQuery) {
        try(var conn = opsFacade.getConnectionByNodeId(paramQuery.getNodeId());) {
            String sql="select name,setting from pg_settings";
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
            String value = null;
            List<DatabaseParamDTO> list=new ArrayList<>();
            while (rs.next()) {
                DatabaseParamData[] fields= DatabaseParamData.values();
                for(int j=0;j<fields.length;j++) {
                    if (rs.getString(1).equals(fields[j].getParamName())) {
                        value = rs.getString(2);
                        log.info(fields[j].toString()+value);
                        DatabaseParamDTO databaseParamDTO=new DatabaseParamDTO();
                        databaseParamDTO.setSeqNo(String.valueOf(j+1));
                        databaseParamDTO.setClassify(fields[j].getClassify());
                        databaseParamDTO.setParamName(fields[j].getParamName());
                        databaseParamDTO.setParamDetail(fields[j].getParamDetail());
                        databaseParamDTO.setActualValue(value);
                        databaseParamDTO.setSuggestValue(fields[j].getSuggestValue());
                        databaseParamDTO.setDefaultValue(fields[j].getDefaultValue());
                        databaseParamDTO.setUnit(fields[j].getUnit());
                        databaseParamDTO.setSuggestExplain(fields[j].getSuggestExplain());
                        list.add(databaseParamDTO);
                    }
                }
            }
            stmt.close();
            return list;
        }catch (Exception e){
            log.info(e.getMessage());
            return null;
        }
    }


    @Override
    public List<OsParamDTO> getOsParamInfo(ParamQuery paramQuery) {
        var node = opsFacade.getOpsNodeById(paramQuery.getNodeId());
        List<OsParamDTO> list=new ArrayList<>();
        SSHOperator ssh = SSHPoolManager.getSSHOperator(node.getPublicIp(), node.getHostPort(), "root",
                paramQuery.getPassword());
        //String paramValue=ssh.executeCommandReturnStr("sysctl -a | grep "+osParamType[i]+" | awk -F=  '{print $2}'");
        String paramValues=ssh.executeCommandReturnStr("sysctl -a");
        String[] values=paramValues.split("\n");
        String paramData=null;
        for(int n=0;n<values.length;n++){
            paramData=values[n].substring(values[n].indexOf("=")+1).trim();
            OsParamData[] fields= OsParamData.values();
            for(int j=0;j<fields.length;j++){
                if(values[n].substring(0,values[n].lastIndexOf("=")).trim().equals(fields[j].getParamName()))
                {
                    OsParamDTO osParamDTO=new OsParamDTO();
                    osParamDTO.setSeqNo(String.valueOf(j+1));
                    osParamDTO.setClassify(fields[j].getClassify());
                    osParamDTO.setParamName(fields[j].getParamName());
                    osParamDTO.setParamDetail(fields[j].getParamDetail());
                    osParamDTO.setActualValue(paramData);
                    osParamDTO.setSuggestValue(fields[j].getSuggestValue());
                    osParamDTO.setDefaultValue(fields[j].getDefaultValue());
                    osParamDTO.setUnit(fields[j].getUnit());
                    osParamDTO.setSuggestExplain(fields[j].getSuggestExplain());
                    list.add(osParamDTO);
                }
            }
        }
        return list;
    }*/


}
