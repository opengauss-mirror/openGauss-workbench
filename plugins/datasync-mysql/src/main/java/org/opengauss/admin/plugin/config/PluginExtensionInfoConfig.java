/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * PluginExtensionInfoConfig.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/config/PluginExtensionInfoConfig.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.config;

import com.gitee.starblues.core.PluginExtensionInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: PluginExtensionInfoConfig
 * @author: xielibo
 * @date: 2023-01-26 16:39
 **/
@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {

    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo", "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGNsYXNzPSJpb25pY29uIiB2aWV3Qm94PSIwIDAgNTEyIDUxMiI+PHRpdGxlPkFkZCBDaXJjbGU8L3RpdGxlPjxwYXRoIGQ9Ik00NDggMjU2YzAtMTA2LTg2LTE5Mi0xOTItMTkyUzY0IDE1MCA2NCAyNTZzODYgMTkyIDE5MiAxOTIgMTkyLTg2IDE5Mi0xOTJ6IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLW1pdGVybGltaXQ9IjEwIiBzdHJva2Utd2lkdGg9IjMyIi8+PHBhdGggZmlsbD0ibm9uZSIgc3Ryb2tlPSJjdXJyZW50Q29sb3IiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgc3Ryb2tlLXdpZHRoPSIzMiIgZD0iTTI1NiAxNzZ2MTYwTTMzNiAyNTZIMTc2Ii8+PC9zdmc+");
        map.put("pluginType", 1);
        map.put("theme", "dark");
        return map;
    }
}
