/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.entity.NotifyTemplate;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/31 10:16
 * @description
 */
public interface NotifyTemplateService extends IService<NotifyTemplate> {
    Page getListPage(String notifyTemplateName, String notifyTemplateType, Page page);

    void saveTemplate(NotifyTemplate notifyTemplate);

    void delById(Long id);

    List<NotifyTemplate> getList(String notifyTemplateType);
}
