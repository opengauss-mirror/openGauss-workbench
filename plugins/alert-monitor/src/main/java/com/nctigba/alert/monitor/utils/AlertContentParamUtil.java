/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.utils;

import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AlertContentParamUtil
 *
 * @since 2023/8/4 17:01
 */
@Component
public class AlertContentParamUtil {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService clusterService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService clusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private HostFacade hostFacade;

    /**
     * setAndGetAlertContentParamDto
     *
     * @param clusterNodeId String
     * @param alertTime LocalDateTime
     * @param level String
     * @param ruleContent String
     * @return AlertContentParamDto
     */
    public AlertContentParamDto setAndGetAlertContentParamDto(
        String clusterNodeId, LocalDateTime alertTime, String level, String ruleContent) {
        OpsClusterNodeEntity opsClusterNodeEntity = clusterNodeService.getById(clusterNodeId);
        if (opsClusterNodeEntity == null) {
            throw new ServiceException("cluster node is not found");
        }
        OpsHostEntity opsHost = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (opsHost == null) {
            throw new ServiceException("host is not found");
        }
        OpsClusterEntity opsClusterEntity = clusterService.getById(opsClusterNodeEntity.getClusterId());
        if (opsClusterEntity == null) {
            throw new ServiceException("cluster is not found");
        }
        AlertContentParamDto contentParamDto = new AlertContentParamDto();
        String nodeName =
            opsClusterEntity.getClusterId() + "/" + opsHost.getPublicIp() + ":" + opsClusterEntity.getPort()
                + "(" + opsClusterNodeEntity.getClusterRole() + ")";
        contentParamDto.setHostname(opsHost.getHostname()).setNodeName(nodeName).setPort(
            opsClusterEntity.getPort() != null ? opsClusterEntity.getPort().toString() : "").setHostIp(
            opsHost.getPublicIp()).setAlertTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(alertTime))
            .setLevel(level);
        if (StrUtil.isNotBlank(ruleContent)) {
            contentParamDto.setContent(new TextParser().parse(ruleContent, contentParamDto));
        }
        return contentParamDto;
    }
}
