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
 * HostFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/HostFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.dto.ops.ClusterNodeDto;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static cn.hutool.core.util.StrUtil.isNotEmpty;

/**
 * @author lhf
 * @date 2022/11/20 21:09
 **/
@Slf4j
@Service
public class HostFacade {
    @Autowired
    private IHostService hostService;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;

    public void add(HostBody hostBody) {
        hostService.add(hostBody);
    }

    public boolean ping(HostBody hostBody) {
        return hostService.ping(hostBody);
    }

    public boolean del(String hostId) {
        return hostService.del(hostId);
    }

    public boolean ping(String hostId, String rootPassword) {
        return hostService.ping(hostId, rootPassword);
    }

    public boolean edit(String hostId, HostBody hostBody) {
        return hostService.edit(hostId, hostBody);
    }

    public IPage<OpsHostVO> pageHost(Page page, String name) {
        return hostService.pageHost(page, name, null, null);
    }

    public OpsHostEntity getById(String id) {
        return hostService.getById(id);
    }

    public List<OpsHostEntity> listByIds(Collection ids) {
        return hostService.listByIds(ids);
    }

    public Long count() {
        return hostService.count();
    }

    public List<OpsHostEntity> listAll() {
        return hostService.listAll(null);
    }

    /**
     * get host list
     *
     * @param hostname hostname
     * @param publicIp public ip
     * @return host list
     */
    public List<OpsHostEntity> getHostList(String hostname, String publicIp) {
        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class);
        queryWrapper.like(isNotEmpty(hostname), OpsHostEntity::getHostname, hostname);
        queryWrapper.like(isNotEmpty(publicIp), OpsHostEntity::getPublicIp, publicIp);
        return hostService.list(queryWrapper);
    }

    /**
     * query host list by condition
     *
     * @param os os
     * @param osVersion osVersion
     * @param cpuArch cpuArch
     * @return host list
     */
    public List<OpsHostEntity> getHostList(String os, String osVersion, String cpuArch) {
        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class);
        queryWrapper.eq(isNotEmpty(os), OpsHostEntity::getOs, os);
        queryWrapper.eq(isNotEmpty(osVersion), OpsHostEntity::getOsVersion, osVersion);
        queryWrapper.eq(isNotEmpty(cpuArch), OpsHostEntity::getCpuArch, cpuArch);
        List<OpsHostEntity> list = hostService.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                item.setDisplayIp(item.getDisplayIp());
            });
        }
        return list;
    }

    /**
     * get ClusterNodeDto info by clusterNodeId
     *
     * @param clusterNodeId clusterNodeId
     * @return ClusterNodeDto
     */
    public ClusterNodeDto getClusterNodeByNodeId(String clusterNodeId) {
        return opsClusterNodeService.getClusterNodeDtoByNodeId(clusterNodeId);
    }

    /**
     * get host info
     *
     * @param hostIp host public Ip
     * @return host
     */
    public OpsHostEntity getByPublicIp(String hostIp) {
        return hostService.getByPublicIp(hostIp);
    }
}
