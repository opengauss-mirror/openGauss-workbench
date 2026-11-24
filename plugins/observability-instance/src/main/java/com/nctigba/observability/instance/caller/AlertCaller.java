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

package com.nctigba.observability.instance.caller;

import com.gitee.starblues.annotation.Caller;
import org.opengauss.admin.common.core.domain.AjaxResult;

import java.util.List;
import java.util.Map;

/**
 * AlertCaller
 *
 * @author wuyuebin
 * @since 2024/7/24 11:09
 */
@Caller("alert")
public interface AlertCaller {
    @Caller.Method("saveAlertRule")
    public AjaxResult saveAlertRule(Map<String, Object> paramMap);
    @Caller.Method("alerts")
    public AjaxResult alerts(List<Map<String, Object>> paramList);
}
