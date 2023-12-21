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
 *  DatabaseUserServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/DatabaseUserServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.UserObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseReturnUserDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseUserInfoDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.model.dto.UpdateUserAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateUserPasswordDTO;
import com.nctigba.datastudio.service.DatabaseUserService;
import com.nctigba.datastudio.utils.LocaleStringUtils;
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
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

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
                userObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).createUserPreviewDDL(request,
                        "true");
        log.info("createUserPreviewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String userPreviewDDL(DatabaseReturnUserDdlDTO request) throws SQLException {
        log.info("createUserPreviewDDL request is: " + request);
        String ddl =
                userObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).userPreviewDDL(request);
        log.info("createUserPreviewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public DatabaseUserInfoDTO userInfo(DatabaseReturnUserDdlDTO request) throws SQLException {
        log.info("createUserPreviewDDL request is: " + request);
        DatabaseUserInfoDTO databaseUserInfoDTO =
                userObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).userInfo(request);
        log.info("createUserPreviewDDL response is: " + databaseUserInfoDTO);
        return databaseUserInfoDTO;
    }

    @Override
    public void createUserDDL(DatabaseCreateUserDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = userObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                    .createUserPreviewDDL(request, "false");
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
            String ddl = userObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).dropUserDDL(request);
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void updateUserPassword(UpdateUserPasswordDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            if (request.getLoginUserPassword().equals(conMap.get(request.getUuid()).getDbPassword())) {
                String ddl = userObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                        .updateUserPassword(request);
                statement.execute(ddl);
            } else {
                throw new CustomException(LocaleStringUtils.transLanguage("1011"));
            }
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void userUpdateAttribute(UpdateUserAttributeDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = userObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).userUpdateAttribute(request);
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public String userUpdateAttributeDdl(UpdateUserAttributeDTO request) {
        return userObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).userUpdateAttribute(request);
    }
}
