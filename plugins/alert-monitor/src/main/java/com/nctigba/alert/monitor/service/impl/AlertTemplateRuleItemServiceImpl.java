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

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
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
public class AlertTemplateRuleItemServiceImpl extends ServiceImpl<AlertTemplateRuleItemMapper, AlertTemplateRuleItemDO>
        implements AlertTemplateRuleItemService {
    @Override
    public void saveOrUpdateList(List<AlertTemplateRuleItemDO> templateRuleItems) {
        this.saveOrUpdateBatch(templateRuleItems);
    }
}
