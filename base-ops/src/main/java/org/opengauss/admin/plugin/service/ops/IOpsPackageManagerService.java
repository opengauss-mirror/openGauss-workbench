package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
public interface IOpsPackageManagerService extends IService<OpsPackageManagerEntity> {
    String getCpuArchByPackagePath(String installPackagePath, OpenGaussVersionEnum version);
}
