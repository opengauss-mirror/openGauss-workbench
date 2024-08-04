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
 *  AlertConfigController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/AlertConfigController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.core.collection.CollectionUtil;
import org.opengauss.admin.common.core.domain.AjaxResult;
import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import com.nctigba.alert.monitor.service.AlertConfigService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/9 10:47
 * @description
 */
@RestController
@RequestMapping("/api/v1/alertConf")
public class AlertConfigController {
    @Autowired
    private AlertConfigService alertConfigService;

    @GetMapping
    public AjaxResult getAlertConf() {
        List<AlertConfigDO> list = alertConfigService.list();
        if (CollectionUtil.isEmpty(list)) {
            return AjaxResult.success(new AlertConfigDO());
        }
        return AjaxResult.success(list.get(0));
    }

    @PostMapping
    public AjaxResult saveAlertConf(@Valid @RequestBody AlertConfigDO alertConfigDO) {
        alertConfigService.saveAlertConf(alertConfigDO);
        return AjaxResult.success();
    }
}
