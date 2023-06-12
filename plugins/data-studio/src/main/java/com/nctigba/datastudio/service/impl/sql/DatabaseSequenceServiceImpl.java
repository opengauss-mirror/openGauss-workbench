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
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Slf4j
@Service
public class DatabaseSequenceServiceImpl implements DatabaseSequenceService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, SequenceObjectSQLService> sequenceObjectSQLService;

    @Resource
    public void setSequenceObjectSQLService(List<SequenceObjectSQLService> SQLServiceList) {
        sequenceObjectSQLService = new HashMap<>();
        for (SequenceObjectSQLService s : SQLServiceList) {
            sequenceObjectSQLService.put(s.type(), s);
        }
    }

    @Override
    public String createSequenceDDL(DatabaseCreateSequenceDTO request) throws Exception {
        log.info("createSequenceDDL request is: " + request);
        String ddl = sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingSequenceDDL(request);
        log.info("createSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public void createSequence(DatabaseCreateSequenceDTO request) {
        log.info("createSequence request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingSequenceDDL(
                    request);
            statement.execute(ddl);
            log.info("createSequence sql is: " + ddl);
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dropSequence(DatabaseDropSequenceDTO request) {
        log.info("dropSequence request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).dropSequenceDDL(request);
            statement.execute(sql);
            log.info("dropSequence sql is: " + sql);
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws Exception {
        log.info("returnSequenceDDL request is: " + request);
        return sequenceObjectSQLService.get(conMap.get(request.getUuid()).getType()).returnSequenceDDL(request);
    }
}
