/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskParam;
import org.opengauss.admin.plugin.enums.MigrationMode;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.exception.MigrationTaskException;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationTaskParamService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.utils.PropertiesUtils;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.plugin.vo.TaskProcessStatus;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MULTI_DB portal installer
 *
 * @since 2025/6/30
 */
@Slf4j
@Component
public class MultiDbPortalMigrationController {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Resource
    private MigrationHostPortalInstallHostService portalInstallHostService;

    @Autowired
    private MigrationTaskParamService migrationTaskParamService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    /**
     * load migration status
     *
     * @param portalInfo portal info
     * @param task task
     */
    public synchronized void startTask(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        String portalHome = portalInfo.getInstallPath() + "portal/";
        String jarPath = portalHome + portalInfo.getJarName();
        String createCommand = generateCreateCommand(portalHome, jarPath, task);
        String createResult = ShellUtil.execCommandGetResult(shellInfo, createCommand).getResult();
        String createSuccessMsg = "Create a migration task successfully";
        if (!createResult.contains(createSuccessMsg)) {
            log.error("Create migration task failed, result: {}", createResult);
            throw new MigrationTaskException("Create migration task failed : " + createResult);
        }

        String workspacePath = String.format("%s/workspace/task_%s/", portalHome, task.getId());
        configTask(shellInfo, workspacePath, task);
        String startCommand = String.format(Locale.ROOT, "cd %s && java -jar %s --migration start %d",
                workspacePath, jarPath, task.getId());
        ShellUtil.execInteractiveCommand(shellInfo, startCommand, getInteractParams(task));
    }

    /**
     * stop incremental migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void stopIncremental(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            executeTaskCurlCommand(portalInfo, task, "stopIncremental");
            return;
        }
        throw new UnsupportedOperationException(
                "Database type '" + sourceDbType + "' does not support stopIncremental operation");
    }

    /**
     * resume incremental migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void resumeIncremental(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            executeTaskCurlCommand(portalInfo, task, "resumeIncremental");
            return;
        }
        throw new UnsupportedOperationException(
                "Database type '" + sourceDbType + "' does not support resumeIncremental operation");
    }

    /**
     * start reverse migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void startReverse(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            executeTaskCurlCommand(portalInfo, task, "startReverse");
            return;
        }
        throw new UnsupportedOperationException(
                "Database type '" + sourceDbType + "' does not support startReverse operation");
    }

    /**
     * resume reverse migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void resumeReverse(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            executeTaskCurlCommand(portalInfo, task, "resumeReverse");
            return;
        }
        throw new UnsupportedOperationException(
                "Database type '" + sourceDbType + "' does not support resumeReverse operation");
    }

    /**
     * stop task
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void stopTask(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        executeTaskCurlCommand(portalInfo, task, "stop");
    }

    /**
     * resume migration task
     *
     * @param migrationTask task
     * @return Map<String, Object>  task status
     */
    public TaskProcessStatus resumeMigrationProcess(MigrationTask migrationTask) {
        DbTypeEnum sourceDbType = migrationTask.getSourceDbType();
        if (!DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            throw new UnsupportedOperationException("Database type '" + sourceDbType
                    + "' does not support resumeIncremental or resumeReverse operation");
        }

        MigrationHostPortalInstall portalInfo = portalInstallHostService.getOneByHostId(migrationTask.getRunHostId());
        if (TaskStatus.INCREMENTAL_PAUSE.getCode().equals(migrationTask.getExecStatus())) {
            resumeIncremental(portalInfo, migrationTask);
        } else if (TaskStatus.REVERSE_PAUSE.getCode().equals(migrationTask.getExecStatus())) {
            resumeReverse(portalInfo, migrationTask);
        } else {
            log.warn("Don't need to resume incremental or reverse migration task");
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
            log.error("Thread sleep interrupted, when wait for incremental or reverse migration task to resume");
        }
        TaskProcessStatus taskProcessStatus = new TaskProcessStatus();
        taskProcessStatus.setSink(true);
        taskProcessStatus.setSource(true);
        return taskProcessStatus;
    }

