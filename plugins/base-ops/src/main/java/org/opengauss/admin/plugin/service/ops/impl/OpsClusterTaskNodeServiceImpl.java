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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterTaskNodeMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskNodeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpsClusterTaskNodeServiceImpl
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
@Service
public class OpsClusterTaskNodeServiceImpl extends ServiceImpl<OpsClusterTaskNodeMapper, OpsClusterTaskNodeEntity> implements IOpsClusterTaskNodeService {
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
}
