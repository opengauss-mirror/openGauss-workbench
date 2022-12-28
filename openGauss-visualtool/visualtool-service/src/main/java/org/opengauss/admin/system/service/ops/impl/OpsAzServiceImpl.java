package org.opengauss.admin.system.service.ops.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.system.mapper.ops.OpsAzMapper;
import org.opengauss.admin.system.service.ops.IOpsAzService;
import org.springframework.stereotype.Service;


/**
 * @author lhf
 * @date 2022/8/6 20:50
 **/
@Service
public class OpsAzServiceImpl extends ServiceImpl<OpsAzMapper, OpsAzEntity> implements IOpsAzService {

    @Override
    public boolean add(OpsAzEntity az) {
       return save(az);
    }
}
