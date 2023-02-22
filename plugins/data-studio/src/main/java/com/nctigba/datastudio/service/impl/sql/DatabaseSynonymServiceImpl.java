package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FOR_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.REPLACE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SYNONYM_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SYNONYM_KEYWORD_SQL;

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
        try {
            String ddl = SYNONYM_ATTRIBUTE_SQL + request.getSynonymName() + QUOTES_SEMICOLON;
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

    @Override
    public void createSynonym(DatabaseCreateSynonymDTO request) {
        log.info("createSynonym request is: " + request);
        try {
            String ddl = splicingSequenceDDL(request);
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            statement.execute(ddl);
            log.info("createSynonym sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void dropSynonym(DatabaseDropSynonymDTO request) {
        log.info("dropSynonym request is: " + request);
        try {
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            String sql = DROP_SQL + SYNONYM_KEYWORD_SQL + request.getSchema() + POINT + request.getSynonymName();
            statement.execute(sql);
            log.info("dropSynonym sql is: " + sql);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }
}
