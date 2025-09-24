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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.dto.ops.PackageDto;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.DateUtils;
import org.opengauss.admin.plugin.constant.OpsConstants;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.OpsPackageSource;
import org.opengauss.admin.plugin.mapper.ops.OpsPackageManagerMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerV2Service;
import org.opengauss.admin.plugin.utils.DownloadUtil;
import org.opengauss.admin.plugin.utils.PathUtils;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OpsPackageManagerV2Service
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
@Service
public class OpsPackageManagerV2Service extends ServiceImpl<OpsPackageManagerMapper, OpsPackageManagerEntity> implements IOpsPackageManagerV2Service {
    private static final String FASTJSON_TYPE_HANDLER = "typeHandler=com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler";
    @Resource
    private WsConnectorManager wsConnectorManager;
    @Resource
    private DownloadUtil downloadUtil;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;
    @Resource
    private PathUtils pathUtils;

    @Override
    public List<OpsPackageVO> queryOpsPackageList(PackageDto packageDict) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(OpsPackageManagerEntity::getOs, packageDict.getOs())
                .eq(OpsPackageManagerEntity::getOsVersion, packageDict.getOsVersion())
                .eq(OpsPackageManagerEntity::getCpuArch, packageDict.getCpuArch())
                .eq(OpsPackageManagerEntity::getPackageVersion, packageDict.getOpenGaussVersion())
                .eq(OpsPackageManagerEntity::getPackageVersionNum, packageDict.getOpenGaussVersionNum())
                .orderByAsc(OpsPackageManagerEntity::getPackageVersionNum);
        return list(queryWrapper).stream().map(OpsPackageManagerEntity::toVO)
                .distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> listVersionNumber() {
        QueryWrapper<OpsPackageManagerEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("distinct package_version_num");
        queryWrapper.isNotNull("package_version_num");
        queryWrapper.orderByDesc("package_version_num");
        List<OpsPackageManagerEntity> list = list(queryWrapper);
        return list.stream().map(OpsPackageManagerEntity::getPackageVersionNum).collect(Collectors.toList());
    }

    @Override
    public void checkingPackageList(List<String> packageIds) {
        List<OpsPackageManagerEntity> packageEntityList = getAndValidPackageByIds(packageIds);
        for (OpsPackageManagerEntity entity : packageEntityList) {
            checkingPackageByEntity(entity);
        }
    }

    @Override
    public boolean checkingPackage(String packageId) {
        OpsPackageManagerEntity entity = getById(packageId);
        Assert.isTrue(Objects.nonNull(entity), "packageId is not exits");
        return checkingPackageByEntity(entity);
    }

    private boolean checkingPackageByEntity(OpsPackageManagerEntity entity) {
        UploadInfo packagePath = entity.getPackagePath();
        boolean isValid = false;
        if (Objects.nonNull(packagePath)) {
            File file = new File(packagePath.getRealPath() + File.separatorChar + packagePath.getName());
            isValid = Files.exists(file.toPath());
            if (isValid) {
                log.info("checking package list, packageId: {}, realPath success :{} ",
                        entity.getPackageId(), entity.getRealPath());
                return isValid;
            }
        }

        if (Objects.equals(entity.getRemark(), OpsConstants.PACKAGE_REMARK)) {
            removeById(entity.getPackageId());
            log.info("checking package list, packageId: {}, realPath file not exit,delete package :{} ",
                entity.getPackageId(), entity.getRealPath());
        } else {
            UpdateWrapper<OpsPackageManagerEntity> wrapper = new UpdateWrapper<>();
            wrapper.set("package_path", "").set("source", "").eq("package_id", entity.getPackageId());
            update(wrapper);
            log.info("checking package list, packageId: {}, realPath file not exit,clear package_path :{} ",
                entity.getPackageId(), entity.getRealPath());
        }
        return isValid;
    }

