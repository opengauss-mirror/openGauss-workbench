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
 *  OpenGaussOperatorController.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/controller
 * /OpenGaussOperatorController.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.container.service.OpenGaussOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * OpenGaussOperatorController
 *
 * @since 2024-6-26 16:39
 **/
@RestController
@RequestMapping("/operator")
public class OpenGaussOperatorController {
    @Autowired
    private OpenGaussOperatorService openGaussOperatorService;

    /**
     * 更新（同步）本地operator
     *
     * @return 更新结果
     */
    @PostMapping("/refresh")
    public AjaxResult refresh() {
        openGaussOperatorService.syncOperatorFromK8s();
        return AjaxResult.success();
    }

    /**
     * 分页查询operator列表
     *
     * @param k8sId    k8s集群id
     * @param type     类型
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return operator列表分页数据
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "k8sId", required = false) String k8sId,
                           @RequestParam(value = "type", required = false) String type,
                           @RequestParam(value = "pageNum", required = true) Integer pageNum,
                           @RequestParam(value = "pageSize", required = true) Integer pageSize) {
        Map<String, Object> pageMap = openGaussOperatorService.findOperator(k8sId, type, pageNum, pageSize);
        return AjaxResult.success(pageMap);
    }
}
