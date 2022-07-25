package com.nctigba.datastudio.config;

import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.service.impl.sql.DbConnectionServiceImpl;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;

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
}
