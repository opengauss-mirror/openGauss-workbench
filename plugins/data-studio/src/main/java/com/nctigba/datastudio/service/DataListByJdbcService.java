/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DataListDTO;

import java.sql.SQLException;

/**
 * DataListByJdbcService
 *
 * @since 2023-6-26
 */
public interface DataListByJdbcService {
    /**
     * query data list sql
     *
     * @param jdbcUrl jdbcUrl
     * @param username username
     * @param password password
     * @param tableSql tableSql
     * @param viewSql viewSql
     * @param fun_prosSql fun_prosSql
     * @param sequenceSql sequenceSql
     * @param synonymSql synonymSql
     * @param foreignTableSql foreignTableSql
     * @param triggerSql triggerSql
     * @param schema_name schema_name
     * @return DataListDTO
     * @throws SQLException SQLException
     * @throws InterruptedException InterruptedException
     */
    DataListDTO dataListQuerySQL(
            String jdbcUrl, String username, String password, String tableSql, String viewSql, String fun_prosSql,
            String sequenceSql, String synonymSql, String foreignTableSql, String triggerSql,
            String schema_name) throws SQLException, InterruptedException;
}
