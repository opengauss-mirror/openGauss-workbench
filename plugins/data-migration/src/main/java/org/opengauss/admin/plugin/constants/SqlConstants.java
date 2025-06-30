/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
