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
 *  AlertRuleService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/AlertRuleService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemExpSrcDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemSrcDO;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.query.RuleQuery;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:08
 * @description
 */
public interface AlertRuleService extends IService<AlertRuleDO> {
    Page<AlertRuleDO> getRulePage(RuleQuery ruleQuery, Page page);

    AlertRuleDO getRuleById(Long id);

    List<AlertRuleDO> getRuleList(List<String> ruleTypes);

    /**
     * getRuleItemSrcList
     *
     * @return List<AlertRuleItemSrc>
     */
    List<AlertRuleItemSrcDO> getRuleItemSrcList();

    /**
     * getRuleItemExpSrcListByRuleItemSrcId
     *
     * @param ruleItemSrcId Long
     * @return List<AlertRuleItemExpSrc>
     */
    List<AlertRuleItemExpSrcDO> getRuleItemExpSrcListByRuleItemSrcId(Long ruleItemSrcId);

    /**
     * saveRule
     *
     * @param alertRule AlertRule
     */
    void saveRule(AlertRuleParamDTO alertRule);

    /**
     * delete rule by ID
     *
     * @param id Long
     */
    void delRuleById(Long id);
}
