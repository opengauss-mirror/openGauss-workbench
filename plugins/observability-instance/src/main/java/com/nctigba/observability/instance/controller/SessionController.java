/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/session/")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @GetMapping(value = "blockAndLongTxc")
    public AjaxResult blockAndLongTxc(String id) {
        return AjaxResult.success(sessionService.blockAndLongTxc(id));
    }

    @GetMapping(value = "detail/general")
    public AjaxResult detailGeneral(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailGeneral(id, sessionid));
    }

    @GetMapping(value = "detail/statistic")
    public AjaxResult detailStatistic(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailStatistic(id, sessionid));
    }

    @GetMapping(value = "detail/blockTree")
    public AjaxResult detailBlockTree(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailBlockTree(id, sessionid));
    }

    @GetMapping(value = "detail/waiting")
    public AjaxResult detailWaiting(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailWaiting(id, sessionid));
    }

    @GetMapping(value = "detail")
    public AjaxResult detail(String id, String sessionid) {
        return AjaxResult.success(sessionService.detail(id, sessionid));
    }
}
