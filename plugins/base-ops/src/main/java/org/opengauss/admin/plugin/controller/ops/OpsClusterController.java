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
 * OpsClusterController.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/OpsClusterController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.env.HostEnv;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.vo.ops.*;
import org.opengauss.admin.plugin.vo.ops.SessionVO;
import org.opengauss.admin.plugin.vo.ops.SlowSqlVO;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation and maintenance cluster operations
 *
 * @author lhf
 * @date 2022/8/6 17:38
 **/
@RestController
@RequestMapping("/opsCluster")
public class OpsClusterController extends BaseController {

    @Autowired
    private IOpsClusterService opsClusterService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    @GetMapping("/hasName")
    public AjaxResult hasName(@RequestParam("name") String name) {
        boolean has = opsClusterService.hasName(name);
        Map<String, String> hasRes = new HashMap<>();
        hasRes.put("has", has ? "Y" : "N");
        return AjaxResult.success(hasRes);
    }

    @PostMapping("/download")
    public AjaxResult download(@RequestBody DownloadBody downloadBody) {
        opsClusterService.download(downloadBody);
        return AjaxResult.success();
    }

    @PostMapping("/install")
    public AjaxResult install(@RequestBody InstallBody installBody) {
        opsClusterService.install(installBody);
        return AjaxResult.success();
    }

    @GetMapping("/upgradeOsCheck")
    public AjaxResult upgradeOsCheck(@RequestParam String clusterId,@RequestParam(value = "rootPassword",required = false) String rootPassword) {
        UpgradeOsCheckVO upgradeOsCheckVO = opsClusterService.upgradeOsCheck(clusterId,rootPassword);
        return AjaxResult.success(upgradeOsCheckVO);
    }

    @PostMapping("/upgrade")
    public AjaxResult upgrade(@RequestBody UpgradeBody upgradeBody) {
        opsClusterService.upgrade(upgradeBody);
        return AjaxResult.success();
    }

    @PostMapping("/upgradeCommit")
    public AjaxResult upgradeCommit(@RequestBody UpgradeBody upgradeBody) {
        opsClusterService.upgradeCommit(upgradeBody);
        return AjaxResult.success();
    }

    @PostMapping("/upgradeRollback")
    public AjaxResult upgradeRollback(@RequestBody UpgradeBody upgradeBody) {
        opsClusterService.upgradeRollback(upgradeBody);
        return AjaxResult.success();
    }

    @PostMapping("/quickInstall")
    public AjaxResult quickInstall(@RequestBody InstallBody installBody) {
        opsClusterService.quickInstall(installBody);
        return AjaxResult.success();
    }

    @PostMapping("/uninstall")
    public AjaxResult uninstall(@RequestBody UnInstallBody unInstallBody) {
        opsClusterService.uninstall(unInstallBody);
        return AjaxResult.success();
    }

    @DeleteMapping("/remove/{clusterId}")
    public AjaxResult remove(@PathVariable("clusterId") String clusterId) {
        opsClusterService.removeCluster(clusterId);
        return AjaxResult.success();
    }

    @PostMapping("/restart")
    public AjaxResult restart(@RequestBody OpsClusterBody restartBody) {
        opsClusterService.restart(restartBody);
        return AjaxResult.success();
    }

    @PostMapping("/start")
    public AjaxResult start(@RequestBody OpsClusterBody startBody) {
        opsClusterService.start(startBody);
        return AjaxResult.success();
    }

    @PostMapping("/stop")
    public AjaxResult stop(@RequestBody OpsClusterBody stopBody) {
        opsClusterService.stop(stopBody);
        return AjaxResult.success();
    }

    @PostMapping("/ssh")
    public AjaxResult ssh(@RequestBody SSHBody sshBody) {
        opsClusterService.ssh(sshBody);
        return AjaxResult.success();
    }

    @GetMapping("/ls")
    public AjaxResult ls(@RequestParam String hostId, @RequestParam String path) {
        List<HostFile> files = opsClusterService.ls(hostId, path);
        return AjaxResult.success(files);
    }

    @GetMapping("/logPath")
    public AjaxResult logPath(@RequestParam String clusterId, @RequestParam String hostId) {
        OpsNodeLogVO opsNodeLogVO = opsClusterService.logPath(clusterId, hostId);
        return AjaxResult.success(opsNodeLogVO);
    }

    @GetMapping("/auditLog")
    public AjaxResult auditLog(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String start, @RequestParam String end) {
        List<AuditLogVO> auditLogVOList = opsClusterService.auditLog(startPage(), clusterId, hostId, start, end);
        return AjaxResult.success(auditLogVOList);
    }

