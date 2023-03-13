package com.nctigba.observability.instance.service.impl;

import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.pool.SSHPoolManager;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ParamInfoService;
import com.nctigba.observability.instance.util.SSHOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParamInfoServiceImpl implements ParamInfoService {

    private final ClusterManager opsFacade;


    @Override
    public List<ParamInfoDTO> getParamInfo(ParamQuery paramQuery) {
        if ("1".equals(paramQuery.getIsRefresh())) {
            if("".equals(paramQuery.getPassword()) || paramQuery.getPassword()==null){
                return null;
            }
            this.updateOsParamInfo(paramQuery);
            this.updateDatabaseParamInfo(paramQuery);
        }
        ResultSet result;
        List<ParamInfoDTO> list = new ArrayList<>();
        try {
            Connection conn = connect_sqlite();
            Statement statement = conn.createStatement();
            result = statement.executeQuery("select param_info.id,param_info.paramName,param_info.paramType" +
                    ",param_info.paramDetail,param_info.suggestValue,param_info.defaultValue,param_info.unit" +
                    ",param_info.suggestExplain,param_value_info.actualValue " +
                    "from param_info left join param_value_info " +
                    "on param_info.id=param_value_info.sid and param_value_info.instance='" + paramQuery.getNodeId() + "';");
            while (result.next()) {
                ParamInfoDTO paramInfo = new ParamInfoDTO();
                paramInfo.setId(result.getInt("id"));
                paramInfo.setParamName(result.getString("paramName"));
                paramInfo.setParamType(result.getString("paramType"));
                paramInfo.setParamDetail(result.getString("paramDetail"));
                paramInfo.setSuggestValue(result.getString("suggestValue"));
                paramInfo.setDefaultValue(result.getString("defaultValue"));
                paramInfo.setUnit(result.getString("unit"));
                paramInfo.setSuggestExplain(result.getString("suggestExplain"));
                paramInfo.setActualValue(result.getString("actualValue"));
                list.add(paramInfo);
            }
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private void updateDatabaseParamInfo(ParamQuery paramQuery) {
        try {
            Connection connect = connect_sqlite();
            Statement statement = connect.createStatement();
            String delSql = "delete from param_value_info " +
                    "where sid in (select id from param_info where paramType='DB');";
            statement.executeUpdate(delSql);
            var conn = opsFacade.getConnectionByNodeId(paramQuery.getNodeId());
            String sql = "select name,setting from pg_settings";
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString(1);
                String value = rs.getString(2);
                String selectSql = "select id from param_info where paramName='" + name + "';";
                ResultSet resultSet = statement.executeQuery(selectSql);
                Integer id = resultSet.getInt("id");
                String insetSql = "insert into param_value_info(sid,instance,actualValue) " +
                        "values(" + id + ",'" + paramQuery.getNodeId() + "','" + value + "');";
                statement.executeUpdate(insetSql);
            }
            statement.close();
            stmt.close();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void updateOsParamInfo(ParamQuery paramQuery) {
        try {
            Connection connect = connect_sqlite();
            Statement statement = connect.createStatement();
            String delSql = "delete from param_value_info " +
                    "where sid in (select id from param_info where paramType='OS');";
            statement.executeUpdate(delSql);
            var node = opsFacade.getOpsNodeById(paramQuery.getNodeId());
            SSHOperator ssh = SSHPoolManager.getSSHOperator(node.getPublicIp(), node.getHostPort(), "root",
                    paramQuery.getPassword());
            String paramValues = ssh.executeCommandReturnStr("sysctl -a");
            String[] values = paramValues.split("\n");
            for (int n = 0; n < values.length; n++) {
                String name = values[n].substring(0, values[n].lastIndexOf("=")).trim();
                String paramData = values[n].substring(values[n].indexOf("=") + 1).trim();
                String selectSql = "select id from param_info where paramName='" + name + "';";
                ResultSet resultSet = statement.executeQuery(selectSql);
                Integer id = resultSet.getInt("id");
                String insetSql = "insert into param_value_info(sid,instance,actualValue) " +
                        "values(" + id + ",'" + paramQuery.getNodeId() + "','" + paramData + "');";
                statement.executeUpdate(insetSql);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static synchronized Connection connect_sqlite() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(JDBC.PREFIX + "data/paramValueInfo.db");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

}
