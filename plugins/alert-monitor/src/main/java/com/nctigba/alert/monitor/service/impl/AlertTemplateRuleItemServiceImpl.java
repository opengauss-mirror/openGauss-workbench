/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/24 10:40
 * @description
 */
@Service
public class AlertTemplateRuleItemServiceImpl extends ServiceImpl<AlertTemplateRuleItemMapper, AlertTemplateRuleItem>
        implements AlertTemplateRuleItemService {
    @Override
    public void saveOrUpdateList(List<AlertTemplateRuleItem> templateRuleItems) {
        this.saveOrUpdateBatch(templateRuleItems);
    }
}
