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
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.enums.ops.ClusterEnvCheckResultEnum;
import org.opengauss.admin.common.enums.ops.OpsClusterTaskStatusEnum;
import org.opengauss.admin.common.enums.ops.OpsHostPortUsedStatusEnum;
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
import org.opengauss.admin.plugin.vo.ops.ClusterPortVo;
import org.opengauss.admin.plugin.vo.ops.TaskStatusVo;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OpsClusterTaskServiceImpl
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@Slf4j
@Service
public class OpsClusterTaskServiceImpl extends ServiceImpl<OpsClusterTaskMapper, OpsClusterTaskEntity>
        implements IOpsClusterTaskService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$";

    @Resource
    private IOpsClusterTaskNodeService opsClusterTaskNodeService;
    @Resource
    private OpsClusterEnvService opsClusterEnvService;
    @Resource
    private IOpsClusterLogService opsClusterLogService;
    @Resource
    private ProviderManager providerManager;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private IOpsPackageManagerV2Service opsPackageManagerV2Service;
    @Resource
    private OpsHostRemoteService opsHostRemoteService;
    @Resource
    private CheckDeployTypeService checkDeployTypeService;
    @Resource
    private ClusterTaskPathFactory clusterTaskPathFactory;


    @Override
    public IPage<OpsClusterTaskEntity> pageByCondition(Page page, OpsClusterTaskQueryParamDTO dto) {
        // 查询集群任务列表 - 条件组装
        LambdaQueryWrapper<OpsClusterTaskEntity> pageWrapper = Wrappers.lambdaQuery(OpsClusterTaskEntity.class);
        pageWrapper.eq(StrUtil.isNotEmpty(dto.getTaskId()), OpsClusterTaskEntity::getClusterId, dto.getTaskId())
                .eq(StrUtil.isNotEmpty(dto.getClusterName()),
                        OpsClusterTaskEntity::getClusterName, dto.getClusterName())
                .eq(StrUtil.isNotEmpty(dto.getOs()), OpsClusterTaskEntity::getOs, dto.getOs())
                .eq(StrUtil.isNotEmpty(dto.getCpuArch()), OpsClusterTaskEntity::getCpuArch, dto.getCpuArch())
                .eq(StrUtil.isNotEmpty(dto.getOpenGaussVersionNum()),
                        OpsClusterTaskEntity::getVersionNum, dto.getOpenGaussVersionNum())
                .eq(StrUtil.isNotEmpty(dto.getHostId()), OpsClusterTaskEntity::getHostId, dto.getHostId())
                .eq(StrUtil.isNotEmpty(dto.getHostUserId()), OpsClusterTaskEntity::getHostUserId, dto.getHostUserId())
                .eq(dto.getNodeNum() > 0, OpsClusterTaskEntity::getClusterNodeNum, dto.getNodeNum())
                .eq(Objects.nonNull(dto.getOpenGaussVersion()),
                        OpsClusterTaskEntity::getVersion, dto.getOpenGaussVersion());
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
        return hostUserList
                .stream()
                .collect(Collectors.toMap(OpsHostUserEntity::getHostUserId, OpsHostUserEntity::getUsername));
    }

    private Map<String, String> queryHostIpByHostIds(List<String> hostIds) {
        if (CollectionUtils.isEmpty(hostIds)) {
            return CollectionUtils.newHashMap();
        }
        List<OpsHostEntity> hostList = opsHostRemoteService.getOpsHostList(hostIds);
        return hostList.stream().collect(Collectors.toMap(OpsHostEntity::getHostId, OpsHostEntity::getPublicIp));
    }

    private void collectClusterTaskHostInfos(List<OpsClusterTaskEntity> records, List<String> hostIds,
                                             List<String> hostUserIds) {
        if (hostIds != null) {
            hostIds.addAll(records
                    .stream()
                    .map(OpsClusterTaskEntity::getHostId)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        if (hostUserIds != null) {
            hostUserIds.addAll(records
                    .stream()
                    .map(OpsClusterTaskEntity::getHostUserId)
                    .distinct()
                    .collect(Collectors.toList()));
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
            return taskVo;
        } catch (Exception ex) {
            throw new OpsException(ex.getMessage());
        }
    }

    private void collectClusterTaskNodeHostInfos(List<OpsClusterTaskNodeEntity> nodeList, List<String> hostIds,
                                                 List<String> hostUserIds) {
        if (hostIds != null) {
            hostIds.addAll(nodeList
                    .stream()
                    .map(OpsClusterTaskNodeEntity::getHostId)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        if (hostUserIds != null) {
            hostUserIds.addAll(nodeList
                    .stream()
                    .map(OpsClusterTaskNodeEntity::getHostUserId)
                    .distinct()
                    .collect(Collectors.toList()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createClusterTask(OpsClusterTaskDTO dto) {
        OpsClusterTaskEntity entity = dto.toEntity();
        try {
            Assert.isTrue(checkPasswordStrength(entity.getDatabasePassword()),
                    "database password is not strong enough");
            checkEnvironmentVariablePath(entity);
            checkPackageInfo(entity);
            Assert.isTrue(!checkClusterNameExist(null, entity.getClusterName()),
                    "cluster name " + entity.getClusterName() + " is exist");
            entity.setStatus(OpsClusterTaskStatusEnum.DRAFT);
            save(entity);
            OperateLogFactory.operateCreate(entity.getClusterId());
            log.info("Create cluster task success, clusterId: {}", entity.getClusterId());
        } catch (Exception ex) {
            log.error("Create cluster task failed", ex);
            throw new OpsException(ex.getMessage());
        }
        return entity.getClusterId();
    }

    @Override
    public boolean checkClusterTask(String clusterId) {
        // 任务ID校验
        OpsClusterTaskEntity taskEntity = getById(clusterId);
        Assert.isTrue(Objects.nonNull(taskEntity), "cluster task id can not exists " + clusterId);

        // 部署类型与节点信息校验
        checkDeployTypeService.check(taskEntity.getVersion(), taskEntity.getDeployType(),
                taskEntity.getClusterNodeNum());

        // 节点信息校验
        List<OpsClusterTaskNodeEntity> nodeEntityList = opsClusterTaskNodeService.listByClusterTaskId(clusterId);
        Assert.isTrue(CollectionUtils.isNotEmpty(nodeEntityList), "cluster task node is empty");
        nodeEntityList.forEach(node -> {
            // 节点安装信息检查
            String hostId = node.getHostId();
            OpsHostEntity host = opsHostRemoteService.getHost(hostId);
            String nodePublicIp = host.getPublicIp();
            OpsHostUserEntity hostUser = opsHostRemoteService.getOpsHostUser(node.getHostUserId());
            boolean canInstall = checkHostAndUserInstallCluster(clusterId, hostId, node.getHostUserId());
            Assert.isTrue(canInstall, clusterId + " host has cluster installation task: "
                    + nodePublicIp + "_(" + hostUser.getUsername() + ")");

            // 节点磁盘空间检查
            checkTaskDirDiskSpace(taskEntity, node);

            // 集群任务端口重复检查
            String databasePort = String.valueOf(taskEntity.getDatabasePort());
            long sameDatabasePortCount = count(Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                    .select(OpsClusterTaskEntity::getDatabasePort)
                    .notIn(OpsClusterTaskEntity::getClusterId, clusterId)
                    .eq(OpsClusterTaskEntity::getHostId, hostId)
                    .eq(OpsClusterTaskEntity::getDatabasePort, databasePort));
            Assert.isTrue(sameDatabasePortCount == 0, "task database port is used: "
                    + nodePublicIp + "_(" + databasePort + ")");

            // 集群节点数据库端口检查
            boolean hostUsed = opsHostRemoteService.portUsed(hostId, Integer.valueOf(databasePort));
            Assert.isTrue(!hostUsed, "host port is used: " + nodePublicIp + "_(" + databasePort + ")");

            if (taskEntity.getEnableCmTool()) {
                // 数据库端口与CM端口检测
                String cmPort = String.valueOf(node.getCmPort());
                Assert.isTrue(!StrUtil.equals(databasePort, cmPort), "database port and cm port must not equal");
                // 集群节点CM端口检查
                boolean isNodeCmUsed = opsClusterTaskNodeService.checkHostPortUsedByCm(clusterId, hostId,
                        node.getCmPort());
                Assert.isTrue(isNodeCmUsed, "host cm port is used: " + nodePublicIp + "_(" + cmPort + ")");

                boolean isHostCmUsed = opsHostRemoteService.portUsed(hostId, Integer.valueOf(cmPort));
                Assert.isTrue(!isHostCmUsed, "host port is used: " + nodePublicIp + "_(" + cmPort + ")");
            }
        });
        return true;
    }

    /**
     * check host instance can install cluster node
     *
     * @param hostIp       host ip
     * @param hostUsername host username
     * @return boolean
     */
    public boolean checkHostInstanceCanInstallClusterNode(String hostIp, String hostUsername) {
        OpsHostEntity host = opsHostRemoteService.getByPublicIp(hostIp);
        Assert.isTrue(Objects.nonNull(host), "host is not exist: " + hostIp);
        OpsHostUserEntity userEntity = opsHostRemoteService.getHostUserByUsername(host.getHostId(), hostUsername);
        Assert.isTrue(Objects.nonNull(userEntity), "host user is not exist: " + hostIp + " " + hostUsername);
        return checkHostAndUserInstallCluster("", host.getHostId(), userEntity.getHostUserId());
    }

    /**
     * Check if the host instance can be assigned a cluster installation task
     * if return true ,current host instance can be assigned a cluster installation task
     *
     * @param clusterId  clusterId
     * @param hostId     hostId
     * @param hostUserId hostUserId
     * @return boolean
     */
    public boolean checkHostAndUserInstallCluster(String clusterId, String hostId, String hostUserId) {
        return opsClusterTaskNodeService.count(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .select(OpsClusterTaskNodeEntity::getClusterNodeId)
                .notIn(StrUtil.isNotEmpty(clusterId), OpsClusterTaskNodeEntity::getClusterId, clusterId)
                .eq(OpsClusterTaskNodeEntity::getHostId, hostId)
                .eq(OpsClusterTaskNodeEntity::getHostUserId, hostUserId)) == 0;
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
                "package version is not match task version pkg: " + packageVersion
                        + " , task: " + entity.getVersion().name());
        Assert.isTrue(StrUtil.equalsIgnoreCase(cpuArch, host.getCpuArch()),
                "host cpu arch and package arch is not match pkg: " + cpuArch + " , host: " + host.getCpuArch());
        Assert.isTrue(StrUtil.equalsIgnoreCase(os, host.getOs()),
                "host os and package os is not match pkg: " + os + " , host: " + host.getOs());

        // 判断openEuler 版本安装包的操作系统版本和主机操作系统版本是否匹配
        String osVersion = host.getOsVersion();
        if (StrUtil.equalsIgnoreCase(os, "openEuler")) {
            if ("20.03".equals(osVersion)) {
                Assert.isTrue(StrUtil.containsIgnoreCase(packageUrl, "/arm/")
                                || StrUtil.containsIgnoreCase(packageUrl, "/x86_openEuler/"),
                        "host os version and package is not match pkg: 20.03 , host: " + osVersion);
            } else if ("22.03".equals(osVersion)) {
                Assert.isTrue(StrUtil.containsIgnoreCase(packageUrl, "/arm_2203/")
                                || StrUtil.containsIgnoreCase(packageUrl, "/x86_openEuler_2203/"),
                        "host os version and package is not match pkg: 22.03 , host:" + osVersion);
            }
        }
    }

    private void checkEnvironmentVariablePath(OpsClusterTaskEntity entity) {
        String envPath = entity.getEnvPath();
        if (StrUtil.isEmpty(envPath)) {
            entity.setEnvPath(SshCommandConstants.DEFAULT_ENV_BASHRC);
        } else {
            if (!envPath.endsWith(".bashrc")) {
                log.warn("The host {} environment variable path {} is not a bashrc file, the default path will be used",
                        entity.getClusterName(), envPath);
                entity.setEnvPath("/home/" + entity.getHostUsername()
                        + "/cluster_env_" + entity.getClusterName() + ".bashrc");
            }
        }
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
    public OpsHostPortUsedStatusEnum checkHostPort(String taskId, String hostId, Integer hostPort) {
        List<OpsClusterTaskEntity> hostDbPortList = list(Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                .select(OpsClusterTaskEntity::getDatabasePort)
                .notIn(StrUtil.isNotEmpty(taskId), OpsClusterTaskEntity::getClusterId, taskId)
                .eq(OpsClusterTaskEntity::getHostId, hostId)
                .eq(OpsClusterTaskEntity::getDatabasePort, hostPort));
        if (CollUtil.isNotEmpty(hostDbPortList)) {
            return OpsHostPortUsedStatusEnum.DATABASE_USED;
        }
        if (opsClusterTaskNodeService.checkHostPortUsedByCm(taskId, hostId, hostPort)) {
            return OpsHostPortUsedStatusEnum.DATABASE_CM_USED;
        }
        boolean isHostUsed = opsHostRemoteService.portUsed(hostId, hostPort);
        return isHostUsed ? OpsHostPortUsedStatusEnum.HOST_USED : OpsHostPortUsedStatusEnum.NO_USED;
    }

    @Override
    public Map<String, ClusterPortVo> checkTaskHostPort(String clusterId) {
        LambdaQueryWrapper<OpsClusterTaskEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterTaskEntity.class);
        queryWrapper.select(OpsClusterTaskEntity::getHostId, OpsClusterTaskEntity::getDatabasePort)
                .eq(OpsClusterTaskEntity::getClusterId, clusterId);
        OpsClusterTaskEntity taskEntity = getOne(queryWrapper);
        Assert.isTrue(Objects.nonNull(taskEntity), "cluster id is not exist");

        // 查询集群节点列表
        List<OpsClusterTaskNodeEntity> hostPortList = opsClusterTaskNodeService.queryClusterNodeAndPortList(clusterId);
        Assert.isTrue(CollUtil.isNotEmpty(hostPortList), "cluster node is not invalid");

        // 校验节点主机端口是否被使用
        Map<String, ClusterPortVo> checkResult = new HashMap<>();
        hostPortList.forEach(hostNode -> {
            String clusterNodeId = hostNode.getClusterNodeId();
            String hostId = hostNode.getHostId();
            String databasePort = String.valueOf(taskEntity.getDatabasePort());
            String cmPort = String.valueOf(hostNode.getCmPort());
            ClusterPortVo nodePort = new ClusterPortVo(clusterNodeId, databasePort, cmPort);

            // 集群节点数据库端口检查
            List<OpsClusterTaskEntity> hostDbPortList = list(Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                    .select(OpsClusterTaskEntity::getDatabasePort)
                    .notIn(OpsClusterTaskEntity::getClusterId, clusterId)
                    .eq(OpsClusterTaskEntity::getHostId, hostId)
                    .eq(OpsClusterTaskEntity::getDatabasePort, databasePort));
            if (CollUtil.isNotEmpty(hostDbPortList)) {
                nodePort.setDatabasePortStatus(OpsHostPortUsedStatusEnum.DATABASE_USED);
            } else {
                boolean isHostUsed = opsHostRemoteService.portUsed(hostId, Integer.valueOf(databasePort));
                nodePort.setDatabasePortStatus(isHostUsed ? OpsHostPortUsedStatusEnum.HOST_USED :
                        OpsHostPortUsedStatusEnum.NO_USED);
            }

            // 集群节点CM端口检查
            boolean isNodeCmUsed = opsClusterTaskNodeService
                    .checkHostPortUsedByCm(clusterId, hostNode.getHostId(), hostNode.getCmPort());
            if (isNodeCmUsed) {
                nodePort.setCmPortStatus(OpsHostPortUsedStatusEnum.DATABASE_CM_USED);
            } else {
                boolean isHostUsed = opsHostRemoteService.portUsed(hostId, Integer.valueOf(cmPort));
                nodePort.setDatabasePortStatus(isHostUsed ? OpsHostPortUsedStatusEnum.HOST_USED :
                        OpsHostPortUsedStatusEnum.NO_USED);
            }
            checkResult.put(nodePort.getClusterNodeId(), nodePort);
        });
        return checkResult;
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
                    updateClusterTaskStatus(task, OpsClusterTaskStatusEnum.WAITING, "");
                } else {
                    result.put(taskId, task != null ? "cluster task is not pending : "
                            + task.getStatus() : "cluster task is not exist");
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
            updateClusterTaskStatus(task, OpsClusterTaskStatusEnum.WAITING, "");
        } else {
            throw new OpsException("cluster task is not failed : " + task.getStatus());
        }
        threadPoolTaskExecutor.submit(this::asyncStartInstallProvider);
    }

    public void asyncStartInstallProvider() {
        while (providerManager.hasTaskWaiting()) {
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
                        updateClusterTaskStatus(taskEntity, OpsClusterTaskStatusEnum.RUNNING, "");
                        providerManager.doInstall(installContext);
                    });
                    TaskManager.registry(providerManager.getBusinessId(clusterId), future);
                } catch (OpsException ex) {
                    updateClusterTaskStatus(taskEntity, OpsClusterTaskStatusEnum.FAILED, ex.getMessage());
                    OperateLogFactory.operateInstall(taskEntity.getClusterId(), ex.toString());
                    log.error("install:{} {} ", clusterId, ex.getMessage(), ex);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateClusterTask(OpsClusterTaskDTO dto) {
        OpsClusterTaskEntity entity = dto.toEntity();
        checkPackageInfo(entity);
        Assert.isTrue(!checkClusterNameExist(entity.getClusterId(), entity.getClusterName()),
                "cluster name " + entity.getClusterName() + " is exist");
        entity.setStatus(OpsClusterTaskStatusEnum.DRAFT);
        // update task
        Assert.isTrue(updateById(entity),
                "cluster task update failed, cluster task id not exist:" + dto.getClusterId());
        Assert.isTrue(checkPasswordStrength(entity.getDatabasePassword()),
                "database password is not strong enough");
        OperateLogFactory.operateUpdate(entity.getClusterId());
    }


    @Override
    public void resetTaskStatusDraft(String clusterId) {
        OpsClusterTaskEntity taskEntity = getById(clusterId);
        Assert.isTrue(Objects.nonNull(taskEntity), "cluster task id not exist:" + clusterId);
        long nodeCount = opsClusterTaskNodeService.count(Wrappers.lambdaQuery(OpsClusterTaskNodeEntity.class)
                .eq(OpsClusterTaskNodeEntity::getClusterId, clusterId));
        update(Wrappers.lambdaUpdate(OpsClusterTaskEntity.class)
                .set(OpsClusterTaskEntity::getStatus, OpsClusterTaskStatusEnum.DRAFT)
                .set(OpsClusterTaskEntity::getClusterNodeNum, nodeCount)
                .set(OpsClusterTaskEntity::getEnvCheckResult, null)
                .set(OpsClusterTaskEntity::getRemark, "reset task draft")
                .eq(OpsClusterTaskEntity::getClusterId, clusterId));
    }


    @Override
    public void confirmClusterTask(String taskId) {
        OpsClusterTaskEntity task = getById(taskId);
        Assert.isTrue(Objects.nonNull(task), "cluster task not exist :" + taskId);
        Assert.isTrue(Objects.equals(task.getStatus(), OpsClusterTaskStatusEnum.DRAFT),
                "cluster task status not correct, can't confirm");
        Assert.isTrue(Objects.equals(task.getEnvCheckResult(), ClusterEnvCheckResultEnum.SUCCESS),
                "cluster task env check not pass, can't confirm");
        updateClusterTaskStatus(task, OpsClusterTaskStatusEnum.PENDING, "");
        OperateLogFactory.operateConfirm(taskId);
        log.info("cluster task confirm success, taskId:{}", taskId);
    }

    private void updateClusterTaskStatus(OpsClusterTaskEntity task, OpsClusterTaskStatusEnum status, String remark) {
        task.setStatus(status);
        task.setRemark(remark);
        updateClusterTaskStatus(task.getClusterId(), status, remark);
    }

    @Override
    public void updateClusterTaskStatus(String clusterId, OpsClusterTaskStatusEnum status, String remark) {
        update(Wrappers.lambdaUpdate(OpsClusterTaskEntity.class)
                .set(OpsClusterTaskEntity::getStatus, status)
                .set(OpsClusterTaskEntity::getRemark, remark)
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
        boolean isAllMatch = envCheckMap
                .values()
                .stream()
                .allMatch(envCheck -> Objects.equals(envCheck.getResult(), ClusterEnvCheckResultEnum.SUCCESS));
        task.setEnvCheckResult(isAllMatch ? ClusterEnvCheckResultEnum.SUCCESS : ClusterEnvCheckResultEnum.FAILED);

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
        return CollUtil.isEmpty(hostEnv.getHardwareEnv().getEnvProperties())
                || CollUtil.isEmpty(hostEnv.getSoftwareEnv().getEnvProperties());
    }

    private boolean checkNodeEnvCheckResultHasError(List<EnvProperty> envProperties) {
        return envProperties.stream()
                .map(EnvProperty::getStatus).anyMatch(status -> status == HostEnvStatusEnum.ERROR);
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
    public boolean checkClusterNameExist(String clusterId, String clusterName) {
        LambdaQueryWrapper<OpsClusterTaskEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                .select(OpsClusterTaskEntity::getClusterId)
                .eq(OpsClusterTaskEntity::getClusterName, clusterName);
        if (StrUtil.isNotEmpty(clusterId)) {
            queryWrapper.notIn(OpsClusterTaskEntity::getClusterId, List.of(clusterId));
        }
        return !list(queryWrapper).isEmpty();
    }

    @Override
    public Map<String, TaskStatusVo> getBatchInstallTaskStatus(List<String> taskIds) {
        LambdaQueryWrapper<OpsClusterTaskEntity> wrapper = Wrappers.lambdaQuery(OpsClusterTaskEntity.class)
                .select(OpsClusterTaskEntity::getClusterId, OpsClusterTaskEntity::getStatus,
                        OpsClusterTaskEntity::getRemark)
                .in(OpsClusterTaskEntity::getClusterId, taskIds);
        List<OpsClusterTaskEntity> list = list(wrapper);
        Map<String, TaskStatusVo> taskStatusMap = list.stream()
                .map(task -> new TaskStatusVo(task.getClusterId(), task.getStatus(), task.getRemark()))
                .collect(Collectors.toMap(TaskStatusVo::getClusterId, Function.identity()));
        Map<String, TaskStatusVo> taskStatusResult = new HashMap<>(taskIds.size());
        taskIds.forEach(taskId -> taskStatusResult.put(taskId, taskStatusMap.get(taskId)));
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

    private boolean checkPasswordStrength(String password) {
        return password.matches(PASSWORD_REGEX);
    }


    private void checkTaskDirDiskSpace(OpsClusterTaskEntity task, OpsClusterTaskNodeEntity node) {
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
        Map<String, String> nodeDiskSpaceResult = checkHostDiskSpace(task.getHostId(), nodePathList);
        parseNodeDiskSpaceResult(nodeDiskSpaceResult);
    }

    private void parseNodeDiskSpaceResult(Map<String, String> nodeDiskSpaceResult) {
        if (CollUtil.isEmpty(nodeDiskSpaceResult)) {
            return;
        }
        nodeDiskSpaceResult.forEach((key, value) -> {
            if (StrUtil.isEmpty(value)) {
                throw new OpsException(" disk top path " + key + " is not exist or not enough space.");
            } else {
                int diskDirSize = Integer.parseInt(value.replace("G", ""));
                if (diskDirSize < 2) {
                    throw new OpsException(" disk top path " + key + " not enough space.");
                }
            }
        });
    }


    /**
     * check host disk space
     *
     * @param hostId host Id
     * @param paths  directory path list
     * @return check result
     */
    public Map<String, String> checkHostDiskSpace(String hostId, List<String> paths) {
        Map<String, String> checkResult = new HashMap<>();
        Map<String, String> topLevelPaths = new HashMap<>();
        clusterTaskPathFactory.addAllPath(topLevelPaths, paths);
        checkHostTopPathsDiskSpace(hostId, topLevelPaths);
        paths.forEach(path -> {
            if (StrUtil.isNotEmpty(path)) {
                String topPath = clusterTaskPathFactory.applyPath(path);
                checkResult.put(path, topLevelPaths.get(topPath));
            }
        });
        return checkResult;
    }

    private void checkHostTopPathsDiskSpace(String hostId, Map<String, String> topLevelPaths) {
        if (MapUtils.isEmpty(topLevelPaths)) {
            return;
        }
        Assert.isTrue(CollectionUtils.isNotEmpty(topLevelPaths),
                "Paths must contain at least one top level directory");
        OpsHostEntity host = opsHostRemoteService.getHost(hostId);
        OpsHostUserEntity hostRootUser = opsHostRemoteService.getHostRootUser(hostId);
        Session rootSession = opsHostRemoteService.getHostUserSession(host, hostRootUser);
        topLevelPaths.keySet().forEach(topLevelPath -> {
            try {
                String topPathSpace = opsHostRemoteService.checkHostDiskSpace(rootSession, topLevelPath) + "G";
                topLevelPaths.put(topLevelPath, topPathSpace);
            } catch (OpsException ex) {
                topLevelPaths.put(topLevelPath, ex.getMessage());
            }
        });
    }

    @Override
    public void modifyClusterNodeCount(String clusterId, int count) {
        update(Wrappers.lambdaUpdate(OpsClusterTaskEntity.class)
                .set(OpsClusterTaskEntity::getClusterNodeNum, count)
                .eq(OpsClusterTaskEntity::getClusterId, clusterId));
    }
}
