package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

import static com.nctigba.datastudio.constants.SqlConstants.*;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Slf4j
@Service
public class DatabaseSequenceServiceImpl implements DatabaseSequenceService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String splicingSequenceDDL(DatabaseCreateSequenceDTO request) throws Exception {
        log.info("splicingSequenceDDL request is: " + request);
        String ddl = CREATE_SQL + SEQUENCE_KEYWORD_SQL + request.getSchema() + POINT + request.getSequenceName();
        if (isNumeric(request.getStart())) {
            ddl = ddl + "\n" + START_KEYWORD_SQL + request.getStart();
        }
        if (isNumeric(request.getIncrement())) {
            ddl = ddl + "\n" + INCREMENT_KEYWORD_SQL + request.getIncrement();
        }
        if (isNumeric(request.getMinValue())) {
            ddl = ddl + "\n" + MINVALUE_KEYWORD_SQL + request.getMinValue();
        } else {
            ddl = ddl + "\n" + NO_MINVALUE_KEYWORD_SQL;
        }
        if (isNumeric(request.getMaxValue())) {
            ddl = ddl + "\n" + MAXVALUE_KEYWORD_SQL + request.getMaxValue();
        }
        if (isNumeric(request.getCache())) {
            ddl = ddl + "\n" + CACHE_KEYWORD_SQL + request.getCache();
        }
        if (request.getCycle().equals("CYCLE")) {
            ddl = ddl + "\n" + CYCLE_KEYWORD_SQL;
        }
        if (!Objects.nonNull(request.getTableSchema())) {
            ddl = ddl + "\n" + OWNED_KEYWORD_SQL + request.getTableSchema() + POINT;
        }
        if (!Objects.nonNull(request.getTableName())) {
            ddl = ddl + request.getTableName() + POINT;
        }
        if (!Objects.nonNull(request.getTableColumn())) {
            ddl = ddl + request.getTableColumn();
        }
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String createSequenceDDL(DatabaseCreateSequenceDTO request) throws Exception {
        log.info("createSequenceDDL request is: " + request);
        String ddl = splicingSequenceDDL(request);
        log.info("createSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public void createSequence(DatabaseCreateSequenceDTO request) {
        log.info("createSequence request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String ddl = splicingSequenceDDL(request);

            statement.execute(ddl);
            log.info("createSequence sql is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public void dropSequence(DatabaseDropSequenceDTO request) {
        log.info("dropSequence request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String sql = DROP_SQL + SEQUENCE_KEYWORD_SQL + request.getSchema() + POINT + request.getSequenceName();
            statement.execute(sql);
            log.info("dropSequence sql is: " + sql);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws Exception {
        log.info("returnSequenceDDL request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement()) {
            String selectSql = SELECT_SEQUENCE_DDL_SQL + request.getSchema() + SELECT_SEQUENCE_DDL_WHERE_SQL + request.getSequenceName() + QUOTES_SEMICOLON;
            try(ResultSet count = statement.executeQuery(SELECT_KEYWORD_SQL + COUNT_SQL +FROM_CLASS_SQL+
                    request.getSchema() + FROM_CLASS_WHERE_SQL + request.getSequenceName() + RELKIND_SQL+ "S" + QUOTES_SEMICOLON)){
                count.next();
                int a = count.getInt("count");
                if(a==0){
                    throw new CustomException("The sequence does not exist");
                }
            }
            String ddl = "";
            try(ResultSet resultSet = statement.executeQuery(selectSql)) {
                while (resultSet.next()) {
                    ddl = SEQUENCE_SET_SQL + resultSet.getString("sequence_schema") + SEMICOLON + "\n"
                            + CREATE_SQL + SEQUENCE_KEYWORD_SQL + resultSet.getString("sequence_name") + "\n"
                            + START_WITH_SQL + resultSet.getString("start_value") + "\n"
                            + INCREMENT_BY_SQL + resultSet.getString("increment") + "\n";
                    if (resultSet.getInt("minimum_value") == 1) {
                        ddl = ddl + NO_MINVALUE_SQL + "\n"
                                + MAXVALUE_SQL + resultSet.getString("maximum_value") + "\n";

                    } else {
                        ddl = ddl + MINVALUE_SQL + resultSet.getString("minimum_value") + "\n"
                                + MAXVALUE_SQL + resultSet.getString("maximum_value") + "\n";

                    }
                    if (resultSet.getString("cycle_option").equals("YES")) {
                        ddl = ddl + CYCLE_SQL + "\n";
                    }
                    ddl = ddl + SEMICOLON;
                }
                log.info("returnSequenceDDL response is: " + ddl);
            }
            return ddl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }
}
