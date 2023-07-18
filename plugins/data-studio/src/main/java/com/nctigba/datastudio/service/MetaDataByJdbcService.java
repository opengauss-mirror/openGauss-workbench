/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
