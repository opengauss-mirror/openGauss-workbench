/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.PrivilegeSetQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

/**
 * PrivilegeSQLService
 *
 * @author liupengfei
 * @since 2024/10/30
 */
public interface PrivilegeSQLService {
    /**
     * database type
     *
     * @return String database type
     */
    String type();

    /**
     * getPrivilegeSql
     *
     * @param request PrivilegeSetQuery
     * @return String sql
     */
    default String getPrivilegeSql(PrivilegeSetQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
