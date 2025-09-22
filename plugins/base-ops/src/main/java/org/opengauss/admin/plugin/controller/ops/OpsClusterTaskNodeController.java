/*
 * Copyright (c) 2024-2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterTaskNodeController.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/OpsClusterTaskNodeController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskNodeDTO;
import org.opengauss.admin.plugin.service.ops.impl.OpsClusterTaskNodeProviderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * cluster task node operations
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@RestController
@RequestMapping("/cluster/task/node")
@Validated
public class OpsClusterTaskNodeController extends BaseController {
    @Resource
    private OpsClusterTaskNodeProviderService clusterTaskNodeProvider;


    /**
     * create cluster task node
     *
     * @param insertDto insertDto
     * @return create result
     */
    @Log(title = "cluster_task_node", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult createClusterTaskNode(@RequestBody @Valid OpsClusterTaskNodeDTO insertDto) {
        return AjaxResult.success(clusterTaskNodeProvider.createClusterTaskNode(insertDto));
    }

    /**
     * list cluster node list
     *
     * @param clusterId cluster id
     * @return list
     */
    @GetMapping("/list")
    public AjaxResult listClusterTaskNode(@RequestParam("clusterId") String clusterId) {
        return AjaxResult.success(clusterTaskNodeProvider.listClusterTaskNode(clusterId));
    }

    /**
     * update cluster task
     *
     * @param dto dto
     * @return update result
     */
    @Log(title = "cluster_task_node", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public AjaxResult updateClusterTaskNode(@RequestBody @Valid OpsClusterTaskNodeDTO dto) {
        return AjaxResult.success(clusterTaskNodeProvider.updateClusterTaskNode(dto));
    }

    /**
     * delete cluster task node
     *
     * @param clusterId clusterId
     * @param nodeId    nodeId
     * @return task node delete result
     */
    @Log(title = "cluster_task_node", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult deleteClusterTaskNode(@RequestParam("clusterId") String clusterId,
                                            @RequestParam("nodeId") String nodeId) {
        return AjaxResult.success(clusterTaskNodeProvider.deleteClusterTaskNode(clusterId, nodeId));
    }
}
