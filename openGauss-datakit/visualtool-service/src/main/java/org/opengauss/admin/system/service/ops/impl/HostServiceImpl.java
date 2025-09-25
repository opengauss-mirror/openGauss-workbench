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
 * HostServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/HostServiceImpl
 * .java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.*;
import org.opengauss.admin.common.core.domain.model.ops.host.HostMonitorVO;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.common.core.handler.ops.cache.SSHChannelManager;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.core.vo.HostInfoVo;
import org.opengauss.admin.common.enums.OsSupportMap;
import org.opengauss.admin.common.enums.agent.AgentStatus;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.excel.ImportAsynInfoUtils;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.domain.HostRecordDataListener;
import org.opengauss.admin.system.mapper.ops.OpsHostMapper;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.HostMonitorCacheService;
import org.opengauss.admin.system.service.JschExecutorService;
import org.opengauss.admin.system.service.ops.*;
import org.opengauss.agent.service.IAgentInstallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * HostService
 *
 * @author lhf
 * @date 2022/8/7 22:28
 **/
@Slf4j
@Service
public class HostServiceImpl extends ServiceImpl<OpsHostMapper, OpsHostEntity> implements IHostService {
    private final Object sessionLock = new Object();
    private String isSwitchingLanguage;
    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private OpsHostMapper hostMapper;
    @Autowired
    private JschExecutorService jschExecutorService;
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Autowired
    @Lazy
    private IOpsClusterService clusterService;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @Lazy
    private IOpsHostTagRelService opsHostTagRelService;
    @Autowired
    @Lazy
    private IOpsHostTagService opsHostTagService;
    @Autowired
    @Lazy
    private HostMonitorCacheService hostMonitorCacheService;
    @Autowired
    @Lazy
    private IAgentInstallService agentInstallService;

    private Cache<String, List<ErrorHostRecord>> errorExcel = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(HostBody hostBody) {
        OpsHostEntity hostEntity = getByPublicIp(hostBody.getPublicIp());
        if (Objects.nonNull(hostEntity) && hostEntity.getPort().equals(hostBody.getPort())) {
            throw new OpsException("host[" + hostEntity.getPublicIp() + "]already exists, please do not add it again");
        }
        OpsAssert.isTrue(StrUtil.isNotEmpty(hostBody.getUsername()), "username is empty");
        SshLogin sshLogin = new SshLogin(hostBody.getPublicIp(), hostBody.getPort(), hostBody.getUsername(),
            encryptionUtils.decrypt(hostBody.getPassword()));
        hostEntity = hostBody.toHostEntity(buildHostInfo(sshLogin));
        save(hostEntity);
        opsHostTagRelService.cleanHostTag(hostEntity.getHostId());
        opsHostTagService.addTag(HostTagInputDto.of(hostBody.getTags(), hostEntity.getHostId()));
        HostUserBody hostUserBody = hostBody.toUserBody(hostEntity.getHostId());
        return hostUserService.add(hostEntity, hostUserBody);
    }

