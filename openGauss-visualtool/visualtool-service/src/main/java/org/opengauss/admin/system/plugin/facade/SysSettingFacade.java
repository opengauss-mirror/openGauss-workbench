package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangyl
 * @date 2023/3/8 09:22
 **/
@Service
public class SysSettingFacade {
    @Autowired
    private ISysSettingService sysSettingService;

    public SysSettingEntity getSysSetting(Integer userId) {
        return sysSettingService.getSetting(userId);
    }

    public boolean checkUploadPath(String path) {
        return sysSettingService.hasUploadPath(path);
    }
}