    @GetMapping("/listSession")
    public AjaxResult listSession(@RequestParam String clusterId, @RequestParam String hostId) {
        List<SessionVO> sessionVOList = opsClusterService.listSession(clusterId, hostId);
        return AjaxResult.success(sessionVOList);
    }

    @GetMapping("/slowSql")
    public AjaxResult slowSql(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String start, @RequestParam String end) {
        List<SlowSqlVO> slowSqlVOList = opsClusterService.slowSql(startPage(), clusterId, hostId, start, end);
        return AjaxResult.success(slowSqlVOList);
    }

    @GetMapping("/download")
    public void download(@RequestParam String hostId, @RequestParam String path, @RequestParam String filename, HttpServletResponse response) {
        opsClusterService.download(hostId, path, filename, response);
    }

    @GetMapping("/listCluster")
    public AjaxResult listCluster() {
        return AjaxResult.success(opsFacade.listCluster());
    }

    @GetMapping("/summary")
    public AjaxResult summary() {
        ClusterSummaryVO clusterSummaryVO = opsClusterService.summary();
        return AjaxResult.success(clusterSummaryVO);
    }

    @PostMapping("/import")
    public AjaxResult importCluster(@RequestBody ImportClusterBody importClusterBody) {
        opsClusterService.importCluster(importClusterBody);
        return AjaxResult.success();
    }

    @GetMapping("/monitor")
    public AjaxResult monitor(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String businessId) {
        opsClusterService.monitor(clusterId, hostId, businessId);
        return AjaxResult.success();
    }

    @GetMapping("/listHost")
    public AjaxResult listHost(@RequestParam("clusterId") String clusterId) {
        List<OpsHostEntity> hostEntities = opsClusterService.listClusterHost(clusterId);
        return AjaxResult.success(hostEntities);
    }

    @GetMapping("/check")
    public AjaxResult check(@RequestParam String clusterId,@RequestParam(value = "rootPassword",required = false) String rootPassword) {
        CheckSummaryVO checkSummaryVO = opsFacade.check(clusterId,rootPassword);
        return AjaxResult.success(checkSummaryVO);
    }

    @GetMapping("/generateconf")
    public AjaxResult generateconf(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String businessId) {
        opsClusterService.generateconf(clusterId, hostId, businessId);
        return AjaxResult.success();
    }

    @GetMapping("/switchover")
    public AjaxResult switchover(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String businessId) {
        opsClusterService.switchover(clusterId, hostId, businessId);
        return AjaxResult.success();
    }

    @GetMapping("/build")
    public AjaxResult build(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String businessId) {
        opsClusterService.build(clusterId, hostId, businessId);
        return AjaxResult.success();
    }

    @GetMapping("/listInstallPackage")
    public AjaxResult listInstallPackage(@RequestParam(value = "version", required = false) OpenGaussVersionEnum openGaussVersionEnum) {
        ListDir listDir = opsClusterService.listInstallPackage(openGaussVersionEnum, getUserId());
        return AjaxResult.success(listDir);
    }

    @GetMapping("/env/{hostId}")
    public AjaxResult env(@PathVariable("hostId") String hostId,
                          @RequestParam(value = "expectedOs", defaultValue = "CENTOS_X86_64") OpenGaussSupportOSEnum expectedOs,
                          @RequestParam(required = false) String rootPassword) {
        HostEnv hostEnv = opsClusterService.env(hostId, expectedOs, rootPassword);
        return AjaxResult.success(hostEnv);
    }

    @GetMapping("/listGucSetting")
    public AjaxResult listGucSetting(@RequestParam("clusterId") String clusterId, @RequestParam("hostId") String hostId) {
        List<GucSettingVO> gucSettingList = opsClusterService.getGucSettingList(clusterId, hostId);
        return AjaxResult.success(gucSettingList);
    }

    @PostMapping("/batchConfigGucSetting")
    public AjaxResult batchConfigGucSetting(@RequestBody GucSettingDto gucBody) {
        opsClusterService.batchConfigGucSetting(gucBody);
        return AjaxResult.success();
    }

    @GetMapping("/listEnterpriseCluster")
    public AjaxResult listEnterpriseCluster() {
        List<OpsClusterVO> result = opsClusterService.listEnterpriseCluster();
        return AjaxResult.success(result);
    }

    @GetMapping
    public AjaxResult threadPoolMonitor() {
        Map<String, Integer> res = opsClusterService.threadPoolMonitor();
        return AjaxResult.success(res);
    }
}
