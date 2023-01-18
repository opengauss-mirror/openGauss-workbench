package com.nctigba.observability.instance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.dto.topsql.TopSQLInfoReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.factory.TopSQLHandlerFactory;
import com.nctigba.observability.instance.handler.topsql.TopSQLHandler;
import com.nctigba.observability.instance.model.InstanceNodeInfo;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import com.nctigba.observability.instance.service.TopSQLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * TopSQL services
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 15:46
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopSQLServiceImpl implements TopSQLService {
    private final TopSQLHandlerFactory topSQLHandlerFactory;

    @Autowired
    private ClusterManager opsFacade;

    @Override
    public boolean testConnection(String nodeId) {
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(nodeId);
        try {
            return handler.getConnection(instanceNodeInfo) != null;
        } catch (Exception e) {
            log.error("test connection exception: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<JSONObject> getTopSQLList(TopSQLListReq topSQLListReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLListReq.getId());
        // query TopSQL list
        return handler.getTopSQLList(instanceNodeInfo, topSQLListReq);
    }

    @Override
    public JSONObject getStatisticalInfo(TopSQLInfoReq topSQLDetailReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLDetailReq.getId());
        // query TopSQL statistical information
        return handler.getStatisticalInfo(instanceNodeInfo, topSQLDetailReq.getSqlId());
    }

    @Override
    public JSONObject getExecutionPlan(TopSQLInfoReq topSQLPlanReq, String type) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLPlanReq.getId());
        // query TopSQL statistical information
        return handler.getExecutionPlan(instanceNodeInfo, topSQLPlanReq.getSqlId(), type);
    }

    @Override
    public List<JSONObject> getPartitionList(TopSQLInfoReq topSQLPartitionReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLPartitionReq.getId());
        // query TopSQL statistical information
        return handler.getPartitionList(instanceNodeInfo, topSQLPartitionReq.getSqlId());
    }

    @Override
    public List<String> getIndexAdvice(TopSQLInfoReq topSQLIndexReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLIndexReq.getId());
        // query TopSQL statistical information
        return handler.getIndexAdvice(instanceNodeInfo, topSQLIndexReq.getSqlId());
    }

    @Override
    public JSONObject getObjectInfo(TopSQLInfoReq topSQLObjectReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLObjectReq.getId());
        // query TopSQL statistical information
        return handler.getObjectInfo(instanceNodeInfo, topSQLObjectReq.getSqlId());
    }

    @Override
    public List<OpsClusterVO> cluster() {
        return opsFacade.getAllOpsCluster();
    }

    @Override
    public OpsClusterNodeVOSub clusterNode(String nodeId) {
        return opsFacade.getOpsNodeById(nodeId);
    }

    /**
     * Query instance node information
     * @param nodeId instance node id
     * @return Instance node information
     */
    public InstanceNodeInfo queryNodeInfo(String nodeId) {
        OpsClusterNodeVO opsClusterNode = opsFacade.getOpsNodeById(nodeId);
        InstanceNodeInfo instanceNodeInfo = new InstanceNodeInfo();
        instanceNodeInfo.setIp(opsClusterNode.getPublicIp());
        instanceNodeInfo.setPort(opsClusterNode.getDbPort());
        instanceNodeInfo.setDbName(opsClusterNode.getDbName());
        instanceNodeInfo.setDbUser(opsClusterNode.getDbUser());
        instanceNodeInfo.setDbUserPassword(opsClusterNode.getDbUserPassword());
        instanceNodeInfo.setDbType(DatabaseType.DEFAULT.getDbType());
        return instanceNodeInfo;
    }
}
