package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.*;

@Slf4j
@Service
public class DatabaseSynonymServiceImpl implements DatabaseSynonymService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String splicingSequenceDDL(DatabaseCreateSynonymDTO request) throws Exception {
        log.info("splicingSequenceDDL request is: " + request);
        String ddl;
        if (request.isReplace()) {
            ddl = CREATE_SQL + REPLACE_KEYWORD_SQL + "\n" + SYNONYM_KEYWORD_SQL + request.getSchema() + POINT + request.getSynonymName() + "\n" + FOR_KEYWORD_SQL + "\n" + request.getSchema() + POINT + request.getObjectName();
        } else {
            ddl = CREATE_SQL + "\n" + SYNONYM_KEYWORD_SQL + request.getSchema() + POINT + request.getSynonymName() + "\n" + FOR_KEYWORD_SQL + "\n" + request.getSchema() + POINT + request.getObjectName();
        }
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String createSynonymDDL(DatabaseCreateSynonymDTO request) throws Exception {
        log.info("createSynonymDDL request is: " + request);
        String ddl = splicingSequenceDDL(request);
        log.info("createSynonymDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) {
        log.info("synonymAttribute request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();) {
            String ddl = SYNONYM_ATTRIBUTE_SQL + request.getSynonymName() + QUOTES_SEMICOLON;
            try(ResultSet count = statement.executeQuery(SYNONYM_COUNT_SQL + request.getSynonymName() + QUOTES_SEMICOLON)) {
                count.next();
                int a = count.getInt("count");
                if (a == 0) {
                    throw new CustomException(LocaleString.transLanguage("2011"));
                }
            }
            Map<String, Object> resultMap;
            try(ResultSet resultSet = statement.executeQuery(ddl)){
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
    public void createSynonym(DatabaseCreateSynonymDTO request) {
        log.info("createSynonym request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String ddl = splicingSequenceDDL(request);
            statement.execute(ddl);
            log.info("createSynonym sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public void dropSynonym(DatabaseDropSynonymDTO request) {
        log.info("dropSynonym request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()){
            String sql = DROP_SQL + SYNONYM_KEYWORD_SQL + request.getSchema() + POINT + request.getSynonymName();
            statement.execute(sql);
            log.info("dropSynonym sql is: " + sql);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }
}
