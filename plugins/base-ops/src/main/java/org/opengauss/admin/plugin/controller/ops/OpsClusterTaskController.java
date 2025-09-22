/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterTaskController.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/OpsClusterTaskController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.commons.collections4.CollectionUtils;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.plugin.base.BaseController;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskDTO;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskQueryParamDTO;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskService;
import org.opengauss.admin.plugin.service.ops.impl.OpsHostRemoteService;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * cluster task operations
 *
 * @author wangchao
 * @since  2024/6/22 9:41
 **/
@RestController
@RequestMapping("/clusterTask")
@Validated
public class OpsClusterTaskController extends BaseController {
    @Resource
    private IOpsClusterTaskService opsClusterTaskService;
    @Resource
    private OpsHostRemoteService opsHostRemoteService;

    /**
     * query host info by ip
     *
     * @param hostIp host ip
     * @return host info
     */
    @GetMapping("/host/{hostIp}")
    public AjaxResult host(@PathVariable String hostIp) {
        Assert.isTrue(StrUtil.isNotEmpty(hostIp), "host ip cannot be empty");
        Assert.isTrue(IpUtils.mayBeIPAddress(hostIp), "ipaddress " + hostIp + "is invalid");
        return AjaxResult.success(opsClusterTaskService.getHostByIp(hostIp));
    }

    /**
     * get az list
     *
     * @return az list
     */
    @GetMapping("/list/az")
    public AjaxResult listAz() {
        return AjaxResult.success(opsHostRemoteService.listAllAz());
    }

    /**
     * get az info by id
     *
     * @param azId azId
     * @return az
     */
    @GetMapping("/az/{azId}")
    public AjaxResult getAzInfo(@PathVariable String azId) {
        return AjaxResult.success(opsHostRemoteService.getAzById(azId));
    }

    /**
     * query host info by os , osVersion, cpuArch
     *
     * @param os        os
     * @param osVersion osVersion
     * @param cpuArch   cpuArch
     * @return host info
     */
    @GetMapping("/host/list")
    public AjaxResult hostList(@RequestParam(name = "os", required = false) String os,
                               @RequestParam(name = "osVersion", required = false) String osVersion,
                               @RequestParam(name = "cpuArch", required = false) String cpuArch) {
        return AjaxResult.success(opsClusterTaskService.getHostList(os, osVersion, cpuArch));
    }

    /**
     * query host user by host id
     *
     * @param hostId host id
     * @return host user list
     */
    @GetMapping("/hostUser/{hostId}")
    public AjaxResult hostUser(@PathVariable String hostId) {
        Assert.isTrue(StrUtil.isNotEmpty(hostId), "host id cannot be empty");
        Assert.isTrue(!StrUtil.equalsIgnoreCase(hostId, "null"), "host id cannot be empty");
        return AjaxResult.success(opsClusterTaskService.listHostUserByHostId(hostId));
    }

    /**
     * check if the server port is occupied
     *
     * @param hostId   host id
     * @param hostPort host port
     * @return check result
     */
    @GetMapping("/hostPort/{hostId}/{hostPort}")
    public AjaxResult hostPort(@RequestParam(name = "taskId") String taskId, @PathVariable String hostId,
                               @PathVariable Integer hostPort) {
        Assert.isTrue(StrUtil.isNotEmpty(hostId), "host id cannot be empty");
        Assert.isTrue(hostPort >= 1024 && hostPort <= 65535, "server port must be between 1024 and 65535");
        return AjaxResult.success(opsClusterTaskService.checkHostPort(taskId, hostId, hostPort));
    }

    /**
     * check cluster all port
     *
     * @param clusterId cluster id
     * @return check result
     */
    @GetMapping("/check/cluster/port")
    public AjaxResult checkTaskHostPort(@RequestParam(name = "clusterId") String clusterId) {
        Assert.isTrue(StrUtil.isNotEmpty(clusterId), "cluster id cannot be empty");
        return AjaxResult.success(opsClusterTaskService.checkTaskHostPort(clusterId));
    }

