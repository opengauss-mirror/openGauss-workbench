package org.opengauss.admin.system.service.ops.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsEncryptionEntity;
import org.opengauss.admin.system.mapper.ops.OpsEncryptionMapper;
import org.opengauss.admin.system.service.ops.IEncryptionService;
import org.springframework.stereotype.Service;

/**
 * @author lhf
 * @date 2022/12/2 18:37
 **/
@Service
public class EncryptionServiceImpl extends ServiceImpl<OpsEncryptionMapper, OpsEncryptionEntity> implements IEncryptionService {
}
