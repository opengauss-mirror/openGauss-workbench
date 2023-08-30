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
 * OpsCheckService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsCheckService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.ops.OpsCheckEntity;
import org.opengauss.admin.plugin.mapper.ops.OpsCheckMapper;
import org.opengauss.admin.plugin.service.ops.IOpsCheckService;
import org.springframework.stereotype.Service;

/**
 * @author lhf
 * @date 2022/11/13 17:17
 **/
@Service
public class OpsCheckService extends ServiceImpl<OpsCheckMapper, OpsCheckEntity> implements IOpsCheckService {
    @Override
    public OpsCheckEntity getLastResByClusterId(String clusterId) {
        LambdaQueryWrapper<OpsCheckEntity> queryWrapper = Wrappers.lambdaQuery(OpsCheckEntity.class)
                .eq(OpsCheckEntity::getClusterId, clusterId)
                .orderByDesc(OpsCheckEntity::getCreateTime);

        return getOne(queryWrapper, false);
    }
}