    /**
     * check host path disk space
     *
     * @param hostId host
     * @param paths  paths
     * @return check result
     */
    @PostMapping("/check/host/disk/space/{hostId}")
    public AjaxResult checkHostDiskSpace(@PathVariable String hostId, @RequestBody List<String> paths) {
        Assert.isTrue(StrUtil.isNotEmpty(hostId), "host id cannot be empty");
        Assert.isTrue(CollectionUtil.isNotEmpty(paths), "paths cannot be empty");
        return AjaxResult.success(opsClusterTaskService.checkHostDiskSpace(hostId, paths));
    }

    /**
     * page query
     *
     * @param dto dto
     * @return clusterTask/page
     */
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody OpsClusterTaskQueryParamDTO dto) {
        IPage<OpsClusterTaskEntity> page = opsClusterTaskService.pageByCondition(startPage(), dto);
        if (Objects.isNull(page)) {
            return getDataTable();
        }
        return getDataTable(page);
    }

    /**
     * check clusterName is existed
     *
     * @param clusterName clusterName
     * @return clusterNameExist
     */
    @GetMapping("/check/name")
    public AjaxResult checkClusterName(@RequestParam(name = "clusterName") String clusterName,
                                       @RequestParam(name = "clusterId") String clusterId) {
        Assert.isTrue(StrUtil.isNotEmpty(clusterName), "cluster name cannot be empty");
        return AjaxResult.success(opsClusterTaskService.checkClusterNameExist(clusterId, clusterName));
    }

    /**
     * check if current host user has installed cluster
     *
     * @param hostIp       host ip
     * @param hostUsername host username
     * @return check result
     */
    @GetMapping("/check/host")
    public AjaxResult checkHostUserCanInstallCluster(@RequestParam(name = "hostIp") String hostIp,
                                                     @RequestParam(name = "hostUsername") String hostUsername) {
        Assert.isTrue(StrUtil.isNotEmpty(hostIp), "host ip cannot be empty");
        Assert.isTrue(StrUtil.isNotEmpty(hostUsername), "host username cannot be empty");
        Assert.isTrue(IpUtils.mayBeIPAddress(hostIp), "ip address " + hostIp + " is illegal");
        return AjaxResult.success(opsClusterTaskService.checkHostInstanceCanInstallClusterNode(hostIp, hostUsername));
    }

    /**
     * query cluster task detail
     *
     * @param taskId taskId
     * @return cluster task detail
     */
    @GetMapping("/detail/{taskId}")
    public AjaxResult queryClusterTaskDetail(@PathVariable("taskId") String taskId) {
        Assert.isTrue(StrUtil.isNotEmpty(taskId), "task id cannot be empty");
        return AjaxResult.success(opsClusterTaskService.queryClusterTaskDetail(taskId));
    }

    /**
     * create cluster task
     *
     * @param dto dto
     * @return create result
     */
    @Log(title = "cluster_task", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult createClusterTask(@RequestBody @Valid OpsClusterTaskDTO dto) {
        try {
            Assert.isTrue(Objects.nonNull(dto), "cluster task create param cannot be null");
            return AjaxResult.success(opsClusterTaskService.createClusterTask(dto));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * check cluster task
     *
     * @param clusterId cluster id
     * @return check result
     */
    @PostMapping("/check")
    public AjaxResult checkClusterTask(@RequestParam("clusterId") String clusterId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(clusterId), "cluster id must be not empty");
            return AjaxResult.success(opsClusterTaskService.checkClusterTask(clusterId));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * update cluster task
     *
     * @param dto dto
     * @return update result
     */
    @Log(title = "cluster_task", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public AjaxResult updateClusterTask(@RequestBody @Valid OpsClusterTaskDTO dto) {
        try {
            Assert.isTrue(Objects.nonNull(dto), "cluster task update param cannot be null");
            Assert.isTrue(StrUtil.isNotEmpty(dto.getClusterId()), "cluster id cannot be empty");
            opsClusterTaskService.updateClusterTask(dto);
            return AjaxResult.success();
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * batch delete cluster task
     *
     * @param taskIds taskIds
     * @return task delete result
     */
    @Log(title = "cluster_task", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult deleteClusterTask(@RequestBody List<String> taskIds) {
        try {
            Assert.isTrue(CollectionUtils.isNotEmpty(taskIds), "cluster task id list cannot be empty");
            Map<String, String> delResult = opsClusterTaskService.deleteClusterTask(taskIds);
            return AjaxResult.success(delResult);
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * force delete task by task id
     *
     * @param taskId task id
     * @return result
     */
    @Log(title = "cluster_task", businessType = BusinessType.DELETE)
    @PostMapping("/force/delete")
    public AjaxResult deleteClusterTask(@RequestParam(name = "taskId") String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            return AjaxResult.success(opsClusterTaskService.deleteClusterTaskForce(taskId));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * confirm cluster task by task id
     *
     * @param taskId task id
     * @return confirm result
     */
    @Log(title = "cluster_task", businessType = BusinessType.CONFIRM)
    @PostMapping("/confirm")
    public AjaxResult confirmClusterTask(@RequestParam String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            opsClusterTaskService.confirmClusterTask(taskId);
            return AjaxResult.success();
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * cluster task of host environment check
     *
     * @param taskId cluster task id
     * @return environment check result
     */
    @Log(title = "cluster_task", businessType = BusinessType.CHECK_ENVIRONMENT)
    @PostMapping("/check/environment")
    public AjaxResult checkClusterEnvironment(@RequestParam String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            opsClusterTaskService.checkClusterTask(taskId);
            return AjaxResult.success(opsClusterTaskService.checkClusterInstallEnvironment(taskId));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * cluster task of host environment check
     *
     * @param taskId cluster task id
     * @return environment check result
     */
    @PostMapping("/check/env/path")
    public AjaxResult checkClusterEnvPath(@RequestParam String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            return AjaxResult.success(opsClusterTaskService.checkClusterInstallEnvironment(taskId));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * get cluster task of host environment check
     *
     * @param taskId taskId
     * @return environment check result
     */
    @GetMapping("/get/environment")
    public AjaxResult getClusterEnvironment(@RequestParam String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            return AjaxResult.success(opsClusterTaskService.getClusterInstallEnvironment(taskId));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * batch install cluster task
     *
     * @param taskIds cluster task ids
     * @return batch install result
     */
    @Log(title = "cluster_task", businessType = BusinessType.BATCH_INSTALL)
    @PostMapping("/batch/install")
    public AjaxResult batchInstallCluster(@RequestBody List<String> taskIds) {
        try {
            Assert.isTrue(CollectionUtils.isNotEmpty(taskIds), "cluster task ids cannot be empty");
            return AjaxResult.success(opsClusterTaskService.batchInstallCluster(taskIds));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    @Log(title = "cluster_task", businessType = BusinessType.RE_INSTALL)
    @PostMapping("/re/install")
    public AjaxResult reInstallCluster(@RequestParam("taskId") String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            opsClusterTaskService.reInstallCluster(taskId);
            return AjaxResult.success();
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * get batch install cluster task status
     *
     * @param taskIds cluster task ids
     * @return batch install status
     */
    @PostMapping("/batch/install/status")
    public AjaxResult getBatchInstallTaskStatus(@RequestBody List<String> taskIds) {
        try {
            Assert.isTrue(CollectionUtils.isNotEmpty(taskIds), "cluster task ids cannot be empty");
            return AjaxResult.success(opsClusterTaskService.getBatchInstallTaskStatus(taskIds));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    /**
     * copy cluster task by old cluster task id
     *
     * @param taskId old cluster task id
     * @return new cluster task id
     */
    @Log(title = "cluster_task", businessType = BusinessType.COPY)
    @PostMapping("/copy")
    public AjaxResult copyClusterTask(@RequestParam String taskId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(taskId), "cluster task id cannot be empty");
            return AjaxResult.success(opsClusterTaskService.copyClusterTask(taskId));
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }
}
