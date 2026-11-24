/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.constants;

/**
 * SQL Constants
 *
 * @since 2025/6/30
 */
public class SqlConstants {
    /**
     * Select all databases from PostgreSQL
     */
    public static final String PGSQL_SELECT_ALL_DATABASES = "SELECT datname FROM pg_database;";

    /**
     * Select all schemas from PostgreSQL current connect database
     */
    public static final String PGSQL_SELECT_ALL_SCHEMAS = "SELECT schema_name FROM information_schema.schemata;";

    /**
     * Select database version
     */
    public static final String SELECT_VERSION = "SELECT version();";
}
