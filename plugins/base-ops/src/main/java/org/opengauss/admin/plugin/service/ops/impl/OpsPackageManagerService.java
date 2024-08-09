/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * OpsPackageManagerService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsPackageManagerService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.common.enums.CpuArchName;
import org.opengauss.admin.common.enums.OsName;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.PackageTypeEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsPackageManagerMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
@Slf4j
@Service
public class OpsPackageManagerService extends ServiceImpl<OpsPackageManagerMapper, OpsPackageManagerEntity> implements IOpsPackageManagerService {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;

    @Override
    public String getCpuArchByPackagePath(String installPackagePath, OpenGaussVersionEnum version) {
        if (!FileUtil.exist(installPackagePath)) {
            throw new OpsException("The installation package does not exist");
        }

        if (version == OpenGaussVersionEnum.ENTERPRISE) {
            return getEnterprisePackageCpuArch(installPackagePath);
        } else if (version == OpenGaussVersionEnum.MINIMAL_LIST) {
            return getMinimalListPackageCpuArch(installPackagePath);
        } else if (version == OpenGaussVersionEnum.LITE) {
            return getLitePackageCpuArch(installPackagePath);
        } else {
            throw new OpsException("An unsupported version");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void savePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException {
        // upload file
        savePackageTarFile(pkg, pkg.getName(), userId);
        saveOrUpdate(pkg);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePackage(OpsPackageManagerEntity pkg, Integer userId) {
        savePackage(pkg, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delPackage(String id) {
        OpsPackageManagerEntity entity = getById(id);
        if (ObjectUtil.isNull(entity)) {
            log.warn(String.format("Can't find package record [%s] to delete. Skip", entity.getPackageId()));
            return;
        }
        String realPath = entity.getRealPath();
        if (StrUtil.isNotEmpty(realPath)) {
            File file = new File(realPath);
            if (!file.exists()) {
                log.warn(String.format("Can't find package tar file [%s] to delete. Skip", entity.getPackagePath().toString()));
            } else {
                boolean result = file.delete();
                if (!result) {
                    log.warn(String.format("Can't delete file [%s]. Skip", entity.getPackagePath().toString()));
                }
            }
        }
        removeById(id);
    }

    @Override
    public OpsPackageVO analysisPkg(String pkgName, String pkgType) {
        OpsPackageVO result = new OpsPackageVO();
        if (StrUtil.containsIgnoreCase(pkgName, CpuArchName.AARCH64.getCpuArchName())) {
            result.setCpuArch(CpuArchName.AARCH64.getCpuArchName());
        } else if (StrUtil.containsIgnoreCase(pkgName, CpuArchName.X86_64.getCpuArchName()) || StrUtil.containsIgnoreCase(pkgName, "64") || StrUtil.containsIgnoreCase(pkgName, "86")) {
            result.setCpuArch(CpuArchName.X86_64.getCpuArchName());
        } else {
            result.setCpuArch(CpuArchName.NOARCH.getCpuArchName());
        }
        if (StrUtil.containsIgnoreCase(pkgName, OsName.CENTOS.getOsName())) {
            result.setOs(OsName.CENTOS.getOsName());
        } else if (StrUtil.containsIgnoreCase(pkgName, OsName.OPEN_EULER.getOsName())) {
            result.setOs(OsName.OPEN_EULER.getOsName());
        } else {
            result.setOs(OsName.ALL.getOsName());
        }

        String patternString = "([0-9]+\\.){1,2}[0-9]+";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(pkgName);
        if (matcher.find()) {
            String version = matcher.group();
            result.setPackageVersionNum(version);
        }

        if (pkgType.equalsIgnoreCase(PackageTypeEnum.OPENGAUSS.getPackageType())) {
            if (StrUtil.containsIgnoreCase(pkgName, "-all")) {
                result.setPackageVersion(OpenGaussVersionEnum.ENTERPRISE.name());
            } else if (StrUtil.containsIgnoreCase(pkgName, "-Lite-")) {
                result.setPackageVersion(OpenGaussVersionEnum.LITE.toString());
            } else {
                result.setPackageVersion(OpenGaussVersionEnum.MINIMAL_LIST.name());
            }
        }
        return result;
    }

    @Deprecated
    @Override
    public UploadInfo upload(MultipartFile file, Integer userId) throws OpsException {
        OpsPackageManagerEntity entity = new OpsPackageManagerEntity();
        UploadInfo info = new UploadInfo();
        entity.setPackagePath(info);
        entity.setFile(file);
        savePackageTarFile(entity, "", userId);
        return info;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deletePkgTar(String path, String id) {
        File file = new File(path);
        boolean result = true;
        if (file.exists()) {
            result = file.delete();
        }
        if (ObjectUtil.isNotNull(id)) {
            UpdateWrapper<OpsPackageManagerEntity> wrapper = new UpdateWrapper<>();
            wrapper.set("package_path", "").eq("package_id", id);
            update(wrapper);
        }
        return result;
    }

    @Override
    public String getSysUploadPath(Integer userId) {
        SysSettingEntity entity = sysSettingFacade.getSysSetting(userId);
        if (ObjectUtil.isNull(entity)) {
            return "";
        }
        return entity.getUploadPath();
    }

    @Override
    public boolean checkUploadPath(String path, Integer userId) {
        return !sysSettingFacade.checkUploadPath(path, userId);
    }

    private String getLitePackageCpuArch(String installPackagePath) {
        if (installPackagePath.contains("aarch64")) {
            return "aarch64";
        }

        if (installPackagePath.contains("x86_64")) {
            return "x86_64";
        }

        return null;
    }

    private String getMinimalListPackageCpuArch(String installPackagePath) {
        String tempFolder = "temp-minimal-" + StrUtil.uuid();
        String tempAbsoluteFolder = FileUtil.getParent(installPackagePath, 1) + File.separator + tempFolder;
        FileUtil.mkdir(tempAbsoluteFolder);

        try {
            try {
                Process tar = Runtime.getRuntime().exec(
                        "tar -xvf " + installPackagePath + " -C " + tempAbsoluteFolder);
                int exitCode = tar.waitFor();
                if (0 != exitCode) {
                    throw new OpsException("Failed to decompress the installation package with exitCode " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to decompress the installation package", e);
                throw new OpsException("Failed to decompress the installation package");
            }
            InputStream in = null;
            try {
                Process file = Runtime.getRuntime().exec("file " + tempAbsoluteFolder + File.separator + "bin" + File.separator + "gs_ctl");
                int exitCode = file.waitFor();
                if (0 != exitCode) {
                    throw new OpsException("Failed to get cpu arch with exitCode " + exitCode);
                }
                in = file.getInputStream();
                String res = IoUtil.read(in, StandardCharsets.UTF_8);
                if (StrUtil.isEmpty(res)) {
                    log.error("Failed to get cpu arch:{}");
                    throw new OpsException("Failed to get cpu arch");
                } else {
                    String[] split = res.split(",");
                    if (split.length < 2) {
                        log.error("Failed to get cpu arch:{}", res);
                        throw new OpsException("Failed to get cpu arch");
                    }

                    String trim = split[1].trim();
                    String[] s = trim.split(" ");

                    if (s.length == 1) {
                        return s[0];
                    }
                    if (s.length > 1) {
                        return s[s.length - 1];
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to get cpu arch", e);
                throw new OpsException("Failed to get cpu arch");
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("close input stream failed: " + e.getMessage());
                    }
                }
            }
            return null;
        } finally {
            threadPoolTaskExecutor.submit(() -> {
                String command = "rm -rf " + tempAbsoluteFolder;
                try {
                    Process exec = Runtime.getRuntime().exec(command);
                    exec.waitFor();
                } catch (Exception ignore) {

                }
            });
        }
    }

    private String getEnterprisePackageCpuArch(String installPackagePath) {
        String version = getVersion(installPackagePath);
        String system = getSystem(installPackagePath);
        log.info("version:{}", version);
        String tempFolder = "temp-enterprise-" + StrUtil.uuid();
        String tempAbsoluteFolder = FileUtil.getParent(installPackagePath, 1) + File.separator + tempFolder;
        FileUtil.mkdir(tempAbsoluteFolder);

        try {
            try {
                String command = "tar -xvf " + installPackagePath + " -C " + tempAbsoluteFolder;
                Process tar = Runtime.getRuntime().exec(command);
                int exitCode = tar.waitFor();
                if (0 != exitCode) {
                    log.error("command:{}", command);
                    throw new OpsException("Failed to decompress the installation package with exitCode " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to decompress the installation package", e);
                throw new OpsException("Failed to decompress the installation package");
            }

            try {
                String command = "tar -xvf " + tempAbsoluteFolder + File.separator + "openGauss-" + version + "-" + system + "-64bit-cm.tar.gz " + " -C " + tempAbsoluteFolder;
                Process tar = Runtime.getRuntime().exec(command);
                int exitCode = tar.waitFor();
                if (0 != exitCode) {
                    log.error("command:{}", command);
                    throw new OpsException("Failed to decompress the installation package with exitCode " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to decompress the installation package", e);
                throw new OpsException("Failed to decompress the installation package");
            }
            InputStream in = null;
            try {
                String command = "file " + tempAbsoluteFolder + File.separator + "bin" + File.separator + "cm_ctl";
                Process file = Runtime.getRuntime().exec(command);
                int exitCode = file.waitFor();
                if (0 != exitCode) {
                    log.error("command:{}", command);
                    throw new OpsException("Failed to get cpu arch with exitCode " + exitCode);
                }
                in = file.getInputStream();
                String res = IoUtil.read(in, StandardCharsets.UTF_8);
                if (StrUtil.isEmpty(res)) {
                    log.error("Failed to get cpu arch:{}");
                    throw new OpsException("Failed to get cpu arch");
                } else {
                    String[] split = res.split(",");
                    if (split.length < 2) {
                        log.error("Failed to get cpu arch:{}", res);
                        throw new OpsException("Failed to get cpu arch");
                    }

                    String trim = split[1].trim();
                    String[] s = trim.split(" ");

                    if (s.length == 1) {
                        return s[0];
                    }
                    if (s.length > 1) {
                        return s[s.length - 1];
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to get cpu arch", e);
                throw new OpsException("Failed to get cpu arch");
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("close input stream failed: " + e.getMessage());
                    }
                }
            }

            return null;
        } finally {
            threadPoolTaskExecutor.submit(() -> {
                String command = "rm -rf " + tempAbsoluteFolder;
                try {
                    Process exec = Runtime.getRuntime().exec(command);
                    exec.waitFor();
                } catch (Exception ignore) {

                }
            });
        }
    }

    private String getSystem(String installPackagePath) {
        String packageName = installPackagePath.substring(installPackagePath.lastIndexOf("/"));
        return packageName.split("-")[2];
    }

    private String getVersion(String installPackagePath) {
        String packageName = installPackagePath.substring(installPackagePath.lastIndexOf("/"));
        return packageName.split("-")[1];
    }

    /**
     * save package tar file to disk
     *
     * @param pkg
     * @param userId
     */
    private void savePackageTarFile(OpsPackageManagerEntity pkg, String pkgManagedName, Integer userId) throws OpsException {
        MultipartFile file = pkg.getFile();
        if (ObjectUtil.isNull(file) || StrUtil.isNotEmpty(pkg.getRealPath())) {
            return;
        }
        SysSettingEntity entity = sysSettingFacade.getSysSetting(userId);
        if (ObjectUtil.isNull(entity)) {
            log.error("Cannot find system setting of user id: " + userId);
            throw new OpsException("Cannot find system setting of your account, please try again");
        }
        // create folder
        String uploadFolder = entity.getUploadPath() + pkgManagedName;
        File folder = new File(uploadFolder);
        if (!folder.exists()) {
            boolean res = folder.mkdirs();
            if (!res) {
                String errMsg = String.format("Can't create folder: %s, please try again", uploadFolder);
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        }
        String fileRealPath = Path.of(uploadFolder, file.getOriginalFilename()).toString();
        try {
            file.transferTo(new File(fileRealPath));
            UploadInfo info = pkg.getPackagePath();
            if (ObjectUtil.isNull(info)) {
                info = new UploadInfo();
            }
            info.setName(file.getOriginalFilename());
            info.setRealPath(fileRealPath);
            pkg.setPackagePath(info);
        } catch (Exception ex) {
            String errMsg = String.format("Upload tar file to %s failed: %s", fileRealPath, ex.getMessage());
            log.error(errMsg);
            throw new OpsException(errMsg);
        }
    }

    @Override
    public boolean hasName(String name) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(OpsPackageManagerEntity::getName, name);
        return count(queryWrapper) > 0;
    }
}
