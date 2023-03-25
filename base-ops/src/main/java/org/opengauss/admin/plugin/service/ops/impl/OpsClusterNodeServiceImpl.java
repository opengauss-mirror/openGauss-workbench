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
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsClusterNodeServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterNodeMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author lhf
 * @date 2022/8/18 09:16
 **/
@Service
public class OpsClusterNodeServiceImpl extends ServiceImpl<OpsClusterNodeMapper, OpsClusterNodeEntity> implements IOpsClusterNodeService {
    @Override
    public List<OpsClusterNodeEntity> listClusterNodeByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<OpsClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterNodeEntity.class)
                .eq(OpsClusterNodeEntity::getClusterId, clusterId);
        return list(queryWrapper);
    }
}
