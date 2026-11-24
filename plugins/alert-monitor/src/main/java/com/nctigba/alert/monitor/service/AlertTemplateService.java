/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.dto.AlertTemplateDTO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/12 11:40
 * @description
 */
public interface AlertTemplateService extends IService<AlertTemplateDO> {
    Page<AlertTemplateDO> getTemplatePage(String templateName, Page page);

    Page<AlertTemplateRuleDO> getTemplateRulePage(Long templateId, String ruleName, Page page);

    AlertTemplateDTO getTemplate(Long id);

    AlertTemplateDO saveTemplate(AlertTemplateQuery templateReq);

    /**
     * getTemplateList
     *
     * @param type String
     * @return List<AlertTemplateDO>
     */
    List<AlertTemplateDO> getTemplateList(String type);

    List<AlertTemplateRuleDO> getTemplateRuleListById(Long templateId);

    void delTemplate(Long id);
}
