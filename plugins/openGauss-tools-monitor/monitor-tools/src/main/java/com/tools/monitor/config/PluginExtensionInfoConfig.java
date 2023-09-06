/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.config;

import com.gitee.starblues.core.PluginExtensionInfo;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * PluginExtensionInfoConfig
 *
 * @author liu
 * @since 2023-08-31
 */
@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {
    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo",
                "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAw"
                        + "IDI0IDI0Ij4NCiAgPGcgaWQ9Iue7hF8xMDIiIGRhdGEtbmFtZT0i57uEIDEwMiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGU"
                        + "oLTEyOSAtNzgpIj4NCiAgICA8cmVjdCBpZD0i55+p5b2iIiB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHRyYW5zZm9y"
                        + "bT0idHJhbnNsYXRlKDEyOSA3OCkiIGZpbGw9IiNkOGQ4ZDgiIG9wYWNpdHk9IjAiLz4NCiAgICA8cGF0aCBpZD0i6"
                        + "IGU5ZCIXzEwIiBkYXRhLW5hbWU9IuiBlOWQiCAxMCIgZD0iTS0zOTQzLjk2MS0xNDc3LjUybDYuMDYzLjAyMmEuNzUu"
                        + "NzUsMCwwLDEsLjc0Ny43NTMuNzUxLjc1MSwwLDAsMS0uNjUxLjc0MWwtLjEuMDA2LTYuMDYzLS4wMjJhLjc1Ljc1LDA"
                        + "sMCwxLS43NDctLjc1My43NS43NSwwLDAsMSwuNjUxLS43NDFaTS0zOTMxLTE0OTJhMywzLDAsMCwxLDIuOTk1LDIuOD"
                        + "I0bC4wMDUuMTc2LS4wMDUsMy41NzNhLjc1Ljc1LDAsMCwxLS43NS43NS43NS43NSwwLDAsMS0uNzQzLS42NDhsLS4wM"
                        + "DctLjEuMDA1LTMuNTczYTEuNSwxLjUsMCwwLDAtMS4zNTUtMS40OTNsLS4xNDUtLjAwN2gtMTRhMS41LDEuNSwwLDAs"
                        + "MC0xLjQ5MywxLjM1NmwtLjAwNy4xNDR2Mi43NWg1YS43NS43NSwwLDAsMSwuNjI4LjM0MWwuMDUyLjA5NS44MjMsMS"
                        + "43ODUsMi4zNi01LjAzOWEuNzUuNzUsMCwwLDEsMS4yODMtLjEyN2wuMDYuMSwxLjAxOSwxLjkzOWEuNzUuNzUsMCwwL"
                        + "DEtLjMxNSwxLjAxMy43NS43NSwwLDAsMS0uOTU5LS4yMjhsLS4wNTMtLjA4Ny0uMzE2LS42LTIuNCw1LjEzMmEuNzUu"
                        + "NzUsMCwwLDEtMS4zLjFsLS4wNTYtLjEtMS4zLTIuODE0aC00LjUydjIuNzVhMS41LDEuNSwwLDAsMCwxLjM1NSwxLjQ"
                        + "5M2wuMTQ1LjAwN2g1Ljc1YS43NS43NSwwLDAsMSwuNzUuNzUuNzUuNzUsMCwwLDEtLjY0OC43NDNsLS4xLjAwN0gtMz"
                        + "k0NWEzLDMsMCwwLDEtMi45OTUtMi44MjRMLTM5NDgtMTQ4MnYtN2EzLDMsMCwwLDEsMi44MjQtMi45OTVsLjE3Ni0uM"
                        + "DA1WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoNDA3OC40ODYgMTU3My45OTkpIiBmaWxsPSIjMTkxOTE5IiBmaWxsLXJ1"
                        + "bGU9ImV2ZW5vZGQiLz4NCiAgICA8cGF0aCBpZD0i5b2i54q257uT5ZCIIiBkPSJNMTAuODUyLDYuMDQzYzEuNDE1LDA"
                        + "sMi4xNDYuODE0LDIuMTU2LDEuODQzdjQuMTcxQTEuOTU1LDEuOTU1LDAsMCwxLDExLjA3OSwxNGwtNC4xMjItLjA0M2"
                        + "MtLjk4NCwwLTIuMTEtLjUxMy0yLjEzLTEuNTQzdi0uOEgzLjc2OWEuNTg2LjU4NiwwLDEsMSwwLTEuMTY3SDQuODI3V"
                        + "jkuN0gzLjc2OWEuNTg2LjU4NiwwLDEsMSwwLTEuMTY3SDQuODI3VjcuNzE2QzQuODMyLDYuNjU5LDUuNTMxLDUuOTg2"
                        + "LDYuOCw2Wk0xMC41MzUsNy4yMSw2Ljg5NCw3LjE2N2MtLjQ4NSwwLS43ODEuMi0uNzg2LjY3NnY0LjE3MWMuMDA1LjQ"
                        + "1Ny4zNDkuNzcxLjkzMi43NzZsMy45MDYuMDQzYS44NzUuODc1LDAsMCwwLC44Ny0uOVY4LjAxNGMuMDA1LS41MjktLj"
                        + "QtLjgxNC0uOTYzLS44Wk03Ljg5Miw5LjgyYS42MTQuNjE0LDAsMCwxLC42NDEuNTg0di45NTlhLjY0My42NDMsMCwwL"
                        + "DEtMS4yODEsMFYxMC40QS42MTQuNjE0LDAsMCwxLDcuODkyLDkuODJaTTkuMDg2LDguMTcxYS41ODYuNTg2LDAsMSwx"
                        + "LDAsMS4xNjdINy40MjVhLjU4Ni41ODYsMCwxLDEsMC0xLjE2N1oiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDEzNy40MTg"
                        + "gODQpIiBmaWxsPSIjMTkxOTE5IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiLz4NCiAgPC9nPg0KPC9zdmc+DQo=");
        return map;
    }
}
