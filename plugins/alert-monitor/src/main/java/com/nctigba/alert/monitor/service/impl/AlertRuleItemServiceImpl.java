/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.entity.AlertRuleItem;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.service.AlertRuleItemService;
import org.springframework.stereotype.Service;

/**
 * AlertRuleItemServiceImpl
 *
 * @since 2023/7/27 15:07
 */
@Service
public class AlertRuleItemServiceImpl extends ServiceImpl<AlertRuleItemMapper, AlertRuleItem>
    implements AlertRuleItemService {
}
