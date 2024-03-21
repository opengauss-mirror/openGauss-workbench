/*
 *  Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  OpsWdrService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/OpsWdrService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.model.entity.OpsWdrDO;
import com.nctigba.observability.instance.model.entity.OpsWdrDO.WdrScopeEnum;
import com.nctigba.observability.instance.model.entity.OpsWdrDO.WdrTypeEnum;
import com.nctigba.observability.instance.model.entity.SnapshotDO;
import com.nctigba.observability.instance.mapper.OpsWdrMapper;
import com.nctigba.observability.instance.mapper.SnapshotMapper;
import com.nctigba.observability.instance.model.dto.WdrGeneratorDTO;
import com.nctigba.observability.instance.service.provider.ClusterOpsProviderManager;
import com.nctigba.observability.instance.util.JschUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/10/13 15:14
 **/
@Slf4j
@Service
public class OpsWdrService extends ServiceImpl<OpsWdrMapper, OpsWdrDO> {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService opsClusterService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private JschUtils jschUtil;
    @Autowired
    private ClusterOpsProviderManager clusterOpsProviderManager;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    protected EncryptionUtils encryptionUtils;
    @Autowired
    private SnapshotMapper snapshotMapper;

    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    public Page<OpsWdrDO> listWdr(
        Page page, String clusterId, WdrScopeEnum wdrScope, WdrTypeEnum wdrType,
        String hostId, Date start, Date end) {
        var wrapper = Wrappers.lambdaQuery(OpsWdrDO.class).eq(OpsWdrDO::getClusterId, clusterId)
            .eq(Objects.nonNull(wdrScope), OpsWdrDO::getScope,
                Objects.nonNull(wdrScope) ? wdrScope.name() : StrUtil.EMPTY)
            .eq(Objects.nonNull(wdrType), OpsWdrDO::getReportType,
                Objects.nonNull(wdrType) ? wdrType.name() : StrUtil.EMPTY)
            .eq(StrUtil.isNotEmpty(hostId), OpsWdrDO::getHostId, hostId)
            .ge(Objects.nonNull(start), OpsWdrDO::getReportAt, start)
            .le(Objects.nonNull(end), OpsWdrDO::getReportAt, end);
        page.setTotal(getBaseMapper().selectCount(wrapper));
        page.setRecords(getBaseMapper().selectList(wrapper.orderByDesc(OpsWdrDO::getCreateTime)
            .last(" limit " + (page.getCurrent() - 1) * page.getSize() + "," + page.getSize())));
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public void generate(WdrGeneratorDTO wdrGeneratorBody) {
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
            generateClusterWdr(clusterEntity, opsClusterNodeEntities, wdrGeneratorBody.getType(),
                wdrGeneratorBody.getStartId(), wdrGeneratorBody.getEndId());
        } else {
            generateNodeWdr(clusterEntity, opsClusterNodeEntities, wdrGeneratorBody.getType(),
                wdrGeneratorBody.getHostId(), wdrGeneratorBody.getStartId(), wdrGeneratorBody.getEndId());
        }
    }

    /**
     * findSnapshot
     *
     * @param id    id
     * @param start start
     * @param end   end
     * @return Map
     */
    public Map<String, Object> findSnapshot(String id, Date start, Date end) {
        Map<String, Object> map = new HashMap<>();
        Long startSnapshot = snapshotMapper.getIdByTimeDesc(id, SnapshotDO::getStartTs, DateUtil.offsetHour(start, -1),
            start);
        map.put("start", startSnapshot == 0 ? null : startSnapshot);
        Long endSnapshot = snapshotMapper.getIdByTimeAsc(id, SnapshotDO::getEndTs, end, DateUtil.offsetHour(end, 1));
        map.put("end", endSnapshot == 0 ? null : endSnapshot);
        if (startSnapshot == null || endSnapshot == null) {
            return map;
        }
        var listWdr = getBaseMapper().selectList(Wrappers.<OpsWdrDO>lambdaQuery()
            .eq(OpsWdrDO::getStartSnapshotId, startSnapshot).eq(OpsWdrDO::getEndSnapshotId, endSnapshot));
        map.put("wdrId", listWdr.stream().map(OpsWdrDO::getWdrId).collect(Collectors.toList()));
        return map;
    }

    @SuppressWarnings({
        "rawtypes",
        "unchecked"
    })
    @Ds(index = 1)
    public Page listSnapshot(Page page, String nodeId) {
        page.setTotal(snapshotMapper.selectCount(null));
        String last = "";
        String orderBy = ServletUtils.getParameter("orderby");
        if (StringUtils.isNotBlank(orderBy)) {
            last += " order by " + orderBy;
        }
        last += " limit " + (page.getCurrent() - 1) * page.getSize() + "," + page.getSize();
        page.setRecords(snapshotMapper.selectList(Wrappers.<SnapshotDO>lambdaQuery().last(last)));
        return page;
    }

