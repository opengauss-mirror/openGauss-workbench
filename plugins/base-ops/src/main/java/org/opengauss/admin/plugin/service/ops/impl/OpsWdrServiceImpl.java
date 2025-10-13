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
 * OpsWdrServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsWdrServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsWdrEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.WdrGeneratorBody;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsWdrMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.IOpsWdrService;
import org.opengauss.admin.plugin.utils.DBUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.vo.ops.DwrSnapshotVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author lhf
 * @date 2022/10/13 15:14
 **/
@Slf4j
@Service
public class OpsWdrServiceImpl extends ServiceImpl<OpsWdrMapper, OpsWdrEntity> implements IOpsWdrService {
    @Autowired
    private IOpsClusterService opsClusterService;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private DBUtil DBUtil;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    private ClusterOpsProviderManager clusterOpsProviderManager;

    @Override
    public List<OpsWdrEntity> listWdr(String clusterId, WdrScopeEnum wdrScope, WdrTypeEnum wdrType, String hostId, Date start, Date end) {
        log.info("clusterId:{},wdrScope:{},wdrType:{},hostId:{},start:{},end:{}",clusterId,wdrScope,wdrType,hostId,start,end);
        LambdaQueryWrapper<OpsWdrEntity> queryWrapper = Wrappers.lambdaQuery(OpsWdrEntity.class)
                .eq(OpsWdrEntity::getClusterId, clusterId)
                .eq(Objects.nonNull(wdrScope), OpsWdrEntity::getScope, Objects.nonNull(wdrScope)?wdrScope.name():StrUtil.EMPTY)
                .eq(Objects.nonNull(wdrType), OpsWdrEntity::getReportType, Objects.nonNull(wdrType)?wdrType.name():StrUtil.EMPTY)
                .eq(StrUtil.isNotEmpty(hostId), OpsWdrEntity::getHostId, hostId)
                .ge(Objects.nonNull(start), OpsWdrEntity::getReportAt, start)
                .le(Objects.nonNull(end), OpsWdrEntity::getReportAt, end)
                .orderByDesc(OpsWdrEntity::getCreateTime);

        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generate(WdrGeneratorBody wdrGeneratorBody) {
        String clusterId = wdrGeneratorBody.getClusterId();
        OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);

        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        WdrScopeEnum scope = wdrGeneratorBody.getScope();
        if (WdrScopeEnum.CLUSTER == scope) {
            generateClusterWdr(clusterEntity, opsClusterNodeEntities, wdrGeneratorBody.getType(), wdrGeneratorBody.getStartId(), wdrGeneratorBody.getEndId());
        } else {
            generateNodeWdr(clusterEntity, opsClusterNodeEntities, wdrGeneratorBody.getType(), wdrGeneratorBody.getHostId(), wdrGeneratorBody.getStartId(), wdrGeneratorBody.getEndId());
        }
    }

