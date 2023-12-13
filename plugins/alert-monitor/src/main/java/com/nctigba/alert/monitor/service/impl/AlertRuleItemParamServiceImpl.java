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
 *  AlertRuleItemParamServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertRuleItemParamServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemParamDO;
import com.nctigba.alert.monitor.mapper.AlertRuleItemParamMapper;
import com.nctigba.alert.monitor.service.AlertRuleItemParamService;
import org.springframework.stereotype.Service;

/**
 * AlertRuleItemParamServiceImpl
 *
 * @since 2023/7/27 15:12
 */
@Service
public class AlertRuleItemParamServiceImpl extends ServiceImpl<AlertRuleItemParamMapper, AlertRuleItemParamDO>
    implements AlertRuleItemParamService {
}
