package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsCheckEntity;

/**
 * @author lhf
 * @date 2022/11/13 17:17
 **/
public interface IOpsCheckService extends IService<OpsCheckEntity> {
    OpsCheckEntity getLastResByClusterId(String clusterId);
}