    private List<OpsPackageManagerEntity> getAndValidPackageByIds(List<String> packageIds) {
        if (CollectionUtils.isEmpty(packageIds)) {
            throw new OpsException("packageId is not empty");
        }
        List<OpsPackageManagerEntity> packageEntityList = list(new LambdaQueryWrapper<OpsPackageManagerEntity>()
                .in(OpsPackageManagerEntity::getPackageId, packageIds));
        if (CollectionUtils.isEmpty(packageEntityList)) {
            log.error("packageId {} is not exist", packageIds);
            throw new OpsException("packageId " + packageIds + " is not exist");
        }
        if (packageIds.size() != packageEntityList.size()) {
            List<String> notExistIds = packageEntityList.stream()
                    .map(OpsPackageManagerEntity::getPackageId)
                    .filter(packageId -> !packageIds.contains(packageId))
                    .collect(Collectors.toList());
            log.error("packageId {} is not exist", notExistIds);
            throw new OpsException("packageId " + notExistIds + " is not exist");
        }
        return packageEntityList;
    }

    @Override
    public void delPackage(List<String> packageIds) {
        List<OpsPackageManagerEntity> packageEntityList = getAndValidPackageByIds(packageIds);
        packageEntityList.forEach(entry -> {
            UploadInfo packagePath = entry.getPackagePath();
            if (Objects.nonNull(packagePath)) {
                Path dir = Paths.get(packagePath.getRealPath());
                try (Stream<Path> pathStream = Files.walk(dir)) {
                    pathStream.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", path, e);
                        }
                    });
                } catch (IOException e) {
                    log.error("Failed to walk directory: {}", dir, e);
                }
            }
            removeById(entry.getPackageId());
        });
    }

    @Override
    public void saveOnlinePackage(OpsPackageManagerEntity entity, Integer userId, String wsBusinessId) {
        // create package path
        UploadInfo packagePath = getPackagePath(entity, userId);
        entity.setPackagePath(packagePath);
        entity.setSource(OpsPackageSource.ONLINE.getName());
        String realPath = entity.getRealPath();
        checkPackageStoragePath(realPath, false);
        // get download progress session
        WsSession wsSession = wsConnectorManager.getSession(wsBusinessId).orElseThrow(() -> new OpsException("No output websocket session found"));
        // download package
        Future<?> future = threadPoolTaskExecutor.submit(() -> downloadUtil.download(entity.getPackageUrl(), realPath, entity.getFileName(), wsSession));
        // register download progress
        TaskManager.registry(wsBusinessId, future);
        saveOrUpdate(entity);
    }


    @Override
    public void updateOnlinePackage(OpsPackageManagerEntity entity, Integer userId, String wsBusinessId) {
        if (Objects.isNull(entity.getPackagePath())) {
            // create package path
            UploadInfo packagePath = getPackagePath(entity, userId);
            entity.setPackagePath(packagePath);
        }
        entity.setSource(OpsPackageSource.ONLINE.getName());
        String realPath = entity.getRealPath();
        checkPackageStoragePath(realPath, true);
        // get download progress session
        WsSession wsSession = wsConnectorManager.getSession(wsBusinessId).orElseThrow(() -> new OpsException("No output websocket session found"));
        // download package
        Future<?> future = threadPoolTaskExecutor.submit(() -> downloadUtil.download(entity.getPackageUrl(), realPath, entity.getFileName(), wsSession));
        // register download progress
        TaskManager.registry(wsBusinessId, future);
        updatePackagePath(entity);
    }

    @Override
    public boolean hasName(String packageId, String name) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(OpsPackageManagerEntity::getName, name)
                .eq(StrUtil.isNotEmpty(packageId), OpsPackageManagerEntity::getPackageId, packageId);
        return count(queryWrapper) > 0;
    }

    @Override
    public void saveUploadPackage(OpsPackageManagerEntity pkg, Integer userId) {
        UploadInfo packagePath = getPackagePath(pkg, userId);
        pkg.setPackagePath(packagePath);
        uploadPackage(pkg, false);
        // save package info
        pkg.setSource(OpsPackageSource.OFFLINE.getName());
        save(pkg);
    }

    @Override
    public void updateUploadPackage(OpsPackageManagerEntity pkg, Integer userId) {
        if (Objects.isNull(pkg.getPackagePath())) {
            UploadInfo packagePath = getPackagePath(pkg, userId);
            pkg.setPackagePath(packagePath);
        }
        uploadPackage(pkg, true);
        pkg.setSource(OpsPackageSource.OFFLINE.getName());
        updatePackagePath(pkg);
    }

    private void updatePackagePath(OpsPackageManagerEntity pkg) {
        LambdaUpdateWrapper<OpsPackageManagerEntity> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(OpsPackageManagerEntity::getPackageId, pkg.getPackageId());
        updateWrapper.set(OpsPackageManagerEntity::getPackagePath, pkg.getPackagePath(), FASTJSON_TYPE_HANDLER);
        updateWrapper.set(OpsPackageManagerEntity::getSource, pkg.getSource());
        update(updateWrapper);
    }

    private void uploadPackage(OpsPackageManagerEntity pkg, boolean isUpdate) {
        MultipartFile file = pkg.getFile();
        if (ObjectUtil.isNull(file)) {
            return;
        }
        UploadInfo packagePath = new UploadInfo();
        // create upload folder is the absolute path  d:\\ops\\upload
        String uploadFolder = pkg.getRealPath();
        // check upload folder
        checkPackageStoragePath(uploadFolder, isUpdate);
        // upload file
        String fileRealPath = Path.of(uploadFolder, file.getOriginalFilename()).toString();
        // save the MultipartFile file to the target (fileRealPath) directory
        try {
            // transferTo param dest ,
            // dest is the absolute path to the file you want to save the uploaded file to
            file.transferTo(new File(fileRealPath));
            packagePath.setName(file.getOriginalFilename());
            packagePath.setRealPath(uploadFolder);
            pkg.setPackagePath(packagePath);
        } catch (Exception ex) {
            String errMsg = String.format("Upload tar file to %s failed: %s", fileRealPath, ex.getMessage());
            log.error(errMsg);
            throw new OpsException(errMsg);
        }
    }

    /**
     * <pre>
     * create package storage folder
     * realPath folder rule : sysUploadPath/versionNum/os/cpuArch/name
     * name rule : the binary file name
     * </pre>
     *
     * @param entity entity
     * @param userId user id
     * @return UploadInfo
     */
    private UploadInfo getPackagePath(OpsPackageManagerEntity entity, Integer userId) {
        UploadInfo packagePath = new UploadInfo();
        SysSettingEntity sysSetting = sysSettingFacade.getSysSetting(userId);
        Assert.isTrue(Objects.nonNull(sysSetting), "System setting not found");
        String sysUploadPath = pathUtils.getPath(sysSetting.getUploadPath());
        packagePath.setRealPath(sysUploadPath
                + entity.getPackageVersionNum() + File.separatorChar
                + entity.getOs() + File.separatorChar
                + entity.getCpuArch() + File.separatorChar
                + entity.getName());
        packagePath.setName(parseDownloadFileName(entity.getPackageUrl()));
        return packagePath;
    }

    private String parseDownloadFileName(String packageUrl) {
        return packageUrl.substring(packageUrl.lastIndexOf("/") + 1);
    }

    /**
     * <pre>
     * check realPath on the dataKit server disk.
     * if realPath not exist,create it.
     * if the realPath exist, check if is update package,
     * if is update package,rename old package.
     * </pre>
     *
     * @param realPath real path of package
     * @param isUpdate is update package
     * @throws OpsException
     */
    private void checkPackageStoragePath(String realPath, boolean isUpdate) {
        if (StrUtil.isEmpty(realPath)) {
            log.error("Cannot find package storage path : " + realPath);
            throw new OpsException("Cannot find package storage path :" + realPath);
        }
        File folder = new File(realPath);
        if (!folder.exists()) {
            boolean res = folder.mkdirs();
            if (!res) {
                String errMsg = String.format("Can't create folder: %s, please try again", realPath);
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        } else if (isUpdate && folder.canWrite()) {
            String updatePath = realPath + "-" + DateUtils.dateTimeNow() + ".update";
            log.warn("Rename package storage path to " + updatePath);
            folder.renameTo(new File(updatePath));
            folder = new File(realPath);
            folder.mkdirs();
        }
    }
}
