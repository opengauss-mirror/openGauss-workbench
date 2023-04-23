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
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsCheckService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import org.opengauss.admin.common.core.domain.entity.ops.OpsCheckEntity;
import org.opengauss.admin.system.mapper.ops.OpsCheckMapper;
import org.opengauss.admin.system.service.ops.IOpsCheckService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public Map<String, OpsCheckEntity> mapLastResByClusterIds(List<String> clusterIds) {
        if (CollUtil.isEmpty(clusterIds)) {
            return Maps.newHashMap();
        }

        LambdaQueryWrapper<OpsCheckEntity> queryWrapper = Wrappers.lambdaQuery(OpsCheckEntity.class)
                .in(OpsCheckEntity::getClusterId, clusterIds);

        List<OpsCheckEntity> list = list(queryWrapper);

        Map<String, OpsCheckEntity> res = new HashMap<>();
        Map<String, List<OpsCheckEntity>> map = new HashMap<>();
        for (OpsCheckEntity opsCheckEntity : list) {
            String clusterId = opsCheckEntity.getClusterId();
            List<OpsCheckEntity> opsCheckEntities = map.get(clusterId);
            if (Objects.isNull(opsCheckEntities)){
                opsCheckEntities = new ArrayList<>();
                map.put(clusterId, opsCheckEntities);
            }

            opsCheckEntities.add(opsCheckEntity);
        }

        map.forEach((k,v)->{
            v.sort(Comparator.comparing(OpsCheckEntity::getCreateTime).reversed());
            res.put(k, v.get(0));
        });
        return res;
    }
}
