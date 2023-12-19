/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DatabaseSynonymServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/DatabaseSynonymServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SynonymObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * DatabaseSynonymServiceImpl
 *
 * @since 2023-09-25
 */
@Slf4j
@Service
public class DatabaseSynonymServiceImpl implements DatabaseSynonymService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, SynonymObjectSQLService> synonymObjectSQLService;

    /**
     * set synonym object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setSynonymObjectSQLService(List<SynonymObjectSQLService> SQLServiceList) {
        synonymObjectSQLService = new HashMap<>();
        for (SynonymObjectSQLService service : SQLServiceList) {
            synonymObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public String createSynonymDDL(DatabaseCreateSynonymDTO request) {
        log.info("createSynonymDDL request is: " + request);
        String ddl = synonymObjectSQLService.get(comGetUuidType(request.getUuid())).splicingSynonymDDL(request);
        log.info("createSynonymDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) throws SQLException {
        log.info("synonymAttribute request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = synonymObjectSQLService.get(comGetUuidType(request.getUuid())).synonymAttributeSQL(
                    request);
            try (
                    ResultSet count = statement.executeQuery(ddl)
            ) {
                if (count.next()) {
                    if (StringUtils.isBlank(String.valueOf(count.getString("synname")))) {
                        throw new CustomException(LocaleStringUtils.transLanguage("2011"));
                    }
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
        }
    }

    @Override
    public void createSynonym(DatabaseCreateSynonymDTO request) throws SQLException {
        log.info("createSynonym request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = synonymObjectSQLService.get(comGetUuidType(request.getUuid())).splicingSynonymDDL(
                    request);
            statement.execute(ddl);
            log.info("createSynonym sql is: " + ddl);
        }
    }

    @Override
    public void dropSynonym(DatabaseDropSynonymDTO request) throws SQLException {
        log.info("dropSynonym request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = synonymObjectSQLService.get(comGetUuidType(request.getUuid())).dropSynonymSQL(request);
            statement.execute(sql);
            log.info("dropSynonym sql is: " + sql);
        }
    }
}
