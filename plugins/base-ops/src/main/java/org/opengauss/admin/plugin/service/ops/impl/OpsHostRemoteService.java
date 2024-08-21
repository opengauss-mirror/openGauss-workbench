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
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.RetBuffer;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.service.ops.IHostService;
import org.opengauss.admin.plugin.utils.JschRetBufferUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.system.plugin.facade.AzFacade;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpHostRemoteService
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
@Service
public class OpsHostRemoteService {
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
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
    private IHostService hostService;
    @Resource
    private JschUtil jschUtil;
    @Resource
    private JschRetBufferUtil jschRetBufferUtil;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * cache session
     *
     * @param hostId      hostId
     * @param hostUserId  hostUserId
     * @param rootSession session
     */
    public void cacheSession(String hostId, String hostUserId, Session rootSession) {
        SESSION_MAP.putIfAbsent(hostId + "_" + hostUserId, rootSession);
    }

    /**
     * get session from cache
     *
     * @param hostId     hostId
     * @param hostUserId hostUserId
     * @return session
     */
    public Session getCacheSession(String hostId, String hostUserId) {
        Session session = SESSION_MAP.get(hostId + "_" + hostUserId);
        if (Objects.nonNull(session) && !session.isConnected()) {
            session.disconnect();
            SESSION_MAP.remove(hostId + "_" + hostUserId);
            return null;
        }
        return session;
    }


    /**
     * get host by hostId, service called from plugin-main hostFacade
     *
     * @param hostId hostId
     * @return host
     */
    public OpsHostEntity getHost(String hostId) {
        Assert.isTrue(StrUtil.isNotEmpty(hostId), "hostId does not empty");
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        return hostEntity;
    }

    /**
     * get host root user by hostId, service called from plugin-main hostFacade
     *
     * @param hostId hostId
     * @return host root user
     */
    public OpsHostUserEntity getHostRootUser(String hostId) {
        OpsHostUserEntity userEntity = hostUserFacade.getHostUserByUsername(hostId, "root");
        if (StrUtil.isEmpty(userEntity.getPassword())) {
            throw new OpsException("root password cannot be empty");
        }
        return userEntity;
    }


    /**
     * get host root session from the session cache, but not cached session,create a session and cache it
     *
     * @param host     host
     * @param hostUser host user
     * @return session
     */
    public Session getHostUserSession(OpsHostEntity host, OpsHostUserEntity hostUser) {
        Assert.isTrue(Objects.nonNull(host), "host information does not exist");
        Assert.isTrue(Objects.nonNull(hostUser), "hostUser information does not exist");
        Assert.isTrue(StrUtil.isNotEmpty(hostUser.getPassword()), "hostUser password does not exist");
        Session cacheSession = getCacheSession(host.getHostId(), hostUser.getHostUserId());
        if (Objects.isNull(cacheSession)) {
            cacheSession = jschUtil.getSession(host.getPublicIp(), host.getPort(), hostUser.getUsername(),
                            encryptionUtils.decrypt(hostUser.getPassword()))
                    .orElseThrow(() -> new OpsException("Failed to establish connection with host"));
            cacheSession(host.getHostId(), hostUser.getHostUserId(), cacheSession);
        }
        return cacheSession;
    }

    /**
     * get host disk space
     *
     * @param rootSession  rootSession
     * @param topLevelPath topLevelPath
     * @return result
     */
    public int checkHostDiskSpace(Session rootSession, String topLevelPath) {
        String command = SshCommandConstants.DIR_FREE_HARD_DISK.replace("{0}", topLevelPath);
        String freeHardDisk = executeJschCommand(rootSession, command);
        return translateDiskFreeSpaceUnitGb(freeHardDisk);
    }

    private int translateDiskFreeSpaceUnitGb(String freeHardDisk) {
        int res = 0;
        String[] split = freeHardDisk.split("\n");
        for (String s : split) {
            try {
                res += Integer.parseInt(s.replace("G", " ").trim());
            } catch (NumberFormatException ingore) {
            }
        }
        return res;
    }

    /**
     * execute command
     *
     * @param rootSession rootSession
     * @param command     command
     * @return result
     */
    public String executeJschCommand(Session rootSession, String command) {
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to execute command : {} ,exitCode:{},res:{}", command,
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get system information");
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command : {} ", command, e);
            throw new OpsException("Failed to get system information");
        }
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
     * @param hostId   hostId
     * @param hostPort hostPort
     * @return true if port used
     */
    public boolean portUsed(String hostId, Integer hostPort) {
        return hostService.portUsed(hostId, hostPort, null);
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
     * @param hostId       hostId
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
     * @param command     command
     * @param rootSession rootSession
     * @param desc        desc
     * @return result
     */
    public String executeCommand(String command, Session rootSession, String desc) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommand(command, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command,
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to execute command");
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command {} {}", desc, command, e);
            throw new OpsException("Failed to execute command : " + desc);
        }
    }

    /**
     * execute command
     *
     * @param command     command
     * @param rootSession rootSession
     * @param retBuffer   retBuffer
     * @param desc        desc
     * @return result
     */
    public String executeCommand(String command, Session rootSession, RetBuffer retBuffer, String desc) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommandWithRetBuffer(command, rootSession, retBuffer);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command,
                        jschResult.getExitCode(), jschResult.getResult());
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
     * @param command     command
     * @param rootSession rootSession
     * @param retBuffer   retBuffer
     * @param desc        desc
     * @return result
     */
    public String executeCommandThenReturnEmpty(String command, Session rootSession, RetBuffer retBuffer, String desc) {
        try {
            JschResult jschResult = jschRetBufferUtil.executeCommand(command, rootSession, retBuffer,
                    null, false);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command,
                        jschResult.getExitCode(), jschResult.getResult());
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command {} {}", desc, command, e);
            throw new OpsException("Failed to execute command : " + desc);
        }
    }

    public String executeCommand(String command, Session rootSession, RetBuffer retBuffer) {
        return executeCommand(command, rootSession, retBuffer, "");
    }

    public String executeCommand(String command, Session rootSession, RetBuffer retBuffer, Map<String, String> autoResponse) {
        try {
            JschResult jschResult = jschRetBufferUtil
                    .executeCommandWithRetBufferAndAutoResponse(command, rootSession, retBuffer, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to execute command {} , exit code: {}, log: {}", command,
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to execute command");
            }
            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to execute command {}", command, e);
            throw new OpsException("Failed to execute command");
        }
    }

    public void executeUpload(Session rootSession, RetBuffer retBuffer, String sourcePath, String installPackageFullPath) {
        jschRetBufferUtil.upload(rootSession, retBuffer, sourcePath, installPackageFullPath);
    }


    public Session createRootSession(String hostId, String rootPassword) {
        OpsHostEntity hostEntity = getHost(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }

        OpsHostUserEntity rootUserEntity = getHostRootUser(hostId);
        if (Objects.nonNull(rootUserEntity) && StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            rootPassword = rootUserEntity.getPassword();
        }

        if (StrUtil.isEmpty(rootPassword)) {
            throw new OpsException("root password does not exist");
        }
        return getHostUserSession(hostEntity, rootUserEntity);
    }
}
