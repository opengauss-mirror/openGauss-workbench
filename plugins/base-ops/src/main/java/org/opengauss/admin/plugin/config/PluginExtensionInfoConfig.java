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
 * base-ops/src/main/java/org/opengauss/admin/plugin/config/PluginExtensionInfoConfig.java
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
    /**
     * plugin id base-ops
     */
    public static final String PLUGIN_ID = "base-ops";

    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo", "PHN2ZyB2ZXJzaW9uPSIxLjEiIGlkPSJDYXBhXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4IgoJIHZpZXdCb3g9IjAgMCAzMTAgMzEwIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAzMTAgMzEwOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+CjxwYXRoIGQ9Ik0zMDAuNTY0LDE3OS4zMTFMMjgyLjQsMTY4LjgyNGMwLjQ4OS00LjU0MywwLjc0Ny05LjE1NCwwLjc0Ny0xMy44MjRzLTAuMjU4LTkuMjgxLTAuNzQ3LTEzLjgyNGwxOC4xNjQtMTAuNDg3CgljMy44ODEtMi4yNDEsNi42NTgtNS44Niw3LjgxNi0xMC4xOTFjMS4xNi00LjMzLDAuNTY0LTguODU0LTEuNjc2LTEyLjczNWwtMzQuOTQzLTYwLjUyNGMtMi45OS01LjE4LTguNTY0LTguMzk2LTE0LjU1MS04LjM5NgoJYy0yLjkzLDAtNS44MjYsMC43NzgtOC4zNzcsMi4yNTFMMjMwLjYxOSw1MS42MWMtNy40MDItNS40MjktMTUuNDA2LTEwLjA4My0yMy44OTMtMTMuODQyVjE2Ljc4MwoJQzIwNi43MjcsNy41MjksMTk5LjE5NSwwLDE4OS45NDUsMGgtNjkuODkxYy05LjI1NCwwLTE2Ljc4MSw3LjUyOS0xNi43ODEsMTYuNzgzdjIwLjk4NWMtOC40ODYsMy43NTktMTYuNDksOC40MTMtMjMuODk0LDEzLjg0MgoJTDYxLjE2NCw0MS4wOTRjLTIuNTUxLTEuNDczLTUuNDQ1LTIuMjUtOC4zNzUtMi4yNWMtNS45ODYsMC0xMS41NjMsMy4yMTUtMTQuNTUzLDguMzk1TDMuMjk1LDEwNy43NjIKCWMtMi4yNDIsMy44ODEtMi44MzYsOC40MDYtMS42NzQsMTIuNzM2YzEuMTU2LDQuMzMsMy45MzUsNy45NDksNy44MTQsMTAuMTkxTDI3LjYsMTQxLjE3NmMtMC40ODksNC41NDMtMC43NDcsOS4xNTQtMC43NDcsMTMuODI0CglzMC4yNTgsOS4yODEsMC43NDcsMTMuODI0TDkuNDM1LDE3OS4zMTFjLTMuODc5LDIuMjQxLTYuNjU4LDUuODYtNy44MTQsMTAuMTkxYy0xLjE2Miw0LjMzLTAuNTY4LDguODU1LDEuNjc0LDEyLjczNWwzNC45NDEsNjAuNTI0CgljMi45OSw1LjE4LDguNTY2LDguMzk1LDE0LjU1Myw4LjM5NWMyLjkzLDAsNS44MjQtMC43NzcsOC4zNzUtMi4yNUw3OS4zOCwyNTguMzljNy40MDMsNS40MjksMTUuNDA3LDEwLjA4MywyMy44OTQsMTMuODQydjIwLjk4NgoJYzAsNC40ODIsMS43NDQsOC42OTUsNC45MTQsMTEuODY2YzMuMTc0LDMuMTY5LDcuMzg1LDQuOTE2LDExLjg2Nyw0LjkxNmg2OS44OTFjOS4yNSwwLDE2Ljc4MS03LjUyOSwxNi43ODEtMTYuNzgydi0yMC45ODYKCWM4LjQ4Ni0zLjc1OSwxNi40OS04LjQxMywyMy44OTMtMTMuODQybDE4LjIxNSwxMC41MTdjMi41NTEsMS40NzMsNS40NDcsMi4yNSw4LjM3NywyLjI1YzUuOTg2LDAsMTEuNTYxLTMuMjE1LDE0LjU1MS04LjM5NQoJbDM0Ljk0My02MC41MjNjMi4yNC0zLjg4MSwyLjgzNi04LjQwNiwxLjY3Ni0xMi43MzZDMzA3LjIyMywxODUuMTcyLDMwNC40NDUsMTgxLjU1MywzMDAuNTY0LDE3OS4zMTF6IE0xNTUsMjQ2LjEwMQoJYy0xOC4yMywwLTM1LjIwNy01LjM1Ny00OS40NDktMTQuNTc5bDMwLjgwMS0zMC44MDRjNi40NDksMi43NzIsMTMuNDUsNC4yNCwyMC42NzcsNC4yNDFjMC4wMDIsMCwwLjAwMywwLDAuMDA0LDAKCWMxNC4wMTEsMCwyNy4xNzUtNS40NjYsMzcuMDY0LTE1LjM5YzEzLjUtMTMuNTM2LDE4LjU0MS0zMy4zNjMsMTMuMTU1LTUxLjc0M2MtMC4zMTMtMS4wNjktMS4xNjgtMS44OTQtMi4yNDgtMi4xNjkKCWMtMS4wNzgtMC4yNzctMi4yMjUsMC4wNC0zLjAxMSwwLjgyOWwtMzIuOTcsMzMuMDY5Yy0xLjk3OS0wLjgwNC02Ljk2MS0zLjU0Mi0xNi4xODYtMTIuNzM2CgljLTkuMjI2LTkuMTk3LTExLjk3Ni0xNC4xNzMtMTIuNzgzLTE2LjE0OGwzMi45NjYtMzMuMDY5YzAuNzg3LTAuNzg5LDEuMDk4LTEuOTM1LDAuODItMy4wMTNjLTAuMjc4LTEuMDc5LTEuMTA1LTEuOTMxLTIuMTc1LTIuMjQxCgljLTQuNzUxLTEuMzc4LTkuNjc2LTIuMDc4LTE0LjYzNy0yLjA3OGMtMTQuMDE2LDAtMjcuMTgxLDUuNDY0LTM3LjA2OSwxNS4zODVjLTkuODczLDkuOTAzLTE1LjI5OSwyMy4wNTctMTUuMjgsMzcuMDM5CgljMC4wMSw3LjIwNCwxLjQ3NiwxNC4xOCw0LjI0LDIwLjYwNGwtMzAuNzIyLDMwLjcyNEM2OS4xNDUsMTg5Ljg2OCw2My44OTYsMTczLjA0Nyw2My44OTYsMTU1CgljMC01MC4zMTMsNDAuNzg3LTkxLjEwMiw5MS4xMDQtOTEuMTAyczkxLjEwMiw0MC43ODksOTEuMTAyLDkxLjEwMlMyMDUuMzE2LDI0Ni4xMDEsMTU1LDI0Ni4xMDF6Ii8+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPg==");
        map.put("descriptionEn", "Basic Operation Plugin");
        return map;
    }
}
