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
 *  NotifyWayController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/NotifyWayController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.service.NotifyWayService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/25 15:49
 * @description
 */
@RestController
@RequestMapping("/api/v1/notifyWay")
public class NotifyWayController extends BaseController {
    @Autowired
    private NotifyWayService notifyWayService;

    @GetMapping
    public TableDataInfo getListPage(String name, String notifyType) {
        Page page = notifyWayService.getListPage(name, notifyType, startPage());
        return getDataTable(page);
    }

    @GetMapping("/list")
    public AjaxResult getList(String notifyType) {
        List<NotifyWayDO> notifyWayDOList = notifyWayService.getList(notifyType);
        return AjaxResult.success(notifyWayDOList);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        NotifyWayDO notifyWayDO = notifyWayService.getById(id);
        return AjaxResult.success(notifyWayDO);
    }

    @PostMapping
    public AjaxResult saveNotifyWay(@Validated @RequestBody NotifyWayDO notifyWayDO) {
        notifyWayService.saveNotifyWay(notifyWayDO);
        return AjaxResult.success();
    }

    /**
     * test the notify way
     *
     * @param notifyWayDO NotifyWay
     * @return AjaxResult.success()
     */
    @PostMapping("/test")
    public AjaxResult testNotifyWay(@Validated @RequestBody NotifyWayDO notifyWayDO) {
        if (!notifyWayDO.getNotifyType().equals(CommonConstants.WEBHOOK)
            && !notifyWayDO.getNotifyType().equals(CommonConstants.SNMP)) {
            return AjaxResult.error();
        }
        boolean isSuccess = notifyWayService.testNotifyWay(notifyWayDO);
        if (!isSuccess) {
            return AjaxResult.error();
        }
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult delById(@PathVariable Long id) {
        notifyWayService.delById(id);
        return AjaxResult.success();
    }
}
