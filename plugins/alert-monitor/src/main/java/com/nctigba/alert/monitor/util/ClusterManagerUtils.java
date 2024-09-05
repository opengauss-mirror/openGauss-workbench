/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ClusterManagerUtils.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/util/ClusterManagerUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.util;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  set dataSource by cluster node
 *
 * @since 2023/12/11 09:24
 */
@Component
@Slf4j
public class ClusterManagerUtils {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    @Autowired(required = false)
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private OpsFacade opsFacade;
    @Autowired(required = false)
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private HostFacade hostFacade;
    @Autowired(required = false)
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private IOpsClusterService opsClusterService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * Set the current data source and manually clear it
     *
     * @param nodeId nodeId
     */
    public void setCurrentDatasource(String nodeId) {
        setCurrentDatasource(nodeId, null);
    }

    /**
     * Set the current data source and manually clear it
     *
     * @param nodeId nodeId
     * @param dbname database name
     */
    public void setCurrentDatasource(String nodeId, String dbname) {
        if (StringUtils.isBlank(nodeId)) {
            return;
        }
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        // Switch if data source exists
        if (ds.getDataSources().containsKey(nodeId)) {
            DynamicDataSourceContextHolder.push(nodeId);
            return;
        }
        // Add if it does not exist
        OpsClusterNodeVOSub node = getOpsNodeById(nodeId);
        if (node == null) {
            throw new CustomException(CommonConstants.NODE_NOT_FOUND);
        }
        if (StringUtils.isBlank(dbname)) {
            dbname = node.getDbName();
        }
        ds.addDataSource(nodeId, dataSourceCreator.createDataSource(new DataSourceProperty()
            .setDriverClassName("org.opengauss.Driver")
            .setUrl(CommonConstants.JDBC_OPENGAUSS + node.getPublicIp() + ":" + node.getDbPort() + "/" + dbname)
            .setUsername(node.getDbUser()).setPassword(encryptionUtils.decrypt(node.getDbUserPassword()))));
        DynamicDataSourceContextHolder.push(nodeId);
    }

    /**
     * pool
     */
    public void pool() {
        DynamicDataSourceContextHolder.poll();
    }

    /**
     * Get all cluster information
     *
     * @return List<OpsClusterVO>
     */
    private List<OpsClusterVO> getAllOpsCluster() {
        if (opsFacade != null) {
            return opsFacade.listCluster();
        }
        return Collections.emptyList();
    }

    /**
     * OpsClusterNodeVOSub
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class OpsClusterNodeVOSub extends OpsClusterNodeVO {
        private String version;

        /**
         * OpsClusterNodeVOSub
         *
         * @param opsClusterNodeVO OpsClusterNodeVO
         * @param version String
         */
        public OpsClusterNodeVOSub(OpsClusterNodeVO opsClusterNodeVO, String version) {
            BeanUtils.copyProperties(opsClusterNodeVO, this);
            this.version = version;
        }
    }

    /**
     * Get the specified node information
     *
     * @param nodeId String
     * @return OpsClusterNodeVOSub
     */
    private OpsClusterNodeVOSub getOpsNodeById(String nodeId) {
        List<OpsClusterVO> opsClusterVOList = getAllOpsCluster();
        if (CollectionUtils.isEmpty(opsClusterVOList)) {
            throw new CustomException(CommonConstants.NODE_NOT_FOUND);
        }
        for (OpsClusterVO cluster : opsClusterVOList) {
            List<OpsClusterNodeVO> nodes = cluster.getClusterNodes();
            if (CollectionUtils.isEmpty(nodes)) {
                continue;
            }
            List<OpsClusterNodeVO> nodeList = nodes.stream().filter(item -> item.getNodeId().equals(nodeId)).collect(
                Collectors.toList());
            if (CollectionUtils.isEmpty(nodeList)) {
                continue;
            }
            return new OpsClusterNodeVOSub(nodeList.get(0), cluster.getVersion());
        }
        throw new CustomException(CommonConstants.NODE_NOT_FOUND);
    }
}