    @Override
    public Page<DwrSnapshotVO> listSnapshot(Page page, String clusterId, String hostId) {
        OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), encryptionUtils.decrypt(clusterEntity.getDatabasePassword())).orElseThrow(() -> new OpsException("Unable to connect to the database"));
            return listSnapshot(page.getCurrent(), page.getSize(), connection);
        }catch (Exception e) {
            log.error("get connection fail");
        }finally {
            if (Objects.nonNull(connection)){
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }

        return new Page<>();
    }

    @Override
    public void createSnapshot(String clusterId, String hostId) {
        OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information is empty");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node configuration not found"));

        String installUserId = nodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish session with host"));

        try {
            clusterOpsProviderManager.provider(clusterEntity.getVersion()).orElseThrow(() -> new OpsException("The current version does not support")).enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);
            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(clusterEntity.getPort()));
            Map<String, List<String>> response = new HashMap<>();
            List<String> responseList = new ArrayList<>();
            String sql = "select create_wdr_snapshot();\n\n\\q";

            responseList.add(sql);

            response.put("openGauss=#", responseList);
            JschResult jschResult = jschUtil.executeCommandWithSerialResponse(clusterEntity.getEnvPath(), clientLoginOpenGauss, session, response, null);
            if (0 != jschResult.getExitCode()) {
                log.error("Generate wdr snapshot exception, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Generate wdr snapshot exception");
            }

        } catch (Exception e) {
            log.error("Generate wdr snapshot exception", e);
            throw new OpsException("Generate wdr snapshot exception");
        }finally {
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(String id) {
        OpsWdrEntity wdrEntity = getById(id);
        if (Objects.isNull(wdrEntity)) {
            throw new OpsException("The record to delete does not exist");
        }

        String hostId = wdrEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(hostId);
        if (CollUtil.isEmpty(hostUserList)) {
            throw new OpsException("Host user information does not exist");
        }

        OpsHostUserEntity installUser = hostUserList.stream().filter(userEntity -> !"root".equals(userEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("No installation user information found"));

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUser.getUsername(), encryptionUtils.decrypt(installUser.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        try {
            rmFile(session, wdrEntity.getReportPath(), wdrEntity.getReportName());
        }finally {
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }

        removeById(id);
    }

    @Override
    public void downloadWdr(String wdrId, HttpServletResponse response) {
        OpsWdrEntity wdrEntity = getById(wdrId);
        if (Objects.isNull(wdrEntity)){
            throw new OpsException("wdr information not found");
        }

        String hostId = wdrEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)){
            throw new OpsException("host information not found");
        }

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(wdrEntity.getUserId());
        if (Objects.isNull(hostUserEntity)){
            throw new OpsException("No user information was found to generate the report");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElseThrow(() -> new OpsException("user failed to establish connection"));

        try {
            jschUtil.download(session, wdrEntity.getReportPath(), wdrEntity.getReportName(), response);
        }finally {
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }
    }

    private void rmFile(Session session, String reportPath, String reportName) {
        String command = MessageFormat.format(SshCommandConstants.DEL_FILE, reportPath + "/" + reportName);
        try {
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                log.error("delete wdr failed，code:{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
            }
        } catch (Exception e) {
            log.error("delete wdr failed");
        }
    }

    private void generateNodeWdr(OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrTypeEnum type, String hostId, String startId, String endId) {
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node configuration not found"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        String installUserId = nodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish session with host"));
        try {
            clusterOpsProviderManager.provider(clusterEntity.getVersion()).orElseThrow(() -> new OpsException("The current version does not support")).enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);

            String wdrPath = "/home/" + userEntity.getUsername();
            String wdrName = "WDR-" + StrUtil.uuid() + ".html";
            doGenerate(wdrPath, wdrName, startId, endId, WdrScopeEnum.NODE, type, session, clusterEntity.getPort(),clusterEntity.getEnvPath());
            OpsWdrEntity opsWdrEntity = new OpsWdrEntity();
            opsWdrEntity.setScope(WdrScopeEnum.NODE);
            opsWdrEntity.setReportAt(new Date());
            opsWdrEntity.setReportType(type);
            opsWdrEntity.setReportName(wdrName);
            opsWdrEntity.setReportPath(wdrPath);
            opsWdrEntity.setStartSnapshotId(startId);
            opsWdrEntity.setEndSnapshotId(endId);
            opsWdrEntity.setClusterId(clusterEntity.getClusterId());
            opsWdrEntity.setUserId(userEntity.getHostUserId());
            opsWdrEntity.setHostId(hostId);
            opsWdrEntity.setNodeId(nodeEntity.getClusterNodeId());
            save(opsWdrEntity);
        }finally {
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }
    }

    private void generateClusterWdr(OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrTypeEnum type, String startId, String endId) {
        OpsClusterNodeEntity masterNodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Cluster master configuration not found"));
        String hostId = masterNodeEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        String installUserId = masterNodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish session with host"));

        try {
            clusterOpsProviderManager.provider(clusterEntity.getVersion()).orElseThrow(() -> new OpsException("The current version does not support")).enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);

            String wdrPath = "/home/" + userEntity.getUsername();
            String wdrName = "WDR-" + StrUtil.uuid() + ".html";
            doGenerate(wdrPath, wdrName, startId, endId, WdrScopeEnum.CLUSTER, type, session, clusterEntity.getPort(), clusterEntity.getEnvPath());

            OpsWdrEntity opsWdrEntity = new OpsWdrEntity();
            opsWdrEntity.setScope(WdrScopeEnum.CLUSTER);
            opsWdrEntity.setHostId(masterNodeEntity.getHostId());
            opsWdrEntity.setReportAt(new Date());
            opsWdrEntity.setReportType(type);
            opsWdrEntity.setReportName(wdrName);
            opsWdrEntity.setReportPath(wdrPath);
            opsWdrEntity.setStartSnapshotId(startId);
            opsWdrEntity.setEndSnapshotId(endId);
            opsWdrEntity.setClusterId(clusterEntity.getClusterId());
            opsWdrEntity.setUserId(userEntity.getHostUserId());
            opsWdrEntity.setNodeId(masterNodeEntity.getClusterNodeId());
            save(opsWdrEntity);
        }finally {
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }
    }

    private Page<DwrSnapshotVO> listSnapshot(Long pageNo, Long pageSize, Connection connection) {
        Page<DwrSnapshotVO> page = new Page<>();
        List<DwrSnapshotVO> res = new ArrayList<>();
        String sql = "select * from snapshot.snapshot";
        if (Objects.nonNull(pageNo) && Objects.nonNull(pageSize) && pageNo > 0 && pageSize > 0) {
            String countSql = "select count(*) from snapshot.snapshot";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(countSql)){
                resultSet.next();
                int count = resultSet.getInt("count");
                page.setTotal(count);
            }catch (Exception e){
                log.error("Query snapshot record exception", e);
            }

            sql = "select * from snapshot.snapshot LIMIT " + pageSize + "OFFSET " + (pageNo - 1) * pageSize;
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                DwrSnapshotVO dwrSnapshotVO = new DwrSnapshotVO();
                dwrSnapshotVO.setSnapshot_id(resultSet.getString("snapshot_id"));
                dwrSnapshotVO.setStart_ts(resultSet.getTimestamp("start_ts"));
                dwrSnapshotVO.setEnd_ts(resultSet.getTimestamp("end_ts"));
                res.add(dwrSnapshotVO);
            }
        } catch (Exception e) {
            log.error("Query snapshot record exception", e);
        }

        page.setRecords(res);
        return page;
    }

    private void doGenerate(String wdrPath, String wdrName, String startId, String endId, WdrScopeEnum scope, WdrTypeEnum type, Session session, Integer port, String envPath) {
        String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
        try {
            String nodeName = null;
            if (scope == WdrScopeEnum.NODE){
                nodeName = "pgxc_node_str()::cstring";
            }
            Map<String, List<String>> response = new HashMap<>();
            List<String> responseList = new ArrayList<>();
            String startSql = "\\a \\t \\o " + wdrPath + "/" + wdrName + "\n";
            String generateSql = "select generate_wdr_report('" + startId + "', '" + endId + "', '" + type.name().toLowerCase() + "', '" + scope.name().toLowerCase() + "'," + nodeName +"); \n";
            String endSql = "\\o \\a \\t \n \\q";

            responseList.add(startSql);
            responseList.add(generateSql);
            responseList.add(endSql);

            response.put("openGauss=#", responseList);
            JschResult jschResult = jschUtil.executeCommandWithSerialResponse(envPath, clientLoginOpenGauss, session, response, null);
            if (0 != jschResult.getExitCode()) {
                log.error("Generated wdr exception, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("generate wdr exception");
            }

        } catch (Exception e) {
            log.error("generate wdr exception", e);
            throw new OpsException("generate wdr exception");
        }
    }
}
