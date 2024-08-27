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
 *  PrivilegeSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/PrivilegeSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.PrivilegeSQLService;
import com.nctigba.datastudio.model.query.PrivilegeSetQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.GRANT_PRIVILEGE;
import static com.nctigba.datastudio.constants.SqlConstants.GRANT_PRIVILEGE_WITH_OPTION;
import static com.nctigba.datastudio.constants.SqlConstants.REVOKE_PRIVILEGE;
import static com.nctigba.datastudio.constants.SqlConstants.REVOKE_PRIVILEGE_WITH_OPTION;

/**
 * PrivilegeSQLServiceImpl
 *
 * @author liupengfei
 * @since 2024/10/30
 */
@Service
public class PrivilegeSQLServiceImpl implements PrivilegeSQLService {
    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String getPrivilegeSql(PrivilegeSetQuery request) {
        StringJoiner checkOptionPrivilegeSJ = new StringJoiner(",");
        StringJoiner checkPrivilegeSJ = new StringJoiner(",");
        StringJoiner checkOptionSJ = new StringJoiner(",");
        for (PrivilegeSetQuery.PrivilegeOption option : request.getPrivilegeOption()) {
            if (option.getCheckPrivilege() && option.getCheckOption()) {
                checkOptionPrivilegeSJ.add(option.getPrivilege());
            } else if (option.getCheckPrivilege()) {
                checkPrivilegeSJ.add(option.getPrivilege());
            } else if (option.getCheckOption()) {
                checkOptionSJ.add(option.getPrivilege());
            } else {
                continue;
            }
        }
        StringJoiner objSJ = new StringJoiner(",");
        for (PrivilegeSetQuery.ObjValue o : request.getObj()) {
            String name = DebugUtils.needQuoteName(o.getName());
            String schema = DebugUtils.needQuoteName(o.getSchema());
            if (schema != null) {
                objSJ.add(schema + "." + name);
            } else {
                objSJ.add(name);
            }
        }
        StringJoiner userSJ = new StringJoiner(",");
        for (String u : request.getUser()) {
            userSJ.add(u);
        }
        return genPrivilegeSql(checkOptionPrivilegeSJ, checkPrivilegeSJ, checkOptionSJ, objSJ, userSJ, request);
    }

    private String genPrivilegeSql(StringJoiner checkOptionPrivilegeSJ,
                                   StringJoiner checkPrivilegeSJ,
                                   StringJoiner checkOptionSJ,
                                   StringJoiner objSJ,
                                   StringJoiner userSJ,
                                   PrivilegeSetQuery request) {
        String checkOptionPrivilege = checkOptionPrivilegeSJ.toString();
        String checkPrivilege = checkPrivilegeSJ.toString();
        String obj = objSJ.toString();
        String user = userSJ.toString();
        StringJoiner sql = new StringJoiner("\n");
        if (!"TABLE".equalsIgnoreCase(request.getType()) && !"VIEW".equalsIgnoreCase(request.getType())) {
            obj = request.getType() + " " + obj;
        }
        if ("grant".equalsIgnoreCase(request.getGrantOrRevoke()) && checkOptionPrivilege.length() != 0) {
            sql.add(String.format(GRANT_PRIVILEGE_WITH_OPTION, checkOptionPrivilege, obj, user));
        }
        if ("grant".equalsIgnoreCase(request.getGrantOrRevoke()) && checkPrivilege.length() != 0) {
            sql.add(String.format(GRANT_PRIVILEGE, checkPrivilege, obj, user));
        }
        if ("revoke".equalsIgnoreCase(request.getGrantOrRevoke()) && (checkOptionPrivilege.length() != 0
                || checkPrivilege.length() != 0)) {
            StringJoiner sj = new StringJoiner(",");
            if (checkOptionPrivilege.length() != 0) {
                sj.add(checkOptionPrivilege);
            }
            if (checkPrivilege.length() != 0) {
                sj.add(checkPrivilege);
            }
            sql.add(String.format(REVOKE_PRIVILEGE, sj, obj, user));
        }
        String checkOption = checkOptionSJ.toString();
        if ("revoke".equalsIgnoreCase(request.getGrantOrRevoke()) && (checkOptionPrivilege.length() != 0
                || checkOption.length() != 0)) {
            StringJoiner sj = new StringJoiner(",");
            if (checkOptionPrivilege.length() != 0) {
                sj.add(checkOptionPrivilege);
            }
            if (checkOption.length() != 0) {
                sj.add(checkOption);
            }
            sql.add(String.format(REVOKE_PRIVILEGE_WITH_OPTION, sj, obj, user));
        }
        return sql.toString();
    }
}
