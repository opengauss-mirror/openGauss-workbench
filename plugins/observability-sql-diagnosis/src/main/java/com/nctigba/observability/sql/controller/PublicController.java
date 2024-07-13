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
 *  PublicController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/PublicController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.mapper.SysPluginsMapper;
import com.nctigba.observability.sql.model.entity.DictionaryConfigDO;
import com.nctigba.observability.sql.model.entity.SysPluginsDO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.DictionaryConfigServiceImpl;
import lombok.RequiredArgsConstructor;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sqlDiagnosis/api/v1")
@RequiredArgsConstructor
public class PublicController {
    private final ClusterManager clusterManager;
    private final DictionaryConfigServiceImpl dictionaryConfigServiceImpl;
    private final SysPluginsMapper sysPluginsMapper;

    /**
     * Retrieves a list of all cluster information.
     *
     * <p>This endpoint, accessed via a GET request to "/clusters", requires no parameters and returns a list of operational clusters managed by the cluster manager.
     *
     * @return A list containing details of all operational clusters.
     */
    @GetMapping("/clusters")
    public List<OpsClusterVO> listCluster() {
        return clusterManager.getAllOpsCluster();
    }

    /**
     * Retrieves the list of database instances associated with the specified cluster.
     *
     * @param nodeId Unique identifier for the target cluster.
     * @return A list of strings representing the names of all database instances within the specified cluster.
     */
    @GetMapping("/clusters/{nodeId}/instances")
    public List<String> datebaseList(@PathVariable String nodeId) {
        return clusterManager.databaseList(nodeId);
    }

    /**
     * Retrieves the list of database schemas for the specified cluster.
     *
     * @param nodeId Unique identifier of the cluster to query.
     * @return A list of string values representing the names of the database schemas within the specified cluster.
     */
    @GetMapping("/clusters/{nodeId}/schema")
    public AjaxResult schemaList(@PathVariable String nodeId) {
        return AjaxResult.success(clusterManager.schemaList(nodeId));
    }

    /**
     * Retrieves a mapping of configuration settings.
     *
     * @return A map object containing configuration information.
     */
    @GetMapping("/node-settings")
    public Map<String, Object> configList() {
        return dictionaryConfigServiceImpl.listFromCache();
    }

    /**
     * Updates configuration information for the specified node.
     *
     * @param nodeId  Node ID, a path variable used to identify the node whose configuration needs updating.
     * @param configs A list of configuration items, passed in the request body, containing the dictionary configurations to be set.
     * @return Returns the updated list of configuration items.
     */
    @PutMapping("/node-settings/{nodeid}")
    public List<DictionaryConfigDO> configSet(@PathVariable("nodeid") String nodeId,
            @RequestBody List<DictionaryConfigDO> configs) {
        configs.forEach(config -> dictionaryConfigServiceImpl.set(config.setNodeId(nodeId)));
        return configs;
    }

    /**
     * Retrieves the status information of a specific plugin instance.
     *
     * <p>This endpoint queries the system plugins table (SysPluginsDO) to check whether a record exists
     * for a particular plugin ID and status, thereby determining if the plugin instance is enabled.</p>
     *
     * @return An AjaxResult object containing the query result. If the plugin instance is found (i.e., it is enabled),
     * a successful result is returned; otherwise, a failure result is returned.
     */
    @GetMapping("/plugin/instance")
    public AjaxResult getInstance() {
        LambdaQueryWrapper<SysPluginsDO> wrapper = Wrappers.<SysPluginsDO>lambdaQuery()
                .eq(SysPluginsDO::getPluginId, "observability-instance")
                .eq(SysPluginsDO::getPluginStatus, "1");
        return AjaxResult.success(sysPluginsMapper.exists(wrapper));
    }
}