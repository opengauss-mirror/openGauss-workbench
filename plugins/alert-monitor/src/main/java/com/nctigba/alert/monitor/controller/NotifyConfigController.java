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

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import org.opengauss.admin.common.core.domain.AjaxResult;
import com.nctigba.alert.monitor.model.query.NotifyConfigQuery;
import com.nctigba.alert.monitor.service.NotifyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/1 01:48
 * @description
 */
@RestController
@RequestMapping("/api/v1/notifyConfig")
public class NotifyConfigController {
    @Autowired
    private NotifyConfigService notifyConfigService;

    @GetMapping("/list")
    public AjaxResult getAllList() {
        List<NotifyConfigDO> list = notifyConfigService.getAllList();
        return AjaxResult.success(list);
    }

    @PostMapping("/list")
    public AjaxResult saveList(@RequestBody List<NotifyConfigDO> list) {
        notifyConfigService.saveList(list);
        return AjaxResult.success();
    }

    @PostMapping("/testConfig")
    public AjaxResult testConfig(@RequestBody NotifyConfigQuery notifyConfigReq) {
        boolean isSuccess = notifyConfigService.testConfig(notifyConfigReq);
        if (!isSuccess) {
            return AjaxResult.error();
        }
        return AjaxResult.success();
    }
}
