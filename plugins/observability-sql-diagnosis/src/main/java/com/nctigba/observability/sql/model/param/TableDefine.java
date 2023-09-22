/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.param;

/**
 * TableDefine
 *
 * @author luomeng
 * @since 2023/9/26
 */
public interface TableDefine {
    /**
     * Table name
     *
     * @return String
     */
    String getTableName();

    /**
     * Table column
     *
     * @return String
     */
    String getColumnDef();

    /**
     * Table is exists
     *
     * @return boolean
     */
    default boolean tableExists() {
        return true;
    }

    /**
     * Create table sql
     *
     * @return String
     */
    default String getTableDefine() {
        return String.format("CREATE TABLE %s %s (%s)", getTableName(), tableExists() ? "" : "IF EXISTS",
                getColumnDef());
    }

    /**
     * Insert data sql
     *
     * @param pojo entity class
     * @return String
     */
    default String insert(Object pojo) {
        return String.format("INSERT INTO %s values (%s)", getTableName(), pojo.toString());
    }
}
