/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterTaskNodeServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsClusterTaskNodeServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskNodeDTO;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterTaskNodeMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskNodeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * OpsClusterTaskNodeServiceImpl
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@Slf4j
@Service
public class OpsClusterTaskNodeServiceImpl extends ServiceImpl<OpsClusterTaskNodeMapper, OpsClusterTaskNodeEntity>
        implements IOpsClusterTaskNodeService {
    @Override
    public List<OpsClusterTaskNodeEntity> listByClusterTaskId(String taskId) {
        return list(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .eq(OpsClusterTaskNodeEntity::getClusterId, taskId));
    }

    @Override
    public List<OpsClusterTaskNodeEntity> listByInstallHostUser(String hostId, String hostUserId) {
        return list(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .eq(OpsClusterTaskNodeEntity::getHostId, hostId)
                .eq(OpsClusterTaskNodeEntity::getHostUserId, hostUserId));
    }

    @Override
    public void removeByClusterId(String taskId) {
        remove(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class).eq(OpsClusterTaskNodeEntity::getClusterId, taskId));
    }

    @Override
    public boolean checkHostPortUsedByCm(String taskId, String hostId, Integer hostPort) {
        List<OpsClusterTaskNodeEntity> cmList = list(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .select(OpsClusterTaskNodeEntity::getClusterId)
                .notIn(StrUtil.isNotEmpty(taskId), OpsClusterTaskNodeEntity::getClusterId, taskId)
                .eq(OpsClusterTaskNodeEntity::getHostId, hostId)
                .eq(OpsClusterTaskNodeEntity::getCmPort, hostPort));
        return CollUtil.isNotEmpty(cmList);
    }


    @Override
    public String deleteClusterTaskNode(String clusterId, String clusterNodeId) {
        removeById(clusterNodeId);
        String delNodeMessage = "cluster %s delete node %s success";
        return String.format(delNodeMessage, clusterId, clusterNodeId);
    }


    @Override
    public String updateClusterTaskNode(OpsClusterTaskNodeDTO updateDto) {
        LambdaUpdateWrapper<OpsClusterTaskNodeEntity> updateWrapper =
                Wrappers.lambdaUpdate(OpsClusterTaskNodeEntity.class);
        updateWrapper.set(OpsClusterTaskNodeEntity::getHostId, updateDto.getHostId())
                .set(OpsClusterTaskNodeEntity::getHostUserId, updateDto.getHostUserId())
                .set(OpsClusterTaskNodeEntity::getNodeType, updateDto.getNodeType())
                .set(OpsClusterTaskNodeEntity::getAzOwner, updateDto.getAzOwner())
                .set(OpsClusterTaskNodeEntity::getCmPort, updateDto.getCmPort())
                .set(OpsClusterTaskNodeEntity::getCmDataPath, updateDto.getCmDataPath())
                .set(OpsClusterTaskNodeEntity::getDataPath, updateDto.getDataPath())
                .set(OpsClusterTaskNodeEntity::getAzPriority, updateDto.getAzPriority())
                .set(OpsClusterTaskNodeEntity::getIsCmMaster, updateDto.getIsCMMaster())
                .set(OpsClusterTaskNodeEntity::getEnvCheckDetail, null)
                .set(OpsClusterTaskNodeEntity::getEnvCheckResult, null)
                .set(OpsClusterTaskNodeEntity::getUpdateTime, LocalDateTime.now())
                .eq(OpsClusterTaskNodeEntity::getClusterNodeId, updateDto.getClusterNodeId());
        update(updateWrapper);
        String updateNodeMessage = " cluster %s update node %s success";
        return String.format(updateNodeMessage, updateDto.getClusterId(), updateDto.getClusterNodeId());
    }

    @Override
    public String saveClusterTaskNode(OpsClusterTaskNodeDTO insertDto) {
        OpsClusterTaskNodeEntity nodeEntity = insertDto.toEntity();
        save(nodeEntity);
        return nodeEntity.getClusterNodeId();
    }

    @Override
    public List<OpsClusterTaskNodeEntity> queryClusterNodeAndPortList(String clusterId) {
        List<OpsClusterTaskNodeEntity> nodeList = list(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .select(OpsClusterTaskNodeEntity::getClusterNodeId, OpsClusterTaskNodeEntity::getHostId,
                        OpsClusterTaskNodeEntity::getCmPort)
                .eq(OpsClusterTaskNodeEntity::getClusterId, clusterId));
        if (CollUtil.isEmpty(nodeList)) {
            return Collections.emptyList();
        }
        return nodeList;
    }
}
