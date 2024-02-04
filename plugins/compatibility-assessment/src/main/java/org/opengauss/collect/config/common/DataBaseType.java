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

package org.opengauss.collect.config.common;

/**
 * Constant
 *
 * @author liu
 * @since 2023-09-17
 */
public class DataBaseType {
    /**
     * MYSQL
     */
    public static final String MYSQL = "mysql";

    /**
     * OPENGAUSS
     */
    public static final String OPENGAUSS = "opengauss";

    /**
     * URL
     */
    public static final String URL = "jdbc:sqlite:data/collect.db";

    /**
     * DRIVER_CLASS_NAME
     */
    public static final String DRIVER_CLASS_NAME = "org.sqlite.JDBC";

    /**
     * SEARCH_DATABASE
     */
    public static final String GAUSS_DATABASE = "SELECT datname FROM pg_database";

    /**
     * MYSQL_DATABASE
     */
    public static final String MYSQL_DATABASE = "SHOW DATABASES";

    /**
     * SEARCH_RES
     */
    public static final String GAUSS_RES = "datname";

    /**
     * MYSQL_RES
     */
    public static final String MYSQL_RES = "Database";
}
