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
 *  AlertTemplateRuleService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/AlertTemplateRuleService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.dto.AlertTemplateRuleDTO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/24 10:31
 * @description
 */
public interface AlertTemplateRuleService extends IService<AlertTemplateRuleDO> {
    AlertTemplateRuleDO getTemplateRule(Long templateRuleId);

    AlertTemplateRuleDO saveTemplateRule(AlertTemplateRuleDO alertTemplateRuleDO);

    List<AlertTemplateRuleDO> getListByTemplateId(Long templateId);

    /**
     * enable rules
     *
     * @param templateRuleId Long
     */
    void enableTemplateRule(Long templateRuleId);

    /**
     * disable rules
     *
     * @param templateRuleId Long
     */
    void disableTemplateRule(Long templateRuleId);

    List<AlertTemplateRuleDTO> getDtoListByRuleId(Long ruleId);
}
