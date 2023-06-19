/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

public interface MetaDataByJdbcService {
    void testQuerySQL(String jdbcUrl, String userName, String password, String sql) throws Exception;

    String versionSQL(String jdbcUrl, String userName, String password, String sql) throws Exception;
}
