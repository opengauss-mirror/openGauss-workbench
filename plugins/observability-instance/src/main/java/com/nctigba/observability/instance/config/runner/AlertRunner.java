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
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/config/runner/AlertRunner.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.config.runner;

import com.nctigba.observability.instance.caller.AlertCaller;
import com.nctigba.observability.instance.constants.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AlertRunner
 *
 * @author wuyuebin
 * @since 2024/7/24 11:14
 */
@Component
@Slf4j
@Profile("!dev")
public class AlertRunner implements ApplicationRunner {
    @Autowired
    private AlertCaller alertCaller;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("ruleName", "实例监控代理状态异常");
            paramMap.put("pluginCode", CommonConstants.PLUGIN_CODE);
            paramMap.put("ruleCode", CommonConstants.EXPORTER_EXCEPTION_STATUS);
            paramMap.put("level", "warn");
            paramMap.put("ruleContent", "${nodeName}状态异常，请相关人员检查该代理是否存在问题！");
            alertCaller.saveAlertRule(paramMap);
        } catch (Exception e) {
            log.error("save the Exporter alert rule fail:{}, exception is {}", e.getMessage(), e);
        }
    }
}
