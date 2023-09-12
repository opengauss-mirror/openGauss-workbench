/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.entity.AlertRuleItemParam;
import com.nctigba.alert.monitor.mapper.AlertRuleItemParamMapper;
import com.nctigba.alert.monitor.service.AlertRuleItemParamService;
import org.springframework.stereotype.Service;

/**
 * AlertRuleItemParamServiceImpl
 *
 * @since 2023/7/27 15:12
 */
@Service
public class AlertRuleItemParamServiceImpl extends ServiceImpl<AlertRuleItemParamMapper, AlertRuleItemParam>
    implements AlertRuleItemParamService {
}
