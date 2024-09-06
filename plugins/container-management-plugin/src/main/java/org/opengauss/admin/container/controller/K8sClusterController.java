/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  K8sClusterController.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/controller/K8sClusterController.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.container.service.K8sClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * K8sClusterController
 *
 * @since 2024-6-26 16:39
 **/
@RestController
@RequestMapping("/k8s/cluster")
public class K8sClusterController {
    @Autowired
    private K8sClusterService k8sClusterService;

    /**
     * 新增k8sCluster
     *
     * @param k8sCluster k8s集群模型
     * @return 创建结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody K8sCluster k8sCluster) {
        String res = k8sClusterService.addK8sClusterInfo(k8sCluster);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 删除k8sCluster
     *
     * @param id k8s集群id
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable String id) {
        String res = k8sClusterService.deleteK8sClusterInfo(id);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 更新k8sCluster
     *
     * @param k8sCluster k8s集群模型
     * @return 更新结果
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody K8sCluster k8sCluster) {
        String res = k8sClusterService.updateK8sClusterInfo(k8sCluster);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 查询k8sCluster列表
     *
     * @param k8sId k8s集群id
     * @return k8s集群列表数据
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(value = "k8sId", required = false) String k8sId) {
        List<K8sCluster> resList = k8sClusterService.getK8sClusterInfo(k8sId);
        return AjaxResult.success(resList);
    }

    /**
     * 分页查询k8sCluster列表
     *
     * @param k8sId    k8s集群id
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return k8s列表分页数据
     */
    @GetMapping("/list/page")
    public AjaxResult pageList(
            @RequestParam(value = "k8sId", required = false) String k8sId,
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = true) Integer pageSize) {
        Map<String, Object> resMap = k8sClusterService.getK8sClusterPageInfo(k8sId, pageNum, pageSize);
        return AjaxResult.success(resMap);
    }
}