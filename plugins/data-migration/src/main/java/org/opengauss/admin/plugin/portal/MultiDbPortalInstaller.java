/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.utils.CommonUtils;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.enums.PortalInstallStatus;
import org.opengauss.admin.plugin.enums.PortalInstallType;
import org.opengauss.admin.plugin.enums.PortalType;
import org.opengauss.admin.plugin.enums.PortalVersion;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationToolPortalDownloadInfoService;
import org.opengauss.admin.plugin.service.TbMigrationTaskGlobalToolsParamService;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.third.party.tools.ThirdPartyToolManager;
import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * MULTI_DB portal installer
 *
 * @since 2025/6/30
 */
@Slf4j
@Component
public class MultiDbPortalInstaller {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;

    @Resource
    private MigrationHostPortalInstallHostService portalInstallHostService;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private TbMigrationTaskGlobalToolsParamService toolsParamService;

    @Autowired
    private MigrationToolPortalDownloadInfoService portalDownloadInfoService;

    /**
     * install portal
     *
     * @param hostId host id
     * @param install install info
     * @param isReinstall is retry install
     * @return AjaxResult.success
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult install(String hostId, MigrationHostPortalInstall install, boolean isReinstall) {
        OpsHostEntity opsHost = hostFacade.getById(hostId);
        if (opsHost == null) {
            log.error("Failed to find host with id {}", hostId);
            return AjaxResult.error("Failed to find host with id " + hostId);
        }
        if (PortalInstallType.ONLINE_INSTALL.getCode().equals(install.getInstallType())) {
            PortalVersion portalVersion = install.getPortalVersion();
            if (portalVersion == null) {
                throw new PortalInstallException("portal version cannot be null");
            }
            MigrationToolPortalDownloadInfo portalDownloadInfo = portalDownloadInfoService.getPortalDownloadInfo(
                    opsHost, PortalType.MULTI_DB, portalVersion);
            install.setPkgDownloadUrl(portalDownloadInfo.getPortalPkgDownloadUrl());
            install.setPkgName(portalDownloadInfo.getPortalPkgName());
            install.setJarName(portalDownloadInfo.getPortalJarName());
        }

        OpsHostUserEntity hostUser = hostUserFacade.getById(install.getHostUserId());
        if (hostUser == null) {
            log.error("Failed to find host user with id {}", install.getHostUserId());
            return AjaxResult.error("Failed to find host user with id " + install.getHostUserId());
        }

        String formatInstallPath = formatInstallPath(install.getInstallPath(), hostUser.getUsername());
        MigrationHostPortalInstall oldInstall = portalInstallHostService.getOneByHostId(install.getRunHostId());
        if (isReinstall && (!formatInstallPath.equals(oldInstall.getInstallPath())
                || !PortalType.MULTI_DB.equals(oldInstall.getPortalType()))) {
            deletePortal(hostId);
        }

        checkUserPermission(opsHost, hostUser, formatInstallPath);
        MigrationHostPortalInstall installInfo = prepareInstallInfo(install, opsHost, hostUser, formatInstallPath);
        try {
            MigrationHostPortalInstall newInstall = portalInstallHostService.saveRecord(installInfo);
            ThirdPartyToolManager.save(ThirdPartyToolEnum.MIGRATION_PORTAL, newInstall.toPortalInstallInfo());
        } catch (IOException e) {
            String errorMsg = "Failed to save portal install info to file system. "
                    + "Please fix this error and restart Datakit. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PortalInstallException(errorMsg);
        }

        if (PortalInstallType.IMPORT_INSTALL.getCode().equals(install.getInstallType())) {
            importPortal(installInfo);
        } else {
            preinstall(installInfo);
            syncInstallPortal(installInfo);
        }
        return AjaxResult.success();
    }

    /**
     * delete portal
     *
     * @param hostId host id
     * @return AjaxResult.success
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deletePortal(String hostId) {
        MigrationHostPortalInstall portalInfo = portalInstallHostService.getOneByHostId(hostId);
        ShellInfoVo shellInfo = createShellInfo(portalInfo);

        String portalHome = portalInfo.getInstallPath() + "portal/";
        String jarName = portalInfo.getJarName();
        String stopKafkaCommand = String.format("cd %s && java -jar %s --kafka stop", portalHome, jarName);
        String stopKafkaResult = ShellUtil.execCommandGetResult(shellInfo, stopKafkaCommand).getResult();
        log.info("Stop kafka result: {}", stopKafkaResult);

        String uninstallCommand = String.format("cd %s && java -jar %s --uninstall tools", portalHome, jarName);
        String uninstallResult = ShellUtil.execCommandGetResult(shellInfo, uninstallCommand).getResult();
        log.info("Uninstall tools result: {}", uninstallResult);

        String rmCommand = String.format("cd %s && rm -rf %s %s %s", portalInfo.getInstallPath(), "portal/",
                portalInfo.getPkgName(), portalInfo.getDatakitLogPath());
        ShellUtil.execCommandGetResult(shellInfo, rmCommand);
        try {
            ThirdPartyToolManager.deleteById(ThirdPartyToolEnum.MIGRATION_PORTAL, portalInfo.getId().toString());
        } catch (IOException e) {
            log.error("Failed to delete portal install info from file system, id: {}", portalInfo.getId(), e);
            throw new PortalInstallException("Failed to delete portal install info from file system. "
                    + "Please fix this error and restart Datakit. " + e.getMessage());
        }
        portalInstallHostService.removeById(portalInfo.getId());
        portalInstallHostService.clearPkgUploadPath(hostId);
        toolsParamService.removeByHostId(portalInfo.getRunHostId());
        return AjaxResult.success();
    }

    private void importPortal(MigrationHostPortalInstall installInfo) {
        threadPoolTaskExecutor.submit(() -> {
            StringBuilder installLog = new StringBuilder();
            boolean isInstallSuccess = false;
            try {
                searchJarPackage(installInfo);
                ShellInfoVo shellInfo = createShellInfo(installInfo);
                installLog.append("START_CHECK_INSTALL").append(System.lineSeparator());
                String portalHome = installInfo.getInstallPath() + "portal/";
                String jarName = installInfo.getJarName();
                String checkInstallCommand =
                        String.format("cd %s && java -jar %s --install check", portalHome, jarName);

                JschResult jschResult = ShellUtil.execCommandGetResult(shellInfo, checkInstallCommand);
                String checkResult = jschResult.getResult();
                log.info("Check install result: {}", checkResult);
                installLog.append(checkResult).append(System.lineSeparator());
                installLog.append("END_CHECK_INSTALL").append(System.lineSeparator());

                if (isInstallSuccess(checkResult)) {
                    isInstallSuccess = true;
                    log.info("Install portal successfully");
                    loadToolParams(installInfo);
                }
            } catch (Exception e) {
                installLog.append("Failed to check portal install status").append(e.getMessage())
                        .append(System.lineSeparator());
                log.error("Failed to check portal install status", e);
            } finally {
                installInfo.setInstallStatus(isInstallSuccess ? PortalInstallStatus.INSTALLED.getCode()
                        : PortalInstallStatus.INSTALL_ERROR.getCode());
                portalInstallHostService.saveRecord(installInfo);
                printInstallPortalLog(installInfo, installLog.toString());
            }
        });
    }

    private boolean isInstallSuccess(String checkResult) {
        if (checkResult == null) {
            return false;
        }

        List<String> successMessages = List.of(
                "FullReplicateTool is already installed",
                "MilvusMigrationTool is already installed",
                "ElasticsearchMigrationTool is already installed",
                "Debezium is already installed",
                "Kafka is already installed"
        );
        return successMessages.stream().allMatch(checkResult::contains);
    }

    private void searchJarPackage(MigrationHostPortalInstall installInfo) {
        ShellInfoVo shellInfo = createShellInfo(installInfo);
        String portalHome = installInfo.getInstallPath() + "portal/";

        String jarName = "openGauss-portal-*.jar";
        String searchJarCommand = String.format("ls %s%s | tail -1", portalHome, jarName);
        JschResult jarJschResult = ShellUtil.execCommandGetResult(shellInfo, searchJarCommand);
        if (jarJschResult.isOk()) {
            String jarResult = jarJschResult.getResult();
            jarName = jarResult.substring(jarResult.lastIndexOf("/") + 1).trim();
        } else {
            portalHome = installInfo.getInstallPath();
            searchJarCommand = String.format("ls %s%s | tail -1", portalHome, jarName);
            jarJschResult = ShellUtil.execCommandGetResult(shellInfo, searchJarCommand);

            if (jarJschResult.isOk()) {
                String jarResult = jarJschResult.getResult();
                jarName = jarResult.substring(jarResult.lastIndexOf("/") + 1).trim();

                String installPath = portalHome.substring(0, portalHome.lastIndexOf("portal/"));
                installInfo.setInstallPath(installPath);
            } else {
                throw new PortalInstallException("Failed to find portal jar package in the installation package, "
                        + "expected name: " + jarName + " but not found");
            }
        }
        installInfo.setJarName(jarName);
    }

    private void syncInstallPortal(MigrationHostPortalInstall installInfo) {
        threadPoolTaskExecutor.submit(() -> {
            StringBuilder installLog = new StringBuilder();
            boolean isInstallSuccess = false;
            try {
                if (PortalInstallType.OFFLINE_INSTALL.getCode().equals(installInfo.getInstallType())) {
                    installLog.append("START_UPLOAD_OFFLINE_PACKAGE").append(System.lineSeparator());
                    UploadInfo uploadResult = uploadPortal(installInfo.getFile(), installInfo);
                    installInfo.setPkgDownloadUrl("");
                    installInfo.setPkgUploadPath(uploadResult);
                    installLog.append("END_UPLOAD_OFFLINE_PACKAGE").append(System.lineSeparator());
                } else if (PortalInstallType.ONLINE_INSTALL.getCode().equals(installInfo.getInstallType())) {
                    downloadInstallPackage(installInfo, installLog);
                } else {
                    throw new PortalInstallException("Invalid install type: " + installInfo.getInstallType());
                }

                isInstallSuccess = installPortal(installInfo, installLog);
                if (isInstallSuccess) {
                    log.info("Install portal successfully");
                    loadToolParams(installInfo);
                }
            } catch (Exception e) {
                installLog.append("Failed to install portal").append(e.getMessage()).append(System.lineSeparator());
                log.error("Failed to install portal", e);
            } finally {
                installInfo.setInstallStatus(isInstallSuccess ? PortalInstallStatus.INSTALLED.getCode()
                        : PortalInstallStatus.INSTALL_ERROR.getCode());
                portalInstallHostService.saveRecord(installInfo);
                printInstallPortalLog(installInfo, installLog.toString());
            }
        });
    }

    private void loadToolParams(MigrationHostPortalInstall installInfo) {
    }

    private boolean installPortal(MigrationHostPortalInstall installInfo, StringBuilder installLog) {
        String installPkgPath = installInfo.getInstallPath() + installInfo.getPkgName();
        String unzipCommand = String.format("tar -zxvf %s -C %s", installPkgPath, installInfo.getInstallPath());
        ShellInfoVo shellInfo = createShellInfo(installInfo);

        JschResult unzipResult = ShellUtil.execCommandGetResult(shellInfo, unzipCommand, 60000);
        installLog.append("START_UNZIP_INSTALLATION_PACKAGE").append(System.lineSeparator());
        installLog.append(unzipResult.getResult()).append(System.lineSeparator());
        installLog.append("END_UNZIP_INSTALLATION_PACKAGE").append(System.lineSeparator());
        log.info("Unzip portal package result: {}", unzipResult.getResult());
        if (!unzipResult.isOk()) {
            throw new PortalInstallException("Unzip portal package failed: " + unzipResult.getResult());
        }

        String portalHome = installInfo.getInstallPath() + "portal/";
        String jarName = "openGauss-portal-*.jar";
        JschResult jarJschResult = ShellUtil.execCommandGetResult(shellInfo,
                String.format("ls %s%s | tail -1", portalHome, jarName));
        if (jarJschResult.isOk()) {
            String jarResult = jarJschResult.getResult();
            jarName = jarResult.substring(jarResult.lastIndexOf("/") + 1).trim();
            installInfo.setJarName(jarName);
        } else {
            throw new PortalInstallException("Failed to find portal jar package in the installation package, "
                    + "expected name: " + jarName + " but not found");
        }

        installLog.append("START_INSTALL_PORTAL").append(System.lineSeparator());
        String installCommand = String.format("cd %s && java -jar %s --install full_replicate milvus_migration_tool "
                + "elasticsearch_migration_tool debezium kafka", portalHome, jarName);
        JschResult jschResult = ShellUtil.execCommandGetResult(shellInfo, installCommand, 600000);
        String installResult = jschResult.getResult();
        installLog.append(installResult).append(System.lineSeparator());
        installLog.append("END_INSTALL_PORTAL").append(System.lineSeparator());
        log.info("Install portal result: {}", installResult);

        String installSuccessMsg = "Install command execute done";
        return installResult != null && installResult.contains(installSuccessMsg);
    }

    private void downloadInstallPackage(MigrationHostPortalInstall installInfo, StringBuilder installLog) {
        ShellInfoVo shellInfo = createShellInfo(installInfo);
        String pkgPath = installInfo.getInstallPath() + installInfo.getPkgName();
        installLog.append("START_DOWNLOAD_ONLINE_PACKAGE").append(System.lineSeparator());
        JschResult isPkgExists = ShellUtil.execCommandGetResult(
                shellInfo, "[ -f " + pkgPath + " ] && echo 1 || echo 0");

        if (Integer.parseInt(isPkgExists.getResult().trim()) == 0) {
            String downloadCommand = String.format("wget -t 1 -P %s %s", installInfo.getInstallPath(),
                    installInfo.getPkgDownloadUrl() + installInfo.getPkgName());
            log.info("wget download portal, command: {}", downloadCommand);
            installLog.append(downloadCommand).append(System.lineSeparator());
            JschResult wgetResult = ShellUtil.execCommandGetResult(shellInfo, downloadCommand, 600000);
            installLog.append(wgetResult.getResult()).append(System.lineSeparator());

            if (!wgetResult.isOk()) {
                log.error("download pkg failed...");
                throw new PortalInstallException("download portal package failed: " + wgetResult.getResult());
            }
        } else {
            installLog.append("portal package already exists").append(System.lineSeparator());
        }
        installLog.append("END_DOWNLOAD_ONLINE_PACKAGE").append(System.lineSeparator());
    }

    private UploadInfo uploadPortal(MultipartFile file, MigrationHostPortalInstall installInfo) {
        if (file == null || ObjectUtils.isEmpty(file.getOriginalFilename())) {
            throw new PortalInstallException("Portal package file is empty");
        }

        String filename = file.getOriginalFilename();
        UploadInfo result = new UploadInfo();
        try (InputStream in = file.getInputStream()) {
            ShellUtil.uploadFile(installInfo.getHost(), installInfo.getPort(), installInfo.getRunUser(),
                    encryptionUtils.decrypt(installInfo.getRunPassword()),
                    installInfo.getInstallPath() + filename, in);
            result.setName(filename);
            installInfo.setPkgName(filename);
            result.setRealPath(installInfo.getInstallPath());
        } catch (IOException e) {
            log.error("Upload portal failed", e);
            throw new PortalInstallException("Upload portal failed: " + e.getMessage());
        }
        return result;
    }

    private void preinstall(MigrationHostPortalInstall installInfo) {
        SshLogin sshLogin = new SshLogin(installInfo.getHost(), installInfo.getPort(), installInfo.getRunUser(),
                encryptionUtils.decrypt(installInfo.getRunPassword()));
        checkJavaEnv(sshLogin);

        ShellInfoVo shellInfo = createShellInfo(installInfo);
        removeInstallPortalLog(shellInfo, installInfo);
        removePortalDir(shellInfo, installInfo);
    }

    private void removeInstallPortalLog(ShellInfoVo shellInfo, MigrationHostPortalInstall installInfo) {
        String command = "rm -rf " + installInfo.getDatakitLogPath();
        JschResult jschResult = ShellUtil.execCommandGetResult(shellInfo, command);
        if (!jschResult.isOk()) {
            log.error("Remove datakit_install_portal.log failed, message: {}", jschResult.getResult());
        }
    }

    private void removePortalDir(ShellInfoVo shellInfo, MigrationHostPortalInstall installInfo) {
        String command = "rm -rf " + installInfo.getInstallPath() + "/portal";
        JschResult jschResult = ShellUtil.execCommandGetResult(shellInfo, command);
        if (!jschResult.isOk()) {
            log.error("Remove portal directory failed, message: {}", jschResult.getResult());
        }
    }

    private void checkJavaEnv(SshLogin sshLogin) {
        String version = jschExecutorFacade.checkJavaVersion(sshLogin);
        int javaVersionMajor = CommonUtils.getJavaVersionMajor(version);
        if (javaVersionMajor < 17) {
            String errMsg = "The java version is not match 17+, "
                    + "please check environment JAVA_HOME,it must configuration in user ~/.bashrc";
            log.warn("{}, host: {}, user: {}", errMsg, sshLogin.getHost(), sshLogin.getUsername());
            throw new PortalInstallException(errMsg);
        }
        log.info("Java version is compatible with the installation requirements.");
    }

    private MigrationHostPortalInstall prepareInstallInfo(
            MigrationHostPortalInstall install, OpsHostEntity opsHost, OpsHostUserEntity hostUser, String installPath) {
        MigrationHostPortalInstall result = new MigrationHostPortalInstall();
        result.setRunHostId(opsHost.getHostId());
        result.setHost(opsHost.getPublicIp());
        result.setPort(opsHost.getPort());
        result.setRunUser(hostUser.getUsername());
        result.setHostUserId(hostUser.getHostUserId());
        result.setRunPassword(hostUser.getPassword());
        result.setInstallPath(installPath);
        result.setPkgName(install.getPkgName());
        result.setInstallStatus(PortalInstallStatus.INSTALLING.getCode());
        result.setInstallType(install.getInstallType());
        result.setPkgDownloadUrl(install.getPkgDownloadUrl());
        result.setPortalType(PortalType.MULTI_DB);
        result.setFile(install.getFile());
        return result;
    }

    private void checkUserPermission(OpsHostEntity opsHost, OpsHostUserEntity hostUser, String installPath) {
        String password = encryptionUtils.decrypt(hostUser.getPassword());
        boolean isExists = PortalHandle.directoryExists(opsHost.getPublicIp(), opsHost.getPort(),
                hostUser.getUsername(), password, installPath);
        if (isExists) {
            boolean isExistsAndHasPermission = PortalHandle.checkWritePermission(opsHost.getPublicIp(),
                    opsHost.getPort(), hostUser.getUsername(), password, installPath);
            if (!isExistsAndHasPermission) {
                throw new PortalInstallException(
                        MigrationErrorCode.PORTAL_INSTALL_PATH_NOT_HAS_WRITE_PERMISSION_ERROR.getMsg());
            }
        } else {
            boolean isCreateSuccess = PortalHandle.mkdirDirectory(opsHost.getPublicIp(), opsHost.getPort(),
                    hostUser.getUsername(), password, installPath);
            if (!isCreateSuccess) {
                throw new PortalInstallException(MigrationErrorCode.PORTAL_CREATE_INSTALL_PATH_FAILED.getMsg());
            }
        }
    }

    private String formatInstallPath(String installPath, String userName) {
        if (ObjectUtils.isEmpty(installPath)) {
            throw new PortalInstallException("Install path cannot be empty");
        }
        String result = installPath.replaceAll("\\s", "");

        if (result.startsWith("~")) {
            result = result.replaceFirst("~", "/home/" + userName);
        }

        if (!result.endsWith("/")) {
            result = result + "/";
        }
        return result;
    }

    private void printInstallPortalLog(MigrationHostPortalInstall installParams, String logInfo) {
        String command = String.format("mkdir -p %s && echo '%s' > %s", installParams.getInstallPath(),
                logInfo.replaceAll("'", "\""), installParams.getDatakitLogPath());
        JschResult result = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(),
                installParams.getRunUser(), encryptionUtils.decrypt(installParams.getRunPassword()), command);
        if (!result.isOk()) {
            log.error("Output logs to datakit_install_portal.log failed: " + result.getResult());
        }
    }

    private ShellInfoVo createShellInfo(MigrationHostPortalInstall portalInfo) {
        return new ShellInfoVo(portalInfo.getHost(), portalInfo.getPort(), portalInfo.getRunUser(),
                encryptionUtils.decrypt(portalInfo.getRunPassword()));
    }
}
