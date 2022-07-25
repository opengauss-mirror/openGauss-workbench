package com.nctigba.datastudio.service;

public interface MetaDataByJdbcService {
    void testQuerySQL(String jdbcUrl, String userName, String password, String sql) throws Exception;
}
