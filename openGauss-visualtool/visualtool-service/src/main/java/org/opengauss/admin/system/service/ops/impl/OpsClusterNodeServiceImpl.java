package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.system.mapper.ops.OpsClusterNodeMapper;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
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
}
