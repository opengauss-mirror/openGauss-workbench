/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRuleItemParam;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleItemParamMapper;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleItemParamService;
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