    private void executeTaskCurlCommand(MigrationHostPortalInstall portalInfo, MigrationTask task, String apiPath) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        String portFilePath = MultiDbPortalDirHelper.getTaskPortFilePath(portalInfo, task.getId()).trim();
        String port = FileUtils.catRemoteFileContents(portFilePath, shellInfo).trim();
        String command = String.format("curl -X POST http://localhost:%s/task/%s", port, apiPath);
        ShellUtil.execCommand(shellInfo, command);
    }

    private void configTask(ShellInfoVo shellInfo, String workspacePath, MigrationTask task) {
        HashMap<String, String> migrationConfig = new HashMap<>();
        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            setDatabaseParams(migrationConfig, task);
            setMigrationTaskParams(migrationConfig, task);
            setOpenGaussClusterParams(migrationConfig, task);
        } else if (DbTypeEnum.MILVUS.equals(sourceDbType)) {
            setMilvusDatabaseParams(migrationConfig, task);
            setMilvusMigrationTaskParams(migrationConfig, task);
        } else if (DbTypeEnum.ELASTICSEARCH.equals(sourceDbType)) {
            setElasticsearchDatabaseParams(migrationConfig, task);
            setElasticsearchMigrationTaskParams(migrationConfig, task);
        } else {
            throw new UnsupportedOperationException("DbTypeEnum '" + sourceDbType + "' is not supported to migration");
        }

        String configFilePath = String.format("%s/config/migration.properties", workspacePath);
        PropertiesUtils.updateRemoteProperties(configFilePath, migrationConfig, shellInfo);
    }

    private String generateCreateCommand(String portalHome, String jarPath, MigrationTask task) {
        return String.format(Locale.ROOT, "cd %s && java -jar %s --task create %d %s",
                portalHome, jarPath, task.getId(), task.getSourceDbType().name());
    }

    private void setDatabaseParams(Map<String, String> migrationConfig, MigrationTask task) {
        migrationConfig.put("pgsql.database.ip", task.getSourceDbHost());
        migrationConfig.put("pgsql.database.port", task.getSourceDbPort());
        migrationConfig.put("pgsql.database.name", task.getSourceDb());
        migrationConfig.put("pgsql.database.username", task.getSourceDbUser());
        migrationConfig.put("pgsql.database.schemas", task.getSourceSchemas());
        migrationConfig.put("opengauss.database.ip", task.getTargetDbHost());
        migrationConfig.put("opengauss.database.port", task.getTargetDbPort());
        migrationConfig.put("opengauss.database.name", task.getTargetDb());
        migrationConfig.put("opengauss.database.username", task.getTargetDbUser());
        migrationConfig.put("use.interactive.password", "true");

        migrationConfig.put("is.adjust.kernel.param", task.getIsAdjustKernelParam() + "");
        if (MigrationMode.hasIncrementalAndReverse(task.getMigrationModelId())) {
            migrationConfig.put("migration.mode", "plan3");
        } else {
            migrationConfig.put("migration.mode", "plan1");
        }
    }

    private void setMilvusDatabaseParams(Map<String, String> migrationConfig, MigrationTask task) {
        migrationConfig.put("milvus.ip", task.getSourceDbHost());
        migrationConfig.put("milvus.port", task.getSourceDbPort());
        migrationConfig.put("milvus.collections", task.getSourceTables());
        migrationConfig.put("opengauss.database.ip", task.getTargetDbHost());
        migrationConfig.put("opengauss.database.port", task.getTargetDbPort());
        migrationConfig.put("opengauss.database.name", task.getTargetDb());
        migrationConfig.put("opengauss.database.username", task.getTargetDbUser());
        migrationConfig.put("use.interactive.password", "true");
        migrationConfig.put("is.adjust.kernel.param", task.getIsAdjustKernelParam().toString());
    }

    private void setElasticsearchDatabaseParams(Map<String, String> migrationConfig, MigrationTask task) {
        String elasticsearchHost = String.format("http://%s:%s", task.getSourceDbHost(), task.getSourceDbPort());
        migrationConfig.put("elasticsearch.host", elasticsearchHost);
        migrationConfig.put("elasticsearch.indexes", task.getSourceTables());
        migrationConfig.put("opengauss.database.ip", task.getTargetDbHost());
        migrationConfig.put("opengauss.database.port", task.getTargetDbPort());
        migrationConfig.put("opengauss.database.name", task.getTargetDb());
        migrationConfig.put("opengauss.database.username", task.getTargetDbUser());
        migrationConfig.put("use.interactive.password", "true");
        migrationConfig.put("is.adjust.kernel.param", task.getIsAdjustKernelParam().toString());
    }

    private LinkedHashMap<String, String> getInteractParams(MigrationTask task) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        if (DbTypeEnum.POSTGRESQL.equals(task.getSourceDbType())) {
            params.put("Please input PostgreSQL database password: ", encryptionUtils.decrypt(task.getSourceDbPass()));
        }
        params.put("Please input openGauss database password: ", encryptionUtils.decrypt(task.getTargetDbPass()));
        return params;
    }

    private void setMigrationTaskParams(Map<String, String> migrationConfig, MigrationTask task) {
        if (task.getIsMigrationObject() != null && task.getIsMigrationObject()) {
            migrationConfig.put("is.migration.object", "true");
        } else {
            migrationConfig.put("is.migration.object", "false");
        }

        List<MigrationTaskParam> migrationTaskParams = migrationTaskParamService.selectByTaskId(task.getId());
        if (!migrationTaskParams.isEmpty()) {
            for (MigrationTaskParam taskParam : migrationTaskParams) {
                if ("schema.mappings".equals(taskParam.getParamKey())) {
                    migrationConfig.put("schema.mappings", taskParam.getParamValue());
                }
            }
        }
    }

    private void setMilvusMigrationTaskParams(Map<String, String> migrationConfig, MigrationTask task) {
        List<MigrationTaskParam> migrationTaskParams = migrationTaskParamService.selectByTaskId(task.getId());
        if (!migrationTaskParams.isEmpty()) {
            for (MigrationTaskParam taskParam : migrationTaskParams) {
                if ("migration.concurrent.threads".equals(taskParam.getParamKey())) {
                    migrationConfig.put("migration.concurrent.threads", taskParam.getParamValue());
                    continue;
                }
                if ("table.mappings".equals(taskParam.getParamKey())) {
                    migrationConfig.put("table.mappings", taskParam.getParamValue());
                }
            }
        }
    }

    private void setElasticsearchMigrationTaskParams(Map<String, String> migrationConfig, MigrationTask task) {
        setMilvusMigrationTaskParams(migrationConfig, task);
    }

    private void setOpenGaussClusterParams(Map<String, String> migrationConfig, MigrationTask task) {
        if (!opsFacade.isNodeInOpsCluster(task.getTargetNodeId())) {
            return;
        }

        OpsClusterVO opsClusterVO = opsFacade.getOpsClusterVOByNodeId(task.getTargetNodeId());
        if (opsClusterVO != null && opsClusterVO.getClusterNodes().size() > 1) {
            List<OpsClusterNodeVO> standbyNodes = opsClusterVO.getOtherNodes(task.getTargetNodeId());
            List<String> hostnames = standbyNodes.stream()
                    .map(OpsClusterNodeVO::getPublicIp)
                    .collect(Collectors.toList());

            List<String> ports = standbyNodes.stream()
                    .map(node -> node.getDbPort().toString())
                    .collect(Collectors.toList());
            migrationConfig.put("opengauss.database.standby.hosts", String.join(",", hostnames));
            migrationConfig.put("opengauss.database.standby.ports", String.join(",", ports));
        }
    }

    private ShellInfoVo createShellInfo(MigrationHostPortalInstall portalInfo) {
        return new ShellInfoVo(portalInfo.getHost(), portalInfo.getPort(), portalInfo.getRunUser(),
                encryptionUtils.decrypt(portalInfo.getRunPassword()));
    }
}
