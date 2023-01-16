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
