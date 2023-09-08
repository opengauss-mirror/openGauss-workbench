/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.instance.constants.CommonConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterManager {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    @Autowired(required = false)
    @AutowiredType(Type.MAIN_PLUGIN)
    private OpsFacade opsFacade;
    @Autowired(required = false)
    @AutowiredType(Type.MAIN_PLUGIN)
    private HostFacade hostFacade;
    @Autowired(required = false)
    @AutowiredType(Type.MAIN_PLUGIN)
    private IOpsClusterService opsClusterService;

    /**
     * getClusterByNodeId
     *
     * @param nodeId nodeId
     * @return OpsClusterEntity
     */
    public OpsClusterEntity getClusterByNodeId(String nodeId) {
        List<OpsClusterVO> opsClusterVOList = getAllOpsCluster();
        if (CollectionUtils.isEmpty(opsClusterVOList)) {
            throw new CustomException(CommonConstants.NODE_NOT_FOUND);
        }
        for (OpsClusterVO cluster : opsClusterVOList) {
            List<OpsClusterNodeVO> nodes = cluster.getClusterNodes();
            if (CollectionUtils.isEmpty(nodes)) {
                continue;
            }
            for (OpsClusterNodeVO clusterNode : nodes) {
                if (nodeId.equals(clusterNode.getNodeId())) {
                    return opsClusterService.getById(cluster.getClusterId());
                }
            }
        }
        throw new CustomException(CommonConstants.NODE_NOT_FOUND);
    }

    public String getNodeIdByCluster(String clusterId, String hostId) {
        for (var vo : getAllOpsCluster()) {
            if (!vo.getClusterId().equals(clusterId)) {
                continue;
            }
            for (var node : vo.getClusterNodes()) {
                if (node.getHostId().equals(hostId)) {
                    return node.getNodeId();
                }
            }
        }
        throw new CustomException(CommonConstants.NODE_NOT_FOUND);
    }

    public Connection getConnectionByClusterHost(String clusterId, String hostId) {
        var clusterEntity = opsClusterService.getById(clusterId);
        var hostEntity = hostFacade.getById(hostId);
        String sourceURL = CommonConstants.JDBC_OPENGAUSS + hostEntity.getPublicIp() + ":" + clusterEntity.getPort()
                + "/postgres";
        Properties info = new Properties();
        info.setProperty("user", clusterEntity.getDatabaseUsername());
        info.setProperty("password", clusterEntity.getDatabasePassword());
        try {
            return DriverManager.getConnection(sourceURL, info);
        } catch (SQLException e) {
            throw new CustomException("connection fail", e);
        }
    }

    /**
     * Set the current data source and manually clear it
     *
     * @see com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder#push(String)
     * @see com.nctigba.observability.instance.service.ClusterManager#pool()
     */
    public void setCurrentDatasource(String nodeId) {
        setCurrentDatasource(nodeId, null);
    }

    /**
     * Set the current data source and manually clear it
     *
     * @see com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder#push(String)
     * @see com.nctigba.observability.instance.service.ClusterManager#pool()
     */
    public void setCurrentDatasource(String nodeId, String dbname) {
        if (StringUtils.isBlank(nodeId))
            return;
        var ds = (DynamicRoutingDataSource) dataSource;
        // Switch if data source exists
        if (ds.getDataSources().containsKey(nodeId)) {
            DynamicDataSourceContextHolder.push(nodeId);
            return;
        }
        // Add if it does not exist
        var node = getOpsNodeById(nodeId);
        if (node == null)
            throw new RuntimeException(CommonConstants.NODE_NOT_FOUND);
        if (StringUtils.isBlank(dbname))
            dbname = node.getDbName();
        ds.addDataSource(nodeId, dataSourceCreator.createDataSource(new DataSourceProperty()
                .setDriverClassName("org.opengauss.Driver")
                .setUrl(CommonConstants.JDBC_OPENGAUSS + node.getPublicIp() + ":" + node.getDbPort() + "/" + dbname)
                .setUsername(node.getDbUser()).setPassword(node.getDbUserPassword())));
        DynamicDataSourceContextHolder.push(nodeId);
    }

    public void pool() {
        DynamicDataSourceContextHolder.poll();
    }

    /**
     * Get all cluster information
     */
    public List<OpsClusterVO> getAllOpsCluster() {
        try {
            if (opsFacade != null)
                return opsFacade.listCluster();
        } catch (Exception e) {
            log.info("get all ops cluster fail:{}", e.getMessage());
        }
        return Collections.emptyList();
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class OpsClusterNodeVOSub extends OpsClusterNodeVO {
        private String version;

        public OpsClusterNodeVOSub(OpsClusterNodeVO opsClusterNodeVO, String version) {
            BeanUtils.copyProperties(opsClusterNodeVO, this);
            this.version = version;
        }
    }

    /**
     * Get the specified node information
     */
    public OpsClusterNodeVOSub getOpsNodeById(String nodeId) {
        List<OpsClusterVO> opsClusterVOList = getAllOpsCluster();
        if (CollectionUtils.isEmpty(opsClusterVOList))
            throw new CustomException(CommonConstants.NODE_NOT_FOUND);
        for (OpsClusterVO cluster : opsClusterVOList) {
            List<OpsClusterNodeVO> nodes = cluster.getClusterNodes();
            if (CollectionUtils.isEmpty(nodes))
                continue;
            for (OpsClusterNodeVO clusterNode : nodes) {
                if (nodeId.equals(clusterNode.getNodeId())) {
                    return new OpsClusterNodeVOSub(clusterNode, cluster.getVersion());
                }
            }
        }
        throw new CustomException(CommonConstants.NODE_NOT_FOUND);
    }
}