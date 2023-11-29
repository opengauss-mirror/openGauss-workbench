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

package org.opengauss.admin.plugin.constants;

/**
 * tools param log start end stringindex
 *
 * @author: www
 * @date: 2023/11/28 11:57
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
public interface ToolsParamsLog {
    /**
     * The constant chameleon_load_config_start.
     */
    String CHAMELEON_LOAD_CONFIG = "chameleon_load_config";

    /**
     * The constant data_check_application_load_config_start.
     */
    String DATA_CHECK_APPLICATION_LOAD_CONFIG = "data_check_application_load_config";

    /**
     * The constant data_check_application_sink_load_config_start.
     */
    String DATA_CHECK_APPLICATION_SINK_LOAD_CONFIG = "data_check_application_sink_load_config";

    /**
     * The constant data_check_application__source_load_config_start.
     */
    String DATA_CHECK_APPLICATION__SOURCE_LOAD_CONFIG = "data_check_application_source_load_config";

    /**
     * The constant debezium_mysql_sink_load_config_start.
     */
    String DEBEZIUM_MYSQL_SINK_LOAD_CONFIG = "debezium_mysql_sink_load_config";

    /**
     * The constant debezium_mysql_source_load_config_start.
     */
    String DEBEZIUM_MYSQL_SOURCE_LOAD_CONFIG = "debezium_mysql_source_load_config";

    /**
     * The constant debezium_opengauss_sink_load_config_start.
     */

    String DEBEZIUM_OPENGAUSS_SINK_LOAD_CONFIG = "debezium_opengauss_sink_load_config";

    /**
     * The constant debezium_opengauss_source_load_config_start.
     */
    String DEBEZIUM_OPENGAUSS_SOURCE_LOAD_CONFIG = "debezium_opengauss_source_load_config";

    /**
     * The constant portal_migration_load_config_start.
     */
    String PORTAL_MIGRATION = "portal_migration_load_config";

    /**
     * The constant new.
     */
    String NEW_PARAM_PREFIX = "new.";

    /**
     * The constant new.
     */
    String START = "start";

    /**
     * The constant new.
     */
    String END = "end";
}
