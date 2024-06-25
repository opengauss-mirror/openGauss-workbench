/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsPackageManagerV2Service.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsPackageManagerV2Service.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsPackageManagerMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerV2Service;
import org.opengauss.admin.plugin.utils.DownloadUtil;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
@Service
public class OpsPackageManagerV2Service extends ServiceImpl<OpsPackageManagerMapper, OpsPackageManagerEntity> implements IOpsPackageManagerV2Service {
    @Resource
    private WsConnectorManager wsConnectorManager;
    @Resource
    private DownloadUtil downloadUtil;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;

    @Override
    public List<OpsPackageVO> queryOpsPackageList(String os, String cpuArch, OpenGaussVersionEnum packageVersion, String packageVersionNum) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(OpsPackageManagerEntity::getOs, os.toLowerCase())
                .eq(OpsPackageManagerEntity::getCpuArch, cpuArch)
                .eq(OpsPackageManagerEntity::getPackageVersion, packageVersion)
                .eq(OpsPackageManagerEntity::getPackageVersionNum, packageVersionNum)
                .orderByDesc(OpsPackageManagerEntity::getPackageVersionNum);
        return list(queryWrapper).stream().map(OpsPackageManagerEntity::toVO)
                .distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> listVersionNumber() {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(OpsPackageManagerEntity::getPackageVersionNum);
        return list(queryWrapper).stream().map(OpsPackageManagerEntity::getPackageVersionNum)
                .distinct().collect(Collectors.toList());
    }

    @Override
    public void checkingPackageList(List<String> packageIds) {
        if (CollectionUtils.isEmpty(packageIds)) {
            return;
        }
        List<OpsPackageManagerEntity> packageEntityList = list(new LambdaQueryWrapper<OpsPackageManagerEntity>()
                .in(OpsPackageManagerEntity::getPackageId, packageIds));
        if (CollectionUtils.isEmpty(packageEntityList)) {
            return;
        }
        packageEntityList.stream().forEach(entry -> {
            File file = new File(entry.getRealPath());
            if (!file.exists()) {
                UpdateWrapper<OpsPackageManagerEntity> wrapper = new UpdateWrapper<>();
                wrapper.set("package_path", "").eq("package_id", entry.getPackageId());
                update(wrapper);
            }
        });
    }

    @Override
    public void savePackageOnline(OpsPackageManagerEntity entity, Integer userId, String wsBusinessId) {
        String realPath = entity.getRealPath();
        if (StrUtil.isEmpty(entity.getRealPath())) {
            log.error("Cannot find package download path : " + realPath);
            throw new OpsException("Cannot find package download path :" + realPath);
        }
        SysSettingEntity settingEntity = sysSettingFacade.getSysSetting(userId);
        if (ObjectUtil.isNull(settingEntity)) {
            log.error("Cannot find system setting of user id: " + userId);
            throw new OpsException("Cannot find system setting of your account, please try again");
        }
        File folder = new File(realPath);
        if (!folder.exists()) {
            boolean res = folder.mkdirs();
            if (!res) {
                String errMsg = String.format("Can't create folder: %s, please try again", realPath);
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        }
        WsSession wsSession = wsConnectorManager.getSession(wsBusinessId).orElseThrow(() -> new OpsException("No output websocket session found"));
        Future<?> future = threadPoolTaskExecutor.submit(() -> downloadUtil.download(entity.getPackageUrl(), realPath, entity.getFileName(), wsSession));
        TaskManager.registry(wsBusinessId, future);
        saveOrUpdate(entity);
    }

    @Override
    public boolean hasName(String packageId, String name) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(OpsPackageManagerEntity::getName, name)
                .eq(StrUtil.isNotEmpty(packageId), OpsPackageManagerEntity::getPackageId, packageId);
        return count(queryWrapper) > 0;
    }
}
