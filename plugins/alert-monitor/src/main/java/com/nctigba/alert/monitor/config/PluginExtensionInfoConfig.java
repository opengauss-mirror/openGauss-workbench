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
 *  PluginExtensionInfoConfig.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/config/PluginExtensionInfoConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.config;

import com.gitee.starblues.core.PluginExtensionInfo;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * plugin extension info
 *
 * @since 2023/4/13 11:37
 */
@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {
    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo",
            "PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3c"
                + "udzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyLjc4MjkgOS41MTE5OUMxMi43ODI5IDYuODYzOTkgMTAuNjMwOSA0Ljcx"
                + "MTk5IDcuOTgyODkgNC43MTE5OUM1LjMzNDg5IDQuNzExOTkgMy4xODI4OSA2Ljg2Mzk5IDMuMTgyODkgOS41MTE5OVYxMy41Mk"
                + "gxLjcxMDg5VjE0LjY0SDE0LjI1NDlWMTMuNTJIMTIuNzgyOVY5LjUxMTk5Wk00LjMwMjg5IDEzLjUxMlY5LjUwMzk5QzQuMzAy"
                + "ODkgNy40NzE5OSA1Ljk1MDg5IDUuODIzOTkgNy45ODI4OSA1LjgyMzk5QzEwLjAxNDkgNS44MjM5OSAxMS42NjI5IDcuNDcxOTk"
                + "gMTEuNjYyOSA5LjUwMzk5VjEzLjUxMkg0LjMwMjg5Wk03LjM5ODg5IDEuMzQzOTlIOC41MTg4OVYzLjI3MTk5SDcuMzk4ODlWM"
                + "S4zNDM5OVpNMTAuMjkwMyAzLjc2MTU5TDExLjI1NDMgMi4wOTE5OUwxMi4yMjQyIDIuNjUxOTlMMTEuMjYwMiA0LjMyMTU5TDE"
                + "wLjI5MDMgMy43NjE1OVpNMTIuNTUzMSA1LjYzNjE1TDE0LjIyMjcgNC42NzIxNUwxNC43ODI3IDUuNjQyMDdMMTMuMTEzMSA2L"
                + "jYwNjA3TDEyLjU1MzEgNS42MzYxNVpNMy43MzQ3MyAyLjU2OTE5TDQuNzA0NjUgMi4wMDkxOUw1LjY2ODY1IDMuNjc4ODdMNC42"
                + "OTg3MyA0LjIzODg3TDMuNzM0NzMgMi41NjkxOVpNMS4xNzk2OSA1LjQ2MTM1TDEuNzM5NjkgNC40OTE0M0wzLjQwOTI5IDUuND"
                + "U1NDNMMi44NDkyOSA2LjQyNTM1TDEuMTc5NjkgNS40NjEzNVoiIGZpbGw9IiM0RTU5NjkiLz4KPHBhdGggZD0iTTguNDE1MjIg"
                + "Ni43MDQxTDYuMTk5MjIgMTAuMzIwMUg3Ljk4MzIyTDcuNTUxMjIgMTMuMDE2MUw5Ljc1OTIyIDkuNDAwMUg3Ljk4MzIyTDguND"
                + "E1MjIgNi43MDQxWiIgZmlsbD0iIzRFNTk2OSIvPgo8L3N2Zz4K");
        map.put("theme", "light");
        map.put("pluginType", 1);
        map.put("isNeedConfigured", 0);
        map.put("configAttrs", "[{\"attrCode\":\"esHost\",\"attrLabel\":\"ES服务器\"},"
            + "{\"attrCode\":\"esPort\",\"attrLabel\":\"端口\"}]");
        map.put("descriptionEn", "Alert Monitor Plugin");
        return map;
    }
}
