/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
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
 *  WdrSnapController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/controller/WdrSnapController.java
 *
 *  -------------------------------------------------------------------------
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