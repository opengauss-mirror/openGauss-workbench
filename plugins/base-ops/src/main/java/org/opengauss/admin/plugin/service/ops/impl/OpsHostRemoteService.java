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
 * OpHostRemoteService.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpHostRemoteService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.vo.HostBean;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.RetBuffer;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.utils.OpsAssert;
import org.opengauss.admin.plugin.utils.OpsJschExecPlugin;
import org.opengauss.admin.plugin.utils.JschRetBufferUtil;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.AzFacade;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.Objects;
import java.util.Map;
import java.util.List;

/**
 * OpHostRemoteService
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
@Service
@Scope("prototype")
public class OpsHostRemoteService {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private AzFacade azFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Resource
    private OpsJschExecPlugin opsJschExecPlugin;
    @Resource
    private JschRetBufferUtil jschRetBufferUtil;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;

    /**
     * get host by hostId, service called from plugin-main hostFacade
     *
     * @param hostId hostId
     * @return host
     */
    public OpsHostEntity getHost(String hostId) {
        Assert.isTrue(StrUtil.isNotEmpty(hostId), "hostId does not empty");
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        OpsAssert.nonNull(hostEntity, "host information does not exist");
        return hostEntity;
    }

    /**
     * query host list by condition
     *
     * @param os os
     * @param osVersion osVersion
     * @param cpuArch cpuArch
     * @return host list
     */
    public List<OpsHostEntity> getHostList(String os, String osVersion, String cpuArch) {
        return hostFacade.getHostList(os, osVersion, cpuArch);
    }

    /**
     * get host root user by hostId, service called from plugin-main hostFacade
     *
     * @param hostId hostId
     * @return host root user
     */
    public OpsHostUserEntity getHostRootUser(String hostId) {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        OpsAssert.nonNull(hostEntity, "Host information does not exist, hostId: " + hostId);

        OpsHostUserEntity userEntity = hostUserFacade.getHostUserByUsername(hostId, "root");
        if (Objects.isNull(userEntity)) {
            throw new OpsException("Please add the host root user, host ip: " + hostEntity.getPublicIp());
        }
        if (StrUtil.isEmpty(userEntity.getPassword())) {
            throw new OpsException("The host root user password is empty, host ip: " + hostEntity.getPublicIp());
        }
        return userEntity;
    }

    /**
     * create a session
     *
     * @param host host
     * @param hostUser host user
     * @return session
     */
    public Session createHostUserSession(OpsHostEntity host, OpsHostUserEntity hostUser) {
        Assert.isTrue(Objects.nonNull(host), "host information does not exist");
        Assert.isTrue(Objects.nonNull(hostUser), "hostUser information does not exist");
        Assert.isTrue(StrUtil.isNotEmpty(hostUser.getPassword()), "hostUser password does not exist");
        return jschRetBufferUtil.getSession(host.getPublicIp(), host.getPort(), hostUser.getUsername(),
                encryptionUtils.decrypt(hostUser.getPassword()))
            .orElseThrow(() -> new OpsException("Failed to establish connection with host"));
    }

    /**
     * create a host session bean
     *
     * @param host host
     * @param hostUser host user
     * @return hostBean
     */
    public HostBean createSessionHostBean(OpsHostEntity host, OpsHostUserEntity hostUser) {
        Assert.isTrue(Objects.nonNull(host), "host information does not exist");
        Assert.isTrue(Objects.nonNull(hostUser), "hostUser information does not exist");
        Assert.isTrue(StrUtil.isNotEmpty(hostUser.getPassword()), "hostUser password does not exist");
        return new HostBean(host.getPublicIp(), host.getPort(), hostUser.getUsername(),
            encryptionUtils.decrypt(hostUser.getPassword()));
    }

    /**
     * get host disk space
     *
     * @param hostBean hostBean
     * @param topLevelPath topLevelPath
     * @return result
     */
    public int checkHostDiskSpace(HostBean hostBean, String topLevelPath) {
        String command = SshCommandConstants.DIR_FREE_HARD_DISK.replace("{0}", topLevelPath);
        String freeHardDisk = opsJschExecPlugin.execCommand(hostBean, command);
        OpsAssert.isTrue(!freeHardDisk.contains("No such file or directory"), freeHardDisk);
        return translateDiskFreeSpaceUnitGb(freeHardDisk);
    }

    private int translateDiskFreeSpaceUnitGb(String freeHardDisk) {
        int res = 0;
        String[] split = freeHardDisk.split("\n");
        for (String s : split) {
            try {
                if (s.contains("G")) {
                    res += Integer.parseInt(s.replace("G", " ").trim());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return res;
    }

    /**
     * get host list by host ids
     *
     * @param hostIds hostIds
     * @return host list
     */
    public List<OpsHostEntity> getOpsHostList(List<String> hostIds) {
        return hostFacade.listByIds(hostIds);
    }

    /**
     * get host user list by host user ids
     *
     * @param hostUserIds hostUserIds
     * @return host user list
     */
    public List<OpsHostUserEntity> getOpsHostUserList(List<String> hostUserIds) {
        return hostUserFacade.listByIds(hostUserIds);
    }

    /**
     * get host user by host user id
     *
     * @param hostUserId hostUserId
     * @return host user
     */
    public OpsHostUserEntity getOpsHostUser(String hostUserId) {
        return hostUserFacade.getById(hostUserId);
    }

    /**
     * get host user list by host id
     *
     * @param hostId hostId
     * @return host user list
     */
    public List<OpsHostUserEntity> listHostUserByHostId(String hostId) {
        return hostUserFacade.listHostUserByHostId(hostId);
    }

    /**
     * get host by host ip
     *
     * @param hostIp hostIp
     * @return host
     */
    public OpsHostEntity getByPublicIp(String hostIp) {
        return hostFacade.getByPublicIp(hostIp);
    }

    /**
     * check port used
     *
     * @param hostId hostId
     * @param hostPort hostPort
     * @return true if port used
     */
    public boolean portUsed(String hostId, Integer hostPort) {
        return portUsed(hostId, hostPort, null);
    }

    /**
     * check port used
     *
     * @param id hostId
     * @param port hostPort
     * @param rootPassword rootPassword
     * @return true if port used
     */
    public boolean portUsed(String id, Integer port, String rootPassword) {
        SshLogin sshLogin = buildHostSshLoginByRootOrNormalUser(id);
        return !jschExecutorFacade.checkOsPortConflict(sshLogin, port);
    }

    /**
     * get host user list by host id
     *
     * @return host user list
     */
    public List<OpsHostEntity> listAll() {
        return hostFacade.listAll();
    }

    /**
     * get host user by host id and username
     *
     * @param hostId hostId
     * @param hostUsername hostUsername
     * @return host user
     */
    public OpsHostUserEntity getHostUserByUsername(String hostId, String hostUsername) {
        return hostUserFacade.getHostUserByUsername(hostId, hostUsername);
    }

    /**
     * get  az
     *
     * @return az list
     */
    public List<OpsAzEntity> listAllAz() {
        return azFacade.listAll();
    }

    /**
     * get az by az id
     *
     * @param azId azId
     * @return az
     */
    public OpsAzEntity getAzById(String azId) {
        return azFacade.getById(azId);
    }

    /**
     * execute command
     *
     * @param command command
     * @param rootSession rootSession
     * @param desc desc
     * @return result
     */
    public String executeCommand(String command, Session rootSession, String desc) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommand(command, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command, jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("Failed to execute command : " + command);
            }
            return jschResult.getResult().trim();
        } catch (IOException | InterruptedException | OpsException e) {
            log.error("Failed to execute command {} {}", desc, command, e);
            throw new OpsException("Failed to execute command : " + command);
        }
    }

    /**
     * execute command
     *
     * @param command command
     * @param rootSession rootSession
     * @param retBuffer retBuffer
     * @param desc desc
     * @return result
     */
    public String executeCommand(String command, Session rootSession, RetBuffer retBuffer, String desc) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommandWithRetBuffer(command, rootSession, retBuffer);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command, jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("Failed to execute command");
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command {} {}", desc, command, e);
            throw new OpsException("Failed to execute command : " + desc);
        }
    }

    /**
     * execute command then return empty
     *
     * @param command command
     * @param rootSession rootSession
     * @param retBuffer retBuffer
     * @param desc desc
     * @return result
     */
    public String executeCommandThenReturnEmpty(String command, Session rootSession, RetBuffer retBuffer, String desc) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommand(command, rootSession, retBuffer, null, false);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command, jschResult.getExitCode(),
                    jschResult.getResult());
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command {} {}", desc, command, e);
            throw new OpsException("Failed to execute command : " + desc);
        }
    }

    /**
     * execute command then return empty
     *
     * @param command command
     * @param rootSession rootSession
     * @param retBuffer retBuffer
     * @return result
     */
    public String executeCommand(String command, Session rootSession, RetBuffer retBuffer) {
        return executeCommand(command, rootSession, retBuffer, "");
    }

    /**
     * execute command then return empty
     *
     * @param command command
     * @param rootSession rootSession
     * @param retBuffer retBuffer
     * @param autoResponse autoResponse
     * @return result
     */
    public String executeCommand(String command, Session rootSession, RetBuffer retBuffer,
        Map<String, String> autoResponse) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommandWithRetBufferAndAutoResponse(command, rootSession,
                retBuffer, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command, jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("Failed to execute command");
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command {}", command, e);
            throw new OpsException("Failed to execute command");
        }
    }

    /**
     * execute command
     *
     * @param command command
     * @param rootSession rootSession
     * @param retBuffer retBuffer
     * @param autoResponse autoResponse
     * @param shouldHandleErrorBeforeExit shouldHandleErrorBeforeExit
     * @return JschResult
     */
    public JschResult executeCommand(String command, Session rootSession, RetBuffer retBuffer,
        Map<String, String> autoResponse, boolean shouldHandleErrorBeforeExit) {
        try {
            return jschRetBufferUtil.executeCommand(command, rootSession, retBuffer, autoResponse,
                shouldHandleErrorBeforeExit);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to execute command {}", command, e);
            throw new OpsException("Failed to execute command: " + command);
        }
    }

    /**
     * upload file
     *
     * @param rootSession rootSession
     * @param retBuffer retBuffer
     * @param sourcePath sourcePath
     * @param installPackageFullPath installPackageFullPath
     */
    public void executeUpload(Session rootSession, RetBuffer retBuffer, String sourcePath,
        String installPackageFullPath) {
        jschRetBufferUtil.upload(rootSession, retBuffer, sourcePath, installPackageFullPath);
    }

    /**
     * close session
     *
     * @param session session
     */
    public void closeSession(Session session) {
        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * 检查Host指定路径是否为空
     *
     * @param hostId hostId
     * @param path path
     * @return true/false
     */
    public boolean pathEmpty(String hostId, String path) {
        SshLogin sshLogin = buildHostSshLoginByRootOrNormalUser(hostId);
        return jschExecutorFacade.checkPathEmpty(sshLogin, path);
    }

    private SshLogin buildHostSshLoginByRootOrNormalUser(String hostId) {
        OpsHostEntity hostEntity = getHost(hostId);
        OpsAssert.isTrue(Objects.nonNull(hostEntity), "host information not found");
        OpsHostUserEntity hostUser = getHostUserRootOrNormal(hostId);
        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), hostUser.getUsername(),
            encryptionUtils.decrypt(hostUser.getPassword()));
        return sshLogin;
    }

    /**
     * get host user,when has root ,return root user,otherwise return normal user
     *
     * @param hostId host id
     * @return host user
     * @throws OpsException host user not found
     */
    private OpsHostUserEntity getHostUserRootOrNormal(String hostId) {
        List<OpsHostUserEntity> userList = hostUserFacade.listHostUserByHostId(hostId);
        OpsHostUserEntity hostUser = userList.stream()
            .filter(user -> StrUtil.isNotEmpty(user.getPassword()))
            .filter(user -> StrUtil.equalsIgnoreCase(user.getUsername(), "root"))
            .findFirst()
            .orElse(null);
        if (Objects.isNull(hostUser)) {
            hostUser = userList.stream()
                .filter(user -> StrUtil.isNotEmpty(user.getPassword()))
                .findAny()
                .orElseThrow(() -> new OpsException("host user does not exist"));
        }
        return hostUser;
    }

    /**
     * check file exist
     *
     * @param hostId hostId
     * @param file file
     * @return boolean
     */
    public boolean checkFileExist(String hostId, String file) {
        SshLogin sshLogin = buildHostSshLoginByRootOrNormalUser(hostId);
        return jschExecutorFacade.checkFileExist(sshLogin, file);
    }

    /**
     * create session with root or normal user
     *
     * @param hostId host
     * @return Session
     */
    public Session createPluginSessionWithRootOrNormalUser(String hostId) {
        SshLogin sshLogin = buildHostSshLoginByRootOrNormalUser(hostId);
        return jschRetBufferUtil.getSession(sshLogin.getHost(), sshLogin.getPort(), sshLogin.getUsername(),
                sshLogin.getPassword())
            .orElseThrow(() -> new OpsException("create plugin session with root or normal user failed"));
    }

    /**
     * get host root or normal user
     *
     * @param hostId host id
     * @return any user
     */
    public OpsHostUserEntity getAnyHostUser(String hostId) {
        return hostUserFacade.listHostUserByHostId(hostId).stream().findAny().get();
    }
}
