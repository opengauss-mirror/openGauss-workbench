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
 * OpsClusterTaskServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsClusterTaskServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.util.Asserts;
import org.jetbrains.annotations.NotNull;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.enums.ops.ClusterEnvCheckResultEnum;
import org.opengauss.admin.common.enums.ops.OpsClusterTaskStatusEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.DateUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.dto.*;
import org.opengauss.admin.plugin.domain.model.ops.env.EnvProperty;
import org.opengauss.admin.plugin.domain.model.ops.env.HostEnv;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.factory.OperateLogFactory;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterTaskMapper;
import org.opengauss.admin.plugin.service.ops.*;
import org.opengauss.admin.plugin.service.ops.impl.function.CheckDeployTypeService;
import org.opengauss.admin.plugin.service.ops.impl.function.ClusterTaskPathFactory;
import org.opengauss.admin.plugin.service.ops.impl.provider.ProviderManager;
import org.opengauss.admin.plugin.vo.ops.ClusterEnvCheck;
import org.opengauss.admin.plugin.vo.ops.ClusterNodeEnvCheck;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * OpsClusterTaskServiceImpl
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
@Service
public class OpsClusterTaskServiceImpl extends ServiceImpl<OpsClusterTaskMapper, OpsClusterTaskEntity> implements IOpsClusterTaskService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private IOpsClusterTaskNodeService opsClusterTaskNodeService;
    @Resource
    private OpsClusterEnvService opsClusterEnvService;
    @Resource
    private IOpsClusterLogService opsClusterLogService;
    @Resource
    private CheckDeployTypeService checkDeployTypeService;
    @Resource
    private ProviderManager providerManager;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private IOpsPackageManagerV2Service opsPackageManagerV2Service;
    @Resource
    private OpsHostRemoteService opsHostRemoteService;
    @Resource
    private ClusterTaskPathFactory clusterTaskPathFactory;

    @Override
    public IPage<OpsClusterTaskEntity> pageByCondition(Page page, OpsClusterTaskQueryParamDTO dto) {
        // 查询集群任务列表 - 条件组装
        LambdaQueryWrapper<OpsClusterTaskEntity> pageWrapper = Wrappers.lambdaQuery(OpsClusterTaskEntity.class);
        pageWrapper.eq(StrUtil.isNotEmpty(dto.getTaskId()), OpsClusterTaskEntity::getClusterId, dto.getTaskId())
                .eq(StrUtil.isNotEmpty(dto.getClusterName()), OpsClusterTaskEntity::getClusterName, dto.getClusterName())
                .eq(StrUtil.isNotEmpty(dto.getOs()), OpsClusterTaskEntity::getOs, dto.getOs())
                .eq(StrUtil.isNotEmpty(dto.getCpuArch()), OpsClusterTaskEntity::getCpuArch, dto.getCpuArch())
                .eq(StrUtil.isNotEmpty(dto.getOpenGaussVersionNum()), OpsClusterTaskEntity::getVersionNum, dto.getOpenGaussVersionNum())
                .eq(StrUtil.isNotEmpty(dto.getHostId()), OpsClusterTaskEntity::getHostId, dto.getHostId())
                .eq(StrUtil.isNotEmpty(dto.getHostUserId()), OpsClusterTaskEntity::getHostUserId, dto.getHostUserId())
                .eq(dto.getNodeNum() > 0, OpsClusterTaskEntity::getClusterNodeNum, dto.getNodeNum())
                .eq(Objects.nonNull(dto.getOpenGaussVersion()), OpsClusterTaskEntity::getVersion, dto.getOpenGaussVersion());
        OpsClusterTaskStatusEnum status = dto.getStatus();
        if (Objects.isNull(status)) {
            pageWrapper.notIn(OpsClusterTaskEntity::getStatus, OpsClusterTaskStatusEnum.DRAFT);
        } else {
            pageWrapper.eq(OpsClusterTaskEntity::getStatus, status.getStatus());
        }
        pageWrapper.orderByDesc(OpsClusterTaskEntity::getCreateTime);
        Page pageResult = page(page, pageWrapper);
        populateTaskInfo(pageResult.getRecords());
        return pageResult;
    }

    private void populateTaskInfo(List<OpsClusterTaskEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<String> hostIds = new LinkedList<>();
        List<String> hostUserIds = new LinkedList<>();
        collectClusterTaskHostInfos(records, hostIds, hostUserIds);
        Map<String, String> hostMap = queryHostIpByHostIds(hostIds);
        Map<String, String> hostUserMap = queryHostUsernameByHostUserIds(hostUserIds);
        records.forEach(nodeVo -> {
            nodeVo.setHostIp(hostMap.get(nodeVo.getHostId()));
            nodeVo.setHostUsername(hostUserMap.get(nodeVo.getHostUserId()));
        });
    }

    private Map<String, String> queryHostUsernameByHostUserIds(List<String> hostUserIds) {
        if (CollectionUtils.isEmpty(hostUserIds)) {
            return CollectionUtils.newHashMap();
        }
        List<OpsHostUserEntity> hostUserList = opsHostRemoteService.getOpsHostUserList(hostUserIds);
        return hostUserList.stream().collect(Collectors.toMap(OpsHostUserEntity::getHostUserId, OpsHostUserEntity::getUsername));
    }

    private Map<String, String> queryHostIpByHostIds(List<String> hostIds) {
        if (CollectionUtils.isEmpty(hostIds)) {
            return CollectionUtils.newHashMap();
        }
        List<OpsHostEntity> hostList = opsHostRemoteService.getOpsHostList(hostIds);
        return hostList.stream().collect(Collectors.toMap(OpsHostEntity::getHostId, OpsHostEntity::getPublicIp));

    }

    private void collectClusterTaskHostInfos(List<OpsClusterTaskEntity> records, List<String> hostIds, List<String> hostUserIds) {
        if (hostIds != null) {
            hostIds.addAll(records.stream().map(OpsClusterTaskEntity::getHostId).distinct().collect(Collectors.toList()));
        }
        if (hostUserIds != null) {
            hostUserIds.addAll(records.stream().map(OpsClusterTaskEntity::getHostUserId).distinct().collect(Collectors.toList()));
        }
    }

    @Override
    public Map<String, String> deleteClusterTask(List<String> taskIds) {
        Map<String, String> result = new HashMap<>();
        try {
            List<OpsClusterTaskEntity> list = listByIds(taskIds);
            Assert.isTrue(CollectionUtils.isNotEmpty(list), "cluster install task not found:" + list);
            Map<String, OpsClusterTaskEntity> taskMap = convertClusterTaskToMap(list);

            taskIds.forEach(taskId -> {
                if (taskMap.containsKey(taskId)) {
                    OpsClusterTaskEntity task = taskMap.get(taskId);
                    if (Objects.equals(task.getStatus(), OpsClusterTaskStatusEnum.RUNNING)) {
                        log.warn("cluster install task is running, cannot be deleted {}", taskId);
                        result.put(taskId, "cluster install task is running, cannot be deleted");
                        return;
                    }
                    if (Objects.equals(task.getStatus(), OpsClusterTaskStatusEnum.SUCCESS)) {
                        log.warn("cluster install task has been completed, cannot be deleted:{}", taskId);
                        result.put(taskId, "cluster install task has been completed, cannot be deleted");
                        return;
                    }
                    opsClusterLogService.deleteOperateLogByClusterId(taskId);
                    opsClusterTaskNodeService.removeByClusterId(taskId);
                    removeById(taskId);
                    log.warn("delete cluster install task:{}", taskId);
                    result.put(taskId, "deleted successfully");
                } else {
                    result.put(taskId, "cluster install task not found");
                    log.warn("cluster install task not found:{}", taskId);
                }
            });
        } catch (Exception ex) {
            throw new OpsException(ex.getMessage());
        }
        return result;
    }

    @NotNull
    private static Map<String, OpsClusterTaskEntity> convertClusterTaskToMap(List<OpsClusterTaskEntity> list) {
        return list.stream().collect(Collectors.toMap(OpsClusterTaskEntity::getClusterId, entity -> entity));
    }

    @Override
    public OpsClusterTaskVO queryClusterTaskDetail(String taskId) {
        try {
            OpsClusterTaskEntity task = getById(taskId);
            Assert.isTrue(Objects.nonNull(task), "集群任务不存在:" + taskId);
            List<OpsClusterTaskNodeEntity> nodeList = opsClusterTaskNodeService.listByClusterTaskId(taskId);
            List<String> hostIds = new LinkedList<>();
            List<String> hostUserIds = new LinkedList<>();
            collectClusterTaskNodeHostInfos(nodeList, hostIds, hostUserIds);
            Map<String, String> hostMap = queryHostIpByHostIds(hostIds);
            Map<String, String> hostUserMap = queryHostUsernameByHostUserIds(hostUserIds);
            task.setHostIp(hostMap.get(task.getHostId()));
            task.setHostUsername(hostUserMap.get(task.getHostUserId()));
            List<OpsClusterTaskNodeVO> nodeVoList = nodeList.stream().map(OpsClusterTaskNodeEntity::toVo)
                    .peek(nodeVo -> {
                        nodeVo.setHostIp(hostMap.get(nodeVo.getHostId()));
                        nodeVo.setHostUsername(hostUserMap.get(nodeVo.getHostUserId()));
                    }).collect(Collectors.toList());
            OpsClusterTaskVO taskVo = task.toVo();
            taskVo.setClusterNodes(nodeVoList);
            Assert.isTrue(CollectionUtils.isNotEmpty(nodeList), "集群节点为空，集群任务信息异常: " + taskId);
            return taskVo;
        } catch (Exception ex) {
            throw new OpsException(ex.getMessage());
        }
    }

    private void collectClusterTaskNodeHostInfos(List<OpsClusterTaskNodeEntity> nodeList, List<String> hostIds, List<String> hostUserIds) {
        if (hostIds != null) {
            hostIds.addAll(nodeList.stream().map(OpsClusterTaskNodeEntity::getHostId).distinct().collect(Collectors.toList()));
        }
        if (hostUserIds != null) {
            hostUserIds.addAll(nodeList.stream().map(OpsClusterTaskNodeEntity::getHostUserId).distinct().collect(Collectors.toList()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createClusterTask(OpsClusterTaskCreateDTO dto) {
        OpsClusterTaskEntity entity = dto.toEntity();
        try {
            checkEnvironmentVariablePath(entity);
            checkPackageInfo(entity);
            Assert.isTrue(!checkClusterNameExist(entity.getClusterName()), "cluster name " + entity.getClusterName() + " is exist");
            List<OpsClusterTaskNodeCreateDTO> clusterNodes = dto.getClusterNodes();
            checkDeployTypeAndNodeNum(entity.getVersion(), entity.getDeployType(), clusterNodes.size());
            entity.setStatus(OpsClusterTaskStatusEnum.DRAFT);
            entity.setClusterNodeNum(clusterNodes.size());
            save(entity);
            if (CollectionUtils.isNotEmpty(clusterNodes)) {
                List<OpsClusterTaskNodeEntity> nodeEntityList = clusterNodes.stream()
                        .map(OpsClusterTaskNodeCreateDTO::toEntity)
                        .peek(node -> {
                            node.setClusterId(entity.getClusterId());
                            if (StrUtil.isNotEmpty(node.getCmDataPath())) {
                                node.setCmDataPath(dto.getOmToolsPath());
                            }
                        })
                        .map(this::checkHostInstanceCanInstallClusterNode)
                        .collect(Collectors.toList());
                opsClusterTaskNodeService.saveBatch(nodeEntityList);
            }
            OpsClusterTaskEntity taskEntity = getById(entity.getClusterId());
            List<OpsClusterTaskNodeEntity> nodeList = opsClusterTaskNodeService.listByClusterTaskId(entity.getClusterId());
            checkTaskDirDiskSpace(taskEntity, nodeList);
            OperateLogFactory.operateCreate(entity.getClusterId());
            log.info("Create cluster task success, clusterId: {}", entity.getClusterId());
        } catch (Exception ex) {
            log.error("Create cluster task failed", ex);
            throw new OpsException(ex.getMessage());
        }
        return entity.getClusterId();
    }


    private void checkPackageInfo(OpsClusterTaskEntity entity) {
        String packageId = entity.getPackageId();
        OpsPackageManagerEntity packageEntity = opsPackageManagerV2Service.getById(packageId);
        OpsHostEntity host = opsHostRemoteService.getHost(entity.getHostId());
        Assert.isTrue(Objects.nonNull(host), "host " + entity.getHostId() + " not exist");
        Assert.isTrue(Objects.nonNull(packageEntity), "Package " + packageId + " not exist");
        String packageVersion = packageEntity.getPackageVersion();
        String cpuArch = packageEntity.getCpuArch();
        String os = packageEntity.getOs();
        String packageUrl = packageEntity.getPackageUrl();

        Assert.isTrue(StrUtil.equalsIgnoreCase(packageVersion, entity.getVersion().name()),
                "package version is not match task version pkg: " + packageVersion + " , task: " + entity.getVersion().name());
        Assert.isTrue(StrUtil.equalsIgnoreCase(cpuArch, host.getCpuArch()),
                "host cpu arch and package arch is not match pkg: " + cpuArch + " , host: " + host.getCpuArch());
        Assert.isTrue(StrUtil.equalsIgnoreCase(os, host.getOs()),
                "host os and package os is not match pkg: " + os + " , host: " + host.getOs());

        // 判断openEuler 版本安装包的操作系统版本和主机操作系统版本是否匹配
        String osVersion = host.getOsVersion();
        if (StrUtil.equalsIgnoreCase(os, "openEuler")) {
            if ("20.03".equals(osVersion)) {
                Assert.isTrue(StrUtil.containsIgnoreCase(packageUrl, "/arm/") || StrUtil.containsIgnoreCase(packageUrl, "/x86_openEuler/"),
                        "host os version and package is not match pkg: 20.03 , host: " + osVersion);
            } else if ("22.03".equals(osVersion)) {
                Assert.isTrue(StrUtil.containsIgnoreCase(packageUrl, "/arm_2203/") || StrUtil.containsIgnoreCase(packageUrl, "/x86_openEuler_2203/"),
                        "host os version and package is not match pkg: 22.03 , host:" + osVersion);
            }
        }
    }

    private void checkEnvironmentVariablePath(OpsClusterTaskEntity entity) {
        String envPath = entity.getEnvPath();
        if (StrUtil.isEmpty(envPath)) {
            entity.setEnvPath("~/.bashrc");
        } else {
            if (!envPath.endsWith(".bashrc")) {
                log.warn("The host {} environment variable path {} is not a bashrc file, the default path will be used",
                        entity.getClusterName(), envPath);
                entity.setEnvPath("/home/" + entity.getHostUsername() + "/cluster_env_" + entity.getClusterName() + ".bashrc");
            }
        }
    }


    private void checkDeployTypeAndNodeNum(OpenGaussVersionEnum version, DeployTypeEnum deployType, int size) {
        checkDeployTypeService.check(version, deployType, size);
    }


    private OpsClusterTaskNodeEntity checkHostInstanceCanInstallClusterNode(OpsClusterTaskNodeEntity entity) {
        boolean canInstall = checkHostInstanceCanInstallClusterNodeById(entity.getHostId(), entity.getHostUserId());
        Assert.isTrue(canInstall, "host has been assigned cluster installation task: " + entity.getHostId() + "_(" + entity.getHostUserId() + ")");
        return entity;
    }

    /**
     * Check if the host instance can be assigned a cluster installation task
     * if return true ,current host instance can be assigned a cluster installation task
     *
     * @param hostId
     * @param hostUserId
     * @return
     */
    private boolean checkHostInstanceCanInstallClusterNodeById(String hostId, String hostUserId) {
        return CollectionUtils.isEmpty(opsClusterTaskNodeService.list(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .select(OpsClusterTaskNodeEntity::getClusterNodeId)
                .eq(OpsClusterTaskNodeEntity::getHostId, hostId)
                .eq(OpsClusterTaskNodeEntity::getHostUserId, hostUserId)));
    }

    @Override
    public boolean checkHostInstanceCanInstallClusterNode(String hostIp, String hostUsername) {
        OpsHostEntity host = opsHostRemoteService.getByPublicIp(hostIp);
        Assert.isTrue(Objects.nonNull(host), "host is not exist: " + hostIp);
        OpsHostUserEntity userEntity = opsHostRemoteService.getHostUserByUsername(host.getHostId(), hostUsername);
        Assert.isTrue(Objects.nonNull(userEntity), "host user is not exist: " + hostIp + " " + hostUsername);
        return checkHostInstanceCanInstallClusterNodeById(host.getHostId(), userEntity.getHostUserId());
    }

    @Override
    public List<OpsHostUserEntity> listHostUserByHostId(String hostId) {
        return opsHostRemoteService.listHostUserByHostId(hostId);
    }

    @Override
    public OpsHostEntity getHostByIp(String hostIp) {
        return opsHostRemoteService.getByPublicIp(hostIp);
    }

    @Override
    public boolean checkHostPort(String hostId, Integer hostPort) {
        return opsHostRemoteService.portUsed(hostId, hostPort);
    }


    @Override
    public Map<String, String> batchInstallCluster(List<String> taskIds) {
        Map<String, String> result = new HashMap<>();
        try {
            List<OpsClusterTaskEntity> list = listByIds(taskIds);
            Assert.isTrue(CollectionUtils.isNotEmpty(list), "cluster task is not exist : " + taskIds);
            Map<String, OpsClusterTaskEntity> taskMap = convertClusterTaskToMap(list);
            taskIds.forEach(taskId -> {
                OpsClusterTaskEntity task = taskMap.get(taskId);
                if (task != null && OpsClusterTaskStatusEnum.isPending(task.getStatus())) {
                    result.put(taskId, "cluster task add cluster task provider queue : " + task.getStatus());
                    providerManager.addClusterTask(taskId);
                    updateClusterTaskStatus(task, OpsClusterTaskStatusEnum.WAITING);
                } else {
                    result.put(taskId, task != null ? "cluster task is not pending : " + task.getStatus() : "cluster task is not exist");
                }
            });
            threadPoolTaskExecutor.submit(this::asyncStartInstallProvider);
        } catch (Exception ex) {
            throw new OpsException(ex.getMessage());
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reInstallCluster(String taskId) {
        OpsClusterTaskEntity task = getById(taskId);
        Assert.isTrue(Objects.nonNull(task), "cluster task is not exist : " + taskId);
        if (OpsClusterTaskStatusEnum.isFailed(task.getStatus())) {
            providerManager.addClusterTask(taskId);
            updateClusterTaskStatus(task, OpsClusterTaskStatusEnum.WAITING);
        } else {
            throw new OpsException("cluster task is not failed : " + task.getStatus());
        }
        threadPoolTaskExecutor.submit(this::asyncStartInstallProvider);
    }

    public void asyncStartInstallProvider() {
        while (providerManager.hasTaskWaiting()) {
            if (providerManager.hasLimitResource()) {
                String clusterId = providerManager.poll();
                if (clusterId != null) {
                    // 执行安装前再次检查当前任务是否存在
                    OpsClusterTaskEntity taskEntity = getById(clusterId);
                    if (taskEntity == null) {
                        log.warn("install:{}", clusterId + " cluster task not found");
                        continue;
                    }
                    log.info("install:{}", JSON.toJSONString(taskEntity));
                    try {
                        InstallContext installContext = providerManager.populateInstallContext(taskEntity);
                        installContext.setRetBuffer(new RetBuffer(clusterId));
                        installContext.checkTaskConfig();
                        Future<?> future = threadPoolTaskExecutor.submit(() -> {
                            updateClusterTaskStatus(taskEntity, OpsClusterTaskStatusEnum.RUNNING);
                            providerManager.doInstall(installContext);
                        });
                        TaskManager.registry(providerManager.getBusinessId(clusterId), future);
                    } catch (OpsException ex) {
                        updateClusterTaskStatus(taskEntity, OpsClusterTaskStatusEnum.FAILED);
                        OperateLogFactory.operateInstall(taskEntity.getClusterId(), ex.toString());
                        log.error("install:{} {} ", clusterId, ex.getMessage(), ex);
                    }
                }
            } else {
                log.info("install:{}", "waiting for task");
                ThreadUtil.safeSleep(1000L);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateClusterTask(OpsClusterTaskUpdateDTO dto) {
        OpsClusterTaskEntity entity = dto.toEntity();
        checkPackageInfo(entity);
        Assert.isTrue(!checkClusterNameExist(entity.getClusterName()), "cluster name " + entity.getClusterName() + " is exist");
        entity.setStatus(OpsClusterTaskStatusEnum.DRAFT);
        List<OpsClusterTaskNodeUpdateDTO> clusterNodes = dto.getClusterNodes();
        Asserts.check(CollectionUtils.isEmpty(clusterNodes), "cluster task update failed, cluster nodes can't be empty");
        entity.setClusterNodeNum(clusterNodes.size());
        Assert.isTrue(updateById(entity), "cluster task update failed, cluster task id not exist:" + dto.getClusterId());

        List<OpsClusterTaskNodeEntity> nodeEntityList = clusterNodes.stream()
                .peek(node -> node.setClusterId(dto.getClusterId()))
                .map(OpsClusterTaskNodeUpdateDTO::toEntity).collect(Collectors.toList());
        checkDeployTypeAndNodeNum(entity.getVersion(), entity.getDeployType(), clusterNodes.size());
        opsClusterTaskNodeService.updateBatchById(nodeEntityList);
        OpsClusterTaskEntity taskEntity = getById(dto.getClusterId());
        List<OpsClusterTaskNodeEntity> nodeList = opsClusterTaskNodeService.listByClusterTaskId(dto.getClusterId());
        checkTaskDirDiskSpace(taskEntity, nodeList);
        OperateLogFactory.operateUpdate(entity.getClusterId());
    }

    private void checkTaskDirDiskSpace(OpsClusterTaskEntity task, List<OpsClusterTaskNodeEntity> nodeList) {
        nodeList.forEach(node -> {
            List<String> nodePathList = new LinkedList<>();
            if (StrUtil.isNotEmpty(task.getCorePath())) {
                nodePathList.add(task.getCorePath());
            }
            if (StrUtil.isNotEmpty(task.getInstallPath())) {
                nodePathList.add(task.getInstallPath());
            }
            if (StrUtil.isNotEmpty(task.getTmpPath())) {
                nodePathList.add(task.getTmpPath());
            }
            if (StrUtil.isNotEmpty(task.getInstallPackagePath())) {
                nodePathList.add(task.getInstallPackagePath());
            }
            if (StrUtil.isNotEmpty(task.getLogPath())) {
                nodePathList.add(task.getLogPath());
            }
            if (StrUtil.isNotEmpty(task.getOmToolsPath())) {
                nodePathList.add(task.getOmToolsPath());
            }
            if (StrUtil.isNotEmpty(node.getDataPath())) {
                nodePathList.add(node.getDataPath());
            }
            if (StrUtil.isNotEmpty(node.getCmDataPath())) {
                nodePathList.add(node.getCmDataPath());
            }
            Map<String, String> nodeDiskSpaceResult = checkHostDiskSpace(node.getHostId(), nodePathList);
            parseNodeDiskSpaceResult(nodeDiskSpaceResult);
        });
    }

    private void parseNodeDiskSpaceResult(Map<String, String> nodeDiskSpaceResult) {
        if (CollUtil.isEmpty(nodeDiskSpaceResult)) {
            return;
        }
        nodeDiskSpaceResult.entrySet().forEach(entry -> {
            String value = entry.getValue();
            if (StrUtil.isEmpty(value)) {
                throw new OpsException(" disk top path " + entry.getKey() + " is not exist or not enough space.");
            } else {
                int diskDirSize = Integer.parseInt(value.replace("G", ""));
                if (diskDirSize < 2) {
                    throw new OpsException(" disk top path " + entry.getKey() + " not enough space.");
                }
            }
        });
    }

    @Override
    public void confirmClusterTask(String taskId) {
        OpsClusterTaskEntity task = getById(taskId);
        Assert.isTrue(Objects.nonNull(task), "cluster task not exist :" + taskId);
        Assert.isTrue(Objects.equals(task.getStatus(), OpsClusterTaskStatusEnum.DRAFT), "cluster task status not correct, can't confirm");
        Assert.isTrue(Objects.equals(task.getEnvCheckResult(), ClusterEnvCheckResultEnum.SUCCESS), "cluster task env check not pass, can't confirm");
        updateClusterTaskStatus(task, OpsClusterTaskStatusEnum.PENDING);
        OperateLogFactory.operateConfirm(taskId);
        log.info("cluster task confirm success, taskId:{}", taskId);
    }

    private void updateClusterTaskStatus(OpsClusterTaskEntity task, OpsClusterTaskStatusEnum status) {
        task.setStatus(status);
        updateClusterTaskStatus(task.getClusterId(), status);
    }

    @Override
    public void updateClusterTaskStatus(String clusterId, OpsClusterTaskStatusEnum status) {
        update(Wrappers.lambdaUpdate(OpsClusterTaskEntity.class)
                .set(OpsClusterTaskEntity::getStatus, status)
                .eq(OpsClusterTaskEntity::getClusterId, clusterId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ClusterEnvCheck checkClusterInstallEnvironment(String taskId) {
        OpsClusterTaskEntity task = getById(taskId);
        Assert.isTrue(Objects.nonNull(task), "cluster task not exist :" + taskId);

        // 执行环境检查
        List<OpsClusterTaskNodeEntity> nodes = opsClusterTaskNodeService.listByClusterTaskId(taskId);
        Assert.isTrue(CollUtil.isNotEmpty(nodes), "cluster task node not exist :" + taskId);

        Map<String, ClusterNodeEnvCheck> envCheckMap = new HashMap<>();
        nodes.forEach(node -> {
            String hostId = node.getHostId();
            HostEnv hostEnv = opsClusterEnvService.env(hostId);
            ClusterNodeEnvCheck envCheck = new ClusterNodeEnvCheck();
            envCheck.setClusterNodeId(node.getClusterNodeId());
            envCheck.setEnvCheckDetails(hostEnv);
            envCheck.setResult(populateNodeEnvCheckResult(hostEnv));
            envCheckMap.put(node.getClusterNodeId(), envCheck);
            node.setEnvCheckResult(envCheck.getResult());
            node.setEnvCheckDetail(parseEnvCheckDetails(envCheck.getEnvCheckDetails()));
        });
        // 更新集群安装任务的最终环境检查结果
        boolean allMatch = envCheckMap.values().stream().allMatch(envCheck -> Objects.equals(envCheck.getResult(), ClusterEnvCheckResultEnum.SUCCESS));
        task.setEnvCheckResult(allMatch ? ClusterEnvCheckResultEnum.SUCCESS : ClusterEnvCheckResultEnum.FAILED);

        updateById(task);
        opsClusterTaskNodeService.updateBatchById(nodes);
        // 环境检查日志在执行完成后记录。
        envCheckMap.keySet().forEach(nodeId -> {
            OperateLogFactory.operateCheckEnvironment(taskId, nodeId, envCheckMap.get(nodeId));
        });
        ClusterEnvCheck clusterEnvCheck = new ClusterEnvCheck();
        clusterEnvCheck.setClusterId(taskId);
        clusterEnvCheck.setResult(task.getEnvCheckResult());
        clusterEnvCheck.setEnvCheckDetails(new LinkedList<>(envCheckMap.values()));
        log.info("cluster task env check  {}", taskId);
        return clusterEnvCheck;
    }

    @Override
    public ClusterEnvCheck getClusterInstallEnvironment(String taskId) {
        OpsClusterTaskEntity task = getById(taskId);
        Assert.isTrue(Objects.nonNull(task), "cluster task not exist :" + taskId);
        List<OpsClusterTaskNodeEntity> nodes = opsClusterTaskNodeService.listByClusterTaskId(taskId);
        Assert.isTrue(Objects.nonNull(nodes), "cluster task node not exist :" + taskId);
        ClusterEnvCheck clusterEnvCheck = new ClusterEnvCheck();
        clusterEnvCheck.setClusterId(taskId);
        clusterEnvCheck.setResult(task.getEnvCheckResult());
        List<ClusterNodeEnvCheck> nodeList = new LinkedList<>();
        nodes.forEach(node -> {
            ClusterNodeEnvCheck nodeEnvCheck = new ClusterNodeEnvCheck();
            nodeEnvCheck.setClusterNodeId(node.getClusterNodeId());
            nodeEnvCheck.setResult(node.getEnvCheckResult());
            nodeEnvCheck.setEnvCheckDetails(parseEnvCheckDetails(node.getEnvCheckDetail()));
            nodeList.add(nodeEnvCheck);
        });
        clusterEnvCheck.setEnvCheckDetails(nodeList);
        return clusterEnvCheck;
    }

    private HostEnv parseEnvCheckDetails(String hostEnvText) {
        try {
            return objectMapper.readValue(hostEnvText, HostEnv.class);
        } catch (JsonProcessingException ignore) {
            log.warn("Failed to parse host env details: {}", hostEnvText);
        }
        return null;
    }

    private String parseEnvCheckDetails(HostEnv hostEnv) {
        try {
            return objectMapper.writeValueAsString(hostEnv);
        } catch (JsonProcessingException ignore) {
            log.warn("Failed to parse host env details: {}", hostEnv);
        }
        return null;
    }

    private ClusterEnvCheckResultEnum populateNodeEnvCheckResult(HostEnv hostEnv) {
        if (checkHostEnvIsEmpty(hostEnv)) {
            return ClusterEnvCheckResultEnum.FAILED;
        }

        Boolean hardStatusHasError = checkNodeEnvCheckResultHasError(hostEnv.getHardwareEnv().getEnvProperties());
        Boolean softStatusHasError = checkNodeEnvCheckResultHasError(hostEnv.getSoftwareEnv().getEnvProperties());

        if (hardStatusHasError || softStatusHasError) {
            return ClusterEnvCheckResultEnum.FAILED;
        } else {
            return ClusterEnvCheckResultEnum.SUCCESS;
        }
    }

    private boolean checkHostEnvIsEmpty(HostEnv hostEnv) {
        if (Objects.isNull(hostEnv)) {
            return true;
        }
        if (Objects.isNull(hostEnv.getHardwareEnv()) || Objects.isNull(hostEnv.getSoftwareEnv())) {
            return true;
        }
        if (CollUtil.isEmpty(hostEnv.getHardwareEnv().getEnvProperties())
                || CollUtil.isEmpty(hostEnv.getSoftwareEnv().getEnvProperties())) {
            return true;
        }
        return false;
    }

    private boolean checkNodeEnvCheckResultHasError(List<EnvProperty> envProperties) {
        return envProperties.stream()
                .map(envProperty -> envProperty.getStatus())
                .filter(status -> status == HostEnvStatusEnum.ERROR)
                .count() > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String copyClusterTask(String taskId) {
        OpsClusterTaskEntity task = getById(taskId);
        Assert.isTrue(Objects.nonNull(task), "cluster task not exist:" + taskId);
        // 复制集群任务 , 重置任务信息
        task.setClusterId(null);
        task.setClusterName(autoCreateClusterName());
        task.setHostId(null);
        task.setHostIp(null);
        task.setHostUserId(null);
        task.setHostUsername(null);
        task.setEnvCheckResult(null);
        task.setStatus(OpsClusterTaskStatusEnum.DRAFT);
        task.setDatabasePassword(null);
        task.setDatabasePort(5432);
        task.setRemark("copy from " + taskId);
        save(task);
        // 复制节点信息
        List<OpsClusterTaskNodeEntity> nodes = opsClusterTaskNodeService.listByClusterTaskId(taskId);
        Assert.isTrue(CollectionUtils.isNotEmpty(nodes), "cluster task node not exist");
        nodes.forEach(node -> {
            node.setClusterId(task.getClusterId());
            node.setClusterNodeId(null);
            node.setHostId(null);
            node.setHostUserId(null);
        });
        // 保存复制任务
        opsClusterTaskNodeService.saveBatch(nodes);
        OperateLogFactory.operateCopy(task.getClusterId());
        log.info("cluster task copy success, clusterId: old: {}  new: {} ", taskId, task.getClusterId());
        return task.getClusterId();
    }

    private String autoCreateClusterName() {
        return "auto-cluster-" + DateUtils.dateTimeNow();
    }

    @Override
    public boolean checkClusterNameExist(String clusterName) {
        return list(Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                .select(OpsClusterTaskEntity::getClusterId)
                .eq(OpsClusterTaskEntity::getClusterName, clusterName))
                .size() > 0;
    }

    @Override
    public Map<String, OpsClusterTaskStatusEnum> getBatchInstallTaskStatus(List<String> taskIds) {
        LambdaQueryWrapper<OpsClusterTaskEntity> wrapper = Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                .select(OpsClusterTaskEntity::getClusterId, OpsClusterTaskEntity::getStatus)
                .in(OpsClusterTaskEntity::getClusterId, taskIds);
        List<OpsClusterTaskEntity> list = list(wrapper);
        Map<String, OpsClusterTaskStatusEnum> taskStatusMap = list.stream()
                .collect(Collectors.toMap(OpsClusterTaskEntity::getClusterId, OpsClusterTaskEntity::getStatus));
        Map<String, OpsClusterTaskStatusEnum> taskStatusResult = new HashMap<>(taskIds.size());
        taskIds.forEach(taskId -> taskStatusResult.put(taskId, taskStatusMap.getOrDefault(taskId, OpsClusterTaskStatusEnum.UNKNOWN)));
        return taskStatusResult;
    }

    @Override
    public List<OpsHostEntity> getHostList(String os, String osVersion, String cpuArch) {
        List<OpsHostEntity> list = opsHostRemoteService.listAll();
        return list.stream()
                .filter(host -> StrUtil.containsIgnoreCase(host.getOs(), os))
                .filter(host -> StrUtil.containsIgnoreCase(host.getOsVersion(), osVersion))
                .filter(host -> StrUtil.containsIgnoreCase(host.getCpuArch(), cpuArch))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> checkHostDiskSpace(String hostId, List<String> paths) {
        Map<String, String> checkResult = new HashMap<>();
        Map<String, String> topLevelPaths = new HashMap<>();
        clusterTaskPathFactory.addAllPath(topLevelPaths, paths);
        checkHostTopPathsDiskSpace(hostId, topLevelPaths);
        paths.forEach(path -> {
            String topPath = clusterTaskPathFactory.applyPath(path);
            checkResult.put(path, topLevelPaths.get(topPath));
        });
        return checkResult;
    }


    private void checkHostTopPathsDiskSpace(String hostId, Map<String, String> topLevelPaths) {
        if (MapUtils.isEmpty(topLevelPaths)) {
            return;
        }
        Assert.isTrue(CollectionUtils.isNotEmpty(topLevelPaths), "Paths must contain at least one top level directory");
        OpsHostEntity host = opsHostRemoteService.getHost(hostId);
        OpsHostUserEntity hostRootUser = opsHostRemoteService.getHostRootUser(hostId);
        Session rootSession = opsHostRemoteService.getHostUserSession(host, hostRootUser);
        topLevelPaths.keySet().forEach(topLevelPath -> {
            try {
                topLevelPaths.put(topLevelPath, opsHostRemoteService.checkHostDiskSpace(rootSession, topLevelPath) + "G");
            } catch (OpsException ex) {
                topLevelPaths.put(topLevelPath, ex.getMessage());
            }
        });
    }
}
