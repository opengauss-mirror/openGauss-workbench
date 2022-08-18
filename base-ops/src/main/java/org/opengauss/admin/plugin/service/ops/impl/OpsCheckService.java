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
