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
 * OpsClusterLogController.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/OpsClusterLogController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.service.ops.IOpsClusterLogService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Cluster Log Controller
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 */
@RestController
@RequestMapping("/opsClusterLog")
public class OpsClusterLogController extends BaseController {
    private static final String OPERATE_LOG_FILE_TEMPLATE = "OPERATE_LOG_%s.log";
    @Resource
    private IOpsClusterLogService opsClusterLogService;
    @Resource
    private IOpsClusterTaskService opsClusterTaskService;

    @GetMapping("/log")
    public String queryClusterDetail(@RequestParam String clusterId) {
        return opsClusterLogService.queryClusterOperateLog(clusterId);
    }

    @GetMapping("/download/log")
    public ResponseEntity<byte[]> downloadClusterDetail(@RequestParam String clusterId) {
        OpsClusterTaskEntity taskEntity = opsClusterTaskService.getById(clusterId);
        Assert.isTrue(Objects.nonNull(taskEntity), "task not exists");
        String context = opsClusterLogService.queryClusterOperateLog(clusterId);
        String clusterName = taskEntity.getClusterName();
        String downloadFileName = String.format(OPERATE_LOG_FILE_TEMPLATE, clusterName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=\"" + downloadFileName + "\"")
                .header("fileName", downloadFileName)
                .body(context.getBytes(StandardCharsets.UTF_8));
    }
}
