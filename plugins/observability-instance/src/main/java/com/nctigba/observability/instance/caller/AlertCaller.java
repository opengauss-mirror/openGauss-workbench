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
 *  DataSourceConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/caller/AlertCaller.java
 *
 *  -------------------------------------------------------------------------
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
