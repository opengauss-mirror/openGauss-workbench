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

import java.util.List;
import java.util.Map;
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

        return list(queryWrapper).stream().collect(Collectors.toMap(OpsCheckEntity::getClusterId, Function.identity()));
    }
}
