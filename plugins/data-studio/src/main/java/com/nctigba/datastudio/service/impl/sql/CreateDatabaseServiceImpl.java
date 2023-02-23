package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.service.CreateDatabaseService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import static com.nctigba.datastudio.constants.SqlConstants.ALTER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DATABASE_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DATABASE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.RENAME_TO_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.WITH_ENCODING_SQL;

@Slf4j
@Service
public class CreateDatabaseServiceImpl implements CreateDatabaseService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Resource
    private DatabaseConnectionDAO databaseConnectionDAO;

    @Override
    public void createDatabase(CreateDatabaseDTO request) throws Exception {
        log.info("createDatabase request is: " + request);
        try {
            String ddl = CREATE_SQL + DATABASE_SQL + request.getDatabaseName() + WITH_ENCODING_SQL + request.getDatabaseCode() + QUOTES_SEMICOLON;
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            statement.execute(ddl);
            log.info("createDatabase sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public DatabaseConnectionDO connectionDatabase(DatabaseConnectionDO database) throws Exception {
        log.info("connectionDatabase request is: " + database);
        ConnectionDTO connectionDTO = new ConnectionDTO();
        DatabaseConnectionUrlDO databaseConnectionUrlDO = new DatabaseConnectionUrlDO();
        DatabaseConnectionDO databaseConnectionDO = databaseConnectionDAO.getByIdDatabase(Integer.parseInt(database.getId()), database.getWebUser());
        String uuid = UUID.randomUUID().toString();
        databaseConnectionDO.setConnectionid(uuid);
        databaseConnectionDO.setDataName(database.getDataName());
        databaseConnectionUrlDO.setUrl(GET_URL_JDBC + databaseConnectionDO.getIp() + ":" + databaseConnectionDO.getPort() + "/" + database.getDataName() + CONFIGURE_TIME);
        databaseConnectionUrlDO.setUserName(databaseConnectionDO.getUserName());
        databaseConnectionUrlDO.setPassword(databaseConnectionDO.getPassword());
        connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return databaseConnectionDO;
    }

    @Override
    public void deleteDatabase(DatabaseNameDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        try {
            String ddl = DROP_SQL + DATABASE_SQL + request.getDatabaseName() + SEMICOLON;
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            statement.execute(ddl);
            log.info("deleteDatabase sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void renameDatabase(RenameDatabaseDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        try {
            String ddl = ALTER_SQL + DATABASE_SQL + request.getOldDatabaseName() + RENAME_TO_SQL + request.getDatabaseName() + SEMICOLON;
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            statement.execute(ddl);
            log.info("deleteDatabase sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> databaseAttribute(DatabaseNameDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        try {
            String ddl = DATABASE_ATTRIBUTE_SQL + request.getDatabaseName() + QUOTES_SEMICOLON;
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ddl);
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("synonymAttribute sql is: " + ddl);
            log.info("synonymAttribute response is: " + resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }
}
