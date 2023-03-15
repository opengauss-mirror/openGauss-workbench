package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
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
import org.opengauss.admin.common.core.domain.model.ops.host.HostMonitorVO;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.common.core.handler.ops.cache.SSHChannelManager;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsHostMapper;
import org.opengauss.admin.system.service.ops.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private IOpsHostTagRelService opsHostTagRelService;
    @Autowired
    private IOpsHostTagService opsHostTagService;

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
        opsHostTagRelService.cleanHostTag(hostEntity.getHostId());
        opsHostTagService.addTag(HostTagInputDto.of(hostBody.getTags(),hostEntity.getHostId()));
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
        if (StrUtil.isEmpty(root.getPassword())){
            if (StrUtil.isEmpty(rootPassword)){
                throw new OpsException("root password does not exist");
            }else {
                root.setPassword(rootPassword);
            }
        }

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

        opsHostTagRelService.cleanHostTag(hostId);
        opsHostTagService.addTag(HostTagInputDto.of(hostBody.getTags(),hostId));

        return true;
    }

    @Override
    public IPage<OpsHostVO> pageHost(Page page, String name, Set<String> tagIds, String os) {
        if (Objects.isNull(tagIds)){
            tagIds = Collections.emptySet();
        }
        final IPage<OpsHostVO> opsHostVOIPage = hostMapper.pageHost(page, name, tagIds, os, tagIds.size());
        final List<OpsHostVO> records = opsHostVOIPage.getRecords();
        populateIsRememberVO(records);
        populateTags(records);
        return opsHostVOIPage;
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
    public void ssh(String hostId, SSHBody sshBody) {
        OpsHostEntity hostEntity = getById(hostId);
        if (Objects.isNull(hostEntity)){
            throw new OpsException("host info not found");
        }

        String password = null;

        OpsHostUserEntity hostUserEntity = hostUserService.getHostUserByUsername(hostId,sshBody.getSshUsername());
        if (Objects.isNull(hostUserEntity)){
            throw new OpsException("host user " +sshBody.getSshUsername()+ " info not found");
        }

        password = hostUserEntity.getPassword();
        if (StrUtil.isEmpty(password)){
            password = sshBody.getSshPassword();
        }

        if (StrUtil.isEmpty(password)){
            throw new OpsException("user " +sshBody.getSshUsername()+ " password not found");
        }


        WsSession wsSession = wsConnectorManager.getSession(sshBody.getBusinessId()).orElseThrow(()->new OpsException("websocket session not exist"));

        Session session = jschUtil.getSession(sshBody.getIp(), sshBody.getSshPort(), sshBody.getSshUsername(), encryptionUtils.decrypt(password))
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

    @Override
    public Map<String,Object> monitor(String hostId, String businessId, String rootPassword) {
        Map<String,Object> res = new HashMap<>();
        res.put("res",true);

        OpsHostEntity hostEntity = getById(hostId);
        if (Objects.isNull(hostEntity)){
            res.put("res",false);
            res.put("msg","host info not found");
            return res;
        }

        OpsHostUserEntity rootUserEntity = hostUserService.getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)){
            res.put("res",false);
            res.put("msg","root user info not found");
            return res;
        }

        if (StrUtil.isEmpty(rootUserEntity.getPassword())){
            if (StrUtil.isNotEmpty(rootPassword)){
                rootUserEntity.setPassword(rootPassword);
            }else {
                res.put("res",false);
                res.put("msg","root user password not found");
                return res;
            }
        }

        Session rootSession = null;
        Optional<Session> session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), rootUserEntity.getUsername(), encryptionUtils.decrypt(rootUserEntity.getPassword()));
        if (session.isPresent()){
            rootSession = session.get();
        }else {
            res.put("res",false);
            res.put("msg","The root user failed to establish a connection");
            return res;
        }

        WsSession wsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("response session does not exist"));

        Session finalRootSession = rootSession;
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                doMonitor(wsSession, finalRootSession);
            }finally {
                if (Objects.nonNull(finalRootSession) && finalRootSession.isConnected()){
                    try {
                        finalRootSession.disconnect();
                    } catch (Exception ignore) {

                    }
                }

                wsUtil.close(wsSession);
            }
        });
        TaskManager.registry(businessId, future);
        return res;
    }

    private void doMonitor(WsSession wsSession, Session rootSession) {
        while (wsSession.getSession().isOpen()){
            HostMonitorVO hostMonitorVO = new HostMonitorVO();
            CountDownLatch countDownLatch = new CountDownLatch(4);

            threadPoolTaskExecutor.submit(()->{
                try {
                    String[] net = netMonitor(rootSession);
                    hostMonitorVO.setUpSpeed(net[0]);
                    hostMonitorVO.setDownSpeed(net[1]);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(()->{
                try {
                    hostMonitorVO.setCpu(cpuMonitor(rootSession));
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(()->{
                try {
                    hostMonitorVO.setMemory(memoryMonitor(rootSession));
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(()->{
                try {
                    hostMonitorVO.setDisk(diskMonitor(rootSession));
                }finally {
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted", e);
                throw new OpsException("monitor error");
            }

            wsUtil.sendText(wsSession, JSON.toJSONString(hostMonitorVO));
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        }
    }

    private String diskMonitor(Session rootSession) {
        String command = "df -Th | egrep -v \"(tmpfs|sr0)\" | tail -n +2|tr -s \" \" | cut -d \" \" -f6|tr -d \"%\"";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (0==jschResult.getExitCode()){
                return jschResult.getResult();
            }else {
                log.error("disk monitor error,exitCode:{},exitMsg:{}",jschResult.getExitCode(),jschResult.getResult());
            }
        }catch (Exception e){
            log.error("disk monitor error:",e);
        }
        throw new OpsException("disk monitor error");
    }

    private String memoryMonitor(Session rootSession) {
        String command = "free -m | awk -F '[ :]+' 'NR==2{printf \"%d\", ($2-$7)/$2*100}'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (0==jschResult.getExitCode()){
                return jschResult.getResult();
            }else {
                log.error("memory monitor error,exitCode:{},exitMsg:{}",jschResult.getExitCode(),jschResult.getResult());
            }
        }catch (Exception e){
            log.error("memory monitor error:",e);
        }
        throw new OpsException("memory monitor error");
    }

    private String cpuMonitor(Session rootSession) {
        String command = "top -b -n1 | fgrep \"Cpu(s)\" | tail -1 | awk -F'id,' '{split($1, vs, \",\"); v=vs[length(vs)]; sub(/\\s+/, \"\", v);sub(/\\s+/, \"\", v); printf \"%d\", 100-v;}'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (0==jschResult.getExitCode()){
                return jschResult.getResult();
            }else {
                log.error("cpu monitor error,exitCode:{},exitMsg:{}",jschResult.getExitCode(),jschResult.getResult());
            }
        }catch (Exception e){
            log.error("cpu monitor error:",e);
        }
        throw new OpsException("cpu monitor error");
    }

    private String[] netMonitor(Session rootSession) {
        String[] res = new String[2];

        String netCardName;
        String netCardNameCommand = "cat /proc/net/dev | awk '{i++; if(i>2){print $1}}' | sed 's/^[\\t]*//g' | sed 's/[:]*$//g' | head -n 1";
        try {
            JschResult jschResult = jschUtil.executeCommand(netCardNameCommand, rootSession);
            if (0==jschResult.getExitCode()){
                netCardName = jschResult.getResult().trim();
            }else {
                log.error("Failed to get network card name,exitCode:{},exitMsg:{}",jschResult.getExitCode(),jschResult.getResult());
                throw new OpsException("Failed to get network card name");
            }
        }catch (Exception e){
            throw new OpsException("cpu monitor error");
        }

        String command = "rx_net1=$(ifconfig "+netCardName+" | awk '/RX packets/{print $5}') && tx_net1=$(ifconfig "+netCardName+" | awk '/TX packets/{print $5}') && sleep 1 && rx_net2=$(ifconfig "+netCardName+" | awk '/RX packets/{print $5}') && tx_net2=$(ifconfig "+netCardName+" | awk '/TX packets/{print $5}') && rx_net=$[($rx_net2-$rx_net1)] && tx_net=$[($tx_net2-$tx_net1)] && echo \"$rx_net|$tx_net\"";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (0==jschResult.getExitCode()){
                String[] split = jschResult.getResult().split("\\|");
                res[0]= split[0];
                res[1] = split[1];
                return res;
            }else {
                log.error("cpu monitor error,exitCode:{},exitMsg:{}",jschResult.getExitCode(),jschResult.getResult());
            }
        }catch (Exception e){
            log.error("cpu monitor error:",e);
        }
        throw new OpsException("cpu monitor error");
    }

    private void populateTags(List<OpsHostVO> list) {
        List<String> hostIds = list.stream().map(OpsHostVO::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIds)){
            Map<String,Set<String>> hostTagNamesMap = opsHostTagRelService.mapByHostIds(hostIds);
            for (OpsHostVO opsHostVO : list) {
                opsHostVO.setTags(hostTagNamesMap.get(opsHostVO.getHostId()));
            }
        }
    }

    private void populateIsRememberVO(List<OpsHostVO> list) {
        final List<String> hostIds = list.stream().map(OpsHostVO::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIds)){
            final List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostIdList(hostIds);
            List<String> isRememberHostIds = new ArrayList<>();
            for (OpsHostUserEntity opsHostUserEntity : opsHostUserEntities) {
                final String username = opsHostUserEntity.getUsername();
                if ("root".equalsIgnoreCase(username) && StrUtil.isNotEmpty(opsHostUserEntity.getPassword())){
                    isRememberHostIds.add(opsHostUserEntity.getHostId());
                }
            }

            for (OpsHostVO opsHostEntity : list) {
                opsHostEntity.setIsRemember(isRememberHostIds.contains(opsHostEntity.getHostId()));
            }
        }
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
