package org.opengauss.admin.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.system.mapper.SysSettingMapper;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

/**
 * System Setting Service
 *
 * @author wangyl
 */
@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSettingEntity> implements ISysSettingService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSetting(SysSettingEntity setting) {
        // create folder if not exist
        String uploadPath = setting.getUploadPath();
        File file = new File(uploadPath);
        // if create failed
        if (!(!file.exists() && file.mkdirs())) {
            log.error(String.format("System setting update failed: folder %s create failed", uploadPath));
        }
        return saveOrUpdate(setting);
    }

    @Override
    public SysSettingEntity getSetting(Integer userId) {
        LambdaQueryWrapper<SysSettingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSettingEntity::getUserId, SysUser.getAdminUserId());
        SysSettingEntity adminResult = getOne(queryWrapper);
        if (SysUser.isAdmin(userId)) {
            return adminResult;
        }
        LambdaQueryWrapper<SysSettingEntity> userQueryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSettingEntity::getUserId, userId);
        SysSettingEntity userResult = getOne(userQueryWrapper);
        // if current user is not admin and there is no setting data
        // return admin setting to user for save
        if (ObjectUtil.isNull(userResult)) {
            adminResult.setUserId(userId);
            return adminResult;
        }
        return userResult;
    }
}
