/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.util.Date;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.service.OpsWdrService;

/**
 * WdrSnapController.java
 *
 * @since 2023-08-25
 */
@RestController
@RequestMapping("/wdr")
public class WdrSnapController extends ControllerConfig {
    @Autowired
    private OpsWdrService wdrService;

    /**
     * findSnapshot
     *
     * @param id    id
     * @param start start
     * @param end   end
     * @return AjaxResult
     */
    @GetMapping("/findSnapshot")
    public AjaxResult findSnapshot(String id, Date start, Date end) {
        return AjaxResult.success(wdrService.findSnapshot(id, start, end));
    }
}