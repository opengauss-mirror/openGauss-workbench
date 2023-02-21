package com.nctigba.datastudio.config;

import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.DbswitchException;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.service.impl.sql.DbConnectionServiceImpl;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Service("connectionConfig")
public class ConnectionConfig {
    @Resource
    private DbConnectionServiceImpl dbConnectionServiceImpl;

    public Connection connectDatabase(Integer id, String webUser) throws Exception {
        DatabaseConnectionUrlDO dbConn = dbConnectionServiceImpl.getDatabaseConnectionById(id, webUser);
        Connection connection = ConnectionUtils.connectGet(
                dbConn.getUrl(),
                dbConn.getUserName(),
                dbConn.getPassword());
        return connection;
    }

    public Connection connectDatabase(String name, String webUser) throws Exception {
        DatabaseConnectionUrlDO dbConn = dbConnectionServiceImpl.getDatabaseConnectionByName(name, webUser);
        Connection connection = ConnectionUtils.connectGet(
                dbConn.getUrl(),
                dbConn.getUserName(),
                dbConn.getPassword());
        return connection;
    }

    public Connection connectDatabase(String uuid) throws Exception {
        ConnectionDTO connectionDTO;
        try{
            connectionDTO = conMap.get(uuid);
        }catch ( Exception e)
        {
            throw new DbswitchException("not found uuid=" + uuid);
        }
        Connection connection = ConnectionUtils.connectGet(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                connectionDTO.getDbPassword());
        connectionDTO.updataConnectionDTO(connectionDTO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return connection;
    }

    public Connection connectDatabaseMap(String uuid, String winName) throws Exception {
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
