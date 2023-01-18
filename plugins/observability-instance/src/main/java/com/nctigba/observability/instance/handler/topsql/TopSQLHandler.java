package com.nctigba.observability.instance.handler.topsql;

import java.sql.Connection;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.model.InstanceNodeInfo;

public interface TopSQLHandler {
    /**
     * target database type
     * @return database type
     */
    String getDatabaseType();

    /**
     * test instance is success
     * @param nodeInfo instance node info
     * @return Connection
     */
    Connection getConnection(InstanceNodeInfo nodeInfo);

    /**
     * fetch TopSQL list from database
     * @param nodeInfo instance node info
     * @param topSQLListReq dto
     * @return TopSQL list
     */
    List<JSONObject> getTopSQLList(InstanceNodeInfo nodeInfo, TopSQLListReq topSQLListReq);

    /**
     * fetch TopSQL statistical information from database
     * @param nodeInfo instance node info
     * @param sqlId TopSQL debug query id
     * @return TopSQL statistical information
     */
    JSONObject getStatisticalInfo(InstanceNodeInfo nodeInfo, String sqlId);

    /**
     * fetch TopSQL execution plan from database
     * @param nodeInfo instance node info
     * @param sqlId TopSQL debug query id
     * @return TopSQL execution plan
     */
    JSONObject getExecutionPlan(InstanceNodeInfo nodeInfo, String sqlId, String type);

    /**
     * fetch TopSQL Partition List from database
     * @param nodeInfo instance node info
     * @param sqlId TopSQL debug query id
     * @return TopSQL Partition List
     */
    List<JSONObject> getPartitionList(InstanceNodeInfo nodeInfo, String sqlId);

    /**
     * fetch TopSQL index advice from database
     * @param nodeInfo instance node info
     * @param sqlId TopSQL debug query id
     * @return TopSQL index advice
     */
    List<String> getIndexAdvice(InstanceNodeInfo nodeInfo, String sqlId);

    /**
     * fetch TopSQL object information from database
     * @param nodeInfo instance node info
     * @param sqlId TopSQL debug query id
     * @return TopSQL object information
     */
    JSONObject getObjectInfo(InstanceNodeInfo nodeInfo, String sqlId);
}
