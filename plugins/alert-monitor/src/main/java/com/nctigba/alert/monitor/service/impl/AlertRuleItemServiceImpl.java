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
import com.nctigba.alert.monitor.model.entity.AlertRuleItemDO;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.service.AlertRuleItemService;
import org.springframework.stereotype.Service;

/**
 * AlertRuleItemServiceImpl
 *
 * @since 2023/7/27 15:07
 */
@Service
public class AlertRuleItemServiceImpl extends ServiceImpl<AlertRuleItemMapper, AlertRuleItemDO>
    implements AlertRuleItemService {
}
