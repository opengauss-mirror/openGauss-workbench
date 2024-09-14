/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.config;

import com.gitee.starblues.core.PluginExtensionInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PluginExtensionInfoConfig
 *
 * @author liu
 * @since 2023-12-08
 */
@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {
    /**
     * PLUGIN_ID
     */
    public static final String PLUGIN_ID = "MetaTune";

    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo", "PHN2ZyB2ZXJzaW9uPSIxLjEiIGlkPSJDYXBhXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG"
                + "1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4IgoJIHZpZXdCb3g9IjAgMCAz"
                + "MTAgMzEwIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAzMTAgMzEwOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+Cj"
                + "xwYXRoIGQ9Ik0zMDAuNTY0LDE3OS4zMTFMMjgyLjQsMTY4LjgyNGMwLjQ4OS00LjU0MywwLjc0Ny05LjE1NCwwLjc0Ny0xMy44"
                + "MjRzLTAuMjU4LTkuMjgxLTAuNzQ3LTEzLjgyNGwxOC4xNjQtMTAuNDg3CgljMy44ODEtMi4yNDEsNi42NTgtNS44Niw3LjgxN"
                + "i0xMC4xOTFjMS4xNi00LjMzLDAuNTY0LTguODU0LTEuNjc2LTEyLjczNWwtMzQuOTQzLTYwLjUyNGMtMi45OS01LjE4LTguNT"
                + "Y0LTguMzk2LTE0LjU1MS04LjM5NgoJYy0yLjkzLDAtNS44MjYsMC43NzgtOC4zNzcsMi4yNTFMMjMwLjYxOSw1MS42MWMtNy4"
                + "0MDItNS40MjktMTUuNDA2LTEwLjA4My0yMy44OTMtMTMuODQyVjE2Ljc4MwoJQzIwNi43MjcsNy41MjksMTk5LjE5NSwwLDE4O"
                + "S45NDUsMGgtNjkuODkxYy05LjI1NCwwLTE2Ljc4MSw3LjUyOS0xNi43ODEsMTYuNzgzdjIwLjk4NWMtOC40ODYsMy43NTktMTY"
                + "uNDksOC40MTMtMjMuODk0LDEzLjg0MgoJTDYxLjE2NCw0MS4wOTRjLTIuNTUxLTEuNDczLTUuNDQ1LTIuMjUtOC4zNzUtMi4yN"
                + "WMtNS45ODYsMC0xMS41NjMsMy4yMTUtMTQuNTUzLDguMzk1TDMuMjk1LDEwNy43NjIKCWMtMi4yNDIsMy44ODEtMi44MzYsOC"
                + "40MDYtMS42NzQsMTIuNzM2YzEuMTU2LDQuMzMsMy45MzUsNy45NDksNy44MTQsMTAuMTkxTDI3LjYsMTQxLjE3NmMtMC40ODk"
                + "sNC41NDMtMC43NDcsOS4xNTQtMC43NDcsMTMuODI0CglzMC4yNTgsOS4yODEsMC43NDcsMTMuODI0TDkuNDM1LDE3OS4zMTFj"
                + "LTMuODc5LDIuMjQxLTYuNjU4LDUuODYtNy44MTQsMTAuMTkxYy0xLjE2Miw0LjMzLTAuNTY4LDguODU1LDEuNjc0LDEyLjczN"
                + "WwzNC45NDEsNjAuNTI0CgljMi45OSw1LjE4LDguNTY2LDguMzk1LDE0LjU1Myw4LjM5NWMyLjkzLDAsNS44MjQtMC43NzcsOC"
                + "4zNzUtMi4yNUw3OS4zOCwyNTguMzljNy40MDMsNS40MjksMTUuNDA3LDEwLjA4MywyMy44OTQsMTMuODQydjIwLjk4NgoJYzA"
                + "sNC40ODIsMS43NDQsOC42OTUsNC45MTQsMTEuODY2YzMuMTc0LDMuMTY5LDcuMzg1LDQuOTE2LDExLjg2Nyw0LjkxNmg2OS44"
                + "OTFjOS4yNSwwLDE2Ljc4MS03LjUyOSwxNi43ODEtMTYuNzgydi0yMC45ODYKCWM4LjQ4Ni0zLjc1OSwxNi40OS04LjQxMywyM"
                + "y44OTMtMTMuODQybDE4LjIxNSwxMC41MTdjMi41NTEsMS40NzMsNS40NDcsMi4yNSw4LjM3NywyLjI1YzUuOTg2LDAsMTEuNT"
                + "YxLTMuMjE1LDE0LjU1MS04LjM5NQoJbDM0Ljk0My02MC41MjNjMi4yNC0zLjg4MSwyLjgzNi04LjQwNiwxLjY3Ni0xMi43MzZ"
                + "DMzA3LjIyMywxODUuMTcyLDMwNC40NDUsMTgxLjU1MywzMDAuNTY0LDE3OS4zMTF6IE0xNTUsMjQ2LjEwMQoJYy0xOC4yMyww"
                + "LTM1LjIwNy01LjM1Ny00OS40NDktMTQuNTc5bDMwLjgwMS0zMC44MDRjNi40NDksMi43NzIsMTMuNDUsNC4yNCwyMC42NzcsNC"
                + "4yNDFjMC4wMDIsMCwwLjAwMywwLDAuMDA0LDAKCWMxNC4wMTEsMCwyNy4xNzUtNS40NjYsMzcuMDY0LTE1LjM5YzEzLjUtMTMu"
                + "NTM2LDE4LjU0MS0zMy4zNjMsMTMuMTU1LTUxLjc0M2MtMC4zMTMtMS4wNjktMS4xNjgtMS44OTQtMi4yNDgtMi4xNjkKCWMtMS"
                + "4wNzgtMC4yNzctMi4yMjUsMC4wNC0zLjAxMSwwLjgyOWwtMzIuOTcsMzMuMDY5Yy0xLjk3OS0wLjgwNC02Ljk2MS0zLjU0Mi0x"
                + "Ni4xODYtMTIuNzM2CgljLTkuMjI2LTkuMTk3LTExLjk3Ni0xNC4xNzMtMTIuNzgzLTE2LjE0OGwzMi45NjYtMzMuMDY5YzAuNzg"
                + "3LTAuNzg5LDEuMDk4LTEuOTM1LDAuODItMy4wMTNjLTAuMjc4LTEuMDc5LTEuMTA1LTEuOTMxLTIuMTc1LTIuMjQxCgljLTQuNz"
                + "UxLTEuMzc4LTkuNjc2LTIuMDc4LTE0LjYzNy0yLjA3OGMtMTQuMDE2LDAtMjcuMTgxLDUuNDY0LTM3LjA2OSwxNS4zODVjLTkuO"
                + "DczLDkuOTAzLTE1LjI5OSwyMy4wNTctMTUuMjgsMzcuMDM5CgljMC4wMSw3LjIwNCwxLjQ3NiwxNC4xOCw0LjI0LDIwLjYwNGw"
                + "tMzAuNzIyLDMwLjcyNEM2OS4xNDUsMTg5Ljg2OCw2My44OTYsMTczLjA0Nyw2My44OTYsMTU1CgljMC01MC4zMTMsNDAuNzg3LT"
                + "kxLjEwMiw5MS4xMDQtOTEuMTAyczkxLjEwMiw0MC43ODksOTEuMTAyLDkxLjEwMlMyMDUuMzE2LDI0Ni4xMDEsMTU1LDI0Ni4xM"
                + "DF6Ii8+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo"
                + "8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3Z"
                + "nPg==");
        map.put("descriptionEn",
                "The database intelligent parameter tuning tool mainly includes the following functions: "
                        + "(1) load characteristics analysis, "
                        + "(2) database schema extraction, "
                        + "(3) load generation, "
                        + "(4) offline tuning, "
                        + "(5) online tuning, and "
                        + "(6) online fine-tuning.");
        return map;
    }
}
