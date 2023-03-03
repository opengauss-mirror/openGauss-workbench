package com.nctigba.datastudio.config;

import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.DbswitchException;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.service.impl.sql.DbConnectionServiceImpl;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Service("connectionConfig")
public class ConnectionConfig {
    @Resource
    private DbConnectionServiceImpl dbConnectionServiceImpl;

    public Connection connectDatabase(String uuid) throws Exception {
        if(!conMap.containsKey(uuid)){
            throw new CustomException("The current connection does not exist");
        }
        ConnectionDTO connectionDTO = conMap.get(uuid);
        Connection connection = ConnectionUtils.connectGet(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                connectionDTO.getDbPassword());
        connectionDTO.updataConnectionDTO(connectionDTO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return connection;
    }

    public Connection connectDatabaseMap(String uuid, String winName) throws Exception {
        if(!conMap.containsKey(uuid)){
            throw new CustomException("The current connection does not exist");
        }
        ConnectionDTO connectionDTO = conMap.get(uuid);
        Connection connection = ConnectionUtils.connectGet(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                connectionDTO.getDbPassword());
        connectionDTO.updataConnectionDTO(connectionDTO, winName);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return connection;
    }
}
