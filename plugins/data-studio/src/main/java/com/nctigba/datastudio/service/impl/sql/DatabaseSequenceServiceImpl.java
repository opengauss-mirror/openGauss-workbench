/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SequenceObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * DatabaseSequenceServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class DatabaseSequenceServiceImpl implements DatabaseSequenceService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, SequenceObjectSQLService> sequenceObjectSQLService;

    /**
     * set sequence object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setSequenceObjectSQLService(List<SequenceObjectSQLService> SQLServiceList) {
        sequenceObjectSQLService = new HashMap<>();
        for (SequenceObjectSQLService service : SQLServiceList) {
            sequenceObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public String createSequenceDDL(DatabaseCreateSequenceDTO request) {
        log.info("createSequenceDDL request is: " + request);
        String ddl = sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingSequenceDDL(request);
        log.info("createSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public void createSequence(DatabaseCreateSequenceDTO request) throws SQLException {
        log.info("createSequence request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingSequenceDDL(
                    request);
            statement.execute(ddl);
            log.info("createSequence sql is: " + ddl);
        }
    }

    @Override
    public void dropSequence(DatabaseDropSequenceDTO request) throws SQLException {
        log.info("dropSequence request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).dropSequenceDDL(request);
            statement.execute(sql);
            log.info("dropSequence sql is: " + sql);
        }
    }

    @Override
    public String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws SQLException {
        log.info("returnSequenceDDL request is: " + request);
        return sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).returnSequenceDDL(request);
    }
}
