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

package com.nctigba.datastudio.service;

/**
 * MetaDataByJdbcService
 *
 * @since 2023-6-26
 */
public interface MetaDataByJdbcService {
    /**
     * test query sql
     *
     * @param jdbcUrl jdbcUrl
     * @param userName userName
     * @param password password
     * @param sql sql
     */
    void testQuerySQL(String jdbcUrl, String userName, String password, String sql);

    /**
     * version sql
     *
     * @param jdbcUrl jdbcUrl
     * @param userName userName
     * @param password password
     * @param sql sql
     * @return String
     */
    String versionSQL(String jdbcUrl, String userName, String password, String sql);
}
