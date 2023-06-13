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
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.constants.TaskConstant;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.dto.CustomDbResource;
import org.opengauss.admin.plugin.dto.MigrationHostDto;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.enums.PortalInstallStatus;
import org.opengauss.admin.plugin.enums.PortalInstallType;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationTaskHostRefMapper;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationTaskHostRefService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.TargetClusterNodeVO;
import org.opengauss.admin.plugin.vo.TargetClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.JdbcDbClusterFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskHostRefServiceImpl extends ServiceImpl<MigrationTaskHostRefMapper, MigrationTaskHostRef> implements MigrationTaskHostRefService {

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JdbcDbClusterFacade jdbcDbClusterFacade;

    @Autowired
    private MigrationTaskHostRefMapper migrationTaskHostRefMapper;
    @Autowired
    private MigrationTaskService migrationTaskService;
    @Autowired
    private MigrationHostPortalInstallHostService migrationHostPortalInstallHostService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskHostRef> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskHostRef::getMainTaskId, mainTaskId);
        remove(query);
    }

    @Override
    public List<MigrationTaskHostRef> listByMainTaskId(Integer mainTaskId) {
        List<MigrationTaskHostRef> hosts = migrationTaskHostRefMapper.selectByMainTaskId(mainTaskId);
        hosts.stream().forEach(h -> {
            OpsHostEntity opsHost = hostFacade.getById(h.getRunHostId());
            h.setHostName(opsHost.getHostname());
            h.setHost(opsHost.getPublicIp());
            h.setPort(opsHost.getPort());
            List<OpsHostUserEntity> opsHostUserEntities = hostUserFacade.listHostUserByHostId(h.getRunHostId());
            if (opsHostUserEntities.size() > 0) {
                OpsHostUserEntity notRoot = opsHostUserEntities.stream().filter(x -> !x.getUsername().equals("root")).findFirst().orElse(null);
                if (notRoot != null) {
                    h.setUser(notRoot.getUsername());
                    h.setPassword(encryptionUtils.decrypt(notRoot.getPassword()));
                }
            }
        });
        return hosts;
    }

    /**
     * get migration exec host list
     *
     * @return host info list
     */
    @Override
    public List<MigrationHostDto> getHosts() {
        ThreadPoolExecutor taskExecutor = ThreadUtil.newExecutor(15, 30);
        List<OpsHostEntity> opsHostEntities = hostFacade.listAll();
        List<Future<MigrationHostDto>> futureList = new ArrayList<>();
        for (OpsHostEntity host : opsHostEntities) {
            Future<MigrationHostDto> future = taskExecutor.submit(() -> {
                MigrationHostDto eachOne = new MigrationHostDto();
                eachOne.setHostInfo(host);
                List<MigrationTask> tasks = migrationTaskService.listRunningTaskByHostId(host.getHostId());
                eachOne.setTasks(tasks);
                MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(host.getHostId());
                if (installHost == null) {
                    OpsHostEntity opsHost = hostFacade.getById(host.getHostId());
                    String[] opsHosInfo = getOpsHosInfo(host.getHostId(), opsHost.getPublicIp(), opsHost.getPort());
                    if (opsHosInfo.length == 0) {
                        return null;
                    }
                    eachOne.setBaseInfos(ListUtil.toList(opsHosInfo));
                    eachOne.setInstallPortalStatus(PortalInstallStatus.NOT_INSTALL.getCode());
                } else {
                    String[] hostInfo = PortalHandle.getHostBaseInfo(installHost.getHost(), installHost.getPort(),
                            installHost.getRunUser(), installHost.getRunPassword());
                    if (hostInfo.length == 0) {
                        return null;
                    }
                    eachOne.setBaseInfos(ListUtil.toList(hostInfo));
                    eachOne.setInstallInfo(installHost);
                    if (installHost.getInstallStatus().equals(PortalInstallStatus.INSTALLED.getCode())) {
                        boolean isInstallPortal = PortalHandle.checkInstallPortal(installHost.getHost(),
                                installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(),
                                installHost.getInstallPath());
                        eachOne.setInstallPortalStatus(isInstallPortal ? PortalInstallStatus.INSTALLED.getCode()
                                : PortalInstallStatus.NOT_INSTALL.getCode());
                    } else {
                        eachOne.setInstallPortalStatus(installHost.getInstallStatus());
                    }
                }
                return eachOne;
            });
            futureList.add(future);
        }
        List<MigrationHostDto> result = new ArrayList<>();
        for (Future<MigrationHostDto> item: futureList) {
            try {
                MigrationHostDto futureResult = item.get();
                if (futureResult != null) {
                    result.add(futureResult);
                }
            } catch (ExecutionException | InterruptedException e) {
                log.error("get host future result failed: " + e.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<JdbcDbClusterVO> getSourceClusters() {
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("MYSQL");
        return jdbcTargetCluster;
    }

    @Override
    public void saveDbResource(CustomDbResource dbResource) {
        saveSource(dbResource.getClusterName(), dbResource.getDbUrl(), dbResource.getUserName(), dbResource.getPassword());
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
            clusterVO.setVersionNum(StringUtils.isNotBlank(o.getVersionNum())
                    ? o.getVersionNum() : TaskConstant.DEFAULT_OPENGAUSS_VERSION);
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
                clusterNodeVO.setDbUserPassword(on.getDbUserPassword());
                clusterNodeVO.setHostPort(on.getHostPort());
                return clusterNodeVO;
            }).collect(Collectors.toList());
            clusterVO.setClusterNodes(nodes);
            return clusterVO;
        }).collect(Collectors.toList());
        List<JdbcDbClusterVO> jdbcTargetCluster = jdbcDbClusterFacade.listAll("openGauss");
        List<TargetClusterVO> jdbcTargetClusters = jdbcTargetCluster.stream().map(jc -> {
            TargetClusterVO clusterVO = new TargetClusterVO();
            clusterVO.setClusterId(jc.getName());
            clusterVO.setClusterName(jc.getName());
            clusterVO.setVersion("3.0.0");
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
                return clusterNodeVO;
            }).collect(Collectors.toList());
            clusterVO.setClusterNodes(nodes);
            return clusterVO;
        }).collect(Collectors.toList());
        targetClusters.addAll(jdbcTargetClusters);
        return targetClusters;
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
        if (clusterNode.getHostPort() == 22) {
            String sql = "select datname from pg_database;";
            List<Map<String, Object>> resultSet = queryTarget(clusterNode, "", sql);
            resultSet.forEach(ret -> {
                Map<String, Object> itemMap = new HashMap<>();
                String datname = ret.get("datname").toString();
                itemMap.put("dbName", datname);
                Integer count = migrationTaskService.countNotFinishByTargetDb(clusterNode.getNodeId(), datname);
                itemMap.put("isSelect", count == 0);
                dbList.add(itemMap);
            });
        }
        return dbList;
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
            conn = DriverManager.getConnection(url, username, password);
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
     * @param schema      schema
     * @param sql         sql
     * @return result list
     */
    private List<Map<String, Object>> queryTarget(OpsClusterNodeVO clusterNode, String schema, String sql) {
        return queryBySqlOnOpengauss(clusterNode.getPublicIp(), clusterNode.getDbPort().toString(),
                clusterNode.getDbName(), clusterNode.getDbUser(), clusterNode.getDbUserPassword(), schema, sql);
    }

    /**
     * SQL querying based on OpenGauss.
     *
     * @param host     host of db
     * @param port     host of db
     * @param database database of db
     * @param dbUser   user of db
     * @param dbPass   password of db
     * @param schema   schema of db
     * @param sql      sql
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
        List<OpsHostUserEntity> opsHostUserEntities = hostUserFacade.listHostUserByHostId(hostId);
        List<OpsHostUserEntity> notRootUsers = opsHostUserEntities.stream().filter(x -> !x.getUsername().equals("root")).collect(Collectors.toList());
        return notRootUsers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult installPortal(String hostId, MigrationHostPortalInstall install) {
        return installPortalProc(hostId, install, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult retryInstallPortal(String hostId, MigrationHostPortalInstall install) {
        return installPortalProc(hostId, install, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public AjaxResult installPortalProc(String hostId, MigrationHostPortalInstall install, boolean isReInstall) {
        MigrationHostPortalInstall oldInstall = migrationHostPortalInstallHostService.getOneByHostId(install.getRunHostId());
        OpsHostEntity opsHost = hostFacade.getById(hostId);
        OpsHostUserEntity hostUser = hostUserFacade.getById(install.getHostUserId());
        String realInstallPath = getInstallPath(install.getInstallPath(), hostUser.getUsername());
        AjaxResult result = checkPermission(opsHost, hostUser, realInstallPath);
        if (!result.isOk()) {
            return result;
        }

        String password = encryptionUtils.decrypt(hostUser.getPassword());

        install.setInstallPath(realInstallPath);
        install.setRunUser(hostUser.getUsername());
        install.setRunHostId(hostId);
        install.setRunPassword(password);
        install.setHost(opsHost.getPublicIp());
        install.setPort(opsHost.getPort());
        install.setInstallStatus(PortalInstallStatus.INSTALLING.getCode());
        UploadInfo uploadResult = uploadPortal(install.getFile(), install);

        MigrationHostPortalInstall physicalInstallParams = new MigrationHostPortalInstall();
        physicalInstallParams.setRunHostId(hostId);
        physicalInstallParams.setHost(install.getHost());
        physicalInstallParams.setPort(install.getPort());
        physicalInstallParams.setRunUser(hostUser.getUsername());
        physicalInstallParams.setHostUserId(hostUser.getHostUserId());
        physicalInstallParams.setRunPassword(password);
        physicalInstallParams.setInstallPath(realInstallPath);
        physicalInstallParams.setJarName(install.getJarName());
        physicalInstallParams.setPkgName(install.getPkgName());
        physicalInstallParams.setInstallStatus(PortalInstallStatus.INSTALLING.getCode());
        physicalInstallParams.setInstallType(install.getInstallType());
        if (install.getInstallType().equals(PortalInstallType.ONLINE_INSTALL.getCode())) {
            physicalInstallParams.setPkgDownloadUrl(install.getPkgDownloadUrl());
            physicalInstallParams.setPkgUploadPath(null);
        } else {
            physicalInstallParams.setPkgDownloadUrl("");
            physicalInstallParams.setPkgUploadPath(uploadResult);
        }
        syncInstallPortalHandler(physicalInstallParams);
        migrationHostPortalInstallHostService.saveRecord(physicalInstallParams);

        // if reinstall path changed, clear old package
        if (isReInstall && !realInstallPath.equals(oldInstall.getInstallPath())) {
            deletePortal(hostId, false);
        }
        return AjaxResult.success();
    }

    private void syncInstallPortalHandler(MigrationHostPortalInstall installParams) {
        threadPoolTaskExecutor.submit(() -> {
            boolean isInstallSuccess;
            try {
                isInstallSuccess = PortalHandle.installPortal(installParams);
            } catch (PortalInstallException e) {
                String cmd = String.format("mkdir -p %s && echo '%s' >> %s",
                        installParams.getInstallPath(),
                        e.getMessage(),
                        installParams.getDatakitLogPath());
                JschResult result = ShellUtil.execCommandGetResult(installParams.getHost(),
                        installParams.getPort(),
                        installParams.getRunUser(),
                        installParams.getRunPassword(), cmd);
                if (!result.isOk()) {
                    log.error("write install error message failed: " + result.getResult());
                }
                isInstallSuccess = false;
            }
            migrationHostPortalInstallHostService.updateStatus(installParams.getRunHostId(), isInstallSuccess ? PortalInstallStatus.INSTALLED.getCode() : PortalInstallStatus.INSTALL_ERROR.getCode());
        });
    }


    @Override
    public String getPortalInstallLog(String hostId) {
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(hostId);

        String datakitLogPath = installHost.getDatakitLogPath();
        String datakitContent = PortalHandle.getTaskLogs(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), datakitLogPath);
        if (StringUtils.isNotBlank(datakitContent)) {
            return datakitContent;
        }

        String logPath = installHost.getPortalLogPath();
        String content = PortalHandle.getTaskLogs(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), logPath);
        String logContent = " ";
        if (StringUtils.isNotBlank(content)) {
            logContent = content;
        }

        return logContent;
    }

    /**
     * get host base information
     *
     * @param hostId   Id of the OpsHost object
     * @param publicIp field 'publicIp' of the OpsHost object
     * @param port     field 'port' of the OpsHost object
     * @return host info array
     */
    public String[] getOpsHosInfo(String hostId, String publicIp, Integer port) {
        List<OpsHostUserEntity> opsHostUserEntities = hostUserFacade.listHostUserByHostId(hostId);
        if (opsHostUserEntities.size() > 0) {
            OpsHostUserEntity hostUser = opsHostUserEntities.get(0);
            if (StringUtils.isNotBlank(hostUser.getPassword())) {
                return PortalHandle.getHostBaseInfo(publicIp, port, hostUser.getUsername(),
                        encryptionUtils.decrypt(hostUser.getPassword()));
            }
        }
        return new String[0];
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deletePortal(String hostId, Boolean onlyPkg) {
        MigrationHostPortalInstall install = migrationHostPortalInstallHostService.getOneByHostId(hostId);
        OpsHostEntity opsHost = hostFacade.getById(hostId);
        OpsHostUserEntity hostUser = hostUserFacade.getById(install.getHostUserId());
        String password = encryptionUtils.decrypt(hostUser.getPassword());
        List<MigrationTask> tasks = migrationTaskService.listRunningTaskByHostId(hostId);
        if (CollUtil.isNotEmpty(tasks)) {
            return AjaxResult.error(MigrationErrorCode.PORTAL_DELETE_ERROR.getCode(),
                    MigrationErrorCode.PORTAL_DELETE_ERROR.getMsg());
        }
        String realInstallPath = getInstallPath(install.getInstallPath(), hostUser.getUsername());
        String portalHome = realInstallPath + "portal/";
        // stop kafka
        try {
            stopKafka(opsHost, hostUser, password, portalHome, install.getJarName(), 0);
        } catch (PortalInstallException ex) {
            return AjaxResult.error(MigrationErrorCode.STOP_KAFKA_ERROR.getCode(), MigrationErrorCode.STOP_KAFKA_ERROR.getMsg());
        }
        // if delete file failed, do nothing
        if (onlyPkg != null && !onlyPkg) {
            ShellUtil.execCommandGetResult(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password, "rm -rf  " + realInstallPath + "portal");
            migrationHostPortalInstallHostService.updateStatus(hostId, PortalInstallStatus.NOT_INSTALL.getCode());
        }
        migrationHostPortalInstallHostService.clearPkgUploadPath(hostId);
        ShellUtil.rmFile(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password, realInstallPath + install.getPkgName());
        return AjaxResult.success();
    }

    /**
     * check is install path has Permission
     *
     * @param opsHost     host to install portal
     * @param hostUser    user to install portal
     * @param installPath portal install path
     * @return check result
     */
    private AjaxResult checkPermission(OpsHostEntity opsHost, OpsHostUserEntity hostUser, String installPath) {
        String realInstallPath = getInstallPath(installPath, hostUser.getUsername());
        String password = encryptionUtils.decrypt(hostUser.getPassword());
        boolean isExists = PortalHandle.directoryExists(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password, realInstallPath);
        if (isExists) {
            boolean isExistsAndHasPermission = PortalHandle.checkWritePermission(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password, realInstallPath);
            return isExistsAndHasPermission ? AjaxResult.success() : AjaxResult.error(MigrationErrorCode.PORTAL_INSTALL_PATH_NOT_HAS_WRITE_PERMISSION_ERROR.getMsg());
        } else {
            boolean createResult = PortalHandle.mkdirDirectory(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password, realInstallPath);
            return createResult ? AjaxResult.success() : AjaxResult.error(MigrationErrorCode.PORTAL_CREATE_INSTALL_PATH_FAILED.getMsg());
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
     * @param install       upload params
     * @return upload result
     */
    private UploadInfo uploadPortal(MultipartFile multipartFile, MigrationHostPortalInstall install) {
        UploadInfo result = new UploadInfo();
        if (multipartFile == null || StrUtil.isEmpty(multipartFile.getOriginalFilename())) {
            log.warn("Upload file is empty, please check");
            return result;
        }
        try (InputStream in = multipartFile.getInputStream()) {
            ShellUtil.uploadFile(install.getHost(), install.getPort(), install.getRunUser(), install.getRunPassword(), install.getInstallPath() + multipartFile.getOriginalFilename(), in);
            result.setName(multipartFile.getOriginalFilename());
            result.setRealPath(install.getInstallPath());
        } catch (IOException e) {
            log.error("Upload file error: " + e.getMessage());
        }
        return result;
    }

    /**
     * stop kafka before uninstall portal
     * @param opsHost portal install host
     * @param hostUser portal install user
     * @param password portal install password
     * @param portalHome portal install path
     * @param jarName portal jar name
     * @param retryCount failed retry count
     */
    private void stopKafka(OpsHostEntity opsHost, OpsHostUserEntity hostUser, String password, String portalHome, String jarName, int retryCount) throws PortalInstallException {
        JschResult result = ShellUtil.execCommandGetResult(opsHost.getPublicIp(), opsHost.getPort(), hostUser.getUsername(), password, "java -Dpath=" + portalHome + " -Dorder=stop_kafka -Dskip=true -jar " + portalHome + jarName);
        if (!result.isOk()) {
            if (retryCount > 3) {
                throw new PortalInstallException("Stop kafka failed after 3 retries: " + result.getResult());
            }
            log.error("stop kafka failed {} times, try again", retryCount);
            stopKafka(opsHost, hostUser, password, portalHome, jarName, ++retryCount);
        }
    }
}
