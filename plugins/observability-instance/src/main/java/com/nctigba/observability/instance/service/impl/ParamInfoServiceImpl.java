package com.nctigba.observability.instance.service.impl;

import com.nctigba.observability.instance.dto.param.DatabaseParamDTO;
import com.nctigba.observability.instance.dto.param.OsParamDTO;
import com.nctigba.observability.instance.model.param.DatabaseParamData;
import com.nctigba.observability.instance.model.param.OsParamData;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.pool.SSHPoolManager;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ParamInfoService;
import com.nctigba.observability.instance.util.SSHOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParamInfoServiceImpl implements ParamInfoService {

    private final ClusterManager opsFacade;
    private final String[] osParamType={"net.ipv4.tcp_max_tw_buckets","net.ipv4.tcp_tw_reuse","net.ipv4.tcp_tw_recycle",
    "net.ipv4.tcp_keepalive_time","net.ipv4.tcp_keepalive_probes","net.ipv4.tcp_keepalive_intvl","net.ipv4.tcp_retries1",
    "net.ipv4.tcp_syn_retries","net.ipv4.tcp_synack_retries","net.ipv4.tcp_retries2","vm.overcommit_memory","net.ipv4.tcp_rmem",
    "net.ipv4.tcp_wmem","net.core.wmem_max","net.core.rmem_max","net.core.wmem_default","net.core.rmem_default",
    "net.ipv4.ip_local_port_range","kernel.sem","vm.min_free_kbytes","net.core.somaxconn","net.ipv4.tcp_syncookies",
    "net.core.netdev_max_backlog","net.ipv4.tcp_max_syn_backlog","net.ipv4.tcp_fin_timeout","kernel.shmall","kernel.shmmax",
    "net.ipv4.tcp_sack","net.ipv4.tcp_timestamps","vm.extfrag_threshold","vm.overcommit_ratio","MTU"};
    private final String[] databaseParamType={"max_process_memory","work_mem","pagewriter_sleep","bgwriter_delay","bgwriter_thread_num",
    "max_io_capacity","log_min_duration_statement","log_duration","track_stmt_stat_level","track_stmt_retention_time",
    "enable_thread_pool","thread_pool_attr","log_statement","log_error_verbosity","log_min_messages","log_min_error_statement"};

    @Override
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

    /*@Override
    public List<OsParamDTO> getOsParamInfo(ParamQuery paramQuery) {
        List<OsParamDTO> list=new ArrayList<>();
        for(int i=0;i<osParamType.length;i++){
            SSHOperator ssh = SSHPoolManager.getSSHOperator("10.10.9.238", 22, "root",
                    "Van@09876");
            String paramValue=ssh.executeCommandReturnStr("sysctl -a | grep "+osParamType[i]+" | awk -F=  '{print $2}'");
            OsParamDTO osParamDTO=new OsParamDTO();
            osParamDTO.setSeqNo(String.valueOf(i+1));
            osParamDTO.setClassify("操作系统");
            osParamDTO.setParamName(osParamType[i]);
            osParamDTO.setParamDetail("表示同时保持TIME_WAIT状态的TCP/IP连接最大数量。如果超过所配置的取值，TIME_WAIT将立刻被释放并打印警告信息。");
            osParamDTO.setActualValue(paramValue.replace("\n","").replace("\t"," "));
            osParamDTO.setSuggestValue("10000");
            osParamDTO.setDefaultValue("180000");
            osParamDTO.setUnit("数目");
            osParamDTO.setSuggestExplain("系统在同时所处理的最大 timewait sockets 数目。如果超过此数的话﹐time-wait socket 会被立即砍除并且显示警告信息。之所以要设定这个限制﹐纯粹为了抵御那些简单的 DoS 攻击﹐不过﹐如果网络条件需要比默认值更多﹐则可以提高它(或许还要增加内存)。(事实上做NAT的时候最好可以适当地增加该值)");
            list.add(osParamDTO);
        }
        return list;
    }*/

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
        //HostUserFacade hostUserFacade=new HostUserFacade();
        //List<OpsHostUserEntity> userList=hostUserFacade.listHostUserByHostId(node.getHostId());
        //for(int n=0;n<userList.size();n++){OpsHostUserEntity opsHostUserEntity=userList.get(n);}

        //for(int i=0;i<osParamType.length;i++){}
        return list;
    }


}
