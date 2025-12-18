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
 * MigrationTaskHostRefServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskHostRefServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.milvus.v2.client.MilvusClientV2;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.formula.functions.T;
import org.elasticsearch.client.RestClient;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.CommonUtils;
import org.opengauss.admin.system.utils.ElasticsearchUtils;
import org.opengauss.admin.system.utils.MilvusUtils;
import org.opengauss.admin.system.utils.MysqlUtils;
import org.opengauss.admin.system.utils.OpengaussUtils;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.system.utils.PostgresqlUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.file.FileUploadUtils;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.plugin.constants.TaskConstant;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.domain.TbMigrationTaskGlobalToolsParam;
import org.opengauss.admin.plugin.dto.MigrationHostDto;
import org.opengauss.admin.plugin.dto.PortalInstallHostDto;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.enums.OpengaussSourceTable;
import org.opengauss.admin.plugin.enums.PortalInstallStatus;
import org.opengauss.admin.plugin.enums.PortalInstallType;
import org.opengauss.admin.plugin.enums.PortalType;
import org.opengauss.admin.plugin.enums.PortalVersion;
import org.opengauss.admin.plugin.enums.ThirdPartySoftwareConfigType;
import org.opengauss.admin.plugin.enums.ToolsConfigEnum;
import org.opengauss.admin.plugin.exception.MigrationTaskException;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.exception.ShellException;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationTaskHostRefMapper;
import org.opengauss.admin.plugin.portal.MultiDbPortal;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationMqInstanceService;
import org.opengauss.admin.plugin.service.MigrationTaskHostRefService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.service.MigrationToolPortalDownloadInfoService;
import org.opengauss.admin.plugin.service.TbMigrationTaskGlobalToolsParamService;
import org.opengauss.admin.plugin.utils.JDBCUtils;
import org.opengauss.admin.plugin.utils.PageHelper;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.HostBaseInfoVo;
import org.opengauss.admin.plugin.vo.OpengaussClusterNodeVo;
import org.opengauss.admin.plugin.vo.OpengaussClusterVo;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.plugin.vo.SourceClusterVo;
import org.opengauss.admin.plugin.vo.TargetClusterNodeVo;
import org.opengauss.admin.plugin.vo.TargetClusterVo;
import org.opengauss.admin.plugin.vo.TargetDatabaseVo;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostMonitorFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.JdbcDbClusterFacade;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskHostRefServiceImpl extends ServiceImpl<MigrationTaskHostRefMapper, MigrationTaskHostRef>
    implements MigrationTaskHostRefService {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostMonitorFacade hostMonitorFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JdbcDbClusterFacade jdbcDbClusterFacade;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;

    @Resource
    private MigrationTaskHostRefMapper migrationTaskHostRefMapper;
    @Resource
    @Lazy
    private MigrationTaskService migrationTaskService;
    @Resource
    private MigrationHostPortalInstallHostService migrationHostPortalInstallHostService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private TbMigrationTaskGlobalToolsParamService toolsParamService;

    @Resource
    private MigrationMqInstanceService migrationThirdPartySoftwareInstanceService;

    @Resource
    private TbMigrationTaskGlobalToolsParamService taskGlobalToolsParamService;

    @Autowired
    private MultiDbPortal multiDbPortal;

    @Autowired
    private MigrationToolPortalDownloadInfoService portalDownloadInfoService;

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskHostRef> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskHostRef::getMainTaskId, mainTaskId);
        remove(query);
    }

    @Override
    public List<MigrationTaskHostRef> listByMainTaskId(Integer mainTaskId) {
        List<MigrationTaskHostRef> hosts = migrationTaskHostRefMapper.selectByMainTaskId(mainTaskId);
        hosts.forEach(h -> {
            OpsHostEntity opsHost = hostFacade.getById(h.getRunHostId());
            if (ObjectUtils.isEmpty(opsHost)) {
                log.error("Cannot get host information by host id: {}", h.getRunHostId());
                throw new MigrationTaskException("The portal host does not exist.");
            }
            h.setHostName(opsHost.getHostname());
            h.setHost(opsHost.getPublicIp());
            h.setPort(opsHost.getPort());
            MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
                h.getRunHostId());
            h.setUser(installHost.getRunUser());
            h.setPassword(installHost.getRunPassword());
        });
        return hosts;
    }

    @Override
    public IPage<MigrationHostDto> getHosts(IPage<MigrationHostDto> iPage, PortalInstallHostDto portalInstallHostDto) {
        List<OpsHostEntity> opsHostEntities = hostFacade.getHostList(portalInstallHostDto.getHostname(),
                portalInstallHostDto.getIp());
        List<MigrationHostDto> hostDtoList = new ArrayList<>();
        opsHostEntities.forEach(host -> {
            MigrationHostDto eachOne = new MigrationHostDto();
            eachOne.setHostInfo(host);
            List<MigrationTask> tasks = migrationTaskService.listRunningTaskByHostId(host.getHostId());
            eachOne.setTasks(tasks);
            MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
                host.getHostId());

            PortalType portalType = portalInstallHostDto.getPortalType();
            List<Integer> installStatus = portalInstallHostDto.getInstallStatusList();
            if (installHost == null) {
                if (portalType != null || (installStatus != null && !installStatus.contains(0))) {
                    return;
                }
                HostBaseInfoVo opsHosInfo = getHostBaseInfo(host.getHostId());
                eachOne.setBaseInfos(opsHosInfo);
                eachOne.setInstallPortalStatus(PortalInstallStatus.NOT_INSTALL.getCode());
            } else {
                if (portalType != null && !installHost.getPortalType().equals(portalType)
                        || (installStatus != null && !installStatus.contains(installHost.getInstallStatus()))) {
                    return;
                }
                HostBaseInfoVo hostInfo = getHostBaseInfo(host.getHostId());
                eachOne.setBaseInfos(hostInfo);
                eachOne.setInstallInfo(installHost);
                eachOne.setInstallPortalStatus(installHost.getInstallStatus());
            }
            hostDtoList.add(eachOne);
        });
        hostDtoList.sort(Comparator.comparing(MigrationHostDto::getBaseInfos));
        return getPageFromList(hostDtoList, iPage.getCurrent(), iPage.getSize());
    }

    private <T> Page<T> getPageFromList(List<T> sourceList, long current, long size) {
        Page<T> page = new Page<>(current, size);
        page.setTotal(sourceList.size());

        int fromIndex = (int) ((current - 1) * size);
        int toIndex = (int) Math.min(fromIndex + size, sourceList.size());

        if (fromIndex > sourceList.size() || fromIndex < 0) {
            page.setRecords(Collections.emptyList());
        } else {
            page.setRecords(sourceList.subList(fromIndex, toIndex));
        }
        return page;
    }

    private HostBaseInfoVo getHostBaseInfo(String hostId) {
        Future<HostBaseInfoVo> future = EXECUTOR_SERVICE.submit(() -> {
            HostBaseInfoVo result = new HostBaseInfoVo();
            String hostInfo = hostMonitorFacade.getMigrationHostInfo(hostId);
            String[] parts = hostInfo.split("\n");
            if (parts.length != 3) {
                return result;
            }
            result.setCpuCoreNum(parts[0]);
            result.setRemainingMemory(parts[1]);
            result.setAvailableDiskSpace(parts[2]);
            result.setCpuUsing(hostMonitorFacade.getCpuUsing(hostId));
            return result;
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("get host base info timeout, hostId: {}", hostId);
        } catch (ExecutionException | InterruptedException e) {
            log.warn("get host base info error, hostId: {}, cause: {}", hostId, e.getMessage());
        } finally {
            future.cancel(true);
        }
        return new HostBaseInfoVo();
    }

    @Override
    public List<JdbcDbClusterVO> getSourceClusters() {
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("MYSQL");
        return jdbcTargetCluster;
    }

    @Override
    public List<JdbcDbClusterVO> getSourceClusters(String dbTypeStr) {
        List<JdbcDbClusterVO> jdbcSourceCluster = new ArrayList<>();
        DbTypeEnum dbType = DbTypeEnum.valueOf(dbTypeStr.toUpperCase(Locale.ROOT));
        if (DbTypeEnum.MYSQL.equals(dbType)) {
            jdbcSourceCluster = jdbcDbClusterFacade.listAll(DbTypeEnum.MYSQL.name());
        } else if (DbTypeEnum.OPENGAUSS.equals(dbType)) {
            jdbcSourceCluster = jdbcDbClusterFacade.listAll(DbTypeEnum.OPENGAUSS.name());
        } else if (DbTypeEnum.POSTGRESQL.equals(dbType)) {
            jdbcSourceCluster = jdbcDbClusterFacade.listAll(DbTypeEnum.POSTGRESQL.name());
        } else {
            log.warn("Unsupported database type to get source clusters. dbType: {}", dbType);
        }
        return jdbcSourceCluster;
    }

    private String getPgsqlVersion(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, encryptionUtils.decrypt(password))) {
            return JDBCUtils.getPgsqlVersion(connection);
        } catch (SQLException e) {
            log.error("Select postgresql version failed", e);
            throw new MigrationTaskException("Select postgresql version failed");
        }
    }

    private boolean isPgsqlVersionSupportMigration(String version) {
        String[] versionList = version.split("\\.");
        int supportMajorVersion = 9;
        int supportMinorVersion = 4;
        if (versionList.length >= 2) {
            int majorVersion = Integer.parseInt(versionList[0]);
            int minorVersion = Integer.parseInt(versionList[1]);
            if (supportMajorVersion < majorVersion) {
                return true;
            }
            if (supportMajorVersion == majorVersion && supportMinorVersion <= minorVersion) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<OpengaussClusterVo> getOpengaussClusters() {
        List<OpsClusterVO> opsClusterVOS = opsFacade.listCluster();
        List<OpengaussClusterVo> targetClusters = opsClusterVOS.stream().map(o -> {
            OpengaussClusterVo clusterVO = new OpengaussClusterVo();
            clusterVO.setClusterId(o.getClusterId());
            clusterVO.setClusterName(o.getClusterName());
            clusterVO.setVersion(o.getVersion());
            clusterVO.setDeployType(o.getDeployType());
            clusterVO.setVersionNum(
                StringUtils.isNotBlank(o.getVersionNum()) ? o.getVersionNum() : TaskConstant.DEFAULT_OPENGAUSS_VERSION);
            List<OpengaussClusterNodeVo> nodes = o.getClusterNodes().stream().map(on -> {
                OpengaussClusterNodeVo clusterNodeVO = new OpengaussClusterNodeVo();
                clusterNodeVO.setNodeId(on.getNodeId());
                clusterNodeVO.setPublicIp(on.getPublicIp());
                clusterNodeVO.setPrivateIp(on.getPrivateIp());
                clusterNodeVO.setHostname(on.getHostname());
                clusterNodeVO.setHostId(on.getHostId());
                clusterNodeVO.setDbPort(on.getDbPort());
                clusterNodeVO.setDbName(on.getDbName());
                clusterNodeVO.setDbUser(on.getDbUser());
                clusterNodeVO.setDbUserPassword(encryptionUtils.decrypt(on.getDbUserPassword()));
                clusterNodeVO.setHostPort(on.getHostPort());
                return clusterNodeVO;
            }).collect(Collectors.toList());
            clusterVO.setClusterNodes(nodes);
            return clusterVO;
        }).collect(Collectors.toList());
        targetClusters.addAll(getJdbcOpengaussClusters());
        return targetClusters;
    }

    private List<OpengaussClusterVo> getJdbcOpengaussClusters() {
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("openGauss");
        return jdbcTargetCluster.stream().map(jc -> {
            OpengaussClusterVo clusterVO = new OpengaussClusterVo();
            clusterVO.setClusterId(jc.getName());
            clusterVO.setClusterName(jc.getName());
            clusterVO.setVersion("3.0.0");
            clusterVO.setDeployType(jc.getDeployType());
            List<OpengaussClusterNodeVo> nodes = jc.getNodes().stream().map(on -> {
                OpengaussClusterNodeVo clusterNodeVO = new OpengaussClusterNodeVo();
                clusterNodeVO.setNodeId(on.getClusterNodeId());
                clusterNodeVO.setPublicIp(on.getIp());
                clusterNodeVO.setPrivateIp(on.getIp());
                clusterNodeVO.setDbPort(Integer.parseInt(on.getPort()));
                clusterNodeVO.setDbName(on.getUrl().substring(on.getUrl().lastIndexOf("/") + 1));
                clusterNodeVO.setDbUser(on.getUsername());
                clusterNodeVO.setDbUserPassword(on.getPassword());
                clusterNodeVO.setHostname(on.getName());
                clusterNodeVO.setHostPort(22);
                return clusterNodeVO;
            }).collect(Collectors.toList());
            clusterVO.setClusterNodes(nodes);
            return clusterVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Boolean> getNodeRoleMap(String clusterName) {
        List<OpsClusterVO> opsClusterVOS = opsFacade.listCluster();
        List<OpsClusterVO> filteredOpsClusterList = opsClusterVOS.stream()
            .filter(cluster -> clusterName.equals(cluster.getClusterId()))
            .collect(Collectors.toList());
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("openGauss");
        List<JdbcDbClusterVO> filteredJdbcList = jdbcTargetCluster.stream()
            .filter(cluster -> clusterName.equals(cluster.getName()))
            .collect(Collectors.toList());
        if (!filteredOpsClusterList.isEmpty()) {
            return judgeClusterConnection(filteredOpsClusterList);
        }
        if (!filteredJdbcList.isEmpty()) {
            return judgeJdbcDbConnection(filteredJdbcList);
        }
        return Collections.emptyMap();
    }

    @Override
    public List<SourceClusterVo> getSourceClusters(DbTypeEnum dbType) {
        if (dbType == null) {
            throw new IllegalArgumentException("Parameter 'dbType' cannot be null");
        }

        List<JdbcDbClusterVO> jdbcDbClusterVos;
        if (DbTypeEnum.MYSQL.equals(dbType)) {
            jdbcDbClusterVos = jdbcDbClusterFacade.listJdbcClusters(dbType);
        } else if (DbTypeEnum.POSTGRESQL.equals(dbType)) {
            jdbcDbClusterVos = jdbcDbClusterFacade.listJdbcClusters(dbType);
        } else if (DbTypeEnum.MILVUS.equals(dbType)) {
            jdbcDbClusterVos = jdbcDbClusterFacade.listNonJdbcClusters(dbType);
        } else if (DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
            jdbcDbClusterVos = jdbcDbClusterFacade.listNonJdbcClusters(dbType);
        } else {
            throw new IllegalArgumentException("Unsupported database type to get source clusters. dbType: " + dbType);
        }
        return jdbcDbClusterVos.stream().map(SourceClusterVo::of).collect(Collectors.toList());
    }

    @Override
    public List<String> getSourceDatabases(DbTypeEnum dbType, String nodeId) {
        if (dbType == null) {
            throw new IllegalArgumentException("Parameter 'dbType' cannot be null");
        }

        if (dbType.isJdbcDriver()) {
            OpsJdbcDbClusterNodeEntity jdbcDbNode = jdbcDbClusterFacade.getJdbcDbClusterNode(nodeId);
            if (jdbcDbNode == null) {
                throw new OpsException("Cluster node not exist, node id: " + nodeId + ", database type: " + dbType);
            }

            return getJdbcDbSourceDatabases(dbType, jdbcDbNode);
        }

        if (DbTypeEnum.MILVUS.equals(dbType)) {
            OpsNonJdbcDbClusterNodeEntity nonJdbcDbNode = jdbcDbClusterFacade.getNonJdbcDbClusterNode(nodeId);
            if (nonJdbcDbNode == null) {
                throw new OpsException("Cluster node not exist, node id: " + nodeId + ", database type: " + dbType);
            }

            return getMilvusSourceDatabases(nonJdbcDbNode);
        }
        throw new IllegalArgumentException("Database type '" + dbType + "' is not supported to get source databases");
    }

    @Override
    public List<String> getSourceSchemas(DbTypeEnum dbType, String nodeId, String dbName) {
        if (dbType == null) {
            throw new IllegalArgumentException("Parameter 'dbType' cannot be null");
        }

        if (!DbTypeEnum.POSTGRESQL.equals(dbType)) {
            throw new IllegalArgumentException("Database type '" + dbType + "' is not supported to get source schemas");
        }

        OpsJdbcDbClusterNodeEntity jdbcDbNode = jdbcDbClusterFacade.getJdbcDbClusterNode(nodeId);
        if (jdbcDbNode == null) {
            throw new OpsException("Cluster node not exist, node id: " + nodeId + ", database type: " + dbType);
        }

        String dbUrl = String.format("jdbc:postgresql://%s:%s/%s", jdbcDbNode.getIp(), jdbcDbNode.getPort(), dbName);
        try (Connection connection = DriverManager.getConnection(
                dbUrl, jdbcDbNode.getUsername(), encryptionUtils.decrypt(jdbcDbNode.getPassword()))) {
            List<String> schemas = PostgresqlUtils.listSchemas(connection);
            schemas.remove("information_schema");
            schemas.remove("pg_catalog");
            schemas.remove("pg_toast");
            return schemas;
        } catch (SQLException e) {
            log.error("Failed to get source schemas, node id: {}, database type: {}", nodeId, dbType, e);
            throw new OpsException("Failed to get source schemas from cluster node " + nodeId
                    + " with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public IPage<String> getSourceTablePage(
            DbTypeEnum dbType, String nodeId, String dbName, String schemaName, Page<T> page
    ) {
        if (dbType == null) {
            throw new IllegalArgumentException("Parameter 'dbType' cannot be null");
        }

        if (dbType.isJdbcDriver()) {
            OpsJdbcDbClusterNodeEntity jdbcDbNode = jdbcDbClusterFacade.getJdbcDbClusterNode(nodeId);
            if (jdbcDbNode == null) {
                throw new OpsException("Cluster node not exist, node id: " + nodeId + ", database type: " + dbType);
            }

            return getJdbcDbSourceTables(dbType, jdbcDbNode, dbName, schemaName, page);
        }

        OpsNonJdbcDbClusterNodeEntity nonJdbcDbNode = jdbcDbClusterFacade.getNonJdbcDbClusterNode(nodeId);
        if (nonJdbcDbNode == null) {
            throw new OpsException("Cluster node not exist, node id: " + nodeId + ", database type: " + dbType);
        }
        return getNonJdbcDbSourceTables(dbType, nonJdbcDbNode, dbName, page);
    }

    @Override
    public List<TargetClusterVo> getTargetClusters() {
        List<TargetClusterVo> targetClusterVos = new ArrayList<>();
        List<OpsClusterVO> opsClusterVOS = opsFacade.listCluster();
        targetClusterVos.addAll(opsClusterVOS.stream().map(TargetClusterVo::of).toList());

        List<JdbcDbClusterVO> jdbcDbClusterVOS = jdbcDbClusterFacade.listJdbcClusters(DbTypeEnum.OPENGAUSS);
        targetClusterVos.addAll(jdbcDbClusterVOS.stream().map(TargetClusterVo::of).toList());
        return targetClusterVos;
    }

    @Override
    public TargetClusterVo getTargetDetail(OpengaussSourceTable sourceTable, String clusterId) {
        if (sourceTable == null) {
            throw new IllegalArgumentException("Parameter 'sourceTable' cannot be null");
        }
        if (StringUtils.isEmpty(clusterId)) {
            throw new IllegalArgumentException("Parameter 'clusterId' cannot be empty");
        }

        if (OpengaussSourceTable.OPS_CLUSTER.equals(sourceTable)) {
            return getTargetClusterVoFromOpsCluster(clusterId);
        }

        return getTargetClusterVoFromJdbcCluster(clusterId);
    }

    @Override
    public List<TargetDatabaseVo> getTargetDatabases(OpengaussSourceTable sourceTable, String nodeId) {
        if (sourceTable == null) {
            throw new IllegalArgumentException("Parameter 'sourceTable' cannot be null");
        }
        if (StringUtils.isEmpty(nodeId)) {
            throw new IllegalArgumentException("Parameter 'nodeId' cannot be empty");
        }

        Map<String, String> databases;
        if (OpengaussSourceTable.OPS_CLUSTER.equals(sourceTable)) {
            OpsClusterNodeVO nodeVo = opsFacade.getOpsClusterNodeVOByNodeId(nodeId);
            if (nodeVo == null) {
                throw new OpsException("OpenGauss cluster node does not exist, node id: " + nodeId);
            }

            String url = String.format("jdbc:opengauss://%s:%s/postgres", nodeVo.getPublicIp(), nodeVo.getDbPort());
            databases = getOpengaussDatabases(url, nodeVo.getDbUser(), nodeVo.getDbUserPassword());
        } else {
            OpsJdbcDbClusterNodeEntity jdbcDbNode = jdbcDbClusterFacade.getJdbcDbClusterNode(nodeId);
            if (jdbcDbNode == null) {
                throw new OpsException("OpenGauss cluster node does not exist, node id: " + nodeId);
            }

            databases = getOpengaussDatabases(jdbcDbNode.getUrl(), jdbcDbNode.getUsername(), jdbcDbNode.getPassword());
        }

        List<TargetDatabaseVo> targetDatabaseVos = new ArrayList<>();
        if (databases == null || databases.isEmpty()) {
            return targetDatabaseVos;
        }

        for (Map.Entry<String, String> entry : databases.entrySet()) {
            String datname = entry.getKey();
            if (datname.equalsIgnoreCase("postgres")) {
                continue;
            }
            boolean isSelect = migrationTaskService.countNotFinishByTargetDb(nodeId, datname) == 0;
            targetDatabaseVos.add(new TargetDatabaseVo(datname, entry.getValue(), isSelect));
        }
        return targetDatabaseVos;
    }

    private Map<String, String> getOpengaussDatabases(String url, String username, String password) {
        try (Connection connection = JdbcUtil.getConnection(url, username, encryptionUtils.decrypt(password))) {
            return OpengaussUtils.getDatabasesWithSqlCompatibility(connection);
        } catch (SQLException e) {
            log.error("Failed to select openGauss databases, url: {}", url, e);
            throw new OpsException("Failed to select openGauss databases, url: " + url + ", exception: "
                    + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private TargetClusterVo getTargetClusterVoFromJdbcCluster(String clusterId) {
        JdbcDbClusterVO jdbcDbClusterVo = jdbcDbClusterFacade.getJdbcDbClusterVoByClusterId(clusterId);
        if (jdbcDbClusterVo == null) {
            throw new OpsException("Jdbc openGauss cluster does not exist, cluster id: " + clusterId);
        }

        TargetClusterVo targetClusterVo = TargetClusterVo.of(jdbcDbClusterVo);
        List<JdbcDbClusterNodeVO> jdbcDbClusterVoNodes = jdbcDbClusterVo.getNodes();
        if (jdbcDbClusterVoNodes.size() == 1) {
            JdbcDbClusterNodeVO nodeVo = jdbcDbClusterVoNodes.get(0);
            targetClusterVo.setUserMaster(JdbcUtil.judgeSystemAdmin(nodeVo.getIp(), nodeVo.getPort(),
                    nodeVo.getUsername(), encryptionUtils.decrypt(nodeVo.getPassword())));
            targetClusterVo.getNodes().forEach(vo -> vo.setPrimary(true));
            return targetClusterVo;
        }

        List<TargetClusterNodeVo> targetClusterNodeVos = new ArrayList<>();
        boolean hasFoundPrimary = false;
        for (JdbcDbClusterNodeVO jdbcNodeVo : jdbcDbClusterVoNodes) {
            TargetClusterNodeVo targetClusterNodeVo = TargetClusterNodeVo.of(jdbcNodeVo);
            targetClusterNodeVos.add(targetClusterNodeVo);

            String ip = jdbcNodeVo.getIp();
            String port = jdbcNodeVo.getPort();
            String username = jdbcNodeVo.getUsername();
            String password = jdbcNodeVo.getPassword();

            if (!hasFoundPrimary && isOpengaussNodePrimary(ip, port, username, encryptionUtils.decrypt(password))) {
                targetClusterNodeVo.setPrimary(true);
                hasFoundPrimary = true;
                targetClusterVo.setUserMaster(JdbcUtil.judgeSystemAdmin(ip, port, username,
                        encryptionUtils.decrypt(password)));
            }
        }

        if (!hasFoundPrimary) {
            throw new OpsException("Has not found primary node in jdbc openGauss cluster, cluster id: " + clusterId);
        }

        targetClusterVo.setNodes(targetClusterNodeVos);
        return targetClusterVo;
    }

    private TargetClusterVo getTargetClusterVoFromOpsCluster(String clusterId) {
        OpsClusterVO opsClusterVO = opsFacade.getOpsClusterVOByClusterId(clusterId);
        if (opsClusterVO == null) {
            throw new OpsException("Ops openGauss cluster does not exist, cluster id: " + clusterId);
        }

        TargetClusterVo targetClusterVo = TargetClusterVo.of(opsClusterVO);
        List<OpsClusterNodeVO> clusterNodes = opsClusterVO.getClusterNodes();
        if (clusterNodes.size() == 1) {
            OpsClusterNodeVO nodeVo = clusterNodes.get(0);
            targetClusterVo.setUserMaster(JdbcUtil.judgeSystemAdmin(nodeVo.getPublicIp(), nodeVo.getDbPort().toString(),
                    nodeVo.getDbUser(), encryptionUtils.decrypt(nodeVo.getDbUserPassword())));
            targetClusterVo.getNodes().forEach(vo -> vo.setPrimary(true));
            return targetClusterVo;
        }

        List<TargetClusterNodeVo> targetClusterNodeVos = new ArrayList<>();
        boolean hasFoundPrimary = false;
        for (OpsClusterNodeVO opsClusterNodeVO : clusterNodes) {
            TargetClusterNodeVo targetClusterNodeVo = TargetClusterNodeVo.of(opsClusterNodeVO);
            targetClusterNodeVos.add(targetClusterNodeVo);

            String ip = opsClusterNodeVO.getPublicIp();
            String port = opsClusterNodeVO.getDbPort().toString();
            String username = opsClusterNodeVO.getDbUser();
            String password = opsClusterNodeVO.getDbUserPassword();

            if (!hasFoundPrimary && isOpengaussNodePrimary(ip, port, username, password)) {
                targetClusterNodeVo.setPrimary(true);
                hasFoundPrimary = true;
                targetClusterVo.setUserMaster(JdbcUtil.judgeSystemAdmin(ip, port, username,
                        encryptionUtils.decrypt(password)));
            }
        }

        if (!hasFoundPrimary) {
            throw new OpsException("Has not found primary node in ops openGauss cluster, cluster id: " + clusterId);
        }

        targetClusterVo.setNodes(targetClusterNodeVos);
        return targetClusterVo;
    }

    private boolean isOpengaussNodePrimary(String ip, String port, String username, String password) {
        String url = String.format("jdbc:opengauss://%s:%s/postgres", ip, port);
        try (Connection connection = JdbcUtil.getConnection(url, username, encryptionUtils.decrypt(password))) {
            return isPrimary(connection);
        } catch (SQLException e) {
            log.error("Failed to connect to openGauss node '{}:{}' with user '{}'", ip, port, username, e);
            throw new OpsException("Failed to connect to openGauss node '" + ip + ":" + port + "' with user '"
                    + username + "', exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private IPage<String> getJdbcDbSourceTables(
            DbTypeEnum dbType, OpsJdbcDbClusterNodeEntity jdbcDbNode, String dbName, String schemaName, Page<T> page
    ) {
        try (Connection connection = JdbcUtil.getConnection(jdbcDbNode.getUrl(), jdbcDbNode.getUsername(),
                encryptionUtils.decrypt(jdbcDbNode.getPassword()))) {
            if (DbTypeEnum.MYSQL.equals(dbType)) {
                List<String> records = MysqlUtils.getDatabaseTablesPage(connection, dbName, page.getCurrent(),
                        page.getSize());
                long total = MysqlUtils.countDatabaseTables(connection, dbName);
                Page<String> resultPage = new Page<>(page.getCurrent(), page.getSize(), total);
                resultPage.setRecords(records);
                return resultPage;
            }

            throw new IllegalArgumentException("Database type '" + dbType + "' is not supported to get source tables");
        } catch (SQLException e) {
            log.error("Failed to get source tables from cluster node {}", jdbcDbNode.getClusterNodeId(), e);
            throw new OpsException("Failed to get source tables from cluster node " + jdbcDbNode.getClusterNodeId()
                    + " with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private IPage<String> getNonJdbcDbSourceTables(
            DbTypeEnum dbType, OpsNonJdbcDbClusterNodeEntity nodeEntity, String dbName, Page<T> page
    ) {
        String ip = nodeEntity.getIp();
        int port = Integer.parseInt(nodeEntity.getPort());
        String username = nodeEntity.getUsername();
        String password = nodeEntity.getPassword() != null ? encryptionUtils.decrypt(nodeEntity.getPassword()) : null;

        MilvusClientV2 milvusClientV2 = null;
        RestClient restClient = null;
        try {
            if (DbTypeEnum.MILVUS.equals(dbType)) {
                milvusClientV2 = MilvusUtils.createMilvusClientV2(ip, port, dbName, username, password);
                List<String> collections = MilvusUtils.listCollections(milvusClientV2).stream().sorted().toList();
                return PageHelper.getPageFromList(collections, page);
            }

            if (DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
                restClient = ElasticsearchUtils.createRestClient(ip, port, username, password);
                List<String> indexes = ElasticsearchUtils.listIndexes(restClient);
                indexes.removeIf(index -> index.startsWith(".kibana"));
                indexes = indexes.stream().sorted().toList();
                return PageHelper.getPageFromList(indexes, page);
            }

            throw new IllegalArgumentException("DbTypeEnum '" + dbType + "' is not supported to get source tables");
        } catch (Exception e) {
            String nodeId = nodeEntity.getClusterNodeId();
            log.error("Failed to get source tables form {} cluster node {}", dbType, nodeId, e);
            throw new OpsException("Failed to get source tables form " + dbType + " cluster node " + nodeId
                    + " with exception " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            if (milvusClientV2 != null) {
                MilvusUtils.closeMilvusClientV2(milvusClientV2);
            }
            if (restClient != null) {
                try {
                    ElasticsearchUtils.closeClient(restClient);
                } catch (IOException e) {
                    log.debug("Failed to close Elasticsearch rest client", e);
                }
            }
        }
    }

    private List<String> getJdbcDbSourceDatabases(DbTypeEnum dbType, OpsJdbcDbClusterNodeEntity jdbcDbNode) {
        try (Connection connection = JdbcUtil.getConnection(jdbcDbNode.getUrl(), jdbcDbNode.getUsername(),
                encryptionUtils.decrypt(jdbcDbNode.getPassword()))) {
            if (DbTypeEnum.MYSQL.equals(dbType)) {
                List<String> databases = MysqlUtils.listDatabases(connection);
                databases.removeIf("information_schema"::equals);
                databases.removeIf("mysql"::equals);
                databases.removeIf("performance_schema"::equals);
                databases.removeIf("sys"::equals);
                return databases;
            }

            if (DbTypeEnum.POSTGRESQL.equals(dbType)) {
                List<String> databases = PostgresqlUtils.listDatabases(connection);
                databases.removeIf("postgres"::equals);
                return databases;
            }

            throw new IllegalArgumentException("Database type " + dbType + " is not supported to get source databases");
        } catch (SQLException e) {
            log.error("Failed to get source databases from cluster node {}", jdbcDbNode.getClusterNodeId(), e);
            throw new OpsException("Failed to get source databases from cluster node " + jdbcDbNode.getClusterNodeId()
                    + " with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private List<String> getMilvusSourceDatabases(OpsNonJdbcDbClusterNodeEntity nodeEntity) {
        String ip = nodeEntity.getIp();
        int port = Integer.parseInt(nodeEntity.getPort());
        String username = nodeEntity.getUsername();
        String password = nodeEntity.getPassword() != null ? encryptionUtils.decrypt(nodeEntity.getPassword()) : null;
        MilvusClientV2 milvusClientV2 = null;
        try {
            milvusClientV2 = MilvusUtils.createMilvusClientV2(ip, port, null, username, password);
            return MilvusUtils.listDatabases(milvusClientV2);
        } catch (Exception e) {
            log.error("Failed to get Milvus databases form cluster node {}", nodeEntity.getClusterNodeId(), e);
            throw new OpsException("Failed to get Milvus databases form cluster node " + nodeEntity.getClusterNodeId()
                    + " with exception " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            if (milvusClientV2 != null) {
                MilvusUtils.closeMilvusClientV2(milvusClientV2);
            }
        }
    }

    private Map<String, Boolean> judgeClusterConnection(List<OpsClusterVO> filteredOpsClusterList) {
        ShellInfoVo shellInfoVo = new ShellInfoVo();
        int opsClusterNodeNum = filteredOpsClusterList.get(0).getClusterNodes().size();
        Map<String, Boolean> result = new HashMap<>();
        for (int i = 0; i < opsClusterNodeNum; i++) {
            OpsClusterNodeVO opsNode = filteredOpsClusterList.get(0).getClusterNodes().get(i);
            if (!filteredOpsClusterList.isEmpty()) {
                shellInfoVo.setIp(opsNode.getPublicIp());
                shellInfoVo.setPort(opsNode.getDbPort());
                shellInfoVo.setUsername(opsNode.getDbUser());
                shellInfoVo.setPassword(opsNode.getDbUserPassword());
            } else {
                log.error("cluster not exist.");
            }
            getConnectionInfo(shellInfoVo, result);
        }
        return result;
    }

    private Map<String, Boolean> judgeJdbcDbConnection(List<JdbcDbClusterVO> filteredJdbcList) {
        ShellInfoVo shellInfoVo = new ShellInfoVo();
        int jdbcClusterLength = filteredJdbcList.get(0).getNodes().size();
        Map<String, Boolean> result = new HashMap<>();
        for (int i = 0; i < jdbcClusterLength; i++) {
            JdbcDbClusterNodeVO jdbcNode = filteredJdbcList.get(0).getNodes().get(i);
            if (!filteredJdbcList.isEmpty()) {
                shellInfoVo.setIp(jdbcNode.getIp());
                shellInfoVo.setPort(Integer.parseInt(jdbcNode.getPort()));
                shellInfoVo.setUsername(jdbcNode.getUsername());
                shellInfoVo.setPassword(jdbcNode.getPassword());
            } else {
                log.error("JdbcDb not exist.");
            }
            getConnectionInfo(shellInfoVo, result);
        }
        return result;
    }

    private void getConnectionInfo(ShellInfoVo shellInfoVo, Map<String, Boolean> result) {
        String driver = "org.opengauss.Driver";
        String url = String.format("jdbc:opengauss://%s:%s/postgres", shellInfoVo.getIp(), shellInfoVo.getPort());
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, shellInfoVo.getUsername(), encryptionUtils.decrypt(shellInfoVo
                .getPassword()));
            result.put(shellInfoVo.getIp() + ":" + shellInfoVo.getPort(), isPrimary(conn));
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Connection failed", e);
            result.put(shellInfoVo.getIp() + ":" + shellInfoVo.getPort(), false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Failed to close connection", e);
                }
            }
        }
    }

    private boolean isPrimary(Connection conn) {
        String sql = "SELECT pg_is_in_recovery()";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return !rs.getBoolean(1);
            }
        } catch (SQLException e) {
            log.error("The execution of the 'SELECT pg_is_in_recovery()' command failed", e);
            throw new OpsException("Failed to execute sql 'SELECT pg_is_in_recovery()', error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<String> getMysqlClusterDbNames(String url, String username, String password) {
        String sql = "SELECT `SCHEMA_NAME` FROM `information_schema`.`SCHEMATA`;";
        List<String> dbList = new ArrayList<>();
        List<Map<String, Object>> resultSet = querySource(url, username, password, sql);
        resultSet.forEach(ret -> {
            String schemaName = ret.get("SCHEMA_NAME").toString();
            dbList.add(schemaName);
        });
        return dbList;
    }

    /**
     * get the list of database names on a node.
     *
     * @param clusterNode cluster node object
     * @return database name list
     */
    @Override
    public List<Map<String, Object>> getOpsClusterDbNames(OpsClusterNodeVO clusterNode) {
        List<Map<String, Object>> dbList = new ArrayList<>();
        if (clusterNode.getHostPort() != 22 || (opsFacade.isNodeInOpsCluster(clusterNode.getNodeId())
            && opsFacade.getOpsClusterVOByNodeId(clusterNode.getNodeId()).getClusterNodes().size() > 1
            && !isPrimaryNodeInCluster(clusterNode))) {
            return dbList;
        }
        String sql = "select datname, datcompatibility from pg_database;";
        List<Map<String, Object>> resultSet = queryTarget(clusterNode, "", sql);
        resultSet.forEach(ret -> {
            Map<String, Object> itemMap = new HashMap<>();
            String datname = ret.get("datname").toString();
            itemMap.put("dbName", datname);
            Integer count = migrationTaskService.countNotFinishByTargetDb(clusterNode.getNodeId(), datname);
            itemMap.put("isSelect", count == 0);
            String datcompatibility = ret.get("datcompatibility").toString();
            itemMap.put("datcompatibility", datcompatibility);
            dbList.add(itemMap);
        });
        return dbList;
    }

    /**
     * determine whether the node is primary
     *
     * @param clusterNode ops cluster node vo
     * @return boolean
     */
    private boolean isPrimaryNodeInCluster(OpsClusterNodeVO clusterNode) {
        String sql = "select * from pg_stat_get_wal_senders();";
        String url = JdbcUtil.getOpengaussJdbcUrl(clusterNode.getPublicIp(), clusterNode.getDbPort().toString(),
            clusterNode.getDbName(), "");
        return JdbcUtil.hasResultSetByExecuteQuery(url, sql, clusterNode.getDbUser(),
            encryptionUtils.decrypt(clusterNode.getDbUserPassword()));
    }

    private List<Map<String, Object>> convertList(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("convert error, message: {}", e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                log.error("convert error, message: {}", e.getMessage());
            }
        }
        return list;
    }

    private List<Map<String, Object>> querySource(String url, String username, String password, String sql) {
        ResultSet resultSet = null;
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, encryptionUtils.decrypt(password));
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            result = convertList(resultSet);
        } catch (SQLException | ClassNotFoundException e) {
            log.error("querySource soruce database error, {}", e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("querySource soruce database error, {}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * SQL querying based on OpenGauss.
     *
     * @param clusterNode ClusterNode Object
     * @param schema schema
     * @param sql sql
     * @return result list
     */
    private List<Map<String, Object>> queryTarget(OpsClusterNodeVO clusterNode, String schema, String sql) {
        return queryBySqlOnOpengauss(clusterNode.getPublicIp(), clusterNode.getDbPort().toString(),
            clusterNode.getDbName(), clusterNode.getDbUser(), encryptionUtils.decrypt(clusterNode.getDbUserPassword()),
            schema, sql);
    }

    /**
     * SQL querying based on OpenGauss.
     *
     * @param host host of db
     * @param port host of db
     * @param database database of db
     * @param dbUser user of db
     * @param dbPass password of db
     * @param schema schema of db
     * @param sql sql
     * @return result list
     */
    @Override
    public List<Map<String, Object>> queryBySqlOnOpengauss(String host, String port, String database, String dbUser,
        String dbPass, String schema, String sql) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("jdbc:opengauss://");
        stringBuilder.append(host).append(":");
        stringBuilder.append(port).append("/");
        stringBuilder.append(database).append("?currentSchema=").append(schema);
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Class.forName("org.opengauss.Driver");
            conn = DriverManager.getConnection(stringBuilder.toString(), dbUser, dbPass);
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            result = convertList(resultSet);
        } catch (ClassNotFoundException | SQLException e) {
            log.error("query target database error, {}", e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("query target database error, {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<OpsHostUserEntity> getHostUsers(String hostId) {
        OpsHostEntity opsHost = hostFacade.getById(hostId);
        OpsAssert.nonNull(opsHost, "host id " + hostId + " not exist");
        List<OpsHostUserEntity> opsHostUserEntities = hostUserFacade.listHostUserByHostId(hostId);
        List<OpsHostUserEntity> notRootUsers = opsHostUserEntities.stream()
            .filter(x -> !x.getUsername().equals("root"))
            .collect(Collectors.toList());
        return notRootUsers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult installPortal(String hostId, MigrationHostPortalInstall install) {
        if (PortalType.MULTI_DB.equals(install.getPortalType())) {
            return multiDbPortal.install(hostId, install, false);
        }
        return installPortalProc(hostId, install, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult retryInstallPortal(String hostId, MigrationHostPortalInstall install) {
        if (PortalType.MULTI_DB.equals(install.getPortalType())) {
            return multiDbPortal.install(hostId, install, true);
        }
        return installPortalProc(hostId, install, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult installPortalFromDatakit(String hostId, MigrationHostPortalInstall install, Integer userId)
        throws PortalInstallException {
        SysSettingEntity sysSettingEntity = sysSettingFacade.getSysSetting(userId);
        if (sysSettingEntity == null) {
            throw new PortalInstallException("not found system setting, please try again");
        }
        String pkgPath = sysSettingEntity.getUploadPath() + "/" + install.getPkgName();
        if (!FileUtil.exist(pkgPath)) {
            String errMsg = String.format("not found portal package %s, please try again", pkgPath);
            throw new PortalInstallException(errMsg);
        }
        // covert datakit local disk file to multipartfile
        try (InputStream in = new FileInputStream(pkgPath)) {
            MultipartFile file = FileUploadUtils.inputStreamToMultipartFile(in, "file", install.getPkgName());
            install.setFile(file);
        } catch (Exception ex) {
            String errMsg = String.format("transfer portal package %s to multipart file failed: %s", pkgPath,
                ex.getMessage());
            throw new PortalInstallException(errMsg);
        }
        return installPortalProc(hostId, install, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public AjaxResult installPortalProc(String hostId, MigrationHostPortalInstall install, boolean isReInstall) {
        OpsHostEntity opsHost = hostFacade.getById(hostId);
        if (PortalInstallType.ONLINE_INSTALL.getCode().equals(install.getInstallType())) {
            PortalVersion portalVersion = install.getPortalVersion();
            if (portalVersion == null) {
                throw new PortalInstallException("portal version cannot be null");
            }
            MigrationToolPortalDownloadInfo portalDownloadInfo = portalDownloadInfoService.getPortalDownloadInfo(
                    opsHost, PortalType.MYSQL_ONLY, portalVersion);
            install.setPkgDownloadUrl(portalDownloadInfo.getPortalPkgDownloadUrl());
            install.setPkgName(portalDownloadInfo.getPortalPkgName());
            install.setJarName(portalDownloadInfo.getPortalJarName());
        }
        if (!install.getInstallType().equals(PortalInstallType.IMPORT_INSTALL.getCode())) {
            preinstall(opsHost, install);
        }
        formatInstallPath(install);
        OpsHostUserEntity hostUser = hostUserFacade.getById(install.getHostUserId());
        String realInstallPath = getInstallPath(install.getInstallPath(), hostUser.getUsername());
        AjaxResult result = checkPermission(opsHost, hostUser, realInstallPath);
        if (!result.isOk()) {
            return result;
        }
        MigrationHostPortalInstall physicalInstallParams = preparePhysicalInstallParams(opsHost, hostUser, install,
            realInstallPath);
        if (PortalInstallType.IMPORT_INSTALL.getCode().equals(install.getInstallType())) {
            handleImportInstallation(physicalInstallParams);
            return AjaxResult.success();
        }
        MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig = saveThirdPartySoftwareRecord(install,
            physicalInstallParams);
        physicalInstallParams.setThirdPartySoftwareConfig(thirdPartySoftwareConfig);
        syncInstallPortalHandler(physicalInstallParams);
        // if reinstall path changed, clear old package
        MigrationHostPortalInstall oldInstall = migrationHostPortalInstallHostService.getOneByHostId(
            install.getRunHostId());
        if (isReInstall && !realInstallPath.equals(oldInstall.getInstallPath())) {
            deletePortal(hostId, false);
        }
        return AjaxResult.success();
    }

    /**
     * do something preinstall, check port used, and check java environment
     *
     * @param opsHost host entity
     * @param install portal install information
     */
    private void preinstall(OpsHostEntity opsHost, MigrationHostPortalInstall install) {
        OpsHostUserEntity installUser = hostUserFacade.getById(install.getHostUserId());
        if (StringUtils.isEmpty(installUser.getPassword())) {
            String errorMessage = "The install-user-password of the host is empty.";
            log.error(errorMessage);
            throw new PortalInstallException(errorMessage);
        }
        // check port used
        Integer configType = install.getThirdPartySoftwareConfig().getThirdPartySoftwareConfigType();
        SshLogin sshLogin = new SshLogin(opsHost.getPublicIp(), opsHost.getPort(), installUser.getUsername(),
            encryptionUtils.decrypt(installUser.getPassword()));
        if (configType.equals(ThirdPartySoftwareConfigType.INSTALL.getCode())) {
            checkPortUsed(sshLogin, install);
        }
        // check java environment
        checkJavaEnv(sshLogin);
    }

    private MigrationHostPortalInstall preparePhysicalInstallParams(OpsHostEntity opsHost, OpsHostUserEntity hostUser,
        MigrationHostPortalInstall install, String installPath) {
        MigrationHostPortalInstall physicalInstallParams = new MigrationHostPortalInstall();
        physicalInstallParams.setRunHostId(opsHost.getHostId());
        physicalInstallParams.setHost(opsHost.getPublicIp());
        physicalInstallParams.setPort(opsHost.getPort());
        physicalInstallParams.setRunUser(hostUser.getUsername());
        physicalInstallParams.setHostUserId(hostUser.getHostUserId());
        physicalInstallParams.setRunPassword(hostUser.getPassword());
        physicalInstallParams.setInstallPath(installPath);
        checkPkgName(install);
        checkJarName(install);
        physicalInstallParams.setJarName(install.getJarName());
        physicalInstallParams.setPkgName(install.getPkgName());
        physicalInstallParams.setPortalType(PortalType.MYSQL_ONLY);
        physicalInstallParams.setInstallStatus(PortalInstallStatus.INSTALLING.getCode());
        physicalInstallParams.setInstallType(install.getInstallType());
        physicalInstallParams.setFile(install.getFile());
        if (physicalInstallParams.getInstallType().equals(PortalInstallType.ONLINE_INSTALL.getCode())) {
            physicalInstallParams.setPkgDownloadUrl(install.getPkgDownloadUrl());
            physicalInstallParams.setPkgUploadPath(null);
        }
        return physicalInstallParams;
    }

    private void handleImportInstallation(MigrationHostPortalInstall physicalInstallParams) {
        physicalInstallParams.setInstallType(PortalInstallType.IMPORT_INSTALL.getCode());
        boolean isInstallSuccess = false;
        Optional<MigrationThirdPartySoftwareConfig> checkMqInstance =
                PortalHandle.checkInstallStatusAndUpdate(physicalInstallParams,
                        encryptionUtils.decrypt(physicalInstallParams.getRunPassword()));
        if (checkMqInstance.isPresent()) {
            isInstallSuccess = true;
            MigrationThirdPartySoftwareConfig mqInstance = checkMqInstance.get();
            if (!mqInstance.isEmpty()) {
                migrationThirdPartySoftwareInstanceService.saveRecord(mqInstance);
            }
        }

        migrationHostPortalInstallHostService.saveRecord(physicalInstallParams);
        if (isInstallSuccess) {
            threadPoolTaskExecutor.submit(() -> {
                physicalInstallParams.setRunPassword(encryptionUtils.decrypt(physicalInstallParams.getRunPassword()));
                loadTaskConfigParams(physicalInstallParams);
                migrationHostPortalInstallHostService.updateStatus(physicalInstallParams.getRunHostId(),
                    PortalInstallStatus.INSTALLED.getCode());
            });
        } else {
            migrationHostPortalInstallHostService.updateStatus(physicalInstallParams.getRunHostId(),
                PortalInstallStatus.INSTALL_ERROR.getCode());
        }
    }

    private MigrationThirdPartySoftwareConfig saveThirdPartySoftwareRecord(MigrationHostPortalInstall install,
        MigrationHostPortalInstall physicalInstallParams) {
        MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig = null;
        if (ThirdPartySoftwareConfigType.INSTALL.getCode()
            .equals(install.getThirdPartySoftwareConfig().getThirdPartySoftwareConfigType())) {
            thirdPartySoftwareConfig = MigrationThirdPartySoftwareConfig.builder()
                .zookeeperPort(install.getThirdPartySoftwareConfig().getZookeeperPort())
                .kafkaPort(install.getThirdPartySoftwareConfig().getKafkaPort())
                .schemaRegistryPort(install.getThirdPartySoftwareConfig().getSchemaRegistryPort())
                .installDir(install.getThirdPartySoftwareConfig().getInstallDir())
                .zkIp(physicalInstallParams.getHost())
                .kafkaIp(physicalInstallParams.getHost())
                .schemaRegistryIp(physicalInstallParams.getHost())
                .thirdPartySoftwareConfigType(ThirdPartySoftwareConfigType.INSTALL.getCode())
                .build();
            thirdPartySoftwareConfig.replacePathHome(physicalInstallParams.getRunUser());
            log.error("thirdPartySoftwareConfig = {}", thirdPartySoftwareConfig);
            migrationThirdPartySoftwareInstanceService.saveRecord(thirdPartySoftwareConfig);
        } else {
            thirdPartySoftwareConfig = migrationThirdPartySoftwareInstanceService.getById(install.getKafkaBindId());
            if (thirdPartySoftwareConfig == null) {
                log.error("select third party software is null");
                return MigrationThirdPartySoftwareConfig.builder().build();
            }
            thirdPartySoftwareConfig.setThirdPartySoftwareConfigType(ThirdPartySoftwareConfigType.BIND.getCode());
            List<String> bindlist = new ArrayList<>(Arrays.asList(thirdPartySoftwareConfig.getBindPortalHost() == null
                ? new String[] {}
                : thirdPartySoftwareConfig.getBindPortalHost().split(",")));
            if (!bindlist.contains(install.getHost())) {
                bindlist.add(physicalInstallParams.getHost());
                thirdPartySoftwareConfig.setBindPortalHost(StringUtils.join(bindlist, ","));
                migrationThirdPartySoftwareInstanceService.saveOrUpdate(thirdPartySoftwareConfig);
            }
        }
        return thirdPartySoftwareConfig;
    }

    private void syncInstallPortalHandler(MigrationHostPortalInstall installParams) {
        threadPoolTaskExecutor.submit(() -> {
            boolean isInstallSuccess;
            StringBuilder installPortalLogTemp = new StringBuilder();
            try {
                // upload portal
                if (installParams.getInstallType().equals(PortalInstallType.OFFLINE_INSTALL.getCode())) {
                    installPortalLogTemp.append("START_UPLOAD_OFFLINE_PACKAGE").append((char) 10);
                    UploadInfo uploadResult = uploadPortal(installParams.getFile(), installParams);
                    installParams.setPkgDownloadUrl("");
                    installParams.setPkgUploadPath(uploadResult);
                    installPortalLogTemp.append("END_UPLOAD_OFFLINE_PACKAGE").append((char) 10);
                }
                migrationHostPortalInstallHostService.saveRecord(installParams);
                installParams.setRunPassword(encryptionUtils.decrypt(installParams.getRunPassword()));
                // remove old datakit_install_portal.log
                removeInstallPortalLog(installParams);
                // check portal dependencies
                checkPortalDependencies(installParams, installPortalLogTemp);
                // install portal
                installPortalLogTemp.append("START_INSTALL_PORTAL").append((char) 10);
                isInstallSuccess = PortalHandle.installPortal(installParams);
            } catch (PortalInstallException e) {
                log.error(e.getMessage());
                installPortalLogTemp.append(e.getMessage()).append((char) 10);
                isInstallSuccess = false;
            } catch (OpsException e) {
                log.error("install portal failed", e);
                installPortalLogTemp.append(e.getMessage()).append((char) 10);
                isInstallSuccess = false;
            }
            if (!isInstallSuccess && ThirdPartySoftwareConfigType.INSTALL.getCode()
                .equals(installParams.getThirdPartySoftwareConfig().getThirdPartySoftwareConfigType())) {
                log.info("install failed remove record");
                migrationThirdPartySoftwareInstanceService.removeInstance(installParams.getHost());
            }
            if (isInstallSuccess) {
                installPortalLogTemp.append("END_INSTALL_PORTAL").append((char) 10);
                loadTaskConfigParams(installParams);
            }
            migrationHostPortalInstallHostService.updateStatus(installParams.getRunHostId(), isInstallSuccess
                ? PortalInstallStatus.INSTALLED.getCode()
                : PortalInstallStatus.INSTALL_ERROR.getCode());
            printInstallPortalLog(installParams, installPortalLogTemp.toString());
        });
    }

    /**
     * check portal dependencies
     *
     * @param installParams install portal information
     */
    private void checkPortalDependencies(MigrationHostPortalInstall installParams, StringBuilder installPortalLogTemp) {
        // get root shell information
        OpsHostUserEntity runUser = hostUserFacade.getHostUserByUsername(installParams.getRunHostId(),
            installParams.getRunUser());
        OpsAssert.nonNull(runUser, "run user " + installParams.getRunUser() + " is not exist");
        ShellInfoVo runShellInfo = new ShellInfoVo(installParams.getHost(), installParams.getPort(),
            runUser.getUsername(), encryptionUtils.decrypt(runUser.getPassword()));
        installPortalLogTemp.append("START_CHECK_PORTAL_DEPENDENCIES").append((char) 10);
        List<String> dependencies = List.of("mysql-devel", "mysql5-devel", "mariadb-devel", "python3-devel",
            "python-devel");
        List<String> missingDependencies = ShellUtil.checkDependencies(runShellInfo, dependencies);
        if (CollUtil.isNotEmpty(missingDependencies)) {
            installPortalLogTemp.append("miss dependencies:").append(missingDependencies);
        }
        OpsHostUserEntity rootUser = hostUserFacade.getRootUserByHostId(installParams.getRunHostId());
        if (Objects.isNull(rootUser)) {
            log.warn("host {} does not have root permission, to install dependencies:{} ,",
                installParams.getRunHostId(), dependencies);
            installPortalLogTemp.append("no root permission, to install dependencies:").append(dependencies);
            installPortalLogTemp.append("END_CHECK_PORTAL_DEPENDENCIES").append((char) 10);
            return;
        }
        ShellInfoVo rootShellInfo = new ShellInfoVo(installParams.getHost(), installParams.getPort(),
            rootUser.getUsername(), encryptionUtils.decrypt(rootUser.getPassword()));
        // Check the mysql-devel mysql5-devel mariadb-devel python3-devel python-devel dependencies.
        // If anyone does not exist, install it.
        try {
            ShellUtil.installDependencies(rootShellInfo, missingDependencies);
        } catch (ShellException e) {
            String logInfo = "Install portal dependencies failed, error message: " + e.getMessage();
            log.error(logInfo);
            installPortalLogTemp.append(logInfo).append((char) 10);
        }
        installPortalLogTemp.append("END_CHECK_PORTAL_DEPENDENCIES").append((char) 10);
    }

    /**
     * remove old datakit_install_portal.log
     *
     * @param installParams install portal information
     */
    private void removeInstallPortalLog(MigrationHostPortalInstall installParams) {
        JschResult jschResult = ShellUtil.execCommandGetResult(installParams.getShellInfoVo(),
            "rm -rf " + installParams.getDatakitLogPath());
        if (!jschResult.isOk()) {
            log.error("Remove datakit_install_portal.log failed, message: {}", jschResult.getResult());
        }
    }

    /**
     * Output logs to datakit_install_portal.log
     *
     * @param installParams portal install information
     */
    private void printInstallPortalLog(MigrationHostPortalInstall installParams, String logInfo) {
        String command = String.format("mkdir -p %s && echo '%s' >> %s", installParams.getInstallPath(), logInfo,
            installParams.getDatakitLogPath());
        JschResult result = ShellUtil.execCommandGetResult(installParams.getShellInfoVo(), command);
        if (!result.isOk()) {
            log.error("Output logs to datakit_install_portal.log failed: " + result.getResult());
        }
    }

    /**
     * 加载portal配置文件
     *
     * @param installParams installParams
     * @author: www
     * @date: 2023/11/28 10:41
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     */
    private void loadTaskConfigParams(MigrationHostPortalInstall installParams) {
        Map<Integer, Map<String, Object>> toolsConfig = null;
        Properties toolsParamsDesc = null;
        try {
            toolsConfig = PortalHandle.loadToolsConfig(installParams);
            Optional<Properties> properties = PortalHandle.loadToolsParamsDesc(installParams.getHost(),
                installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(),
                installParams.getInstallPath());
            if (properties.isPresent()) {
                toolsParamsDesc = properties.get();
            }
        } catch (PortalInstallException e) {
            log.error("loadTaskConfigParams failed", e);
            return;
        }
        List<TbMigrationTaskGlobalToolsParam> globalToolsParams = new ArrayList<>();
        for (ToolsConfigEnum configEnum : ToolsConfigEnum.values()) {
            Map<String, Object> toolConfigs = toolsConfig.get(configEnum.getType());
            for (Map.Entry<String, Object> toolConfig : toolConfigs.entrySet()) {
                if (toolConfig.getKey().matches("^\\d\\.\\d\\..*")) {
                    continue;
                }
                TbMigrationTaskGlobalToolsParam taskGlobalToolsParam = new TbMigrationTaskGlobalToolsParam();
                taskGlobalToolsParam.setConfigId(configEnum.getType());
                taskGlobalToolsParam.setParamKey(toolConfig.getKey());
                Object value = toolConfig.getValue();
                taskGlobalToolsParam.setParamValueAndType(value);
                taskGlobalToolsParam.setPortalHostID(installParams.getRunHostId());
                taskGlobalToolsParam.setDeleteFlag(TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.USED.getDeleteFlag());
                String paramDescKey = taskGlobalToolsParam.getConfigId() + "."
                    + taskGlobalToolsParam.getParamValueType() + "." + taskGlobalToolsParam.getParamKey();
                taskGlobalToolsParam.setParamDesc(
                    toolsParamsDesc == null ? "" : String.valueOf(toolsParamsDesc.get(paramDescKey)));
                globalToolsParams.add(taskGlobalToolsParam);
            }
        }
        taskGlobalToolsParamService.saveBatch(globalToolsParams);
    }

    @Override
    public String getPortalInstallLog(String hostId) {
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(hostId);
        String password = encryptionUtils.decrypt(installHost.getRunPassword());
        String datakitLogPath = installHost.getDatakitLogPath();
        String datakitContent = PortalHandle.getTaskLogs(installHost.getHost(), installHost.getPort(),
            installHost.getRunUser(), password, datakitLogPath);
        if (StringUtils.isNotBlank(datakitContent)) {
            return datakitContent;
        }
        String logPath = installHost.getPortalLogPath();
        String content = PortalHandle.getTaskLogs(installHost.getHost(), installHost.getPort(),
            installHost.getRunUser(), password, logPath);
        String logContent = " ";
        if (StringUtils.isNotBlank(content)) {
            logContent = content;
        }
        return logContent;
    }

    @Override
    public UploadInfo upload(MultipartFile file, Integer userId) throws PortalInstallException {
        UploadInfo uploadInfo = new UploadInfo();
        if (ObjectUtil.isNull(file)) {
            return uploadInfo;
        }
        SysSettingEntity entity = sysSettingFacade.getSysSetting(userId);
        if (ObjectUtil.isNull(entity)) {
            log.error("Cannot find system setting of user id: " + userId);
            throw new PortalInstallException("Cannot find system setting of your account, please try again");
        }
        // create folder
        File folder = new File(entity.getUploadPath());
        if (!folder.exists()) {
            boolean res = folder.mkdirs();
            if (!res) {
                String errMsg = String.format("Can't create folder: %s, please try again", entity.getUploadPath());
                log.error(errMsg);
                throw new PortalInstallException(errMsg);
            }
        }
        String fileRealPath = Path.of(entity.getUploadPath(), file.getOriginalFilename()).toString();
        try {
            file.transferTo(new File(fileRealPath));
            uploadInfo.setName(file.getOriginalFilename());
            uploadInfo.setRealPath(fileRealPath);
        } catch (Exception ex) {
            String errMsg = String.format("Upload tar file to %s failed: %s", fileRealPath, ex.getMessage());
            log.error(errMsg);
            throw new PortalInstallException(errMsg);
        }
        return uploadInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deletePortal(String hostId, Boolean onlyPkg) {
        MigrationHostPortalInstall install = migrationHostPortalInstallHostService.getOneByHostId(hostId);
        List<MigrationTask> tasks = migrationTaskService.listRunningTaskByHostId(hostId);
        if (CollUtil.isNotEmpty(tasks)) {
            return AjaxResult.error(MigrationErrorCode.PORTAL_DELETE_ERROR.getCode(),
                MigrationErrorCode.PORTAL_DELETE_ERROR.getMsg());
        }
        if (PortalType.MULTI_DB.equals(install.getPortalType())) {
            return multiDbPortal.deletePortal(hostId);
        }

        List<String> bindPortalKafkas = migrationThirdPartySoftwareInstanceService.listBindHostsByPortalHost(
            install.getHost());
        if (!CollectionUtils.isEmpty(bindPortalKafkas)) {
            return AjaxResult.error(MigrationErrorCode.PORTAL_DELETE_ERROR_FOR_KAFKA_USED.getCode(),
                MigrationErrorCode.PORTAL_DELETE_ERROR_FOR_KAFKA_USED.getMsg());
        }
        OpsHostUserEntity hostUser = hostUserFacade.getById(install.getHostUserId());
        String realInstallPath = getInstallPath(install.getInstallPath(), hostUser.getUsername());
        install.setInstallPath(realInstallPath);
        String portalHome = realInstallPath + "portal/";
        // stop kafka
        OpsHostEntity opsHost = hostFacade.getById(hostId);
        String password = encryptionUtils.decrypt(hostUser.getPassword());
        try {
            SshLogin sshLogin = new SshLogin(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(),
                password);
            stopKafka(sshLogin, portalHome, install.getJarName(), 0);
        } catch (PortalInstallException ex) {
            return AjaxResult.error(MigrationErrorCode.STOP_KAFKA_ERROR.getCode(),
                MigrationErrorCode.STOP_KAFKA_ERROR.getMsg());
        }
        clearChameleonEnvData(portalHome, install.getJarName(), opsHost, hostUser, password);
        // if delete file failed, do nothing
        if (onlyPkg != null && !onlyPkg) {
            removeKafkaTools(install, portalHome, opsHost, hostUser, password);
            ShellUtil.execCommandGetResult(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password,
                "rm -rf  " + realInstallPath + "portal");
            migrationHostPortalInstallHostService.removeById(install.getId());
        }
        migrationHostPortalInstallHostService.clearPkgUploadPath(hostId);
        ShellUtil.rmFile(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password,
            realInstallPath + install.getPkgName());
        ShellUtil.rmFile(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password,
            install.getDatakitLogPath());
        toolsParamService.removeByHostId(install.getRunHostId());
        return AjaxResult.success();
    }

    private void clearChameleonEnvData(String portalHome, String jarName, OpsHostEntity opsHost,
                                       OpsHostUserEntity hostUser, String password) {
        String chameleonHome = portalHome + "tools" + TaskConstant.PATH_SEPARATOR + "chameleon"
                + TaskConstant.PATH_SEPARATOR;
        String[] splitName = jarName.split("-");
        if (splitName.length < 2) {
            // if jarName is not valid, do nothing
            return;
        }
        String version = splitName[1];
        String chameleonVersionHome = chameleonHome + "chameleon" + "-" + version + TaskConstant.PATH_SEPARATOR;
        String command = "sh clear_env_var.sh";
        command = "cd " + chameleonVersionHome + " && " + command;
        log.info("chameleon home: {}", chameleonVersionHome);
        ShellUtil.execCommandGetResult(opsHost.getPublicIp(), opsHost.getPort(),
                hostUser.getUsername(), password, command);
        log.info("clear chameleon env data success");
    }

    private void removeKafkaTools(MigrationHostPortalInstall install, String portalHome, OpsHostEntity opsHost,
        OpsHostUserEntity hostUser, String password) {
        String kafkaInstallPath = migrationThirdPartySoftwareInstanceService.removeInstance(install.getHost());
        if (StringUtils.isEmpty(kafkaInstallPath)) {
            String propertiesPath = portalHome + "workspace/1/config/toolspath.properties";
            boolean isPropertiesExists = PortalHandle.fileExists(opsHost.getPublicIp(), opsHost.getPort(),
                hostUser.getUsername(), password, propertiesPath);
            if (isPropertiesExists) {
                String command = String.format("grep '^confluent.install.path' %sworkspace/1/config/"
                    + "toolspath.properties | awk -F '=' '{print $2}' | tr -d ' '", portalHome);
                JschResult jschResult = ShellUtil.execCommandGetResult(opsHost.getPublicIp(), opsHost.getPort(),
                    hostUser.getUsername(), password, command);
                kafkaInstallPath = jschResult.isOk() ? jschResult.getResult().trim() : "";
            }
        }
        if (!StringUtils.isEmpty(kafkaInstallPath)) {
            ShellUtil.execCommandGetResult(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password,
                "rm -rf  " + kafkaInstallPath + "confluent-5.5.1");
        }
    }

    /**
     * check is install path has Permission
     *
     * @param opsHost host to install portal
     * @param hostUser user to install portal
     * @param installPath portal install path
     * @return check result
     */
    private AjaxResult checkPermission(OpsHostEntity opsHost, OpsHostUserEntity hostUser, String installPath) {
        String realInstallPath = getInstallPath(installPath, hostUser.getUsername());
        String password = encryptionUtils.decrypt(hostUser.getPassword());
        boolean isExists = PortalHandle.directoryExists(opsHost.getPublicIp(), opsHost.getPort(),
            hostUser.getUsername(), password, realInstallPath);
        if (isExists) {
            boolean isExistsAndHasPermission = PortalHandle.checkWritePermission(opsHost.getPublicIp(),
                opsHost.getPort(), hostUser.getUsername(), password, realInstallPath);
            return isExistsAndHasPermission
                ? AjaxResult.success()
                : AjaxResult.error(MigrationErrorCode.PORTAL_INSTALL_PATH_NOT_HAS_WRITE_PERMISSION_ERROR.getMsg());
        } else {
            boolean isCreateSuccess = PortalHandle.mkdirDirectory(opsHost.getPublicIp(), opsHost.getPort(),
                hostUser.getUsername(), password, realInstallPath);
            return isCreateSuccess
                ? AjaxResult.success()
                : AjaxResult.error(MigrationErrorCode.PORTAL_CREATE_INSTALL_PATH_FAILED.getMsg());
        }
    }

    private void formatInstallPath(MigrationHostPortalInstall install) {
        String installPath = install.getInstallPath();
        String mqInstallDir = install.getThirdPartySoftwareConfig().getInstallDir();
        if (installPath != null) {
            install.setInstallPath(installPath.replaceAll("\\s", ""));
        }
        if (mqInstallDir != null) {
            install.getThirdPartySoftwareConfig().setInstallDir(mqInstallDir.replaceAll("\\s", ""));
        }
    }

    private String getInstallPath(String installPath, String userName) {
        String result = installPath;
        if (installPath.equals("~/") || installPath.equals("~")) {
            result = "/home/" + userName + "/";
        }
        return result;
    }

    /**
     * upload portal to remote
     *
     * @param multipartFile file instance
     * @param install upload params
     * @return upload result
     */
    private UploadInfo uploadPortal(MultipartFile multipartFile, MigrationHostPortalInstall install)
        throws PortalInstallException {
        UploadInfo result = new UploadInfo();
        if (multipartFile == null || StrUtil.isEmpty(multipartFile.getOriginalFilename())) {
            log.warn("Upload file is empty, please check");
            return result;
        }
        try (InputStream in = multipartFile.getInputStream()) {
            ShellUtil.uploadFile(install.getHost(), install.getPort(), install.getRunUser(),
                encryptionUtils.decrypt(install.getRunPassword()),
                install.getInstallPath() + multipartFile.getOriginalFilename(), in);
            result.setName(multipartFile.getOriginalFilename());
            result.setRealPath(install.getInstallPath());
        } catch (Exception e) {
            String errMsg = "Upload portal error: " + e.getMessage();
            log.error(errMsg);
            throw new PortalInstallException(errMsg);
        }
        return result;
    }

    /**
     * stop kafka before uninstall portal
     *
     * @param sshLogin portal install sshLogin
     * @param portalHome portal install path
     * @param jarName portal jar name
     * @param retryCount failed retry count
     */
    private void stopKafka(SshLogin sshLogin, String portalHome, String jarName, int retryCount)
        throws PortalInstallException {
        String jarPath = portalHome + jarName;
        if (retryCount == 0) {
            boolean isJatExists = PortalHandle.fileExists(sshLogin.getHost(), sshLogin.getPort(),
                sshLogin.getUsername(), sshLogin.getPassword(), jarPath);
            if (!isJatExists) {
                return;
            }
        }
        String stopKafkaCommand = "java -Dpath=" + portalHome + " -Dorder=stop_kafka -Dskip=true -jar " + jarPath;
        JschResult result = ShellUtil.execCommandGetResult(sshLogin.getHost(), sshLogin.getPort(),
            sshLogin.getUsername(), sshLogin.getPassword(), stopKafkaCommand);
        if (!result.isOk()) {
            if (retryCount > 3) {
                throw new PortalInstallException("Stop kafka failed after 3 retries: " + result.getResult());
            }
            log.error("stop kafka failed {} times, try again, host: {}", retryCount, sshLogin.getHost());
            stopKafka(sshLogin, portalHome, jarName, ++retryCount);
        }
    }

    /**
     * check the third tools install port has used
     *
     * @param sshLogin sshLogin
     * @param install Migration host portal install information
     */
    private void checkPortUsed(SshLogin sshLogin, MigrationHostPortalInstall install) {
        // check port used
        int zkPort = Integer.parseInt(install.getThirdPartySoftwareConfig().getZookeeperPort());
        OpsAssert.isTrue(jschExecutorFacade.checkOsPortConflict(sshLogin, zkPort),
            "Zookeeper port " + zkPort + " is used, please check and try again");
        int kafkaPort = Integer.parseInt(install.getThirdPartySoftwareConfig().getKafkaPort());
        OpsAssert.isTrue(jschExecutorFacade.checkOsPortConflict(sshLogin, kafkaPort),
            "Kafka port " + kafkaPort + " is used, please check and try again");
        int schemaRegistryPort = Integer.parseInt(install.getThirdPartySoftwareConfig().getSchemaRegistryPort());
        OpsAssert.isTrue(jschExecutorFacade.checkOsPortConflict(sshLogin, schemaRegistryPort),
            "schema registry port " + schemaRegistryPort + " is used, please check and try again");
    }

    /**
     * check java environment and java version
     *
     * @param sshLogin user shell information
     */
    private void checkJavaEnv(SshLogin sshLogin) {
        // Execute the java -version command. If the command fails, thrown an exception.
        String version = jschExecutorFacade.checkJavaVersion(sshLogin);
        // check the java version
        int javaVersionMajor = CommonUtils.getJavaVersionMajor(version);
        if (javaVersionMajor < 17) {
            String errMsg = "The java version is not match 17+, "
                + "please check environment JAVA_HOME,it must configuration in user ~/.bashrc";
            log.warn("{}, host: {}, user: {}", errMsg, sshLogin.getHost(), sshLogin.getUsername());
            throw new PortalInstallException(errMsg);
        }
        log.info("Java version is compatible with the installation requirements.");
    }

    /**
     * @param install install information
     */
    private void checkPkgName(MigrationHostPortalInstall install) {
        String pkgName = install.getPkgName();
        if (!StringUtils.isEmpty(pkgName)) {
            int index = pkgName.indexOf("PortalControl");
            if (index != -1) {
                install.setPkgName(pkgName.substring(index));
            } else {
                log.error("The portal package name does not contain 'PortalControl'. Please check.");
            }
        }
    }

    /**
     * @param install install information
     */
    private void checkJarName(MigrationHostPortalInstall install) {
        String pkgName = install.getPkgName();
        if (!StringUtils.isEmpty(pkgName)) {
            String[] parts = pkgName.split("-");
            if (parts.length > 1) {
                String versionNumber = parts[1];
                String signVersion = "6.0.0";
                if (versionNumber.compareTo(signVersion) >= 0) {
                    install.setJarName("portalControl-" + versionNumber + "-exec.jar");
                }
            } else {
                log.error("Failed to obtain the portal version number by parsing the installation package name.");
            }
        }
    }

    @Override
    public boolean isConnectUserAdmin(OpsClusterNodeVO clusterNode) {
        if (clusterNode == null || clusterNode.getDbPort() == null || StringUtils.isAnyBlank(clusterNode.getPublicIp(),
            clusterNode.getDbUser(), clusterNode.getDbUserPassword())) {
            log.warn("Invalid input data. Please check the input parameters.");
            return false;
        }
        return JdbcUtil.judgeSystemAdmin(clusterNode.getPublicIp(), clusterNode.getDbPort().toString(),
            clusterNode.getDbUser(), encryptionUtils.decrypt(clusterNode.getDbUserPassword()));
    }
}
