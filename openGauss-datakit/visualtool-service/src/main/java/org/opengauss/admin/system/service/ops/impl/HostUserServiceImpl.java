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
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.mapper.ops.OpsHostUserMapper;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.JschExecutorService;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import javax.annotation.Resource;

/**
 * @author lhf
 * @date 2022/8/8 15:40
 **/
@Slf4j
@Service
public class HostUserServiceImpl extends ServiceImpl<OpsHostUserMapper, OpsHostUserEntity> implements IHostUserService {

    @Autowired
    private IHostService hostService;
    @Autowired
    private JschUtil jschUtil;
    @Resource
    private JschExecutorService jschExecutorService;
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;

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
    public boolean add(HostUserBody hostUserBody) {
        String hostId = hostUserBody.getHostId();
        OpsHostEntity hostEntity = hostService.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        String username = hostUserBody.getUsername();
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
            .eq(OpsHostUserEntity::getHostId, hostId)
            .eq(OpsHostUserEntity::getUsername, username);
        long count = count(queryWrapper);
        if (count > 0) {
            throw new UserAlreadyExistsException("User information already exists");
        }
        OpsHostUserEntity rootUser = getRootUserByHostId(hostId);
        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), username,
            encryptionUtils.decrypt(hostUserBody.getPassword()));
        OpsHostUserEntity opsHostUserEntity;
        if (Objects.isNull(rootUser)) {
            if (jschExecutorService.checkOsUserExist(sshLogin)) {
                opsHostUserEntity = hostUserBody.toEntity(false);
                opsHostUserEntity.setSudo(opsHostUserEntity.isRootUser());
            } else {
                throw new OpsException("User does not exist and no permission to create os user");
            }
        } else {
            SshLogin rootLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), rootUser.getUsername(),
                encryptionUtils.decrypt(rootUser.getPassword()));
            OpsAssert.isTrue(StrUtil.isNotEmpty(rootUser.getPassword()),
                "Incorrect password, not have permission to create os user");
            Boolean isSudo = jschExecutorService.checkOsUserSudo(rootLogin, username);
            if (jschExecutorService.checkOsUserExist(rootLogin, username)) {
                OpsAssert.isTrue(jschExecutorService.checkOsUserExist(sshLogin),
                    "Incorrect password, please enter correct password");
            } else {
                createPhysicalUser(rootLogin, hostUserBody.getUsername(), hostUserBody.getPassword());
            }
            opsHostUserEntity = hostUserBody.toEntity(isSudo);
        }
        return save(opsHostUserEntity);
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
     * <pre>
     * The function of editing users currently only supports modifying the user passwords,
     * and does not support the passwords of remote server users.
     * After modifying the passwords of server users, the passwords of users
     * managed by DataKit itself need to be manually modified and updated.
     * <pre/>
     *
     * @param hostUserId host User Id
     * @param hostUserBody host User Body
     * @return boolean edit result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(String hostUserId, HostUserBody hostUserBody) {
        OpsHostUserEntity hostUserEntity = getById(hostUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("User information does not exist");
        }
        String hostId = hostUserEntity.getHostId();
        OpsHostEntity hostEntity = hostService.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        // no root user, or directly modify the root user,
        // detect the current modification user information and build the user information entity object
        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserBody.getUsername(),
            encryptionUtils.decrypt(hostUserBody.getPassword()));
        OpsAssert.isTrue(jschExecutorService.checkOsUserExist(sshLogin), "The user does not exist or password error");
        OpsHostUserEntity newEntity = hostUserBody.toEntity(false);
        newEntity.setHostId(hostUserEntity.getHostId());
        newEntity.setHostUserId(hostUserEntity.getHostUserId());
        return updateById(newEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(String hostUserId) {
        if (opsClusterNodeService.countByHostUserId(hostUserId) > 0) {
            throw new OpsException("User is being used by the cluster");
        }
        OpsHostUserEntity hostUserEntity = getById(hostUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("User information does not exist");
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
}
