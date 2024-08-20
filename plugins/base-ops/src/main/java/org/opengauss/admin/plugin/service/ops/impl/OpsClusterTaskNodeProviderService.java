/*
 * Copyright (c) 2024-2024 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsClusterTaskNodeProviderService.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/OpsClusterTaskNodeProviderService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskNodeDTO;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskService;
import org.opengauss.admin.plugin.service.ops.impl.function.CheckDeployTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * OpsClusterTaskNodeProviderService
 *
 * @author wangchao
 * @since  2024/6/22 9:41
 **/
@Service("clusterTaskNodeProvider")
public class OpsClusterTaskNodeProviderService {
    @Resource
    private IOpsClusterTaskNodeService opsClusterTaskNodeService;
    @Resource
    private IOpsClusterTaskService opsClusterTaskService;
    @Resource
    private CheckDeployTypeService checkDeployTypeService;


    /**
     * create cluster task node
     *
     * @param insertDto insertDto
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public String createClusterTaskNode(OpsClusterTaskNodeDTO insertDto) {
        Assert.isTrue(Objects.nonNull(insertDto), "cluster task node cannot be null");
        String clusterId = insertDto.getClusterId();
        OpsClusterTaskEntity taskEntity = opsClusterTaskService.getById(clusterId);
        Assert.isTrue(Objects.nonNull(taskEntity), "cluster task id can not exists " + clusterId);
        String clusterNodeId = opsClusterTaskNodeService.saveClusterTaskNode(insertDto);
        opsClusterTaskService.modifyClusterNodeCount(clusterId, taskEntity.getClusterNodeNum() + 1);
        opsClusterTaskService.resetTaskStatusDraft(clusterId);
        return clusterNodeId;
    }

    /**
     * update cluster task node
     *
     * @param updateDto updateDto
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public String updateClusterTaskNode(OpsClusterTaskNodeDTO updateDto) {
        Assert.isTrue(Objects.nonNull(updateDto), "cluster task update param cannot be null");
        String clusterId = updateDto.getClusterId();
        Assert.isTrue(StrUtil.isNotEmpty(clusterId), "cluster id cannot be empty");
        OpsClusterTaskEntity taskEntity = opsClusterTaskService.getById(clusterId);
        Assert.isTrue(Objects.nonNull(taskEntity), "cluster task id is not exists " + clusterId);
        String clusterNodeId = updateDto.getClusterNodeId();
        OpsClusterTaskNodeEntity node = opsClusterTaskNodeService.getById(clusterNodeId);
        Assert.isTrue(Objects.nonNull(node), "cluster node is not exists " + clusterNodeId);
        String msg = opsClusterTaskNodeService.updateClusterTaskNode(updateDto);
        opsClusterTaskService.resetTaskStatusDraft(clusterId);
        return msg;
    }

    /**
     * delete cluster task node
     *
     * @param clusterId cluster id
     * @param nodeId    node id
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public String deleteClusterTaskNode(String clusterId, String nodeId) {
        Assert.isTrue(StrUtil.isNotEmpty(clusterId), "cluster  id cannot be empty");
        Assert.isTrue(StrUtil.isNotEmpty(nodeId), "cluster node id cannot be empty");
        OpsClusterTaskEntity taskEntity = opsClusterTaskService.getById(clusterId);
        Assert.isTrue(Objects.nonNull(taskEntity), "cluster task id can not exists " + clusterId);
        int nodeCount = taskEntity.getClusterNodeNum() - 1;
        checkDeployTypeService.check(taskEntity.getVersion(), taskEntity.getDeployType(), nodeCount);
        String msg = opsClusterTaskNodeService.deleteClusterTaskNode(clusterId, nodeId);
        opsClusterTaskService.modifyClusterNodeCount(clusterId, nodeCount);
        opsClusterTaskService.resetTaskStatusDraft(clusterId);
        return msg;
    }
}
