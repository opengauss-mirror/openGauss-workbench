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
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.mapper.ops.OpsEncryptionMapper;
import org.opengauss.admin.system.mapper.ops.OpsHostUserMapper;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

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
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Autowired
    private OpsEncryptionMapper encryptionMapper;
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
            throw new OpsException("User information already exists");
        }

        OpsHostUserEntity rootUser = getRootUserByHostId(hostId);
        if (Objects.isNull(rootUser)){
            rootUser = new OpsHostUserEntity();
            rootUser.setUsername("root");
            rootUser.setPassword(hostUserBody.getPassword());
        }

        if (StrUtil.isEmpty(rootUser.getPassword())){
            if (StrUtil.isNotEmpty(hostUserBody.getRootPassword())){
                rootUser.setPassword(hostUserBody.getRootPassword());
            }else {
                throw new OpsException("root password does not exist");
            }
        }

        Boolean sudo = false;
        if (physicalExist(hostEntity.getPublicIp(), hostEntity.getPort(), rootUser, username)) {
            try {
                Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), username, encryptionUtils.decrypt(hostUserBody.getPassword())).orElseThrow(() -> new OpsException("Incorrect password, please enter correct password"));
                if (Objects.isNull(session)) {
                    throw new OpsException("Incorrect password, please enter correct password");
                } else {
                    session.disconnect();
                }
            } catch (Exception e) {
                log.error("connection exception:", e);
                throw new OpsException("Incorrect password, please enter correct password");
            }

            sudo = getSudo(hostEntity.getPublicIp(), hostEntity.getPort(),rootUser,username);
        } else {
            createPhysicalUser(hostEntity.getPublicIp(), hostEntity.getPort(), rootUser, hostUserBody.getUsername(), hostUserBody.getPassword());
        }

        OpsHostUserEntity opsHostUserEntity = hostUserBody.toEntity(sudo);
        return save(opsHostUserEntity);
    }

    private Boolean getSudo(String publicIp, Integer port, OpsHostUserEntity rootUser, String username) {
        if (Objects.isNull(rootUser)) {
            throw new OpsException("The root user information was not obtained");
        }

        Session session = jschUtil.getSession(publicIp, port, "root", encryptionUtils.decrypt(rootUser.getPassword())).orElseThrow(() -> new OpsException("root user login failed"));
        try {
            String command = "sudo -l -U " + username;
            JschResult jschResult = jschUtil.executeCommand(command, session);

            return Objects.nonNull(jschResult) && StrUtil.isNotEmpty(jschResult.getResult()) && !jschResult.getResult().contains("not allowed to run");
        }catch (Exception e){
            log.error("get sudo fail：",e);
        }finally {
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }
        return false;
    }

    private void createPhysicalUser(String publicIp, Integer port, OpsHostUserEntity rootUser, String username, String password) {
        if (Objects.isNull(rootUser)) {
            throw new OpsException("The root user information was not obtained");
        }

        Session session = jschUtil.getSession(publicIp, port, "root", encryptionUtils.decrypt(rootUser.getPassword())).orElseThrow(() -> new OpsException("root user login failed"));

        try {
            JschResult jschResult = null;
            try {

                String command = "useradd " + username;
                jschResult = jschUtil.executeCommand(command, session);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("create" + username + "User failed, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("create " + username + " User failed");
            }
        } catch (IOException e) {
            log.error("create" + username + "User failed", e);
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
            throw new OpsException("create" + username + "User failed");
        }

        try {
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
                log.error("update" + username + "User password failed, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("update" + username + "User password failed");
            }
        } catch (IOException e) {
            log.error("update" + username + "User password failed", e);
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
            throw new OpsException("update" + username + "User password failed");
        }

        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }
    }

    private boolean physicalExist(String publicIp, Integer port, OpsHostUserEntity rootUser, String username) {
        if (Objects.isNull(rootUser)) {
            throw new OpsException("The root user information was not obtained");
        }

        Session session = jschUtil.getSession(publicIp, port, "root", encryptionUtils.decrypt(rootUser.getPassword())).orElseThrow(() -> new OpsException("root user login failed"));
        try {
            String command = "id -u " + username;
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("query" + username + "User failed", e);
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
            throw new OpsException("query " + username + " User failed");
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

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

        OpsHostUserEntity rootUserEntity = getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)){
            rootUserEntity = new OpsHostUserEntity();
            rootUserEntity.setUsername("root");
            rootUserEntity.setPassword(hostUserBody.getPassword());
        }

        if (StrUtil.isEmpty(rootUserEntity.getPassword())){
            if (StrUtil.isNotEmpty(hostUserBody.getRootPassword())){
                rootUserEntity.setPassword(hostUserBody.getRootPassword());
            }else {
                throw new OpsException("root password does not exist");
            }
        }

        Boolean sudo = getSudo(hostEntity.getPublicIp(),hostEntity.getPort(),rootUserEntity,hostUserEntity.getUsername());
        OpsHostUserEntity newEntity = hostUserBody.toEntity(sudo);
        newEntity.setHostId(hostUserEntity.getHostId());
        newEntity.setHostUserId(hostUserEntity.getHostUserId());

        if (physicalExist(hostEntity.getPublicIp(), hostEntity.getPort(), rootUserEntity, newEntity.getUsername())) {
            try {
                Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserBody.getUsername(), encryptionUtils.decrypt(hostUserBody.getPassword())).orElseThrow(() -> new OpsException("user already exists，incorrect password, please enter correct password"));
                if (Objects.isNull(session)) {
                    throw new OpsException("user already exists , incorrect password, please enter correct password");
                } else {
                    session.disconnect();
                }
            } catch (Exception e) {
                log.error("connection exception:", e);
                throw new OpsException("user already exists , incorrect password, please enter correct password");
            }
        } else {
            createPhysicalUser(hostEntity.getPublicIp(), hostEntity.getPort(), rootUserEntity, newEntity.getUsername(), hostUserBody.getRootPassword());
        }

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

        if ("root".equals(hostUserEntity.getUsername())) {
            throw new OpsException("Can't delete root user");
        }

        return removeById(hostUserId);
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
    public void cleanPassword(String hostUserId) {
        final LambdaUpdateWrapper<OpsHostUserEntity> updateWrapper = Wrappers.lambdaUpdate(OpsHostUserEntity.class)
                .set(OpsHostUserEntity::getPassword, null)
                .eq(OpsHostUserEntity::getHostUserId, hostUserId);
        update(updateWrapper);
    }
}
