/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.UserObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.service.DatabaseUserService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
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
 * DatabaseUserServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class DatabaseUserServiceImpl implements DatabaseUserService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, UserObjectSQLService> userObjectSQLServiceMap;

    /**
     * set synonym object sql service
     *
     * @param sqlServiceList sqlServiceList
     */
    @Resource
    public void setSynonymObjectSQLService(List<UserObjectSQLService> sqlServiceList) {
        userObjectSQLServiceMap = new HashMap<>();
        for (UserObjectSQLService service : sqlServiceList) {
            userObjectSQLServiceMap.put(service.type(), service);
        }
    }

    @Override
    public String createUserPreviewDDL(DatabaseCreateUserDTO request) {
        log.info("createUserPreviewDDL request is: " + request);
        String ddl =
                userObjectSQLServiceMap.get(conMap.get(request.getUuid()).getType()).createUserPreviewDDL(request,
                        "true");
        log.info("createUserPreviewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public void createUserDDL(DatabaseCreateUserDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = userObjectSQLServiceMap.get(conMap.get(request.getUuid())
                    .getType()).createUserPreviewDDL(request, "false");
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void dropUserDDL(DatabaseUsserCheckDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = userObjectSQLServiceMap.get(conMap.get(request.getUuid())
                    .getType()).dropUserDDL(request);
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }
}
