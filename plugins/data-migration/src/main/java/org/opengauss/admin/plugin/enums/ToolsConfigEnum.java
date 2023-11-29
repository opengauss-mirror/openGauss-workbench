/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opengauss.admin.plugin.constants.ToolsParamsLog;

/**
 * ToolsConfigEnum
 *
 * @author: www
 * @date: 2023/11/28 15:18
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@AllArgsConstructor
@Getter
public enum ToolsConfigEnum {
    CHAMELEON_CONFIG(1, "config.yml",
            ToolsParamsLog.CHAMELEON_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.CHAMELEON_LOAD_CONFIG + ToolsParamsLog.END),
    DATA_CHECK_APPLICATION(2, "application.yml",
            ToolsParamsLog.DATA_CHECK_APPLICATION_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DATA_CHECK_APPLICATION_LOAD_CONFIG + ToolsParamsLog.END),
    DATA_CHECK_APPLICATION_SINK(3, "application-sink.yml",
            ToolsParamsLog.DATA_CHECK_APPLICATION_SINK_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DATA_CHECK_APPLICATION_SINK_LOAD_CONFIG + ToolsParamsLog.END),
    DATA_CHECK_APPLICATION_SOURCE(4, "application-source.yml",
            ToolsParamsLog.DATA_CHECK_APPLICATION__SOURCE_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DATA_CHECK_APPLICATION__SOURCE_LOAD_CONFIG + ToolsParamsLog.END),
    DEBEZIUM_MYSQL_SINK(5, "mysql-sink.properties",
            ToolsParamsLog.DEBEZIUM_MYSQL_SINK_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DEBEZIUM_MYSQL_SINK_LOAD_CONFIG + ToolsParamsLog.END),
    DEBEZIUM_MYSQL_SOURCE(6, "mysql-source.properties",
            ToolsParamsLog.DEBEZIUM_MYSQL_SOURCE_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DEBEZIUM_MYSQL_SOURCE_LOAD_CONFIG + ToolsParamsLog.END),
    DEBEZIUM_OPENGAUSS_SINK(7, "opengauss-sink.properties",
            ToolsParamsLog.DEBEZIUM_OPENGAUSS_SINK_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DEBEZIUM_OPENGAUSS_SINK_LOAD_CONFIG + ToolsParamsLog.END),
    DEBEZIUM_OPENGAUSS_SOURCE(8, "opengauss-source.properties",
            ToolsParamsLog.DEBEZIUM_OPENGAUSS_SOURCE_LOAD_CONFIG + ToolsParamsLog.START,
            ToolsParamsLog.DEBEZIUM_OPENGAUSS_SOURCE_LOAD_CONFIG + ToolsParamsLog.END),
    PORTAL_MIGRATION(9, "migrationConfig.properties",
            ToolsParamsLog.PORTAL_MIGRATION + ToolsParamsLog.START,
            ToolsParamsLog.PORTAL_MIGRATION + ToolsParamsLog.END);
    private Integer type;
    private String configName;
    private String startFromLog;
    private String endStrFromLog;
}
