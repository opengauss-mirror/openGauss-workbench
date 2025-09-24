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
 * HostUserServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl
 * /HostUserServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.exception.ops.UserAlreadyExistsException;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.mapper.ops.OpsHostUserMapper;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.JschExecutorService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.agent.service.IAgentInstallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.Resource;

/**
 * @author lhf
 * @date 2022/8/8 15:40
 **/
@Slf4j
@Service
public class HostUserServiceImpl extends ServiceImpl<OpsHostUserMapper, OpsHostUserEntity> implements IHostUserService {
    @Autowired
    private JschUtil jschUtil;
    @Resource
    private JschExecutorService jschExecutorService;
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Autowired
    @Lazy
    private IOpsClusterNodeService opsClusterNodeService;
    @Resource
    @Lazy
    private IAgentInstallService agentInstallService;

    @Override
    public List<OpsHostUserEntity> listHostUserByHostId(String hostId) {
        if (StrUtil.isEmpty(hostId)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId);
        return list(queryWrapper);
    }

    @Override
    public boolean removeByHostId(String hostId) {
        if (StrUtil.isEmpty(hostId)) {
            return false;
        }
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId);
        return remove(queryWrapper);
    }

    @Override
    public List<OpsHostUserEntity> listHostUserByHostIdList(List<String> hostIdList) {
        if (CollUtil.isEmpty(hostIdList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .in(OpsHostUserEntity::getHostId, hostIdList);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(OpsHostEntity host, HostUserBody hostUserBody) {
        // check user exists
        String username = hostUserBody.getUsername();
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostUserBody.getHostId())
            .eq(OpsHostUserEntity::getUsername, username);
        long count = count(queryWrapper);
        if (count > 0) {
            throw new UserAlreadyExistsException("User information already exists");
        }

        OpsHostUserEntity opsHostUserEntity = hostUserBody.toEntity(false);
        return doAddOrEdit(host, opsHostUserEntity, true);
    }

    private void createPhysicalUser(SshLogin rootLogin, String username, String password) {
        try {
            jschExecutorService.createOsUser(rootLogin, username);
        } catch (OpsException ex) {
            throw new OpsException("create" + username + "User failed");
        }
        Session session = null;
        try {
            session = jschExecutorService.createSession(rootLogin);
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("password:", encryptionUtils.decrypt(password));
            autoResponse.put("password:", encryptionUtils.decrypt(password));
            JschResult jschResult = null;
            try {
                String command = MessageFormat.format(SshCommandConstants.CHANGE_OMM_PASSWORD_TEMPLATE, username);
                jschResult = jschUtil.executeCommand(command, session, autoResponse);
            } catch (InterruptedException e) {
                throw new OpsException("Do not interrupt the thread");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("update" + username + "User password failed, exit code: {}, log: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("update" + username + "User password failed");
            }
        } catch (IOException e) {
            log.error("update" + username + "User password failed", e);
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
            throw new OpsException("update" + username + "User password failed");
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * The function of editing users currently only supports modifying the user passwords,
     * and does not support the passwords of remote server users.
     * After modifying the passwords of server users, the passwords of users
     * managed by DataKit itself need to be manually modified and updated.
     *
     * @param host host info
     * @param hostUserId host User Id
     * @param hostUserBody host User Body
     * @return boolean edit result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(OpsHostEntity host, String hostUserId, HostUserBody hostUserBody) {
        // check user exists and username is not changed
        OpsHostUserEntity hostUserEntity = getById(hostUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("User information does not exist");
        }
        if (!hostUserEntity.getUsername().equals(hostUserBody.getUsername())) {
            throw new OpsException("The username cannot be modified");
        }

        OpsHostUserEntity newEntity = hostUserBody.toEntity(false);
        newEntity.setHostId(hostUserEntity.getHostId());
        newEntity.setHostUserId(hostUserEntity.getHostUserId());
        return doAddOrEdit(host, newEntity, false);
    }

    private boolean doAddOrEdit(OpsHostEntity hostEntity, OpsHostUserEntity userEntity, boolean isAdd) {
        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
            encryptionUtils.decrypt(userEntity.getPassword()));
        if (userEntity.isRootUser()) {
            if (!jschExecutorService.checkOsUserExist(sshLogin)) {
                throw new OpsException("Incorrect password, please enter correct password");
            }
            userEntity.setSudo(true);
            return isAdd ? save(userEntity) : updateById(userEntity);
        }
        if (jschExecutorService.checkOsUserExist(sshLogin)) {
            userEntity.setSudo(false);
            if (jschExecutorService.checkOsUserSudo(sshLogin)) {
                userEntity.setSudo(true);
            }
            return isAdd ? save(userEntity) : updateById(userEntity);
        }
        // if user cannot connect, use root user to check user exists, if not exists, create user or throw error
        String hostId = userEntity.getHostId();
        OpsHostUserEntity rootUser = getRootUserByHostId(hostId);
        if (Objects.isNull(rootUser)) {
            throw new OpsException("User does not exist or the password is incorrect");
        }
        SshLogin rootLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), rootUser.getUsername(),
            encryptionUtils.decrypt(rootUser.getPassword()));
        if (jschExecutorService.checkOsUserExist(rootLogin, userEntity.getUsername())) {
            throw new OpsException("Incorrect password, please enter correct password");
        }
        if (isAdd) {
            createPhysicalUser(rootLogin, userEntity.getUsername(), userEntity.getPassword());
            userEntity.setSudo(false);
            return save(userEntity);
        } else {
            throw new OpsException("User does not exist");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(String hostUserId) {
        OpsHostUserEntity hostUserEntity = getById(hostUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("User information does not exist");
        }
        if (opsClusterNodeService.countByHostUserId(hostUserId) > 0) {
            throw new OpsException("User is being used by the cluster");
        }
        if (agentInstallService.countByHostUserId(hostUserId) > 0) {
            throw new OpsException("User is being used by the agent");
        }
        List<OpsHostUserEntity> list = listHostUserByHostId(hostUserEntity.getHostId());
        if (list.size() > 1) {
            return removeById(hostUserId);
        } else {
            log.warn("The last user of host[{}] cannot be deleted", hostUserEntity.getHostId());
            return false;
        }
    }

    @Override
    public OpsHostUserEntity getOmmUserByHostId(String hostId) {
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId)
            .eq(OpsHostUserEntity::getUsername, "omm");
        return getOne(queryWrapper, false);
    }

    @Override
    public OpsHostUserEntity getRootUserByHostId(String hostId) {
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId)
            .eq(OpsHostUserEntity::getUsername, "root");
        return getOne(queryWrapper, false);
    }

    @Override
    public OpsHostUserEntity getAnyUserByHostId(String hostId) {
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId);
        List<OpsHostUserEntity> list = list(queryWrapper);
        return list.stream().filter(user -> StrUtil.isNotEmpty(user.getPassword())).findAny().orElse(null);
    }

    @Override
    public void cleanPassword(String hostUserId) {
        LambdaUpdateWrapper<OpsHostUserEntity> updateWrapper = Wrappers.lambdaUpdate(OpsHostUserEntity.class)
            .set(OpsHostUserEntity::getPassword, null)
            .set(OpsHostUserEntity::getSudo, Boolean.TRUE)
            .eq(OpsHostUserEntity::getHostUserId, hostUserId);
        update(updateWrapper);
    }

    @Override
    public OpsHostUserEntity getHostUserByUsername(String hostId, String username) {
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId)
            .eq(OpsHostUserEntity::getUsername, username);
        return getOne(queryWrapper, false);
    }

    @Override
    public boolean hasRootPermission(OpsHostEntity hostEntity, String userId) {
        OpsHostUserEntity userEntity = getById(userId);
        if (Objects.isNull(userEntity)) {
            log.error("Cannot get user information by user id: {}", userId);
            throw new OpsException("User information does not exist");
        }

        if (userEntity.isRootUser()) {
            return true;
        }

        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                encryptionUtils.decrypt(userEntity.getPassword()));
        return jschExecutorService.checkOsUserSudo(sshLogin);
    }
}
