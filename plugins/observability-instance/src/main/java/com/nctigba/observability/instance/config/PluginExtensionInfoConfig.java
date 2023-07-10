/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.config;

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
                        + "d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyLjU5MjggMTMuMTg5MUMxMi45NTYxIDEzLjE4OTEgMTMuMj"
                        + "A3IDEzLjQzOTQgMTMuMjA3IDEzLjgwMTJDMTMuMjA3IDE0LjE0NyAxMi45Nzc0IDE0LjM5MTQgMTIuNjM5NiAxNC40"
                        + "MTE5TDEyLjU5MjggMTQuNDEzNEgzLjYxOTE4QzMuMjU2NTEgMTQuNDEzNCAzLjAwNDk2IDE0LjE2MzMgMy4wMDQ5Ni"
                        + "AxMy44MDEyQzMuMDA0OTYgMTMuNDU1MSAzLjIzNDU5IDEzLjIxMSAzLjU3MjM2IDEzLjE5MDNMMy42MTkxOCAxMy4x"
                        + "ODkxSDEyLjU5MjhaTTE0LjExNTggMi4xOTI2M0wxNC4xNjcgMi4xOTQxMUMxNC4zODk2IDIuMjA2NTkgMTQuNTk5ID"
                        + "IuMzAzNjkgMTQuNzUyMyAyLjQ2NTUzQzE0LjkwNTYgMi42MjczNyAxNC45OTEyIDIuODQxNyAxNC45OTE2IDMuMDY0"
                        + "NjNWMTEuMjgxOEwxNC45OTAxIDExLjMzMzFDMTQuOTc2MyAxMS41NTU1IDE0Ljg3ODEgMTEuNzY0MiAxNC43MTU2ID"
                        + "ExLjkxNjdDMTQuNTUzMSAxMi4wNjkyIDE0LjMzODYgMTIuMTU0IDE0LjExNTggMTIuMTUzOEgyLjE3ODU5TDIuMTI3"
                        + "MzMgMTIuMTUyM0MxLjkwNDg1IDEyLjEzOTYgMS42OTU2NCAxMi4wNDI0IDEuNTQyMzkgMTEuODgwNkMxLjM4OTE0ID"
                        + "ExLjcxODggMS4zMDM0MyAxMS41MDQ3IDEuMzAyNzMgMTEuMjgxOFYzLjA2NDYzTDEuMzA0MjIgMy4wMTMzN0MxLjMx"
                        + "ODEgMi43OTA5NyAxLjQxNjI4IDIuNTgyMjMgMS41Nzg3NSAyLjQyOTcyQzEuNzQxMjMgMi4yNzcyMiAxLjk1NTc1ID"
                        + "IuMTkyNDIgMi4xNzg1OSAyLjE5MjYzSDE0LjExNThaTTEzLjk5NyAzLjE4Mjg1SDIuMjk3MVYxMS4xNjNIMTMuOTk3"
                        + "VjMuMTgyODVaIiBmaWxsPSIjNEU1OTY5Ii8+CjxwYXRoIGQ9Ik0xMS4xMDc4IDYuNjc1NjFMMTAuNTIwMyA2LjYwND"
                        + "VDMTAuNDc1NyA2LjQ0NjY0IDEwLjQxMzggNi4yOTQyIDEwLjMzNTcgNi4xNDk5OEwxMC43MDAxIDUuNjg3MTdDMTAu"
                        + "Nzg3OCA1LjU3NTE3IDEwLjc2NTMgNS4zOTk3NiAxMC42NTI3IDUuMjg3NzZMMTAuMjcyIDQuOTEwMjhDMTAuMTU4OC"
                        + "A0Ljc5Nzk4IDkuOTgyMjEgNC43NzYwNiA5Ljg2OTAyIDQuODYyODdMOS40MTU2OSA1LjIxNDg3QzkuMjY2NzIgNS4x"
                        + "MzE3NiA5LjEwOTE1IDUuMDY1MSA4Ljk0NTc2IDUuMDE2MDZMOC44NzU4NCA0LjQ0ODk0QzguODU4MDYgNC4zMDg1ID"
                        + "guNzE2NDMgNC4xOTk0NiA4LjU1NzMyIDQuMTk5NDZIOC4wMTkyNUM3Ljg2MDE0IDQuMTk5NDYgNy43MTg1MSA0LjMw"
                        + "ODUgNy43MDA3MyA0LjQ0ODk0TDcuNjMyNTggNS4wMDMzMUM3LjQ2MzY5IDUuMDUwNzIgNy4zMDEzMiA1LjExNTYxID"
                        + "cuMTQ2NjUgNS4xOTkxN0w2LjcwMTAyIDQuODU0ODdDNi41ODgxMyA0Ljc2ODA2IDYuNDExMjUgNC43OTAyOCA2LjI5"
                        + "ODA2IDQuOTAyMjhMNS45MTc5MSA1LjI3OTc2QzUuODA0NzMgNS4zOTE3NiA1Ljc4MjUxIDUuNTY3MTcgNS44Njk5MS"
                        + "A1LjY3OTE3TDYuMjE1MzkgNi4xMTY4QzYuMTMwMzYgNi4yNjg1OCA2LjA2MzA2IDYuNDI5NjQgNi4wMTQ4IDYuNTk2"
                        + "OEw1LjQ1MzAyIDYuNjY0OTRDNS4zMTExIDYuNjgyMTMgNS4yMDExNyA2LjgyMjU3IDUuMjAxMTcgNi45ODA1VjcuNT"
                        + "E0NDNDNS4yMDExNyA3LjY3MjM1IDUuMzExMSA3LjgxMjggNS40NTMwMiA3LjgzMDI4TDYuMDE5NTQgNy44OTk5MUM2"
                        + "LjA2NzU0IDguMDYyNTcgNi4xMzQyMSA4LjIxODcyIDYuMjE3MTcgOC4zNjcxN0w1Ljg2MDQzIDguODIwNUM1Ljc3Mz"
                        + "AyIDguOTMyOCA1Ljc5NTI1IDkuMTA3OTEgNS45MDgxMyA5LjIyMDJMNi4yODg4OCA5LjU5NzY4QzYuNDAxNzYgOS43"
                        + "MDk2OCA2LjU3ODM2IDkuNzMxOTEgNi42OTE1NCA5LjY0NTA5TDcuMTU0NjUgOS4yODY1N0M3LjI5OTg0IDkuMzYzNj"
                        + "EgNy40NTM5MSA5LjQyNzAyIDcuNjEzMzIgOS40NzI2NUw3LjY4NjUxIDEwLjA2MDJDNy43MDQyOCAxMC4yMDA5IDcu"
                        + "ODQ1NjIgMTAuMzA5NyA4LjAwNTAyIDEwLjMwOTdIOC41NDMxQzguNzAyMjEgMTAuMzA5NyA4Ljg0Mzg0IDEwLjIwMD"
                        + "kgOC44NjEzMiAxMC4wNjAyTDguOTM0OCA5LjQ2NzkxQzkuMDg5MTUgOS40MjIwOSA5LjIzODQ2IDkuMzYwNzQgOS4z"
                        + "ODA0MyA5LjI4NDhMOS44NTk1NCA5LjY1NjA2QzkuOTcyNzMgOS43NDI4NyAxMC4xNDkzIDkuNzIwNjUgMTAuMjYyNS"
                        + "A5LjYwODY1TDEwLjY0MyA5LjIzMTE3QzEwLjc1NTggOS4xMTkxNyAxMC43Nzg0IDguOTQzNzYgMTAuNjkwNyA4Ljgz"
                        + "MTQ2TDEwLjMxOTcgOC4zNTk0NkMxMC4zOTc2IDguMjE4NzIgMTAuNDYxMyA4LjA3MDI4IDEwLjUwNzUgNy45MTcwOU"
                        + "wxMS4xMDkzIDcuODQ0NUMxMS4yNTA5IDcuODI3MDIgMTEuMzYxMiA3LjY4NjU3IDExLjM2MTIgNy41Mjg2NVY2Ljk5"
                        + "NDcyQzExLjM1OTQgNi44MzM4MyAxMS4yNDk1IDYuNjkzMDkgMTEuMTA3OCA2LjY3NTkxVjYuNjc1NjFaTTguMjgyMD"
                        + "YgOC40OTg0M0M3LjU4Nzg0IDguNDk4NDMgNy4wMjQyOCA3Ljk0MDggNy4wMjQyOCA3LjI1MDcyQzcuMDI0MjggNi41"
                        + "NjAzNSA3LjU4Nzg0IDYuMDAzMzEgOC4yODIwNiA2LjAwMzMxQzguOTc1OTkgNi4wMDMzMSA5LjUzOTU0IDYuNTYwMz"
                        + "UgOS41Mzk1NCA3LjI1MDcyQzkuNTM5NTQgNy45NDEwOSA4Ljk3NTk5IDguNDk4MTMgOC4yODIwNiA4LjQ5ODEzVjgu"
                        + "NDk4NDNaIiBmaWxsPSIjNEU1OTY5Ii8+Cjwvc3ZnPgo=");
        map.put("theme", "light");
        map.put("pluginType", 1);
        map.put("isNeedConfigured", 0);
        map.put("configAttrs",
                "[{\"attrCode\":\"esHost\",\"attrLabel\":\"ES服务器\"},{\"attrCode\":\"esPort\",\"attrLabel\":\"端口\"}]");
        return map;
    }
}