package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
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

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(HostBody hostBody) {
        OpsHostEntity hostEntity = getByPublicIp(hostBody.getPublicIp());
        if (Objects.nonNull(hostEntity) && hostEntity.getPort().equals(hostBody.getPort())) {
            throw new OpsException("host[" + hostEntity.getPublicIp() + "]already exists, please do not add it again");
        }

        hostEntity = hostBody.toHostEntity(jschUtil, getHostName(jschUtil, encryptionUtils, hostBody.getPublicIp(), hostBody.getPort(), hostBody.getPassword()));
        save(hostEntity);

        OpsHostUserEntity hostUserEntity = hostBody.toRootUser(hostEntity.getHostId());
        return hostUserService.save(hostUserEntity);
    }

    private String getHostName(JschUtil jschUtil, EncryptionUtils encryptionUtils, @NotEmpty(message = "The IP address cannot be empty") String publicIp, Integer port, String password) {
        Session session = jschUtil.getSession(publicIp, port, "root", encryptionUtils.decrypt(password)).orElseThrow(() -> new OpsException("Failed to establish a session with the host. Procedure"));
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
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
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
        }

        OpsHostEntity newHostEntity = hostBody.toHostEntity(jschUtil, getHostName(jschUtil, encryptionUtils, hostBody.getPublicIp(), hostBody.getPort(), hostBody.getPassword()));
        newHostEntity.setHostId(hostId);
        updateById(newHostEntity);

        root.setPassword(hostBody.getPassword());
        hostUserService.saveOrUpdate(root);

        return true;
    }

    @Override
    public IPage<OpsHostVO> pageHost(Page page, String name) {
        return hostMapper.pageHost(page, name);
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
