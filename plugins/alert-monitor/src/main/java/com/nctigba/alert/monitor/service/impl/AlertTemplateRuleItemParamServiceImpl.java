/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItemParam;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemParamMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemParamService;
import org.springframework.stereotype.Service;

/**
 * @author wuyuebin
 * @date 2023/6/5 19:25
 * @description
 */
@Service
public class AlertTemplateRuleItemParamServiceImpl
        extends ServiceImpl<AlertTemplateRuleItemParamMapper, AlertTemplateRuleItemParam>
        implements AlertTemplateRuleItemParamService {
}
