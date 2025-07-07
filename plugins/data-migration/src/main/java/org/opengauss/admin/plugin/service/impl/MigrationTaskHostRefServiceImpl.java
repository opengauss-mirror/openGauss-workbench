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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcInfo;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.file.FileUploadUtils;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.plugin.constants.SqlConstants;
import org.opengauss.admin.plugin.constants.TaskConstant;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;
import org.opengauss.admin.plugin.domain.TbMigrationTaskGlobalToolsParam;
import org.opengauss.admin.plugin.dto.CustomDbResource;
import org.opengauss.admin.plugin.dto.MigrationHostDto;
import org.opengauss.admin.plugin.dto.PortalInstallHostDto;
import org.opengauss.admin.plugin.enums.DbTypeEnum;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.enums.PortalInstallStatus;
import org.opengauss.admin.plugin.enums.PortalInstallType;
import org.opengauss.admin.plugin.enums.PortalType;
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
import org.opengauss.admin.plugin.service.TbMigrationTaskGlobalToolsParamService;
import org.opengauss.admin.plugin.utils.JDBCUtils;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.plugin.vo.TargetClusterNodeVO;
import org.opengauss.admin.plugin.vo.TargetClusterVO;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.opengauss.admin.plugin.enums.DbTypeEnum.MYSQL;
import static org.opengauss.admin.plugin.enums.DbTypeEnum.OPENGAUSS;
import static org.opengauss.admin.plugin.enums.DbTypeEnum.POSTGRESQL;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskHostRefServiceImpl extends ServiceImpl<MigrationTaskHostRefMapper, MigrationTaskHostRef>
    implements MigrationTaskHostRefService {

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
            if (installHost == null) {
                if (portalType != null) {
                    return;
                }
                String[] opsHosInfo = getHostBaseInfo(host.getHostId());
                if (opsHosInfo.length == 0) {
                    log.warn("Host no install , opsHostInfo is empty {}", host);
                    return;
                }
                eachOne.setBaseInfos(ListUtil.toList(opsHosInfo));
                eachOne.setInstallPortalStatus(PortalInstallStatus.NOT_INSTALL.getCode());
            } else {
                if (portalType != null && !installHost.getPortalType().equals(portalType)) {
                    return;
                }
                String[] hostInfo = getHostBaseInfo(host.getHostId());
                if (hostInfo.length == 0) {
                    log.warn("Host install , opsHostInfo is empty {}", host);
                    return;
                }
                eachOne.setBaseInfos(ListUtil.toList(hostInfo));
                eachOne.setInstallInfo(installHost);
                eachOne.setInstallPortalStatus(installHost.getInstallStatus());
            }
            hostDtoList.add(eachOne);
        });
        hostDtoList.sort(Comparator.comparing(o -> o.getHostInfo().getPublicIp()));
        List<MigrationHostDto> totalHostDto = filterBaseInfos(hostDtoList, portalInstallHostDto);
        return getPageFromList(totalHostDto, iPage.getCurrent(), iPage.getSize());
    }

    private List<MigrationHostDto> filterBaseInfos(
            List<MigrationHostDto> hostDtoList, PortalInstallHostDto portalInstallHostDto) {
        List<MigrationHostDto> resultList = new ArrayList<>();
        for (MigrationHostDto hostDto : hostDtoList) {
            List<String> baseInfos = hostDto.getBaseInfos();
            if (baseInfos.size() == 3) {
                String cpu = baseInfos.get(0);
                Integer expectCpu = portalInstallHostDto.getCpu();
                if (cpu == null || (expectCpu != null && !Integer.valueOf(cpu).equals(expectCpu))) {
                    continue;
                }

                String memory = baseInfos.get(1);
                Double expectMemory = portalInstallHostDto.getMemory();
                if (memory == null || (expectMemory != null && Double.parseDouble(memory) < expectMemory)) {
                    continue;
                }

                String disk = baseInfos.get(2);
                Double expectDisk = portalInstallHostDto.getDisk();
                if (disk == null || (expectDisk != null && Double.parseDouble(disk) < expectDisk)) {
                    continue;
                }
                resultList.add(hostDto);
            }
        }
        return resultList;
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

    private String[] getHostBaseInfo(String hostId) {
        String hostInfo = hostMonitorFacade.getMigrationHostInfo(hostId);
        if (StrUtil.isEmpty(hostInfo)) {
            return new String[3];
        }
        return hostInfo.split("\n");
    }

    @Override
    public List<JdbcDbClusterVO> getSourceClusters() {
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("MYSQL");
        return jdbcTargetCluster;
    }

    @Override
    public List<JdbcDbClusterVO> getSourceClusters(String dbTypeStr) {
        List<JdbcDbClusterVO> jdbcSourceCluster = new ArrayList<>();
        DbTypeEnum dbType = DbTypeEnum.fromString(dbTypeStr.toUpperCase(Locale.ROOT));
        switch (dbType) {
            case MYSQL:
                jdbcSourceCluster = jdbcDbClusterFacade.listAll(MYSQL.getDbType());
                break;
            case OPENGAUSS:
                jdbcSourceCluster = jdbcDbClusterFacade.listAll(OPENGAUSS.getDbType());
                jdbcSourceCluster.addAll(getClustersManagementList());
                break;
            case POSTGRESQL:
                jdbcSourceCluster = jdbcDbClusterFacade.listAll(POSTGRESQL.getDbType());
                break;
            default:
                log.warn("Unsupported database type.");
                break;
        }
        return jdbcSourceCluster;
    }

    @Override
    public List<JdbcDbClusterVO> getPgsqlClusters() {
        List<JdbcDbClusterVO> jdbcDbClusterVOList = jdbcDbClusterFacade.listAll(
                org.opengauss.admin.common.enums.ops.DbTypeEnum.POSTGRESQL.name());
        if (jdbcDbClusterVOList.isEmpty()) {
            return jdbcDbClusterVOList;
        }

        for (JdbcDbClusterVO jdbcDbClusterVO : jdbcDbClusterVOList) {
            for (JdbcDbClusterNodeVO dbClusterNodeVO : jdbcDbClusterVO.getNodes()) {
                if (dbClusterNodeVO == null) {
                    continue;
                }

                String version = getPgsqlVersion(dbClusterNodeVO.getUrl(), dbClusterNodeVO.getUsername(),
                        dbClusterNodeVO.getPassword());
                boolean isSupport = isPgsqlVersionSupportMigration(version);
                if (!isSupport) {
                    log.error("Current version {} is not supported for migration.", version);
                }
                break;
            }
        }
        return jdbcDbClusterVOList;
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
    public void saveDbResource(CustomDbResource dbResource) {
        saveSource(dbResource.getClusterName(), dbResource.getDbUrl(), dbResource.getUserName(),
            dbResource.getPassword());
    }

    @Override
    public void saveSource(String clusterName, String dbUrl, String username, String password) {
        JdbcDbClusterInputDto clusterInput = new JdbcDbClusterInputDto();
        clusterInput.setClusterName(clusterName);
        JdbcDbClusterNodeInputDto node = new JdbcDbClusterNodeInputDto();
        node.setUrl(dbUrl);
        node.setUsername(username);
        node.setPassword(password);
        List<JdbcDbClusterNodeInputDto> nodes = new ArrayList<>();
        nodes.add(node);
        clusterInput.setNodes(nodes);
        jdbcDbClusterFacade.add(clusterInput);
    }

    @Override
    public List<TargetClusterVO> getTargetClusters() {
        List<OpsClusterVO> opsClusterVOS = opsFacade.listCluster();
        List<TargetClusterVO> targetClusters = opsClusterVOS.stream().map(o -> {
            TargetClusterVO clusterVO = new TargetClusterVO();
            clusterVO.setClusterId(o.getClusterId());
            clusterVO.setClusterName(o.getClusterName());
            clusterVO.setVersion(o.getVersion());
            clusterVO.setDeployType(o.getDeployType());
            clusterVO.setVersionNum(
                StringUtils.isNotBlank(o.getVersionNum()) ? o.getVersionNum() : TaskConstant.DEFAULT_OPENGAUSS_VERSION);
            List<TargetClusterNodeVO> nodes = o.getClusterNodes().stream().map(on -> {
                TargetClusterNodeVO clusterNodeVO = new TargetClusterNodeVO();
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
                clusterNodeVO.setIsSystemAdmin(false);
                return clusterNodeVO;
            }).collect(Collectors.toList());
            clusterVO.setClusterNodes(nodes);
            return clusterVO;
        }).collect(Collectors.toList());
        targetClusters.addAll(getJdbcTargetClusters());
        return targetClusters;
    }

    private List<JdbcDbClusterVO> getClustersManagementList() {
        List<OpsClusterVO> opsClusterVOS = opsFacade.listCluster();
        return opsClusterVOS.stream().map(cluster -> {
            JdbcDbClusterVO jdbcDbClusterVO = new JdbcDbClusterVO();
            jdbcDbClusterVO.setClusterId(cluster.getClusterId());
            jdbcDbClusterVO.setName(cluster.getClusterName());
            jdbcDbClusterVO.setDeployType(cluster.getDeployType());
            jdbcDbClusterVO.setNodes(getJdbcDbClusterList(cluster));
            return jdbcDbClusterVO;
        }).collect(Collectors.toList());
    }

    private List<JdbcDbClusterNodeVO> getJdbcDbClusterList(OpsClusterVO cluster) {
        return cluster.getClusterNodes().stream().map(node -> {
            JdbcDbClusterNodeVO jdbcDbClusterNodeVO = new JdbcDbClusterNodeVO();
            jdbcDbClusterNodeVO.setClusterNodeId(node.getNodeId());
            jdbcDbClusterNodeVO.setName(node.getNodeId());
            jdbcDbClusterNodeVO.setIp(node.getPublicIp());
            jdbcDbClusterNodeVO.setPort(node.getDbPort().toString());
            jdbcDbClusterNodeVO.setUsername(node.getDbUser());
            jdbcDbClusterNodeVO.setPassword(node.getDbUserPassword());
            String url = String.format("jdbc:opengauss://%s:%s/postgres", node.getPublicIp(), node.getDbPort());
            jdbcDbClusterNodeVO.setUrl(url);
            return jdbcDbClusterNodeVO;
        }).collect(Collectors.toList());
    }

    private List<TargetClusterVO> getJdbcTargetClusters() {
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("openGauss");
        return jdbcTargetCluster.stream().map(jc -> {
            TargetClusterVO clusterVO = new TargetClusterVO();
            clusterVO.setClusterId(jc.getName());
            clusterVO.setClusterName(jc.getName());
            clusterVO.setVersion("3.0.0");
            clusterVO.setDeployType(jc.getDeployType());
            List<TargetClusterNodeVO> nodes = jc.getNodes().stream().map(on -> {
                TargetClusterNodeVO clusterNodeVO = new TargetClusterNodeVO();
                clusterNodeVO.setNodeId(on.getClusterNodeId());
                clusterNodeVO.setPublicIp(on.getIp());
                clusterNodeVO.setPrivateIp(on.getIp());
                clusterNodeVO.setDbPort(Integer.parseInt(on.getPort()));
                clusterNodeVO.setDbName(on.getUrl().substring(on.getUrl().lastIndexOf("/") + 1));
                clusterNodeVO.setDbUser(on.getUsername());
                clusterNodeVO.setDbUserPassword(on.getPassword());
                clusterNodeVO.setHostname(on.getName());
                clusterNodeVO.setHostPort(22);
                clusterNodeVO.setIsSystemAdmin(false);
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

    @Override
    public List<String> getPgsqlClusterDbNames(String url, String username, String password) {
        List<String> databases = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, encryptionUtils.decrypt(password));
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SqlConstants.PGSQL_SELECT_ALL_DATABASES)) {
            while (resultSet.next()) {
                String dbName = resultSet.getString("datname");
                databases.add(dbName);
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve databases", e);
            throw new MigrationTaskException("Failed to retrieve databases" + e.getMessage());
        }
        return databases;
    }


    @Override
    public List<String> getPgsqlDbSchemas(String url, String username, String password, String dbName) {
        List<String> schemas = new ArrayList<>();

        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);
        String dbUrl = String.format("jdbc:postgresql://%s:%s/%s", jdbcInfo.getIp(), jdbcInfo.getPort(), dbName);
        try (Connection connection = DriverManager.getConnection(dbUrl, username, encryptionUtils.decrypt(password));
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SqlConstants.PGSQL_SELECT_ALL_SCHEMAS)) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("schema_name");
                schemas.add(schemaName);
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve schemas", e);
            throw new MigrationTaskException("Failed to retrieve schemas" + e.getMessage());
        }

        if (!schemas.isEmpty()) {
            schemas.remove("information_schema");
            schemas.remove("pg_catalog");
            schemas.remove("pg_toast");
        }

        return schemas;
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
            log.error("stop kafka failed {},{} {} times, try again", sshLogin, portalHome, retryCount);
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
        Pattern pattern = Pattern.compile("version \"(1[1-9]|[2-9][0-9]+)\\.");
        if (!pattern.matcher(version).find()) {
            String errMsg = "The java version is not match 11+, "
                + "please check environment JAVA_HOME,it must configuration in user ~/.bashrc";
            log.warn("{} {}", sshLogin, errMsg);
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

    /**
     * get tables by sourceDb
     *
     * @param dbName database name
     * @param url source database connection
     * @param username username of db connection
     * @param password password of db connection
     * @return page result
     */
    @Override
    public IPage<Object> pageByDB(Page page, String dbName, String url, String username, String password) {
        long offset = (page.getCurrent() - 1) * page.getSize();
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = '%s' "
            + "and Table_type = 'BASE TABLE' LIMIT %d OFFSET %d";
        String sqlFormat = String.format(sql, dbName, page.getSize(), offset);
        List<Object> tables = new ArrayList<>();
        List<Map<String, Object>> rs = querySource(url, username, password, sqlFormat);
        for (Map<String, Object> map : rs) {
            tables.addAll(map.values());
        }
        String countSql = String.format("SELECT count(1) as total FROM information_schema.tables "
            + "WHERE table_schema = '%s' and Table_type = 'BASE TABLE'", dbName);
        List<Map<String, Object>> rsCount = querySource(url, username, password, countSql);
        if (rsCount.size() > 0) {
            long count = Long.parseLong(rsCount.get(0).get("total").toString());
            page.setTotal(count);
        }
        page.setRecords(tables);
        return page;
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
