package org.opengauss.admin.plugin.service.ops.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.mapper.ops.OpsPackageManagerMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.springframework.stereotype.Service;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
@Service
public class OpsPackageManagerService extends ServiceImpl<OpsPackageManagerMapper, OpsPackageManagerEntity> implements IOpsPackageManagerService {
}
