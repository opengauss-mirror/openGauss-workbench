/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SynonymObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Slf4j
@Service
public class DatabaseSynonymServiceImpl implements DatabaseSynonymService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, SynonymObjectSQLService> synonymObjectSQLService;

    @Resource
    public void setSynonymObjectSQLService(List<SynonymObjectSQLService> SQLServiceList) {
        synonymObjectSQLService = new HashMap<>();
        for (SynonymObjectSQLService s : SQLServiceList) {
            synonymObjectSQLService.put(s.type(), s);
        }
    }

    @Override
    public String createSynonymDDL(DatabaseCreateSynonymDTO request) throws Exception {
        log.info("createSynonymDDL request is: " + request);
        String ddl = synonymObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingSequenceDDL(request);
        log.info("createSynonymDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) {
        log.info("synonymAttribute request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = synonymObjectSQLService.get(conMap.get(request.getUuid()).getType()).synonymAttributeSQL(
                    request);
            try (
                    ResultSet count = statement.executeQuery(ddl)
            ) {
                count.next();
                if (StringUtils.isBlank(String.valueOf(count.getString("synname")))) {
                    throw new CustomException(LocaleString.transLanguage("2011"));
                }
            }
            Map<String, Object> resultMap;
            try (
                    ResultSet resultSet = statement.executeQuery(ddl)
            ) {
                resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("synonymAttribute sql is: " + ddl);
                log.info("synonymAttribute response is: " + resultMap);
            }
            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createSynonym(DatabaseCreateSynonymDTO request) {
        log.info("createSynonym request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = synonymObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingSequenceDDL(
                    request);
            statement.execute(ddl);
            log.info("createSynonym sql is: " + ddl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dropSynonym(DatabaseDropSynonymDTO request) {
        log.info("dropSynonym request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = synonymObjectSQLService.get(conMap.get(request.getUuid()).getType()).dropSynonymSQL(request);
            statement.execute(sql);
            log.info("dropSynonym sql is: " + sql);
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }
}
