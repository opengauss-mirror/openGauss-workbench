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
 * openGauss-visualtool-plugin/src/main/java/org/opengauss/admin/plugin/config/PluginExtensionInfoConfig.java
 *
 * -------------------------------------------------------------------------
 */

package com.tools.monitor.config;

import com.gitee.starblues.core.PluginExtensionInfo;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;


@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {

    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo", "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0" +
                "JveD0iMCAwIDI0IDI0Ij4NCiAgPGcgaWQ9Iue7hF8xMDIiIGRhdGEtbmFtZT0i57uEIDEwMiIgdHJhbnNmb3JtPSJ0cmFuc2xh" +
                "dGUoLTEyOSAtNzgpIj4NCiAgICA8cmVjdCBpZD0i55+p5b2iIiB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHRyYW5zZm9ybT0idH" +
                "JhbnNsYXRlKDEyOSA3OCkiIGZpbGw9IiNkOGQ4ZDgiIG9wYWNpdHk9IjAiLz4NCiAgICA8cGF0aCBpZD0i6IGU5ZCIXzEwIiBk" +
                "YXRhLW5hbWU9IuiBlOWQiCAxMCIgZD0iTS0zOTQzLjk2MS0xNDc3LjUybDYuMDYzLjAyMmEuNzUuNzUsMCwwLDEsLjc0Ny43NT" +
                "MuNzUxLjc1MSwwLDAsMS0uNjUxLjc0MWwtLjEuMDA2LTYuMDYzLS4wMjJhLjc1Ljc1LDAsMCwxLS43NDctLjc1My43NS43NSww" +
                "LDAsMSwuNjUxLS43NDFaTS0zOTMxLTE0OTJhMywzLDAsMCwxLDIuOTk1LDIuODI0bC4wMDUuMTc2LS4wMDUsMy41NzNhLjc1Lj" +
                "c1LDAsMCwxLS43NS43NS43NS43NSwwLDAsMS0uNzQzLS42NDhsLS4wMDctLjEuMDA1LTMuNTczYTEuNSwxLjUsMCwwLDAtMS4z" +
                "NTUtMS40OTNsLS4xNDUtLjAwN2gtMTRhMS41LDEuNSwwLDAsMC0xLjQ5MywxLjM1NmwtLjAwNy4xNDR2Mi43NWg1YS43NS43NS" +
                "wwLDAsMSwuNjI4LjM0MWwuMDUyLjA5NS44MjMsMS43ODUsMi4zNi01LjAzOWEuNzUuNzUsMCwwLDEsMS4yODMtLjEyN2wuMDYu" +
                "MSwxLjAxOSwxLjkzOWEuNzUuNzUsMCwwLDEtLjMxNSwxLjAxMy43NS43NSwwLDAsMS0uOTU5LS4yMjhsLS4wNTMtLjA4Ny0uMz" +
                "E2LS42LTIuNCw1LjEzMmEuNzUuNzUsMCwwLDEtMS4zLjFsLS4wNTYtLjEtMS4zLTIuODE0aC00LjUydjIuNzVhMS41LDEuNSwwL" +
                "DAsMCwxLjM1NSwxLjQ5M2wuMTQ1LjAwN2g1Ljc1YS43NS43NSwwLDAsMSwuNzUuNzUuNzUuNzUsMCwwLDEtLjY0OC43NDNsLS4x" +
                "LjAwN0gtMzk0NWEzLDMsMCwwLDEtMi45OTUtMi44MjRMLTM5NDgtMTQ4MnYtN2EzLDMsMCwwLDEsMi44MjQtMi45OTVsLjE3Ni0" +
                "uMDA1WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoNDA3OC40ODYgMTU3My45OTkpIiBmaWxsPSIjMTkxOTE5IiBmaWxsLXJ1bGU9ImV" +
                "2ZW5vZGQiLz4NCiAgICA8cGF0aCBpZD0i5b2i54q257uT5ZCIIiBkPSJNMTAuODUyLDYuMDQzYzEuNDE1LDAsMi4xNDYuODE0LDI" +
                "uMTU2LDEuODQzdjQuMTcxQTEuOTU1LDEuOTU1LDAsMCwxLDExLjA3OSwxNGwtNC4xMjItLjA0M2MtLjk4NCwwLTIuMTEtLjUxMy" +
                "0yLjEzLTEuNTQzdi0uOEgzLjc2OWEuNTg2LjU4NiwwLDEsMSwwLTEuMTY3SDQuODI3VjkuN0gzLjc2OWEuNTg2LjU4NiwwLDEsM" +
                "SwwLTEuMTY3SDQuODI3VjcuNzE2QzQuODMyLDYuNjU5LDUuNTMxLDUuOTg2LDYuOCw2Wk0xMC41MzUsNy4yMSw2Ljg5NCw3LjE" +
                "2N2MtLjQ4NSwwLS43ODEuMi0uNzg2LjY3NnY0LjE3MWMuMDA1LjQ1Ny4zNDkuNzcxLjkzMi43NzZsMy45MDYuMDQzYS44NzUuO" +
                "Dc1LDAsMCwwLC44Ny0uOVY4LjAxNGMuMDA1LS41MjktLjQtLjgxNC0uOTYzLS44Wk03Ljg5Miw5LjgyYS42MTQuNjE0LDAsMCw" +
                "xLC42NDEuNTg0di45NTlhLjY0My42NDMsMCwwLDEtMS4yODEsMFYxMC40QS42MTQuNjE0LDAsMCwxLDcuODkyLDkuODJaTTkuMD" +
                "g2LDguMTcxYS41ODYuNTg2LDAsMSwxLDAsMS4xNjdINy40MjVhLjU4Ni41ODYsMCwxLDEsMC0xLjE2N1oiIHRyYW5zZm9ybT0id" +
                "HJhbnNsYXRlKDEzNy40MTggODQpIiBmaWxsPSIjMTkxOTE5IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiLz4NCiAgPC9nPg0KPC9zdmc" +
                "+DQo=");
        return map;
    }
}
