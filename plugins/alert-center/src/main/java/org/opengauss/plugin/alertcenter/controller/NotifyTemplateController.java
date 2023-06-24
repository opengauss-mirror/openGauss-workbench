/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.plugin.alertcenter.entity.NotifyTemplate;
import org.opengauss.plugin.alertcenter.service.NotifyTemplateService;
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
@RequestMapping("/alertCenter/api/v1/notifyTemplate")
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
        List<NotifyTemplate> list = notifyTemplateService.getList(notifyTemplateType);
        return AjaxResult.success(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        NotifyTemplate notifyTemplate = notifyTemplateService.getById(id);
        return AjaxResult.success(notifyTemplate);
    }

    @PostMapping
    public AjaxResult saveTemplate(@Validated @RequestBody NotifyTemplate notifyTemplate) {
        notifyTemplateService.saveTemplate(notifyTemplate);
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult delById(@PathVariable Long id) {
        notifyTemplateService.delById(id);
        return AjaxResult.success();
    }
}
