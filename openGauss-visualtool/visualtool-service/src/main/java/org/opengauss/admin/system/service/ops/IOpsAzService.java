package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;

/**
 * O&M Availability Zone Service Class
 *
 * @author lhf
 * @date 2022/8/6 20:49
 **/
public interface IOpsAzService extends IService<OpsAzEntity> {
    /**
     * Add Availability Zone
     *
     * @param az
     * @return
     */
    boolean add(OpsAzEntity az);

    boolean hasName(String name);
}
