package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.system.service.ops.IOpsAzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author lhf
 * @date 2022/11/20 21:09
 **/
@Service
public class AzFacade {
    @Autowired
    private IOpsAzService opsAzService;

    public boolean add(OpsAzEntity az) {
        return opsAzService.save(az);
    }

    public List<OpsAzEntity> listByIds(Collection ids) {
        return opsAzService.listByIds(ids);
    }

    public OpsAzEntity getById(String id) {
        return opsAzService.getById(id);
    }

}
