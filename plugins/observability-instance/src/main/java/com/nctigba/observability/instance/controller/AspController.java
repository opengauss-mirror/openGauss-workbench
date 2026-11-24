/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.instance.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.model.query.AspCountQuery;
import com.nctigba.observability.instance.service.AspService;

import lombok.RequiredArgsConstructor;

/**
 * AspController.java
 *
 * @since 2023-08-25
 */
@RestController
@RequestMapping("/instanceMonitoring/api/v1/asp")
@RequiredArgsConstructor
public class AspController extends ControllerConfig {
    private final AspService aspService;

    /**
     * list
     *
     * @param req req
     * @return AjaxResult
     */
    @GetMapping(value = "/count")
    public AjaxResult list(AspCountQuery req) {
        return AjaxResult.success(aspService.count(req));
    }

    /**
     * analysis
     *
     * @param req req
     * @return AjaxResult
     */
    @GetMapping(value = "/analysis")
    public AjaxResult analysis(AspCountQuery req) {
        return AjaxResult.success(aspService.analysis(req));
    }
}
