package com.nctigba.observability.instance.service;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.topsql.TopSQLInfoReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.List;

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

    /**
     * Get cluster list
     * @return OpsClusterVO List
     */
    List<OpsClusterVO> cluster();

    /**
     * Get cluster node
     * @param nodeId
     * @return OpsClusterNodeVO
     */
    OpsClusterNodeVOSub clusterNode(String nodeId);
}
