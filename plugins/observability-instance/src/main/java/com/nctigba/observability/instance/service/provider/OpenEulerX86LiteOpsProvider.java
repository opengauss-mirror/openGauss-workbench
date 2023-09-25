/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service.provider;

import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrScopeEnum;
import com.nctigba.observability.instance.service.provider.ClusterOpsProviderManager.OpenGaussSupportOSEnum;
import com.nctigba.observability.instance.util.JschUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/12 09:26
 **/
@Slf4j
@Service
public class OpenEulerX86LiteOpsProvider extends AbstractOpsProvider {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschUtil jschUtil;

    @Override
    public OpenGaussVersionEnum version() {
        return OpenGaussVersionEnum.LITE;
    }

    @Override
    public OpenGaussSupportOSEnum os() {
        return OpenGaussSupportOSEnum.OPENEULER_X86_64;
    }

    @Override
    public void enableWdrSnapshot(
        Session session, OpsClusterEntity clusterEntity,
        List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath) {
        if (StrUtil.isEmpty(dataPath)) {
            dataPath = opsClusterNodeEntities.stream().filter(
                node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(
                () -> new OpsException("Master node configuration not found")).getDataPath();
        }

        String checkCommand = "gs_guc check -D " + dataPath + " -c \"enable_wdr_snapshot\"";
        try {
            JschResult jschResult = jschUtil.executeCommand(checkCommand, session, clusterEntity.getEnvPath());
            if (jschResult.getResult().contains("enable_wdr_snapshot=on")) {
                return;
            }
        } catch (Exception e) {
            log.error("Failed to set the enable_wdr_snapshot parameter", e);
            throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
        }

        String command = "gs_guc reload -D " + dataPath + " -c \"enable_wdr_snapshot=on\"";

        try {
            JschResult jschResult = jschUtil.executeCommand(command, session, clusterEntity.getEnvPath());
            if (0 != jschResult.getExitCode()) {
                log.error("set enable_wdr_snapshot parameter failed, exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
            }
        } catch (Exception e) {
            log.error("Failed to set the enable_wdr_snapshot parameter", e);
            throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
        }
    }
}