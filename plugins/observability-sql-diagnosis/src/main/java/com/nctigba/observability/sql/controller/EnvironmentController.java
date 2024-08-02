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
 *  EnvironmentController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/EnvironmentController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.vo.AgentClusterNodeVO;
import com.nctigba.observability.sql.model.vo.AgentClusterVO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/observability/v1/environment")
public class EnvironmentController {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/basePath")
    public String basePath() {
        var full = EnvironmentController.class.getResource(EnvironmentController.class.getSimpleName() + ".class");
        var path = full.getPath();
        int jarIndex = path.indexOf(".jar");
        int lastSlashIndex = path.lastIndexOf(File.separator, jarIndex);
        int preSlashIndex = path.lastIndexOf(File.separator, lastSlashIndex - 1);
        return path.substring("file:".length(), preSlashIndex + 1);
    }

    /**
     * Select all agent
     *
     * @return List
     */
    @GetMapping("/agent")
    public List<AgentClusterVO> listAgent() {
        var env = envMapper.selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType, List.of(
                NctigbaEnvDO.envType.AGENT)));
        var clusters = clusterManager.getAllOpsCluster();
        env.forEach(e -> {
            if (e.getNodeid() == null) {
                clusters.forEach(c -> {
                    for (var node : c.getClusterNodes()) {
                        if (node.getHostId().equals(e.getHostid())) {
                            e.setNodeid(node.getNodeId());
                            return;
                        }
                    }
                });
                if (e.getNodeid() != null) {
                    envMapper.updateById(e);
                }
            }
        });
        var hosts = env.stream().map(NctigbaEnvDO::getNodeid).collect(Collectors.toSet());
        List<OpsClusterVO> clusterVOList = clusters.stream().filter(c -> {
            var nodes = c.getClusterNodes().stream().filter(n -> hosts.contains(n.getNodeId())).collect(
                    Collectors.toList());
            c.setClusterNodes(nodes);
            return nodes.size() > 0;
        }).collect(Collectors.toList());
        return reBuildTree(clusterVOList, env);
    }

    private List<AgentClusterVO> reBuildTree(List<OpsClusterVO> clusterVOList, List<NctigbaEnvDO> env) {
        List<AgentClusterVO> agentClusterVOList = new ArrayList<>();
        for (OpsClusterVO opsClusterVO : clusterVOList) {
            AgentClusterVO agentClusterVO = new AgentClusterVO();
            agentClusterVO.setClusterId(opsClusterVO.getClusterId());
            agentClusterVO.setClusterName(opsClusterVO.getClusterName());
            agentClusterVO.setDbType(opsClusterVO.getDeployType());
            List<AgentClusterNodeVO> agentClusterNodeVOList = new ArrayList<>();
            for (OpsClusterNodeVO opsClusterNodeVO : opsClusterVO.getClusterNodes()) {
                AgentClusterNodeVO agentClusterNodeVO = new AgentClusterNodeVO();
                agentClusterNodeVO.setNodeId(opsClusterNodeVO.getNodeId());
                agentClusterNodeVO.setHostId(opsClusterNodeVO.getHostId());
                agentClusterNodeVO.setClusterRole(opsClusterNodeVO.getClusterRole());
                agentClusterNodeVO.setDbPort(opsClusterNodeVO.getDbPort());
                agentClusterNodeVO.setPublicIp(opsClusterNodeVO.getPublicIp());
                env.forEach(f -> {
                    if (f.getNodeid().equals(opsClusterNodeVO.getNodeId()) && f.getHostid().equals(
                            opsClusterNodeVO.getHostId())) {
                        agentClusterNodeVO.setId(f.getId());
                    }
                });
                agentClusterNodeVOList.add(agentClusterNodeVO);
            }
            agentClusterVO.setClusterNodes(agentClusterNodeVOList);
            agentClusterVOList.add(agentClusterVO);
        }
        return agentClusterVOList;
    }

    @GetMapping("/hosts")
    public List<OpsHostEntity> hosts() {
        return hostFacade.listAll();
    }

    /**
     * Get host user
     *
     * @param nodeId nodeId
     * @return List
     */
    @GetMapping("/hostUser/{nodeId}")
    public AjaxResult hostUser(@PathVariable String nodeId) {
        var node = clusterManager.getOpsNodeById(nodeId);
        var hostId = node.getHostId();
        return AjaxResult.success(hostUserFacade.listHostUserByHostId(hostId));
    }
}