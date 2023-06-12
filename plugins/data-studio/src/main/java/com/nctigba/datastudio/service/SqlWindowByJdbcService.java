/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DataListDTO;

import java.util.List;

public interface SqlWindowByJdbcService {

    List<String> schemaListQuerySQL(String jdbcUrl, String username, String password, String sql) throws Exception;

    DataListDTO dataListQuerySQL(String jdbcUrl, String username, String password, String tableSql,
                                 String viewSql, String fun_prosSql, String schema_name) throws Exception;
}
