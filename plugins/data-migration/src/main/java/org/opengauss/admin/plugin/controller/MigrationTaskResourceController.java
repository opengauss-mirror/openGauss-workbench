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
 * MigrationTaskResourceController.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/controller/MigrationTaskResourceController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.controller;

import com.alibaba.fastjson.JSON;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;
import org.opengauss.admin.plugin.dto.PortalInstallHostDto;
import org.opengauss.admin.plugin.enums.OpengaussSourceTable;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.service.MigrationTaskHostRefService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.OpengaussClusterVo;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * handler for migration task resources.
 *
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@RestController
@RequestMapping("/resource")
public class MigrationTaskResourceController extends BaseController {

    @Autowired
    private MigrationTaskHostRefService migrationTaskHostRefService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    /**
     * retrieve the source cluster
     *
     * @return AjaxResult Response
     */
    @GetMapping("/sourceClusters")
    public AjaxResult getSourceClusters(@RequestParam(required = false) String dbType) {
        List<JdbcDbClusterVO> sourceClusters = dbType == null
                ? migrationTaskHostRefService.getSourceClusters()
                : migrationTaskHostRefService.getSourceClusters(dbType);
        return AjaxResult.success(Map.of("sourceClusters", sourceClusters));
    }

    /**
     * Get the source db clusters by db type
     *
     * @param dbType db type
     * @return AjaxResult Response
     */
    @GetMapping("/source/clusters")
    public AjaxResult getSourceClusters(DbTypeEnum dbType) {
        return AjaxResult.success(migrationTaskHostRefService.getSourceClusters(dbType));
    }

    /**
     * Get source cluster databases
     *
     * @param dbType db type
     * @param nodeId cluster node id
     * @return AjaxResult
     */
    @GetMapping("/source/databases")
    public AjaxResult getSourceDatabases(DbTypeEnum dbType, String nodeId) {
        return AjaxResult.success(migrationTaskHostRefService.getSourceDatabases(dbType, nodeId));
    }

    /**
     * Get source cluster schemas
     *
     * @param dbType db type, default is POSTGRESQL
     * @param nodeId cluster node id
     * @param dbName database name
     * @return AjaxResult
     */
    @GetMapping("/source/schemas")
    public AjaxResult getSourceSchemas(
            @RequestParam(required = false, defaultValue = "POSTGRESQL") DbTypeEnum dbType, String nodeId, String dbName
    ) {
        List<String> schemas = migrationTaskHostRefService.getSourceSchemas(dbType, nodeId, dbName);
        return AjaxResult.success(schemas);
    }

    /**
     * Get source cluster table page
     *
     * @param dbType db type
     * @param nodeId cluster node id
     * @param dbName database name
     * @param schemaName schema name
     * @return AjaxResult
     */
    @GetMapping("/source/table/page")
    public AjaxResult getSourceTablePage(
            @RequestParam(required = true) DbTypeEnum dbType, @RequestParam(required = true) String nodeId,
            @RequestParam(required = false) String dbName, @RequestParam(required = false) String schemaName
    ) {
        return AjaxResult.success(migrationTaskHostRefService.getSourceTablePage(dbType, nodeId, dbName, schemaName,
                startPage()));
    }

    /**
     * Get the target db clusters by db type
     *
     * @return AjaxResult
     */
    @GetMapping("/target/clusters")
    public AjaxResult getTargetClusters() {
        return AjaxResult.success(migrationTaskHostRefService.getTargetClusters());
    }

    /**
     * Get target cluster detail
     *
     * @param sourceTable source table
     * @param clusterId target cluster id
     * @return AjaxResult
     */
    @GetMapping("/target/detail")
    public AjaxResult getTargetDetail(OpengaussSourceTable sourceTable, String clusterId) {
        return AjaxResult.success(migrationTaskHostRefService.getTargetDetail(sourceTable, clusterId));
    }

    /**
     * Get target cluster databases
     *
     * @param sourceTable source table
     * @param nodeId cluster node id
     * @return AjaxResult
     */
    @GetMapping("/target/databases")
    public AjaxResult getTargetDatabases(OpengaussSourceTable sourceTable, String nodeId) {
        return AjaxResult.success(migrationTaskHostRefService.getTargetDatabases(sourceTable, nodeId));
    }

    /**
     * retrieve the target cluster
     *
     * @return AjaxResult Response
     */
    @GetMapping("/targetClusters")
    public AjaxResult getOpengaussClusters() {
        List<OpengaussClusterVo> targetClusters = migrationTaskHostRefService.getOpengaussClusters();
        Map<String, Object> result = new HashMap<>();
        result.put("targetClusters", targetClusters);
        return AjaxResult.success(result);
    }

    /**
     * get target cluster databases
     *
     * @param clusterNode clusterNode
     * @return AjaxResult
     */
    @PostMapping("/getTargetClusterDbs")
    public AjaxResult getTargetClusterDbs(@RequestBody OpsClusterNodeVO clusterNode) {
        return AjaxResult.success(migrationTaskHostRefService.getOpsClusterDbNames(clusterNode));
    }

