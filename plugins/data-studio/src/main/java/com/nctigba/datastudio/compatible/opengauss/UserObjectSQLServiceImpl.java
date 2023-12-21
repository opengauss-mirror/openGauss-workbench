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
 *  UserObjectSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/UserObjectSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.UserObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseReturnUserDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseUserInfoDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.model.dto.UpdateUserAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateUserPasswordDTO;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_COMMENT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_CONNECTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_DATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_POOL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_POWER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_ROLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_ROLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GRANT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.RESOURCE_POOL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.REVOKE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ROLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_ROLES_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UPDAT_USER_PASSWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.USER_CONNECTION_LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VALID_BEGIN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VALID_UNTIL_SQL;
import static com.nctigba.datastudio.utils.DebugUtils.cutBrace;
import static com.nctigba.datastudio.utils.SecretUtils.desEncrypt;

/**
 * UserObjectSQLServiceImpl achieve
 *
 * @since 2023-10-10
 */
@Slf4j
@Service
public class UserObjectSQLServiceImpl implements UserObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String createUserPreviewDDL(DatabaseCreateUserDTO request, String passwordType) {
        String password;
        if (passwordType.equals("true")) {
            password = "********";
        } else {
            password = request.getPassword();
        }
        StringBuilder userRole = new StringBuilder();
        if (request.getType().equals("user")) {
            userRole.append(String.format(USER_SQL, request.getName()));
        } else {
            userRole.append(String.format(ROLE_SQL, request.getName()));
        }
        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format(CREATE_USER_SQL, userRole, getDdlCondition(request), password));
        String comment = "";
        if (StringUtils.isNotEmpty(request.getComment())) {
            comment = String.format(COMMENT_ROLE_SQL, request.getName(), request.getComment());
        }
        ddl.append(comment);
        return String.valueOf(ddl);
    }

    /**
     * get ddl
     *
     * @param request request
     * @return StringBuilder
     */
    private StringBuilder getDdlCondition(DatabaseCreateUserDTO request) {
        StringBuilder ddlCondition = new StringBuilder();
        if (request.getPower().size() >= 1) {
            ddlCondition.append(LF);
            for (int i = 0; i < request.getPower().size(); i++) {
                ddlCondition.append(request.getPower().get(i)).append(LF);
            }
        }
        if (StringUtils.isNotEmpty(request.getConnectionLimit()) && !request.getConnectionLimit().equals("-1")) {
            ddlCondition.append(String.format(USER_CONNECTION_LIMIT_SQL, request.getConnectionLimit()));
        }
        if (StringUtils.isNotEmpty(request.getBeginDate())) {
            ddlCondition.append(String.format(VALID_BEGIN_SQL, request.getBeginDate()));
        }
        if (StringUtils.isNotEmpty(request.getEndDate())) {
            ddlCondition.append(String.format(VALID_UNTIL_SQL, request.getEndDate()));
        }
        if (StringUtils.isNotEmpty(request.getResourcePool())) {
            ddlCondition.append(String.format(RESOURCE_POOL_SQL, request.getResourcePool()));
        }
        int size = request.getRole().size();
        if (size >= 1) {
            ddlCondition.append(" ROLE ").append(LF);
            for (int i = 0; i < size; i++) {
                ddlCondition.append(request.getRole().get(i));
                if (i < size - 1) {
                    ddlCondition.append(COMMA);
                }
            }
        }
        int adminSize = request.getAdministrator().size();
        if (adminSize >= 1) {
            ddlCondition.append(" ADMIN ").append(LF);
            for (int i = 0; i < adminSize; i++) {
                ddlCondition.append(request.getAdministrator().get(i));
                if (i < adminSize - 1) {
                    ddlCondition.append(COMMA);
                }
            }
        }
        return ddlCondition;
    }

    @Override
    public String dropUserDDL(DatabaseUsserCheckDTO request) {
        String ddl;
        if (request.getType().equals("user")) {
            ddl = String.format(DROP_USER_SQL, request.getUserName());
        } else {
            ddl = String.format(DROP_ROLE_SQL, request.getUserName());
        }
        log.info("createUserPreviewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String userPreviewDDL(DatabaseReturnUserDdlDTO request) throws SQLException {
        log.info("viewAttributeData request is: " + request);
        DatabaseUserInfoDTO databaseUserInfoDTO = userInfo(request);
        DatabaseCreateUserDTO databaseCreateUserDTO = new DatabaseCreateUserDTO();
        databaseCreateUserDTO.setName(databaseUserInfoDTO.getName());
        databaseCreateUserDTO.setType(databaseUserInfoDTO.getType());
        databaseCreateUserDTO.setBeginDate(databaseUserInfoDTO.getBeginDate());
        databaseCreateUserDTO.setEndDate(databaseUserInfoDTO.getEndDate());
        databaseCreateUserDTO.setConnectionLimit(String.valueOf(databaseUserInfoDTO.getConnectionLimit()));
        databaseCreateUserDTO.setRole(databaseUserInfoDTO.getRole());
        databaseCreateUserDTO.setAdministrator(databaseUserInfoDTO.getAdministrator());
        databaseCreateUserDTO.setPower(databaseUserInfoDTO.getPower());
        databaseCreateUserDTO.setComment(databaseUserInfoDTO.getComment());
        databaseCreateUserDTO.setResourcePool(databaseUserInfoDTO.getResourcePool());
        String ddl;
        ddl = createUserPreviewDDL(databaseCreateUserDTO, "true");
        return ddl;
    }

    @Override
    public DatabaseUserInfoDTO userInfo(DatabaseReturnUserDdlDTO request) throws SQLException {
        log.info("viewAttributeData request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String selectSql;
            if ("user".equals(request.getType())) {
                selectSql = String.format(SELECT_USER_SQL, request.getUserName(), request.getUserName());
            } else {
                selectSql = String.format(SELECT_ROLES_SQL, request.getUserName(), request.getUserName());
            }
            log.info("viewAttributeData sql is: " + selectSql);
            try (
                    ResultSet resultSet = statement.executeQuery(selectSql)
            ) {
                resultSet.next();
                List<String> power = new ArrayList<>();
                log.info("aaaaaaa sql is: " + resultSet.getString("belong"));
                if ("user".equals(request.getType())) {
                    listAddNotNull(resultSet.getString("rolcanlogin"), power);
                }
                listAddNotNull(resultSet.getString("rolinherit"), power);
                listAddNotNull(resultSet.getString("rolreplication"), power);
                listAddNotNull(resultSet.getString("rolcreaterole"), power);
                listAddNotNull(resultSet.getString("rolcreatedb"), power);
                listAddNotNull(resultSet.getString("rolauditadmin"), power);
                listAddNotNull(resultSet.getString("rolsystemadmin"), power);
                DatabaseUserInfoDTO databaseUserInfoDTO = new DatabaseUserInfoDTO();
                databaseUserInfoDTO.setOid(resultSet.getString("oid"));
                databaseUserInfoDTO.setName(resultSet.getString("rolname"));
                databaseUserInfoDTO.setPassword("********");
                databaseUserInfoDTO.setBeginDate(resultSet.getString("rolvalidbegin"));
                databaseUserInfoDTO.setEndDate(resultSet.getString("rolvaliduntil"));
                databaseUserInfoDTO.setConnectionLimit(resultSet.getInt("rolconnlimit"));
                databaseUserInfoDTO.setResourcePool(resultSet.getString("rolrespool"));
                databaseUserInfoDTO.setComment(resultSet.getString("Description"));
                databaseUserInfoDTO.setPower(power);

                List<String> role = cutBrace(resultSet.getString("grrolname"));
                databaseUserInfoDTO.setRole(role);
                List<String> administrator = cutBrace(resultSet.getString("merolname"));
                databaseUserInfoDTO.setAdministrator(administrator);
                List<String> belongList = cutBrace(resultSet.getString("belong"));
                databaseUserInfoDTO.setBelong(belongList);
                databaseUserInfoDTO.setType(request.getType());
                log.info("databaseUserInfoDTO is: {}", databaseUserInfoDTO);
                return databaseUserInfoDTO;
            }
        }
    }

    @Override
    public String updateUserPassword(UpdateUserPasswordDTO request) {
        String ddl;
        ddl = String.format(UPDAT_USER_PASSWORD_SQL, request.getType(), request.getUserName(),
                desEncrypt(request.getNewPassword()), desEncrypt(request.getOldPassword()));
        log.info("updateUserPassword response is: " + ddl);
        return ddl;
    }


    @Override
    public String userUpdateAttribute(UpdateUserAttributeDTO request) {
        StringBuilder ddl = new StringBuilder();
        String name;
        if (StringUtils.isNotEmpty(request.getNewName())) {
            name = request.getNewName();
            ddl.append(String.format(ALTER_NAME_SQL, request.getType(), request.getOldName(), request.getNewName()));
        } else {
            name = request.getOldName();
        }

        if (StringUtils.isNotEmpty(request.getEndDate())) {
            ddl.append(String.format(ALTER_DATE_SQL, request.getType(), name, "UNTIL", request.getEndDate()));
        }

        if (StringUtils.isNotEmpty(request.getBeginDate())) {
            ddl.append(String.format(ALTER_DATE_SQL, request.getType(), name, "BEGIN", request.getBeginDate()));
        }

        if (StringUtils.isNotEmpty(request.getConnectionLimit())) {
            ddl.append(String.format(ALTER_CONNECTION_SQL, request.getType(), name, request.getConnectionLimit()));
        }

        if (StringUtils.isNotEmpty(request.getResourcePool())) {
            ddl.append(String.format(ALTER_POOL_SQL, request.getType(), name, request.getResourcePool()));
        }

        Map<String, Boolean> changePower = request.getChangePower();
        if (!changePower.isEmpty()) {
            for (String key : changePower.keySet()) {
                if (changePower.get(key)) {
                    ddl.append(String.format(ALTER_POWER_SQL, request.getType(), name, key));
                } else {
                    ddl.append(String.format(ALTER_POWER_SQL, request.getType(), name, "NO" + key));
                }
            }
        }

        Map<String, Boolean> changeBelong = request.getChangeBelong();
        if (!changeBelong.isEmpty()) {
            for (String key : changeBelong.keySet()) {
                if (changeBelong.get(key)) {
                    ddl.append(String.format(GRANT_SQL, key, name));
                } else {
                    ddl.append(String.format(REVOKE_SQL, key, name));
                }
            }
        }

        if (StringUtils.isNotEmpty(request.getResourcePool())) {
            ddl.append(String.format(ALTER_COMMENT_SQL, request.getType(), name, request.getComment()));
        }
        log.info("userUpdateAttribute response is: " + ddl);
        return String.valueOf(ddl);
    }

    /**
     * list Add Not Null
     *
     * @param request request
     * @param list list
     */
    public void listAddNotNull(String request, List<String> list) {
        if (StringUtils.isNotEmpty(request)) {
            list.add(request);
        }
    }
}
