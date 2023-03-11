package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;


/**
 * System Setting Interface
 *
 * @author wangyl
 */
public interface ISysSettingService extends IService<SysSettingEntity> {
    boolean updateSetting(SysSettingEntity setting);

    SysSettingEntity getSetting(Integer userId);
}
