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
 *  NotifyTemplateController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/NotifyTemplateController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
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
 * @date 2023/5/31 10:20
 * @description
 */
@RestController
@RequestMapping("/api/v1/notifyTemplate")
public class NotifyTemplateController extends BaseController {
    @Autowired
    private NotifyTemplateService notifyTemplateService;

    @GetMapping
    public TableDataInfo getListPage(String notifyTemplateName, String notifyTemplateType) {
        Page page = notifyTemplateService.getListPage(notifyTemplateName, notifyTemplateType, startPage());
        return getDataTable(page);
    }

    @GetMapping("/list")
    public AjaxResult getList(String notifyTemplateType) {
        List<NotifyTemplateDO> list = notifyTemplateService.getList(notifyTemplateType);
        return AjaxResult.success(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        NotifyTemplateDO notifyTemplateDO = notifyTemplateService.getById(id);
        return AjaxResult.success(notifyTemplateDO);
    }

    @PostMapping
    public AjaxResult saveTemplate(@Validated @RequestBody NotifyTemplateDO notifyTemplateDO) {
        notifyTemplateService.saveTemplate(notifyTemplateDO);
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult delById(@PathVariable Long id) {
        notifyTemplateService.delById(id);
        return AjaxResult.success();
    }
}
