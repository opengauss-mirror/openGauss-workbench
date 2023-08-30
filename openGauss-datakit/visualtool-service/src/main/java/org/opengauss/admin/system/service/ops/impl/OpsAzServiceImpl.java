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
 * OpsAzServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsAzServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.mapper.ops.OpsAzMapper;
import org.opengauss.admin.system.service.ops.IOpsAzService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author lhf
 * @date 2022/8/6 20:50
 **/
@Service
public class OpsAzServiceImpl extends ServiceImpl<OpsAzMapper, OpsAzEntity> implements IOpsAzService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(OpsAzEntity az) {
        if (hasName(az.getName())) {
            throw new OpsException("name already exists");
        }
        return save(az);
    }

    @Override
    public boolean hasName(String name) {
        LambdaQueryWrapper<OpsAzEntity> queryWrapper = Wrappers.lambdaQuery(OpsAzEntity.class)
                .eq(OpsAzEntity::getName, name);
        return count(queryWrapper) > 0;
    }
}
