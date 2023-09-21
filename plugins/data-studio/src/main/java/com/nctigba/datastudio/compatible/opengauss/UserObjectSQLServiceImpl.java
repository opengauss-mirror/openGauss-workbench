/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.UserObjectSQLService;
import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_ROLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_ROLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.RESOURCE_POOL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ROLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.USER_CONNECTION_LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.USER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VALID_BEGIN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VALID_UNTIL_SQL;

/**
 * UserObjectSQLServiceImpl achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class UserObjectSQLServiceImpl implements UserObjectSQLService {
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

    private StringBuilder getDdlCondition(DatabaseCreateUserDTO request) {
        StringBuilder ddlCondition = new StringBuilder();
        if (request.getPower().size() >= 1) {
            for (int i = 0; i < request.getPower().size(); i++) {
                ddlCondition.append(LF).append(request.getPower().get(i));
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
            for (int i = 0; i < request.getRole().size(); i++) {
                ddlCondition.append(request.getRole().get(i));
                if (i < size - 1) {
                    ddlCondition.append(COMMA);
                }
            }
        }
        int adminSize = request.getAdministrator().size();
        if (adminSize >= 1) {
            ddlCondition.append(" ADMIN ").append(LF);
            for (int i = 0; i < request.getRole().size(); i++) {
                ddlCondition.append(request.getAdministrator().get(i));
                if (i < size - 1) {
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
}
