package com.nctigba.observability.instance.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.topsql.TopSQLInfoReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;

public interface TopSQLService {

    /**
     * test instance is success
     * @param nodeId instance id
     * @return success or not
     */
    boolean testConnection(String nodeId);

    /**
     * Get TopSQL list
     * @param topSQLListReq TopSQLListReq
     * @return List of TopSQL information
     */
    List<JSONObject> getTopSQLList(TopSQLListReq topSQLListReq);

    /**
     * Get TopSQL statistical information
     * @param topSQLDetailReq detail dto
     * @return Statistical information
     */
    JSONObject getStatisticalInfo(TopSQLInfoReq topSQLDetailReq);

    /**
     * Get TopSQL exectuion plan
     * @param topSQLPlanReq plan dto
     * @return Execution plan
     */
    JSONObject getExecutionPlan(TopSQLInfoReq topSQLPlanReq, String type);

    /**
     * Get TopSQL Partition Infomation
     * @param topSQLPartitionReq plan dto
     * @return Partition List
     */
    List<JSONObject> getPartitionList(TopSQLInfoReq topSQLPartitionReq);

    /**
     * Get TopSQL index advice
     * @param topSQLIndexReq index dto
     * @return Index advice
     */
    List<String> getIndexAdvice(TopSQLInfoReq topSQLIndexReq);

    /**
     * Get TopSQL object information
     * @param topSQLObjectReq object dto
     * @return object information
     */
    JSONObject getObjectInfo(TopSQLInfoReq topSQLObjectReq);
}