    /**
     * get source cluster databases
     *
     * @param url jdbc url
     * @param username username
     * @param password password
     * @return AjaxResult
     */
    @PostMapping("/getSourceClusterDbs")
    public AjaxResult getSourceClusterDbs(String url, String username, String password) {
        return AjaxResult.success(migrationTaskHostRefService.getMysqlClusterDbNames(url, username, password));
    }

    /**
     * get host page list
     *
     * @param portalInstallHostDto portal install host info
     * @return AjaxResult host page list info
     */
    @PostMapping("/getHosts")
    public AjaxResult getHosts(@RequestBody PortalInstallHostDto portalInstallHostDto) {
        return AjaxResult.success(migrationTaskHostRefService.getHosts(startPage(), portalInstallHostDto));
    }

    @GetMapping("/listAllHostUser")
    public AjaxResult listAllHostUser(@RequestParam("hostIds") List<String> hostIds) {
        return AjaxResult.success(hostUserFacade.listHostUserByHostIdList(hostIds));
    }

    @GetMapping("/hostUsers/{hostId}")
    public AjaxResult hostUsers(@PathVariable String hostId) {
        return AjaxResult.success(migrationTaskHostRefService.getHostUsers(hostId));
    }

    @PostMapping("/installPortal/{hostId}")
    public AjaxResult installPortal(@PathVariable String hostId, @ModelAttribute MigrationHostPortalInstall install) {
        addSplashToPath(install);
        return migrationTaskHostRefService.installPortal(hostId, install);
    }

    @PostMapping("/installPortalFromDatakit/{hostId}")
    public AjaxResult installPortalFromDatakit(@PathVariable String hostId, @ModelAttribute MigrationHostPortalInstall install) {
        addSplashToPath(install);
        return migrationTaskHostRefService.installPortalFromDatakit(hostId, install, getUserId());
    }

    @PostMapping("/retryInstallPortal/{hostId}")
    public AjaxResult retryInstallPortal(@PathVariable String hostId, @ModelAttribute MigrationHostPortalInstall install) {
        addSplashToPath(install);
        return migrationTaskHostRefService.retryInstallPortal(hostId, install);
    }

    /**
     * Download taskEnv log file
     */
    @GetMapping("/log/downloadEnv/{hostId}")
    public void downloadEnvLog(@PathVariable String hostId, HttpServletResponse response) throws Exception {
        String logContent = migrationTaskHostRefService.getPortalInstallLog(hostId);
        byte[] bytes = logContent.getBytes(StandardCharsets.UTF_8);
        String logName = "installError.log";
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String filename = "log_" + hostId + "_" + date + "_" + logName;
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, filename);
        OutputStream output = new BufferedOutputStream(response.getOutputStream());
        output.write(bytes);
        output.flush();
        output.close();
    }

    @DeleteMapping("/deletePortal/{hostId}")
    public AjaxResult deletePortal(@PathVariable String hostId, @RequestParam(required = false) Boolean onlyPkg) {
        return migrationTaskHostRefService.deletePortal(hostId, onlyPkg);
    }

    @PostMapping("/uploadPortal")
    public AjaxResult upload(@RequestParam MultipartFile file) {
        try {
            UploadInfo info = migrationTaskHostRefService.upload(file, getUserId());
            return AjaxResult.success(info.toVO());
        } catch (PortalInstallException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    private void addSplashToPath(MigrationHostPortalInstall install) {
        String installPath = install.getInstallPath();
        installPath = formatPath(installPath);
        install.setInstallPath(installPath);
        if (install.getThirdPartySoftwareConfig() != null) {
            MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig = install.getThirdPartySoftwareConfig();
            String thirdPartySoftwareInstallDir = formatPath(thirdPartySoftwareConfig.getInstallDir());
            thirdPartySoftwareConfig.setInstallDir(thirdPartySoftwareInstallDir);
            install.setThirdPartySoftwareConfig(thirdPartySoftwareConfig);
        }
    }

    private static String formatPath(String installPath) {
        String path = installPath;
        if (StrUtil.isNotEmpty(installPath) && !installPath.equals("~")) {
            String lastStr = installPath.substring(installPath.length() - 1);
            if (!lastStr.equals("/")) {
                path = path + "/";
            }
        }
        return path;
    }

    @Override
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UploadInfo.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                UploadInfo info = JSON.parseObject(text, UploadInfo.class);
                setValue(info);
            }
        });
    }

    /**
     * is openGauss connect user admin
     *
     * @param clusterNode cluster node
     * @return AjaxResult
     */
    @PostMapping("/isAdmin")
    public AjaxResult isConnectUserAdmin(@RequestBody OpsClusterNodeVO clusterNode) {
        return AjaxResult.success(migrationTaskHostRefService.isConnectUserAdmin(clusterNode));
    }
}
