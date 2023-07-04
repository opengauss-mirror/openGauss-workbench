/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.service.NotifyWayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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
        List<NotifyWay> notifyWayList = notifyWayService.getList(notifyType);
        return AjaxResult.success(notifyWayList);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        NotifyWay notifyWay = notifyWayService.getById(id);
        return AjaxResult.success(notifyWay);
    }

    @PostMapping
    public AjaxResult saveNotifyWay(@Valid @RequestBody NotifyWay notifyWay) {
        notifyWayService.saveNotifyWay(notifyWay);
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult delById(@PathVariable Long id) {
        notifyWayService.delById(id);
        return AjaxResult.success();
    }
}