    @Override
    public void invokeFile(String uuid, HashMap<String, InputStream> fileStreamMap, String currentLocale) {
        isSwitchingLanguage = currentLocale;
        ExecutorService executor = Executors.newCachedThreadPool();
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(attributes, true);
        final InputStream inputStream = fileStreamMap.get(uuid);
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    startInvoke(inputStream, uuid);
                } catch (OpsException e) {
                    log.error("Unable to import.", e);
                    ImportAsynInfoUtils.getAsynInfo(uuid).setMsg(e.getMessage());
                    ImportAsynInfoUtils.getAsynInfo(uuid).setEnd(true);
                    throw new OpsException("Unable to import.");
                }
            }
        });
    }

    @Override
    public void downloadTemplate(HttpServletResponse response, String currentLocale) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            OutputStream out = response.getOutputStream();
            if ("zh-CN".equals(currentLocale)) {
                EasyExcel.write(out, HostRecord.class).sheet("模板").doWrite(new ArrayList<HostRecord>());
            } else {
                EasyExcel.write(out, HostRecord.class)
                    .head(head(0))
                    .sheet("Template")
                    .doWrite(new ArrayList<HostRecord>());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("Download template failed.", e);
            throw new OpsException("Download template failed.");
        }
    }

    @Override
    public void downloadErrorExcel(HttpServletResponse response, String uuid) {
        List<ErrorHostRecord> errorhostRecords = errorExcel.getIfPresent(uuid);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("错误报告" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()),
                StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            OutputStream out = response.getOutputStream();
            if ("zh-CN".equals(isSwitchingLanguage)) {
                EasyExcel.write(out, ErrorHostRecord.class).sheet("错误报告").doWrite(errorhostRecords);
            } else {
                EasyExcel.write(out, ErrorHostRecord.class)
                    .head(head(1))
                    .sheet("Error Report")
                    .doWrite(errorhostRecords);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("Download failed.", e);
            throw new OpsException("Download failed.");
        }
    }

    private void startInvoke(InputStream inputStream, String uuid) {
        try {
            List<HostRecord> hostRecords = readExcelFile(inputStream);
            if (CollectionUtils.isEmpty(hostRecords) || hostRecords.size() == 0) {
                log.error("Failed to parse data.");
                throw new OpsException("Failed to parse data.");
            }
            ImportAsynInfoUtils.getAsynInfo(uuid).setTotality(hostRecords.size());
            Map<String, List<HostRecord>> groupedHostLists = groupHostRecordsByIP(hostRecords);
            invoke(groupedHostLists, uuid);
            ImportAsynInfoUtils.getAsynInfo(uuid).setEnd(true);
        } catch (IllegalArgumentException e) {
            log.error("File format mismatch.", e);
            throw new OpsException("The format of the file you are trying to import does not match the template. "
                + "Please re-download the template and fill it out again.");
        } catch (ExcelDataConvertException e) {
            log.error("Data parsing format error.", e);
            throw new OpsException(e.getRowIndex() + "row" + (e.getColumnIndex() + 1)
                + "column,parsing exception, please modify and re-upload.");
        }
    }

    private List<HostRecord> readExcelFile(InputStream inputStream) {
        HostRecordDataListener hostRecordDataListener = new HostRecordDataListener(this, isSwitchingLanguage);
        try {
            return EasyExcel.read(inputStream, HostRecord.class, hostRecordDataListener).sheet().doReadSync();
        } catch (Exception e) {
            log.error("Failed to parse data.", e);
            throw new OpsException("Failed to parse data.");
        }
    }

    private Map<String, List<HostRecord>> groupHostRecordsByIP(List<HostRecord> hostRecords) {
        Map<String, List<HostRecord>> map = new HashMap<>();
        for (HostRecord hostRecord : hostRecords) {
            List<HostRecord> list = map.getOrDefault(hostRecord.getPublicIp(), new ArrayList<>());
            list.add(hostRecord);
            map.put(hostRecord.getPublicIp(), list);
        }
        return map;
    }

    private void invoke(Map<String, List<HostRecord>> groupedHostLists, String uuid) {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        List<Future<?>> futures = new ArrayList<>();
        for (List<HostRecord> list : groupedHostLists.values()) {
            Future<?> future = executorService.submit(() -> {
                for (HostRecord hostRecord : list) {
                    try {
                        invokeRecord(hostRecord, uuid);
                        ImportAsynInfoUtils.addSuccessSum(uuid);
                    } catch (OpsException e) {
                        log.error("Record of failure.", e);
                        ImportAsynInfoUtils.addErrorSum(uuid);
                        hostRecord.setRemark(e.getMessage());
                        addHostRecordToErrorMap(uuid, errorExcel, hostRecord);
                    } finally {
                        ImportAsynInfoUtils.addDoneSum(uuid);
                    }
                }
            });
            futures.add(future);
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to obtain the thread execution result.", e);
                throw new OpsException(e.getMessage());
            }
        }
        executorService.shutdown();
    }

    private void invokeRecord(@Validated HostRecord hostRecord, String uuid) {
        if (!checkRecord(hostRecord)) {
            throw new OpsException("Some attributes have not been filled in.");
        }
        OpsHostEntity hostEntity = getByPublicIp(hostRecord.getPublicIp());
        if (Objects.isNull(hostEntity)) {
            addHostInfo(hostRecord);
        } else {
            addUserInfo(hostRecord, hostEntity);
        }
    }

    private void addHostInfo(HostRecord hostRecord) {
        SshLogin sshLogin = new SshLogin(hostRecord.getPublicIp(), hostRecord.getPort(), hostRecord.getUserName(),
            encryptionUtils.decrypt(hostRecord.getPassword()));
        hostRecord.toHostEntity(getHostInfoVo(sshLogin));
        HostBody hostBody = hostRecord.toHostBody();
        hostBody.setPassword(encryptionUtils.encrypt(hostBody.getPassword()));
        add(hostBody);
    }

    private void addUserInfo(HostRecord hostRecord, OpsHostEntity hostEntity) {
        HostUserBody hostUserBody = new HostUserBody();
        hostUserBody.setHostId(hostEntity.getHostId());
        hostUserBody.setUsername(hostRecord.getUserName());
        hostUserBody.setPassword(encryptionUtils.encrypt(hostRecord.getPassword()));
        hostUserService.add(hostEntity, hostUserBody);
        List<String> tags = addAllTagsToNewCollection(hostRecord.getTags());
        opsHostTagService.addTag(HostTagInputDto.of(tags, hostEntity.getHostId()));
    }

    private List<List<String>> createBaseHeaders() {
        return new ArrayList<>(
            Arrays.asList(List.of("id"), List.of("name"), List.of("privateIP"), List.of("publicIP"), List.of("port"),
                List.of("userName"), List.of("password"), List.of("isAdmin(yes|no)"), List.of("tags"),
                List.of("remark")));
    }

    private List<List<String>> head(int tempOrerro) {
        List<List<String>> headers = createBaseHeaders();
        if (tempOrerro == 1) {
            headers.add(List.of("timestamp"));
        }
        return headers;
    }

    private synchronized void addHostRecordToErrorMap(String uuid, Cache<String, List<ErrorHostRecord>> errorExcel,
        HostRecord hostRecord) {
        List<ErrorHostRecord> list = errorExcel.getIfPresent(uuid);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(hostRecord.toErrorHostRecord());
        errorExcel.put(uuid, list);
    }

    private Boolean checkRecord(HostRecord hostRecord) {
        return Objects.nonNull(hostRecord.getName()) && Objects.nonNull(hostRecord.getIsAdmin()) && Objects.nonNull(
            hostRecord.getPublicIp()) && Objects.nonNull(hostRecord.getPrivateIp()) && Objects.nonNull(
            hostRecord.getPort()) && Objects.nonNull(hostRecord.getUserName()) && Objects.nonNull(
            hostRecord.getPassword());
    }

    private List<String> addAllTagsToNewCollection(List<String> tags) {
        List<String> hostTags = new ArrayList<>();
        if (tags != null) {
            hostTags.addAll(tags);
        }
        return hostTags;
    }

    private HostInfoVo buildHostInfo(SshLogin sshLogin) {
        HostInfoVo hostInfoVo = new HostInfoVo();
        try {
            log.info("get host info {}", sshLogin);
            hostInfoVo = getHostInfoVo(sshLogin);
        } catch (OpsException e) {
            log.error("Failed to get host info", e);
        }
        return hostInfoVo;
    }

    private HostInfoVo getHostInfoVo(SshLogin sshLogin) {
        HostInfoVo hostInfoVo = new HostInfoVo();
        hostInfoVo.setHostname(jschExecutorService.getHostname(sshLogin));
        hostInfoVo.setCpuArch(jschExecutorService.getCpuArch(sshLogin));
        hostInfoVo.setOs(jschExecutorService.getOs(sshLogin));
        hostInfoVo.setOsVersion(jschExecutorService.getOsVersion(sshLogin));
        return hostInfoVo;
    }

    @Override
    public boolean ping(HostBody hostBody) {
        SshLogin sshLogin = new SshLogin(hostBody.getPublicIp(), hostBody.getPort(), hostBody.getUsername(),
            encryptionUtils.decrypt(hostBody.getPassword()));
        return jschExecutorService.checkOsUserExist(sshLogin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(String hostId) {
        if (clusterService.countByHostId(hostId) > 0) {
            throw new OpsException("The host is being used by the cluster");
        }
        if (agentInstallService.hasInstall(hostId)) {
            throw new OpsException("The host must be uninstalled the agent before deleting host");
        }
        removeById(hostId);
        hostUserService.removeByHostId(hostId);
        hostMonitorCacheService.deleteHostCache(hostId);
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
        OpsHostUserEntity userEntity = opsHostUserEntities.stream().findAny().get();
        SshLogin pingLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
            encryptionUtils.decrypt(userEntity.getPassword()));
        return jschExecutorService.checkOsUserExist(pingLogin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(String hostId, HostBody hostBody) {
        OpsHostEntity hostEntity = checkHostExist(hostId);
        if (StringUtils.isBlank(hostBody.getName())) {
            throw new OpsException("Host name cannot be empty");
        }
        if (ObjectUtils.isEmpty(hostBody.getPort())) {
            throw new OpsException("Host port cannot be empty");
        }
        if (!hostEntity.getPrivateIp().equals(hostBody.getPrivateIp()) || !hostEntity.getPublicIp()
            .equals(hostBody.getPublicIp())) {
            throw new OpsException("Host public and private IP cannot be modified");
        }
        if (StringUtils.isAnyBlank(hostBody.getUsername(), hostBody.getPassword())) {
            throw new OpsException("Host username and password cannot be empty");
        }
        SshLogin sshLogin = new SshLogin(hostBody.getPublicIp(), hostBody.getPort(), hostBody.getUsername(),
            encryptionUtils.decrypt(hostBody.getPassword()));
        HostInfoVo hostInfoVo = getHostInfoVo(sshLogin);
        hostEntity.setName(hostBody.getName());
        hostEntity.setPort(hostBody.getPort());
        hostEntity.setRemark(hostBody.getRemark());
        hostEntity.setOs(hostInfoVo.getOs());
        hostEntity.setOsVersion(hostInfoVo.getOsVersion());
        hostEntity.setCpuArch(hostInfoVo.getCpuArch());
        hostEntity.setHostname(hostInfoVo.getHostname());
        updateById(hostEntity);
        opsHostTagRelService.cleanHostTag(hostId);
        opsHostTagService.addTag(HostTagInputDto.of(hostBody.getTags(), hostId));
        return true;
    }

    @Override
    public IPage<OpsHostVO> pageHost(Page page, String name, Set<String> tagIds, String os) {
        if (Objects.isNull(tagIds)) {
            tagIds = Collections.emptySet();
        }
        final IPage<OpsHostVO> opsHostVOIPage = hostMapper.pageHost(page, name, tagIds, os, tagIds.size());
        final List<OpsHostVO> records = opsHostVOIPage.getRecords();
        populateIsRememberVO(records);
        populateTags(records);
        populateAgentInstallInfoAndStatus(records);
        return opsHostVOIPage;
    }

    private void populateAgentInstallInfoAndStatus(List<OpsHostVO> records) {
        Map<String, AgentInstallEntity> agentInstallMap = agentInstallService.queryAgentInstallInfo();
        for (OpsHostVO record : records) {
            AgentInstallEntity agentInstall = agentInstallMap.get(record.getHostId());
            if (Objects.isNull(agentInstall)) {
                record.setAgentStatus(AgentStatus.UNINSTALL);
            } else {
                record.setAgentName(agentInstall.getAgentName());
                record.setAgentInstallPort(agentInstall.getAgentPort());
                record.setAgentStatus(agentInstall.getStatus());
                record.setAgentInstallPath(agentInstall.getInstallPath());
                record.setAgentInstallUsername(agentInstall.getInstallUser());
            }
        }
    }

    @Override
    public void ssh(SSHBody sshBody) {
        WsSession wsSession = wsConnectorManager.getSession(sshBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        Session session = jschExecutorService.createSession(
            new SshLogin(sshBody.getIp(), sshBody.getSshPort(), sshBody.getSshUsername(),
                encryptionUtils.decrypt(sshBody.getSshPassword())));
        ChannelShell channelShell = jschExecutorService.openChannelShell(session);
        SSHChannelManager.registerChannelShell(sshBody.getBusinessId(), channelShell);
        Future<?> future = threadPoolTaskExecutor.submit(
            () -> jschExecutorService.channelToWsSession(channelShell, wsSession));
        TaskManager.registry(sshBody.getBusinessId(), future);
    }

    @Override
    public void ssh(String hostId, SSHBody sshBody) {
        OpsHostEntity hostEntity = getById(hostId);
        OpsAssert.nonNull(hostEntity, "host info not found");
        OpsAssert.isTrue(StrUtil.isNotEmpty(sshBody.getSshUsername()), "ssh username can not be empty");
        String password = null;
        OpsHostUserEntity hostUserEntity = hostUserService.getHostUserByUsername(hostId, sshBody.getSshUsername());
        OpsAssert.nonNull(hostUserEntity, "host user " + sshBody.getSshUsername() + " info not found");
        password = hostUserEntity.getPassword();
        if (StrUtil.isEmpty(password)) {
            password = sshBody.getSshPassword();
        }
        OpsAssert.isTrue(StrUtil.isNotEmpty(password), "ssh username password can not be empty");
        WsSession wsSession = wsConnectorManager.getSession(sshBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        Session session = jschExecutorService.createSession(
            new SshLogin(sshBody.getIp(), sshBody.getSshPort(), sshBody.getSshUsername(),
                encryptionUtils.decrypt(password)));
        ChannelShell channelShell = jschUtil.openChannelShell(session);
        SSHChannelManager.registerChannelShell(sshBody.getBusinessId(), channelShell);
        Future<?> future = threadPoolTaskExecutor.submit(() -> jschUtil.channelToWsSession(channelShell, wsSession));
        TaskManager.registry(sshBody.getBusinessId(), future);
    }

    @Override
    public Map<String, String> mapOsByIps(Set<String> ipSet) {
        if (CollUtil.isEmpty(ipSet)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class)
            .in(OpsHostEntity::getPublicIp, ipSet);
        List<OpsHostEntity> hostList = list(queryWrapper);
        return hostList.stream()
            .filter(host -> StrUtil.isNotEmpty(host.getOs()))
            .collect(Collectors.toMap(OpsHostEntity::getPublicIp, OpsHostEntity::getOs));
    }

    @Override
    public List<OpsHostEntity> listAll(String azId) {
        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class);
        List<OpsHostEntity> list = list(queryWrapper);
        populateIsRemember(list);
        return list;
    }

    @Override
    public Map<String, Object> pageMonitor(List<String> hostIds, String businessId) {
        Map<String, Object> res = new HashMap<>();
        res.put("res", true);
        List<String> pageHostList;
        try {
            pageHostList = list().stream()
                .filter(hostEntity -> hostIds.contains(hostEntity.getHostId()))
                .map(OpsHostEntity::getHostId)
                .collect(Collectors.toList());
            if (pageHostList.size() != hostIds.size()) {
                res.put("miss_msg", "some host ids not found");
                res.put("miss", CollUtil.subtract(hostIds, pageHostList));
            }
        } catch (IllegalArgumentException | OpsException e) {
            res.put("res", false);
            res.put("msg", e.getMessage());
            return res;
        }
        Runnable worker = TaskManager.getRegistryWorker(businessId);
        if (worker == null) {
            WsSession wsSession = wsConnectorManager.getSession(businessId)
                .orElseThrow(() -> new OpsException("response session does not exist"));
            worker = new PageMonitorWorker(wsSession, pageHostList);
            TaskManager.registryWorker(businessId, worker);
            Future<?> future = threadPoolTaskExecutor.submit(worker);
            TaskManager.registry(businessId, future);
        } else {
            if (worker instanceof PageMonitorWorker pageMonitorWorker) {
                pageMonitorWorker.resetHostIds(pageHostList);
            } else {
                throw new OpsException("worker is not PageMonitorWorker");
            }
        }
        return res;
    }

    /**
     * PageMonitorWorker
     */
    @AllArgsConstructor
    class PageMonitorWorker implements Runnable {
        private final WsSession wsSession;
        private final List<String> hostIds;

        /**
         * reset hostIds
         *
         * @param hostIds hostIds
         */
        public void resetHostIds(List<String> hostIds) {
            this.hostIds.clear();
            this.hostIds.addAll(hostIds);
        }

        @Override
        public void run() {
            try {
                doPageMonitor();
            } finally {
                hostIds.clear();
                wsUtil.close(wsSession);
            }
        }

        private void doPageMonitor() {
            while (true) {
                synchronized (sessionLock) {
                    if (!wsSession.getSession().isOpen()) {
                        break;
                    }
                }
                for (String hostId : hostIds) {
                    if (!wsSession.getSession().isOpen()) {
                        break;
                    }
                    HostMonitorVO hostMonitorVO = new HostMonitorVO();
                    try {
                        hostMonitorVO.setHostId(hostId);
                        String[] net = netMonitor(hostId);
                        hostMonitorVO.setUpSpeed(net[0]);
                        hostMonitorVO.setDownSpeed(net[1]);
                        hostMonitorVO.setCpu(cpuMonitor(hostId));
                        hostMonitorVO.setMemory(memoryMonitor(hostId));
                        hostMonitorVO.setDisk(diskMonitor(hostId));
                    } catch (OpsException | NullPointerException ex) {
                        log.error("Failed to collect monitor data for hostId: {}", hostId, ex);
                    }
                    if (wsSession.getSession().isOpen()) {
                        wsUtil.sendText(wsSession, JSON.toJSONString(hostMonitorVO));
                    }
                }
                ThreadUtil.safeSleep(1000L);
            }
        }
    }

    @Override
    public void updateHostOsVersion(OpsHostEntity opsHostEntity) {
        String hostId = opsHostEntity.getHostId();
        List<OpsHostUserEntity> userList = hostUserService.listHostUserByHostId(hostId);
        OpsAssert.isTrue(CollUtil.isNotEmpty(userList), "host does not config user");
        OpsHostUserEntity userEntity = userList.stream().findAny().get();
        SshLogin sshLogin = new SshLogin(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), userEntity.getUsername(),
            encryptionUtils.decrypt(userEntity.getPassword()));
        opsHostEntity.setOsVersion(jschExecutorService.getOsVersion(sshLogin));
        updateById(opsHostEntity);
    }

    private void doMonitor(WsSession wsSession, String hostId) {
        while (wsSession.getSession().isOpen()) {
            HostMonitorVO hostMonitorVO = new HostMonitorVO();
            try {
                String[] net = netMonitor(hostId);
                hostMonitorVO.setUpSpeed(net[0]);
                hostMonitorVO.setDownSpeed(net[1]);
                hostMonitorVO.setCpu(cpuMonitor(hostId));
                hostMonitorVO.setMemory(memoryMonitor(hostId));
                hostMonitorVO.setDisk(diskMonitor(hostId));
            } catch (OpsException | NullPointerException ex) {
                log.error("doMonitor error", ex);
            }
            wsUtil.sendText(wsSession, JSON.toJSONString(hostMonitorVO));
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        }
    }

    private String diskMonitor(String hostId) {
        return hostMonitorCacheService.getDiskUse(hostId);
    }

    private String memoryMonitor(String hostId) {
        return hostMonitorCacheService.getMemoryUsing(hostId);
    }

    private String cpuMonitor(String hostId) {
        return hostMonitorCacheService.getCpuUsing(hostId);
    }

    private String[] netMonitor(String hostId) {
        return hostMonitorCacheService.getNetMonitor(hostId, false);
    }

    private void populateTags(List<OpsHostVO> list) {
        List<String> hostIds = list.stream().map(OpsHostVO::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIds)) {
            Map<String, Set<String>> hostTagNamesMap = opsHostTagRelService.mapByHostIds(hostIds);
            for (OpsHostVO opsHostVO : list) {
                opsHostVO.setTags(hostTagNamesMap.get(opsHostVO.getHostId()));
            }
        }
    }

    private void populateIsRememberVO(List<OpsHostVO> list) {
        List<String> hostIds = list.stream().map(OpsHostVO::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIds)) {
            List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostIdList(hostIds);
            List<String> isRememberHostIds = new ArrayList<>();
            for (OpsHostUserEntity opsHostUserEntity : opsHostUserEntities) {
                String username = opsHostUserEntity.getUsername();
                if ("root".equalsIgnoreCase(username) && StrUtil.isNotEmpty(opsHostUserEntity.getPassword())) {
                    isRememberHostIds.add(opsHostUserEntity.getHostId());
                }
            }
            for (OpsHostVO opsHostEntity : list) {
                opsHostEntity.setIsRemember(isRememberHostIds.contains(opsHostEntity.getHostId()));
            }
        }
    }

    private void populateIsRemember(List<OpsHostEntity> list) {
        List<String> hostIds = list.stream().map(OpsHostEntity::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIds)) {
            List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostIdList(hostIds);
            List<String> isRememberHostIds = new ArrayList<>();
            for (OpsHostUserEntity opsHostUserEntity : opsHostUserEntities) {
                String username = opsHostUserEntity.getUsername();
                if ("root".equalsIgnoreCase(username) && StrUtil.isNotEmpty(opsHostUserEntity.getPassword())) {
                    isRememberHostIds.add(opsHostUserEntity.getHostId());
                }
            }
            for (OpsHostEntity opsHostEntity : list) {
                opsHostEntity.setIsRemember(isRememberHostIds.contains(opsHostEntity.getHostId()));
            }
        }
    }

    @Override
    public OpsHostEntity getByPublicIp(String publicIp) {
        if (StrUtil.isEmpty(publicIp)) {
            return null;
        }
        LambdaQueryWrapper<OpsHostEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostEntity.class)
            .eq(OpsHostEntity::getPublicIp, publicIp);
        return getOne(queryWrapper, false);
    }

    /**
     * build os entity mapped
     *
     * @param hostId hostId
     * @return os entity mapped
     */
    @Override
    public OpsHostEntity getMappedHostEntityById(String hostId) {
        OpsHostEntity opsHostEntity = getById(hostId);
        OsSupportMap osMapEnum = OsSupportMap.of(opsHostEntity.getOs(), opsHostEntity.getOsVersion(),
            opsHostEntity.getCpuArch());
        opsHostEntity.setOs(osMapEnum.getOs());
        opsHostEntity.setOsVersion(osMapEnum.getOsVersion());
        return opsHostEntity;
    }

    @Override
    public void updateHostOsName(String host, String osName) {
        LambdaUpdateWrapper<OpsHostEntity> updateWrapper = Wrappers.lambdaUpdate(OpsHostEntity.class)
            .set(OpsHostEntity::getOs, osName)
            .eq(OpsHostEntity::getPublicIp, host);
        update(updateWrapper);
    }

    @Override
    public void updateHostOsVersion(String host, String osVersion) {
        LambdaUpdateWrapper<OpsHostEntity> updateWrapper = Wrappers.lambdaUpdate(OpsHostEntity.class)
            .set(OpsHostEntity::getOsVersion, osVersion)
            .eq(OpsHostEntity::getPublicIp, host);
        update(updateWrapper);
    }

    @Override
    public void updateHostCpu(String host, String cpuArch, String cpuCoreNum, String cpuFreq) {
        String cpuFreqNum = "";
        if (StrUtil.isNotEmpty(cpuFreq)) {
            cpuFreqNum = cpuFreq.replace("GHz", "");
        }
        LambdaUpdateWrapper<OpsHostEntity> updateWrapper = Wrappers.lambdaUpdate(OpsHostEntity.class)
            .set(StrUtil.isNotEmpty(cpuArch), OpsHostEntity::getCpuArch, cpuArch)
            .set(StrUtil.isNotEmpty(cpuCoreNum), OpsHostEntity::getLogicalCores, cpuCoreNum)
            .set(StrUtil.isNotEmpty(cpuFreq), OpsHostEntity::getCpuFreq, cpuFreqNum)
            .eq(OpsHostEntity::getPublicIp, host);
        update(updateWrapper);
    }

    private OpsHostEntity checkHostExist(String hostId) {
        OpsHostEntity hostEntity = getById(hostId);
        if (Objects.isNull(hostEntity)) {
            log.error("Cannot get host information by host id: {}", hostId);
            throw new OpsException("host information does not exist");
        }
        return hostEntity;
    }
}
