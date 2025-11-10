/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.web.controller;

import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.framework.config.SystemHandleDiagnostic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SysHandleDiagnosticController
 *
 * @author: wangchao
 * @Date: 2025/11/10 10:24
 * @Description: SysHandleDiagnosticController
 * @since 7.0.0-RC3
 **/
@RestController
@RequestMapping("/system/")
public class SysHandleDiagnosticController extends BaseController {
    @Autowired
    private SystemHandleDiagnostic systemHandleDiagnostic;

    /**
     * start system handler monitor
     *
     * @return ajaxResult
     */
    @GetMapping("/handler/monitor/start")
    public AjaxResult startMonitor() {
        systemHandleDiagnostic.startMonitor();
        return AjaxResult.success();
    }

    /**
     * pause system handler monitor
     *
     * @return ajaxResult
     */
    @GetMapping("/handler/monitor/pause")
    public AjaxResult pauseMonitor() {
        systemHandleDiagnostic.pauseMonitor();
        return AjaxResult.success();
    }

    /**
     * resume system handler monitor
     *
     * @return ajaxResult
     */
    @GetMapping("/handler/monitor/resume")
    public AjaxResult resumeMonitor() {
        systemHandleDiagnostic.resumeMonitor();
        return AjaxResult.success();
    }

    /**
     * check status system handler monitor
     *
     * @return ajaxResult
     */
    @GetMapping("/handler/monitor/status")
    public AjaxResult isMonitorEnabled() {
        return AjaxResult.success(systemHandleDiagnostic.isMonitorEnabled());
    }

    /**
     * close system handler monitor
     *
     * @return ajaxResult
     */
    @GetMapping("/handler/monitor/close")
    public AjaxResult closeMonitor() {
        systemHandleDiagnostic.closeMonitor();
        return AjaxResult.success();
    }
}