    public void createSnapshot(String clusterId, String hostId) {
        OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException(CommonConstants.HOST_NOT_EXIST);
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information is empty");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getHostId().equals(hostId)).findFirst()
            .orElseThrow(() -> new OpsException("Cluster node configuration not found"));

        String installUserId = nodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException(CommonConstants.INSTALLATION_USER_NOT_EXIST);
        }

        var session = jschUtil
            .getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                decrypt(userEntity.getPassword()))
            .orElseThrow(() -> new OpsException(CommonConstants.FAILED_TO_ESTABLISH_HOST));

        try {
            clusterOpsProviderManager.provider(clusterEntity.getVersion(), null)
                .orElseThrow(() -> new OpsException(CommonConstants.CURRENT_VERSION_NOT_SUPPORT))
                .enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);
            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN,
                String.valueOf(clusterEntity.getPort()));
            Map<String, List<String>> response = new HashMap<>();
            List<String> responseList = new ArrayList<>();
            String sql = "select create_wdr_snapshot();\n\n\\q";

            responseList.add(sql);

            response.put("openGauss=#", responseList);
            JschResult jschResult = jschUtil.executeCommandWithSerialResponse(clusterEntity.getEnvPath(),
                clientLoginOpenGauss, session, response, null);
            if (0 != jschResult.getExitCode()) {
                log.error("Generate wdr snapshot exception, exit code: {}, log: {}", jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException(CommonConstants.GENERATE_WDR_SNAPSHOT_EXCEPTION);
            }

        } catch (Exception e) {
            log.error(CommonConstants.GENERATE_WDR_SNAPSHOT_EXCEPTION, e);
            throw new OpsException(CommonConstants.GENERATE_WDR_SNAPSHOT_EXCEPTION);
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private String decrypt(String password) {
        return encryptionUtils.decrypt(password);
    }

    @Transactional(rollbackFor = Exception.class)
    public void del(String id) {
        OpsWdrDO wdrEntity = getById(id);
        if (Objects.isNull(wdrEntity)) {
            throw new OpsException("The record to delete does not exist");
        }

        String hostId = wdrEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException(CommonConstants.HOST_NOT_EXIST);
        }

        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(hostId);
        if (CollUtil.isEmpty(hostUserList)) {
            throw new OpsException("Host user information does not exist");
        }

        OpsHostUserEntity installUser = hostUserList.stream()
            .filter(userEntity -> !"root".equals(userEntity.getUsername())).findFirst()
            .orElseThrow(() -> new OpsException("No installation user information found"));

        Session session = jschUtil
            .getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUser.getUsername(),
                decrypt(installUser.getPassword()))
            .orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        try {
            rmFile(session, wdrEntity.getReportPath(), wdrEntity.getReportName());
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }

        removeById(id);
    }

    public void downloadWdr(String wdrId, HttpServletResponse response) {
        OpsWdrDO wdrEntity = getById(wdrId);
        if (Objects.isNull(wdrEntity)) {
            throw new OpsException("wdr information not found");
        }

        String hostId = wdrEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(wdrEntity.getUserId());
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("No user information was found to generate the report");
        }

        Session session = jschUtil
            .getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(),
                decrypt(hostUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("user failed to establish connection"));

        try {
            jschUtil.download(session, wdrEntity.getReportPath(), wdrEntity.getReportName(), response);
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
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

    private void generateNodeWdr(
        OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities,
        WdrTypeEnum type, String hostId, String startId, String endId) {
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getHostId().equals(hostId)).findFirst()
            .orElseThrow(() -> new OpsException("Cluster node configuration not found"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException(CommonConstants.HOST_NOT_EXIST);
        }

        String installUserId = nodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException(CommonConstants.INSTALLATION_USER_NOT_EXIST);
        }

        Session session = jschUtil
            .getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                decrypt(userEntity.getPassword()))
            .orElseThrow(() -> new OpsException(CommonConstants.FAILED_TO_ESTABLISH_HOST));
        try {
            clusterOpsProviderManager.provider(clusterEntity.getVersion(), null)
                .orElseThrow(() -> new OpsException(CommonConstants.CURRENT_VERSION_NOT_SUPPORT))
                .enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);
            String wdrPath = "/home/" + userEntity.getUsername() + "/wdr";
            jschUtil.executeCommand("mkdir -p " + wdrPath, clusterEntity.getEnvPath(), session, null);
            String wdrName = "WDR-" + StrUtil.uuid() + ".html";
            doGenerate(wdrPath, wdrName, startId, endId, WdrScopeEnum.NODE, type, session, clusterEntity.getPort(),
                clusterEntity.getEnvPath());
            OpsWdrDO opsWdrDO = new OpsWdrDO();
            opsWdrDO.setScope(WdrScopeEnum.NODE);
            opsWdrDO.setReportAt(new Date());
            opsWdrDO.setReportType(type);
            opsWdrDO.setReportName(wdrName);
            opsWdrDO.setReportPath(wdrPath);
            opsWdrDO.setStartSnapshotId(startId);
            opsWdrDO.setEndSnapshotId(endId);
            opsWdrDO.setClusterId(clusterEntity.getClusterId());
            opsWdrDO.setUserId(userEntity.getHostUserId());
            opsWdrDO.setHostId(hostId);
            opsWdrDO.setNodeId(nodeEntity.getClusterNodeId());
            save(opsWdrDO);
        } catch (IOException | InterruptedException e) {
            throw new OpsException(e.getMessage());
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void generateClusterWdr(
        OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities,
        WdrTypeEnum type, String startId, String endId) {
        OpsClusterNodeEntity masterNodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst()
            .orElseThrow(() -> new OpsException("Cluster master configuration not found"));
        String hostId = masterNodeEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException(CommonConstants.HOST_NOT_EXIST);
        }

        String installUserId = masterNodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException(CommonConstants.INSTALLATION_USER_NOT_EXIST);
        }

        Session session = jschUtil
            .getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                decrypt(userEntity.getPassword()))
            .orElseThrow(() -> new OpsException(CommonConstants.FAILED_TO_ESTABLISH_HOST));

        try {
            clusterOpsProviderManager.provider(clusterEntity.getVersion(), null)
                .orElseThrow(() -> new OpsException(CommonConstants.CURRENT_VERSION_NOT_SUPPORT))
                .enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);
            String wdrPath = "/home/" + userEntity.getUsername() + "/wdr";
            jschUtil.executeCommand("mkdir -p " + wdrPath, clusterEntity.getEnvPath(), session, null);
            String wdrName = "WDR-" + StrUtil.uuid() + ".html";
            doGenerate(wdrPath, wdrName, startId, endId, WdrScopeEnum.CLUSTER, type, session, clusterEntity.getPort(),
                clusterEntity.getEnvPath());

            OpsWdrDO opsWdrDO = new OpsWdrDO();
            opsWdrDO.setScope(WdrScopeEnum.CLUSTER);
            opsWdrDO.setHostId(masterNodeEntity.getHostId());
            opsWdrDO.setReportAt(new Date());
            opsWdrDO.setReportType(type);
            opsWdrDO.setReportName(wdrName);
            opsWdrDO.setReportPath(wdrPath);
            opsWdrDO.setStartSnapshotId(startId);
            opsWdrDO.setEndSnapshotId(endId);
            opsWdrDO.setClusterId(clusterEntity.getClusterId());
            opsWdrDO.setUserId(userEntity.getHostUserId());
            opsWdrDO.setNodeId(masterNodeEntity.getClusterNodeId());
            save(opsWdrDO);
        } catch (IOException | InterruptedException e) {
            throw new OpsException(e.getMessage());
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void doGenerate(
        String wdrPath, String wdrName, String startId, String endId, WdrScopeEnum scope,
        WdrTypeEnum type, Session session, Integer port, String envPath) {
        String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
        try {
            String nodeName = null;
            if (scope == WdrScopeEnum.NODE) {
                nodeName = "pgxc_node_str()::cstring";
            }
            Map<String, List<String>> response = new HashMap<>();
            List<String> responseList = new ArrayList<>();
            String startSql = "\\a \\t \\o " + wdrPath + "/" + wdrName + "\n";
            String generateSql = "select generate_wdr_report('" + startId + "', '" + endId + "', '"
                + type.name().toLowerCase() + "', '" + scope.name().toLowerCase() + "'," + nodeName + "); \n";
            String endSql = "\\o \\a \\t \n \\q";

            responseList.add(startSql);
            responseList.add(generateSql);
            responseList.add(endSql);

            response.put("openGauss=#", responseList);
            JschResult jschResult = jschUtil.executeCommandWithSerialResponse(envPath, clientLoginOpenGauss, session,
                response, null);
            if (0 != jschResult.getExitCode()) {
                log.error("Generated wdr exception, exit code: {}, log: {}", jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException(CommonConstants.GENERATE_WDR__EXCEPTION);
            }
        } catch (Exception e) {
            log.error(CommonConstants.GENERATE_WDR__EXCEPTION, e);
            throw new OpsException(CommonConstants.GENERATE_WDR__EXCEPTION);
        }
    }
}
