/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/config/PluginExtensionInfoConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.config;

import com.gitee.starblues.core.PluginExtensionInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PluginExtensionInfoConfig
 *
 * @since 2024-8-26 16:39
 **/
@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {
    public static final String PLUGIN_ID = "container-management-plugin";

    /**
     * extensionInfo
     *
     * @return map
     */
    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo",
                "PHN2ZyB2ZXJzaW9uPSIxLjEiIGlkPSJDYXBhXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eG"
                        + "xpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4IgoJIHZpZXdCb3g9IjAgMC"
                        + "AzMTAgMzEwIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAzMTAgMzEwOyIgeG1sOnNwYWNlPSJwcm"
                        + "VzZXJ2ZSI+CjxwYXRoIGQ9Ik0zMDAuNTY0LDE3OS4zMTFMMjgyLjQsMTY4LjgyNGMwLjQ4OS00LjU0MywwLjc0Ny"
                        + "05LjE1NCwwLjc0Ny0xMy44MjRzLTAuMjU4LTkuMjgxLTAuNzQ3LTEzLjgyNGwxOC4xNjQtMTAuNDg3CgljMy44ODE"
                        + "tMi4yNDEsNi42NTgtNS44Niw3LjgxNi0xMC4xOTFjMS4xNi00LjMzLDAuNTY0LTguODU0LTEuNjc2LTEyLjczNWwt"
                        + "MzQuOTQzLTYwLjUyNGMtMi45OS01LjE4LTguNTY0LTguMzk2LTE0LjU1MS04LjM5NgoJYy0yLjkzLDAtNS44MjYsMC"
                        + "43NzgtOC4zNzcsMi4yNTFMMjMwLjYxOSw1MS42MWMtNy40MDItNS40MjktMTUuNDA2LTEwLjA4My0yMy44OTMtMTMu"
                        + "ODQyVjE2Ljc4MwoJQzIwNi43MjcsNy41MjksMTk5LjE5NSwwLDE4OS45NDUsMGgtNjkuODkxYy05LjI1NCwwLTE2Ljc"
                        + "4MSw3LjUyOS0xNi43ODEsMTYuNzgzdjIwLjk4NWMtOC40ODYsMy43NTktMTYuNDksOC40MTMtMjMuODk0LDEzLjg0M"
                        + "goJTDYxLjE2NCw0MS4wOTRjLTIuNTUxLTEuNDczLTUuNDQ1LTIuMjUtOC4zNzUtMi4yNWMtNS45ODYsMC0xMS41NjMs"
                        + "My4yMTUtMTQuNTUzLDguMzk1TDMuMjk1LDEwNy43NjIKCWMtMi4yNDIsMy44ODEtMi44MzYsOC40MDYtMS42NzQsMTI"
                        + "uNzM2YzEuMTU2LDQuMzMsMy45MzUsNy45NDksNy44MTQsMTAuMTkxTDI3LjYsMTQxLjE3NmMtMC40ODksNC41NDMt"
                        + "MC43NDcsOS4xNTQtMC43NDcsMTMuODI0CglzMC4yNTgsOS4yODEsMC43NDcsMTMuODI0TDkuNDM1LDE3OS4zMTFjLTM"
                        + "uODc5LDIuMjQxLTYuNjU4LDUuODYtNy44MTQsMTAuMTkxYy0xLjE2Miw0LjMzLTAuNTY4LDguODU1LDEuNjc0LDEyLj"
                        + "czNWwzNC45NDEsNjAuNTI0CgljMi45OSw1LjE4LDguNTY2LDguMzk1LDE0LjU1Myw4LjM5NWMyLjkzLDAsNS44MjQtM"
                        + "C43NzcsOC4zNzUtMi4yNUw3OS4zOCwyNTguMzljNy40MDMsNS40MjksMTUuNDA3LDEwLjA4MywyMy44OTQsMTMuODQyd"
                        + "jIwLjk4NgoJYzAsNC40ODIsMS43NDQsOC42OTUsNC45MTQsMTEuODY2YzMuMTc0LDMuMTY5LDcuMzg1LDQuOTE2LDExL"
                        + "jg2Nyw0LjkxNmg2OS44OTFjOS4yNSwwLDE2Ljc4MS03LjUyOSwxNi43ODEtMTYuNzgydi0yMC45ODYKCWM4LjQ4Ni0z"
                        + "Ljc1OSwxNi40OS04LjQxMywyMy44OTMtMTMuODQybDE4LjIxNSwxMC41MTdjMi41NTEsMS40NzMsNS40NDcsMi4yNSw"
                        + "4LjM3NywyLjI1YzUuOTg2LDAsMTEuNTYxLTMuMjE1LDE0LjU1MS04LjM5NQoJbDM0Ljk0My02MC41MjNjMi4yNC0zLj"
                        + "g4MSwyLjgzNi04LjQwNiwxLjY3Ni0xMi43MzZDMzA3LjIyMywxODUuMTcyLDMwNC40NDUsMTgxLjU1MywzMDAuNTY0L"
                        + "DE3OS4zMTF6IE0xNTUsMjQ2LjEwMQoJYy0xOC4yMywwLTM1LjIwNy01LjM1Ny00OS40NDktMTQuNTc5bDMwLjgwMS0z"
                        + "MC44MDRjNi40NDksMi43NzIsMTMuNDUsNC4yNCwyMC42NzcsNC4yNDFjMC4wMDIsMCwwLjAwMywwLDAuMDA0LDAKCWM"
                        + "xNC4wMTEsMCwyNy4xNzUtNS40NjYsMzcuMDY0LTE1LjM5YzEzLjUtMTMuNTM2LDE4LjU0MS0zMy4zNjMsMTMuMTU1L"
                        + "TUxLjc0M2MtMC4zMTMtMS4wNjktMS4xNjgtMS44OTQtMi4yNDgtMi4xNjkKCWMtMS4wNzgtMC4yNzctMi4yMjUsMC4wN"
                        + "C0zLjAxMSwwLjgyOWwtMzIuOTcsMzMuMDY5Yy0xLjk3OS0wLjgwNC02Ljk2MS0zLjU0Mi0xNi4xODYtMTIuNzM2Cgl"
                        + "jLTkuMjI2LTkuMTk3LTExLjk3Ni0xNC4xNzMtMTIuNzgzLTE2LjE0OGwzMi45NjYtMzMuMDY5YzAuNzg3LTAuNzg5L"
                        + "DEuMDk4LTEuOTM1LDAuODItMy4wMTNjLTAuMjc4LTEuMDc5LTEuMTA1LTEuOTMxLTIuMTc1LTIuMjQxCgljLTQuNzU"
                        + "xLTEuMzc4LTkuNjc2LTIuMDc4LTE0LjYzNy0yLjA3OGMtMTQuMDE2LDAtMjcuMTgxLDUuNDY0LTM3LjA2OSwxNS4zOD"
                        + "VjLTkuODczLDkuOTAzLTE1LjI5OSwyMy4wNTctMTUuMjgsMzcuMDM5CgljMC4wMSw3LjIwNCwxLjQ3NiwxNC4xOCw0L"
                        + "jI0LDIwLjYwNGwtMzAuNzIyLDMwLjcyNEM2OS4xNDUsMTg5Ljg2OCw2My44OTYsMTczLjA0Nyw2My44OTYsMTU1Cglj"
                        + "MC01MC4zMTMsNDAuNzg3LTkxLjEwMiw5MS4xMDQtOTEuMTAyczkxLjEwMiw0MC43ODksOTEuMTAyLDkxLjEwMlMyMDU"
                        + "uMzE2LDI0Ni4xMDEsMTU1LDI0Ni4xMDF6Ii8+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjxn"
                        + "Pgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c"
                        + "+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPg==");
        return map;
    }
}
