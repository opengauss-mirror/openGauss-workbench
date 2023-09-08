/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.dto.asp.AspCountReq;
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
    public AjaxResult list(AspCountReq req) {
        return AjaxResult.success(aspService.count(req));
    }

    /**
     * analysis
     *
     * @param req req
     * @return AjaxResult
     */
    @GetMapping(value = "/analysis")
    public AjaxResult analysis(AspCountReq req) {
        return AjaxResult.success(aspService.analysis(req));
    }
}
