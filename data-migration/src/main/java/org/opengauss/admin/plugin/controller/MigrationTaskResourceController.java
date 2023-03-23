package org.opengauss.admin.plugin.controller;

import cn.hutool.core.date.DateUtil;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.dto.CustomDbResource;
import org.opengauss.admin.plugin.service.MigrationTaskHostRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@RestController
@RequestMapping("/resource")
public class MigrationTaskResourceController extends BaseController {

    @Autowired
    private MigrationTaskHostRefService migrationTaskHostRefService;


    @GetMapping("/getClusters")
    public AjaxResult getTargetClusters() {
        List<JdbcDbClusterVO> sourceClusters = migrationTaskHostRefService.getSourceClusters();
        List<OpsClusterVO> targetClusters = migrationTaskHostRefService.getTargetClusters();
        Map<String, Object> result = new HashMap<>();
        result.put("sourceClusters", sourceClusters);
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

    @GetMapping("/installPortal/{hostId}")
    public AjaxResult installPortal(@PathVariable String hostId) {
        migrationTaskHostRefService.installPortal(hostId, false);
        return AjaxResult.success();
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
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength(bytes.length);
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
    }

}
