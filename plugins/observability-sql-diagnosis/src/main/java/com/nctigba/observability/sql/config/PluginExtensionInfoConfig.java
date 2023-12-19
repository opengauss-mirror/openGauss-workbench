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
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/config/PluginExtensionInfoConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gitee.starblues.core.PluginExtensionInfo;

@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {
    @Override
    public Map<String, Object> extensionInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("logo",
            "PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93"
                + "d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyLjU5MjggMTMuMTg5MUMxMi45NTYxIDEzLjE4OTEgMTMuMjA3ID"
                + "EzLjQzOTQgMTMuMjA3IDEzLjgwMTJDMTMuMjA3IDE0LjE0NyAxMi45Nzc0IDE0LjM5MTQgMTIuNjM5NiAxNC40MTE5TDEy"
                + "LjU5MjggMTQuNDEzNEgzLjYxOTE4QzMuMjU2NTEgMTQuNDEzNCAzLjAwNDk2IDE0LjE2MzMgMy4wMDQ5NiAxMy44MDEyQz"
                + "MuMDA0OTYgMTMuNDU1MSAzLjIzNDU5IDEzLjIxMSAzLjU3MjM2IDEzLjE5MDNMMy42MTkxOCAxMy4xODkxSDEyLjU5Mjha"
                + "TTE0LjExNTggMi4xOTI2M0wxNC4xNjcgMi4xOTQxMUMxNC4zODk2IDIuMjA2NTkgMTQuNTk5IDIuMzAzNjkgMTQuNzUyMy"
                + "AyLjQ2NTUzQzE0LjkwNTYgMi42MjczNyAxNC45OTEyIDIuODQxNyAxNC45OTE2IDMuMDY0NjNWMTEuMjgxOEwxNC45OTAx"
                + "IDExLjMzMzFDMTQuOTc2MyAxMS41NTU1IDE0Ljg3ODEgMTEuNzY0MiAxNC43MTU2IDExLjkxNjdDMTQuNTUzMSAxMi4wNj"
                + "kyIDE0LjMzODYgMTIuMTU0IDE0LjExNTggMTIuMTUzOEgyLjE3ODU5TDIuMTI3MzMgMTIuMTUyM0MxLjkwNDg1IDEyLjEz"
                + "OTYgMS42OTU2NCAxMi4wNDI0IDEuNTQyMzkgMTEuODgwNkMxLjM4OTE0IDExLjcxODggMS4zMDM0MyAxMS41MDQ3IDEuMz"
                + "AyNzMgMTEuMjgxOFYzLjA2NDYzTDEuMzA0MjIgMy4wMTMzN0MxLjMxODEgMi43OTA5NyAxLjQxNjI4IDIuNTgyMjMgMS41"
                + "Nzg3NSAyLjQyOTcyQzEuNzQxMjMgMi4yNzcyMiAxLjk1NTc1IDIuMTkyNDIgMi4xNzg1OSAyLjE5MjYzSDE0LjExNThaTT"
                + "EzLjk5NyAzLjE4Mjg1SDIuMjk3MVYxMS4xNjNIMTMuOTk3VjMuMTgyODVaIiBmaWxsPSIjNEU1OTY5Ii8+CjxwYXRoIGQ9"
                + "Ik0xMS4xMDc4IDYuNjc1NjFMMTAuNTIwMyA2LjYwNDVDMTAuNDc1NyA2LjQ0NjY0IDEwLjQxMzggNi4yOTQyIDEwLjMzNT"
                + "cgNi4xNDk5OEwxMC43MDAxIDUuNjg3MTdDMTAuNzg3OCA1LjU3NTE3IDEwLjc2NTMgNS4zOTk3NiAxMC42NTI3IDUuMjg3"
                + "NzZMMTAuMjcyIDQuOTEwMjhDMTAuMTU4OCA0Ljc5Nzk4IDkuOTgyMjEgNC43NzYwNiA5Ljg2OTAyIDQuODYyODdMOS40MT"
                + "U2OSA1LjIxNDg3QzkuMjY2NzIgNS4xMzE3NiA5LjEwOTE1IDUuMDY1MSA4Ljk0NTc2IDUuMDE2MDZMOC44NzU4NCA0LjQ0"
                + "ODk0QzguODU4MDYgNC4zMDg1IDguNzE2NDMgNC4xOTk0NiA4LjU1NzMyIDQuMTk5NDZIOC4wMTkyNUM3Ljg2MDE0IDQuMT"
                + "k5NDYgNy43MTg1MSA0LjMwODUgNy43MDA3MyA0LjQ0ODk0TDcuNjMyNTggNS4wMDMzMUM3LjQ2MzY5IDUuMDUwNzIgNy4z"
                + "MDEzMiA1LjExNTYxIDcuMTQ2NjUgNS4xOTkxN0w2LjcwMTAyIDQuODU0ODdDNi41ODgxMyA0Ljc2ODA2IDYuNDExMjUgNC"
                + "43OTAyOCA2LjI5ODA2IDQuOTAyMjhMNS45MTc5MSA1LjI3OTc2QzUuODA0NzMgNS4zOTE3NiA1Ljc4MjUxIDUuNTY3MTcg"
                + "NS44Njk5MSA1LjY3OTE3TDYuMjE1MzkgNi4xMTY4QzYuMTMwMzYgNi4yNjg1OCA2LjA2MzA2IDYuNDI5NjQgNi4wMTQ4ID"
                + "YuNTk2OEw1LjQ1MzAyIDYuNjY0OTRDNS4zMTExIDYuNjgyMTMgNS4yMDExNyA2LjgyMjU3IDUuMjAxMTcgNi45ODA1Vjcu"
                + "NTE0NDNDNS4yMDExNyA3LjY3MjM1IDUuMzExMSA3LjgxMjggNS40NTMwMiA3LjgzMDI4TDYuMDE5NTQgNy44OTk5MUM2Lj"
                + "A2NzU0IDguMDYyNTcgNi4xMzQyMSA4LjIxODcyIDYuMjE3MTcgOC4zNjcxN0w1Ljg2MDQzIDguODIwNUM1Ljc3MzAyIDgu"
                + "OTMyOCA1Ljc5NTI1IDkuMTA3OTEgNS45MDgxMyA5LjIyMDJMNi4yODg4OCA5LjU5NzY4QzYuNDAxNzYgOS43MDk2OCA2Lj"
                + "U3ODM2IDkuNzMxOTEgNi42OTE1NCA5LjY0NTA5TDcuMTU0NjUgOS4yODY1N0M3LjI5OTg0IDkuMzYzNjEgNy40NTM5MSA5"
                + "LjQyNzAyIDcuNjEzMzIgOS40NzI2NUw3LjY4NjUxIDEwLjA2MDJDNy43MDQyOCAxMC4yMDA5IDcuODQ1NjIgMTAuMzA5Ny"
                + "A4LjAwNTAyIDEwLjMwOTdIOC41NDMxQzguNzAyMjEgMTAuMzA5NyA4Ljg0Mzg0IDEwLjIwMDkgOC44NjEzMiAxMC4wNjAy"
                + "TDguOTM0OCA5LjQ2NzkxQzkuMDg5MTUgOS40MjIwOSA5LjIzODQ2IDkuMzYwNzQgOS4zODA0MyA5LjI4NDhMOS44NTk1NC"
                + "A5LjY1NjA2QzkuOTcyNzMgOS43NDI4NyAxMC4xNDkzIDkuNzIwNjUgMTAuMjYyNSA5LjYwODY1TDEwLjY0MyA5LjIzMTE3"
                + "QzEwLjc1NTggOS4xMTkxNyAxMC43Nzg0IDguOTQzNzYgMTAuNjkwNyA4LjgzMTQ2TDEwLjMxOTcgOC4zNTk0NkMxMC4zOT"
                + "c2IDguMjE4NzIgMTAuNDYxMyA4LjA3MDI4IDEwLjUwNzUgNy45MTcwOUwxMS4xMDkzIDcuODQ0NUMxMS4yNTA5IDcuODI3"
                + "MDIgMTEuMzYxMiA3LjY4NjU3IDExLjM2MTIgNy41Mjg2NVY2Ljk5NDcyQzExLjM1OTQgNi44MzM4MyAxMS4yNDk1IDYuNj"
                + "kzMDkgMTEuMTA3OCA2LjY3NTkxVjYuNjc1NjFaTTguMjgyMDYgOC40OTg0M0M3LjU4Nzg0IDguNDk4NDMgNy4wMjQyOCA3"
                + "Ljk0MDggNy4wMjQyOCA3LjI1MDcyQzcuMDI0MjggNi41NjAzNSA3LjU4Nzg0IDYuMDAzMzEgOC4yODIwNiA2LjAwMzMxQz"
                + "guOTc1OTkgNi4wMDMzMSA5LjUzOTU0IDYuNTYwMzUgOS41Mzk1NCA3LjI1MDcyQzkuNTM5NTQgNy45NDEwOSA4Ljk3NTk5"
                + "IDguNDk4MTMgOC4yODIwNiA4LjQ5ODEzVjguNDk4NDNaIiBmaWxsPSIjNEU1OTY5Ii8+Cjwvc3ZnPgo=");
        map.put("theme", "light");
        map.put("pluginType", 1);
        map.put("isNeedConfigured", 0);
        map.put("configAttrs",
            "[{\"attrCode\":\"esHost\",\"attrLabel\":\"ES服务器\"},{\"attrCode\":\"esPort\",\"attrLabel\":\"端口\"}]");
        return map;
    }
}