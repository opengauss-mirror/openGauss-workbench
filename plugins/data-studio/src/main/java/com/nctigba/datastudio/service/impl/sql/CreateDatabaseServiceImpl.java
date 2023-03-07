package com.nctigba.datastudio.service.impl.sql;

import com.alibaba.druid.util.StringUtils;
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
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
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

import static com.nctigba.datastudio.constants.SqlConstants.*;

@Slf4j
@Service
public class CreateDatabaseServiceImpl implements CreateDatabaseService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Resource
    private DatabaseConnectionDAO databaseConnectionDAO;
    @Resource
    private MetaDataByJdbcService metaDataByJdbcService;
    @Override
    public void createDatabase(CreateDatabaseDTO request) throws Exception {
        log.info("createDatabase request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String ddl = CREATE_SQL + DATABASE_SQL + request.getDatabaseName() + WITH_ENCODING_SQL + request.getDatabaseCode() + DBCOMPATIBILITY_SQL + request.getCompatibleType();
            if (!StringUtils.isEmpty(request.getCollation())){
                ddl = ddl + LC_COLLATE_SQL + request.getCollation();
            }
            if (!StringUtils.isEmpty(request.getCharacterType())){
                ddl = ddl + LC_CTYPE_SQL + request.getCharacterType();
            }
            if (Integer.valueOf(request.getConRestrictions()) >= -1){
                ddl = ddl +QUOTES+ CONNECTION_LIMIT_SQL + request.getConRestrictions()+ SEMICOLON;
            }else{
                throw new CustomException(LocaleString.transLanguage("2013"));
            }

            statement.execute(ddl);
            log.info("createDatabase sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
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
        metaDataByJdbcService.testQuerySQL(databaseConnectionUrlDO.getUrl(),databaseConnectionUrlDO.getUserName(),databaseConnectionUrlDO.getPassword(),
                "SELECT 1");
        connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return databaseConnectionDO;
    }

    @Override
    public void deleteDatabase(DatabaseNameDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        String ddl = DROP_SQL + DATABASE_SQL + request.getDatabaseName() + SEMICOLON;
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            statement.execute(ddl);
            log.info("deleteDatabase sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public void renameDatabase(RenameDatabaseDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        String ddl = null;
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid())) {
            if(request.getOldDatabaseName().equals(request.getDatabaseName())){
                if (Integer.valueOf(request.getConRestrictions()) >= -1){
                    ddl = ALTER_SQL + DATABASE_SQL + request.getOldDatabaseName() + WITH_KEYWORD_SQL +CONNECTION_LIMIT_SQL+ request.getConRestrictions() + SEMICOLON;
                    try(Statement statement = connection.createStatement()){
                        statement.execute(ddl);
                        log.info("deleteDatabase sql is: " + ddl);
                    }
                }else{
                    throw new CustomException(LocaleString.transLanguage("2013"));
                }
            }else {
                ddl = ALTER_SQL + DATABASE_SQL + request.getOldDatabaseName() + RENAME_TO_SQL + request.getDatabaseName() + SEMICOLON;
                try(Statement statement = connection.createStatement()){
                    statement.execute(ddl);
                    log.info("deleteDatabase sql is: " + ddl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public Map<String, Object> databaseAttribute(DatabaseNameDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        Map<String, Object> resultMap;
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String ddl = DATABASE_ATTRIBUTE_SQL + request.getDatabaseName() + QUOTES_SEMICOLON;
            try(ResultSet resultSet = statement.executeQuery(ddl);){
                resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("synonymAttribute sql is: " + ddl);
                log.info("synonymAttribute response is: " + resultMap);
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }
    @Override
    public Map<String, Object> databaseAttributeUpdate(DatabaseNameDTO request) throws Exception {
        log.info("deleteDatabase request is: " + request);
        Map<String, Object> resultMap;
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String ddl = DATABASE_UPDATA_ATTRIBUTE_SQL + request.getDatabaseName() + QUOTES_SEMICOLON;
            try(ResultSet resultSet = statement.executeQuery(ddl)) {
                resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("synonymAttribute sql is: " + ddl);
                log.info("synonymAttribute response is: " + resultMap);
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }
}
