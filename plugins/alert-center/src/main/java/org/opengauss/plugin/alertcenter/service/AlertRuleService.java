/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.dto.AlertRuleDto;
import org.opengauss.plugin.alertcenter.dto.RuleItemPropertyDto;
import org.opengauss.plugin.alertcenter.entity.AlertRule;
import org.opengauss.plugin.alertcenter.model.RuleReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:08
 * @description
 */
public interface AlertRuleService extends IService<AlertRule> {
    Page<AlertRuleDto> getRulePage(RuleReq ruleReq, Page page);

    AlertRule getRuleById(Long id);

    List<RuleItemPropertyDto> getRuleItemProperties();

    List<AlertRuleDto> getRuleList();
}
