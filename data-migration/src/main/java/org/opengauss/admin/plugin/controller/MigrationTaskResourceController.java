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

import cn.hutool.core.date.DateUtil;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.dto.CustomDbResource;
import org.opengauss.admin.plugin.service.MigrationTaskHostRefService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.TargetClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
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


    /**
     * retrieve the source and target cluster
     *
     * @return AjaxResult Response
     */
    @GetMapping("/getClusters")
    public AjaxResult getClusters() {
        List<JdbcDbClusterVO> sourceClusters = migrationTaskHostRefService.getSourceClusters();
        List<TargetClusterVO> targetClusters = migrationTaskHostRefService.getTargetClusters();
        Map<String, Object> result = new HashMap<>();
        result.put("sourceClusters", sourceClusters);
        result.put("targetClusters", targetClusters);
        return AjaxResult.success(result);
    }

    /**
     * retrieve the source cluster
     *
     * @return AjaxResult Response
     */
    @GetMapping("/sourceClusters")
    public AjaxResult getSourceClusters() {
        List<JdbcDbClusterVO> sourceClusters = migrationTaskHostRefService.getSourceClusters();
        Map<String, Object> result = new HashMap<>();
        result.put("sourceClusters", sourceClusters);
        return AjaxResult.success(result);
    }

    /**
     * retrieve the target cluster
     *
     * @return AjaxResult Response
     */
    @GetMapping("/targetClusters")
    public AjaxResult getTargetClusters() {
        List<TargetClusterVO> targetClusters = migrationTaskHostRefService.getTargetClusters();
        Map<String, Object> result = new HashMap<>();
        result.put("targetClusters", targetClusters);
        return AjaxResult.success(result);
    }

    @GetMapping("/getTargetClusterDbs")
    public AjaxResult getTargetClusterDbs(OpsClusterNodeVO clusterNode) {
        return AjaxResult.success(migrationTaskHostRefService.getOpsClusterDbNames(clusterNode));
    }

    @GetMapping("/getSourceClusterDbs")
    public AjaxResult getSourceClusterDbs(String url, String username, String password) {
        return AjaxResult.success(migrationTaskHostRefService.getMysqlClusterDbNames(url, username, password));
    }

    @GetMapping("/getHosts")
    public AjaxResult getHosts() {
        return AjaxResult.success(migrationTaskHostRefService.getHosts());
    }

    @PostMapping("/saveCustomMysql")
    public AjaxResult saveCustomMysql(@RequestBody CustomDbResource dbResource) {
        migrationTaskHostRefService.saveDbResource(dbResource);
        return AjaxResult.success();
    }

    @GetMapping("/hostUsers/{hostId}")
    public AjaxResult hostUsers(@PathVariable String hostId) {
        return AjaxResult.success(migrationTaskHostRefService.getHostUsers(hostId));
    }

    @GetMapping("/installPortal/{hostId}")
    public AjaxResult installPortal(@PathVariable String hostId, String hostUserId, String installPath) {
        if(!installPath.equals("~")) {
            String lastStr = installPath.substring(installPath.length() - 1);
            if (!lastStr.equals("/")) {
                installPath += "/";
            }
        }
        return migrationTaskHostRefService.installPortal(hostId, hostUserId, installPath);
    }

    @GetMapping("/retryInstallPortal/{hostId}")
    public AjaxResult retryInstallPortal(@PathVariable String hostId) {
        return migrationTaskHostRefService.retryInstallPortal(hostId);
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

}
