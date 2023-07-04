/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/24 10:40
 * @description
 */
public interface AlertTemplateRuleItemService extends IService<AlertTemplateRuleItem> {
    void saveOrUpdateList(List<AlertTemplateRuleItem> templateRuleItems);
}
