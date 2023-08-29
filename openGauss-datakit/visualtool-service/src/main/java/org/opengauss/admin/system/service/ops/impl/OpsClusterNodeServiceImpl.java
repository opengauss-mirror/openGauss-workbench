/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * OpsClusterNodeServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsClusterNodeServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.dto.ops.ClusterNodeDto;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.system.mapper.ops.OpsClusterNodeMapper;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/18 09:16
 **/
@Service
public class OpsClusterNodeServiceImpl extends ServiceImpl<OpsClusterNodeMapper, OpsClusterNodeEntity> implements IOpsClusterNodeService {

    @Autowired
    private IHostService hostService;
    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private IOpsClusterService opsClusterService;

    @Override
    public List<OpsClusterNodeEntity> listClusterNodeByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<OpsClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterNodeEntity.class)
                .eq(OpsClusterNodeEntity::getClusterId, clusterId);
        return list(queryWrapper);
    }

    @Override
    public long countByHostId(String hostId) {
        LambdaQueryWrapper<OpsClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterNodeEntity.class)
                .eq(OpsClusterNodeEntity::getHostId, hostId);
        return count(queryWrapper);
    }

    @Override
    public long countByHostUserId(String hostUserId) {
        LambdaQueryWrapper<OpsClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterNodeEntity.class)
                .eq(OpsClusterNodeEntity::getInstallUserId, hostUserId);
        return count(queryWrapper);
    }

    @Override
    public Map<String, List<OpsClusterNodeEntity>> listClusterNodeByClusterIds(List<String> clusterIds) {
        if (CollUtil.isEmpty(clusterIds)) {
            return Maps.newHashMap();
        }
        LambdaQueryWrapper<OpsClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterNodeEntity.class)
                .in(OpsClusterNodeEntity::getClusterId, clusterIds);

        return list(queryWrapper).stream().collect(Collectors.groupingBy(OpsClusterNodeEntity::getClusterId));
    }

    /**
     * get ClusterNodeDto info by clusterNodeId
     *
     * @param clusterNodeId clusterNodeId
     * @return ClusterNodeDto
     */
    @Override
    public ClusterNodeDto getClusterNodeDtoByNodeId(String clusterNodeId) {
        ClusterNodeDto nodeDto = new ClusterNodeDto();
        if (StringUtils.isNotEmpty(clusterNodeId)) {
            OpsClusterNodeEntity entity = this.getById(clusterNodeId);
            if (entity != null) {
                OpsClusterEntity cluster = opsClusterService.getById(entity.getClusterId());
                OpsHostEntity hostEntity = hostService.getById(entity.getHostId());
                OpsHostUserEntity hostUserEntity = hostUserService.getById(entity.getInstallUserId());
                if (cluster != null) {
                    nodeDto.setIsMiniVersion(OpenGaussVersionEnum.MINIMAL_LIST.name()
                            .equals(cluster.getVersion().name()));
                }
                nodeDto.setInstallPath(entity.getInstallPath());
                if (nodeDto.getIsMiniVersion()) {
                    nodeDto.setDataPath(entity.getDataPath() + "/single_node");
                } else {
                    nodeDto.setDataPath(entity.getDataPath());
                }
                if (hostEntity != null) {
                    nodeDto.setPublicIp(hostEntity.getPublicIp());
                    nodeDto.setPort(hostEntity.getPort());
                }
                if (hostUserEntity != null) {
                    nodeDto.setInstallUserName(hostUserEntity.getUsername());
                    nodeDto.setInstallPassword(hostUserEntity.getPassword());
                }
            }
        }
        return nodeDto;
    }
}
