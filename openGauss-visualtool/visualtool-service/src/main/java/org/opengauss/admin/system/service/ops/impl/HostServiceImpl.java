package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.core.handler.ops.cache.SSHChannelManager;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.mapper.ops.OpsHostMapper;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/7 22:28
 **/
@Slf4j
@Service
public class HostServiceImpl extends ServiceImpl<OpsHostMapper, OpsHostEntity> implements IHostService {

    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private OpsHostMapper hostMapper;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Autowired
    private IOpsClusterService clusterService;
    @Autowired
    private WsConnectorManager wsConnectorManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(HostBody hostBody) {
        OpsHostEntity hostEntity = getByPublicIp(hostBody.getPublicIp());
        if (Objects.nonNull(hostEntity) && hostEntity.getPort().equals(hostBody.getPort())) {
            throw new OpsException("host[" + hostEntity.getPublicIp() + "]already exists, please do not add it again");
        }

        Session session = jschUtil.getSession(hostBody.getPublicIp(), hostBody.getPort(), "root", encryptionUtils.decrypt(hostBody.getPassword())).orElseThrow(() -> new OpsException("Failed to establish a session with the host"));
        try {
            hostEntity = hostBody.toHostEntity(getHostName(session),getOS(session),getCpuArch(session));
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
        save(hostEntity);

        OpsHostUserEntity hostUserEntity = hostBody.toRootUser(hostEntity.getHostId());
        return hostUserService.save(hostUserEntity);
    }

    private String getCpuArch(Session rootSession) {
        String command = "lscpu | grep Architecture: | head -n 1 | awk -F ':' '{print $2}'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode()!=0){
                log.error("Failed to get cpu architecture information,exitCode:{},res:{}",jschResult.getExitCode(),jschResult.getResult());
                throw new OpsException("Failed to get cpu architecture information");
            }

            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to get cpu architecture information",e);
            throw new OpsException("Failed to get cpu architecture information");
        }
    }

    private String getOS(Session rootSession) {
        String command = "cat /etc/os-release | grep ID= | head -n 1 | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode()!=0){
                log.error("Failed to get system information,exitCode:{},res:{}",jschResult.getExitCode(),jschResult.getResult());
                throw new OpsException("Failed to get system information");
            }

            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to get system information",e);
            throw new OpsException("Failed to get system information");
        }
    }

    private String getHostName(Session session) {
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.HOSTNAME, session);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to obtain host name, exit code: {}", jschResult.getExitCode());
                throw new OpsException("Failed to obtain the host name");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to obtain the host name：{}", e);
            throw new OpsException("Failed to obtain the host name");
        }
    }

    @Override
    public boolean ping(HostBody hostBody) {
        Session session = jschUtil.getSession(hostBody.getPublicIp(), hostBody.getPort(), "root", encryptionUtils.decrypt(hostBody.getPassword())).orElse(null);
        if (session != null) {
            session.disconnect();
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(String hostId) {
        if (clusterService.countByHostId(hostId) > 0) {
            throw new OpsException("The host is being used by the cluster");
        }
        removeById(hostId);
        hostUserService.removeByHostId(hostId);
        return true;
    }

    @Override
    public boolean ping(String hostId, String rootPassword) {
        OpsHostEntity hostEntity = getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostId(hostId);
        if (CollUtil.isEmpty(opsHostUserEntities)) {
            throw new OpsException("User information not found");
        }

        OpsHostUserEntity root = opsHostUserEntities.stream().filter(opsHostUserEntity -> "root".equals(opsHostUserEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("root user information not found"));
        root.setPassword(rootPassword);

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(root.getPassword())).orElse(null);
        if (session != null) {
            session.disconnect();
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(String hostId, HostBody hostBody) {
        OpsHostEntity hostEntity = getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostId(hostId);
        OpsHostUserEntity root = null;
        if (CollUtil.isNotEmpty(opsHostUserEntities)) {
            root = opsHostUserEntities.stream().filter(hostUser -> "root".equals(hostUser.getUsername())).findFirst().orElse(null);
        }

        if (Objects.isNull(root)) {
            root = hostBody.toRootUser(hostId);
            hostUserService.save(root);
        }else {
            if (Objects.nonNull(hostBody.getIsRemember()) && hostBody.getIsRemember()){
                root.setPassword(hostBody.getPassword());
                hostUserService.updateById(root);
            }else {
                hostUserService.cleanPassword(root.getHostUserId());
            }
        }

        Session session = jschUtil.getSession(hostBody.getPublicIp(), hostBody.getPort(), "root", encryptionUtils.decrypt(hostBody.getPassword())).orElseThrow(() -> new OpsException("Failed to establish a session with the host"));
        OpsHostEntity newHostEntity = hostBody.toHostEntity(getHostName(session),getOS(session),getCpuArch(session));
        newHostEntity.setHostId(hostId);
        updateById(newHostEntity);

        return true;
    }

    @Override
    public IPage<OpsHostVO> pageHost(Page page, String name) {
        return hostMapper.pageHost(page, name);
    }

    @Override
    public void ssh(SSHBody sshBody) {
        WsSession wsSession = wsConnectorManager.getSession(sshBody.getBusinessId()).orElseThrow(()->new OpsException("websocket session not exist"));

        Session session = jschUtil.getSession(sshBody.getIp(), sshBody.getSshPort(), sshBody.getSshUsername(), encryptionUtils.decrypt(sshBody.getSshPassword()))
                .orElseThrow(() -> new OpsException("Failed to establish session with host"));

        ChannelShell channelShell = jschUtil.openChannelShell(session);

        SSHChannelManager.registerChannelShell(sshBody.getBusinessId(),channelShell);

        Future<?> future = threadPoolTaskExecutor.submit(() -> jschUtil.channelToWsSession(channelShell, wsSession));

        TaskManager.registry(sshBody.getBusinessId(), future);
    }

    @Override
    public Map<String, String> mapOsByIps(Set<String> ipSet) {
        if (CollUtil.isEmpty(ipSet)){
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class)
                .in(OpsHostEntity::getPublicIp, ipSet);

        List<OpsHostEntity> hostList = list(queryWrapper);
        return hostList.stream().collect(Collectors.toMap(OpsHostEntity::getPublicIp,OpsHostEntity::getOs));
    }

    @Override
    public List<OpsHostEntity> listAll(String azId) {
        LambdaQueryWrapper<OpsHostEntity> queryWrapper =
                Wrappers.lambdaQuery(OpsHostEntity.class)
                        .eq(StrUtil.isNotEmpty(azId), OpsHostEntity::getAzId, azId);

        List<OpsHostEntity> list = list(queryWrapper);
        populateIsRemember(list);
        return list;
    }

    private void populateIsRemember(List<OpsHostEntity> list) {
        final List<String> hostIds = list.stream().map(OpsHostEntity::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIds)){
            final List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostIdList(hostIds);
            List<String> isRememberHostIds = new ArrayList<>();
            for (OpsHostUserEntity opsHostUserEntity : opsHostUserEntities) {
                final String username = opsHostUserEntity.getUsername();
                if ("root".equalsIgnoreCase(username) && StrUtil.isNotEmpty(opsHostUserEntity.getPassword())){
                    isRememberHostIds.add(opsHostUserEntity.getHostId());
                }
            }

            for (OpsHostEntity opsHostEntity : list) {
                opsHostEntity.setIsRemember(isRememberHostIds.contains(opsHostEntity.getHostId()));
            }
        }
    }

    private OpsHostEntity getByPublicIp(String publicIp) {
        if (StrUtil.isEmpty(publicIp)) {
            return null;
        }

        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class)
                .eq(OpsHostEntity::getPublicIp, publicIp);
        return getOne(queryWrapper, false);
    }

}
