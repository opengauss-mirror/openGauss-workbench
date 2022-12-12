package com.nctigba.observability.instance.handler.instance;


import javax.sql.DataSource;

import com.nctigba.observability.instance.model.InstanceNodeInfo;

public interface InstanceHandler {
    /**
     * Database Type
     *
     * @return String 数据库类型
     */
    String getDatabaseType();

    /**
     * Test data connection status
     *
     * @param dataSource data source
     * @param testSql    Test sql
     * @return boolean Connection Test Results
     */
    boolean testConnectStatus(DataSource dataSource, String testSql);


    /**
     * 根据节点信息获取datasource
     *
     * @param nodeInfo Node Information
     * @return DataSource data source
     */
    DataSource getDataSource(InstanceNodeInfo nodeInfo);
}
