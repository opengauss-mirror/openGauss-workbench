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

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

/**
 * SchemaObjectSQLService
 *
 * @since 2023-6-26
 */
public interface SchemaObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * query all users ddl
     *
     * @return String
     */
    default String queryAllUsersDDL() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * query schema ddl
     *
     * @param oid oid
     * @return String
     */
    default String querySchemaDDL(String oid) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create comment schema sql
     *
     * @param schemaName schemaName
     * @param description description
     * @return String
     */
    default String createCommentSchemaSQL(String schemaName, String description) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create schema sql
     *
     * @param schemaName schemaName
     * @param owner owner
     * @return String
     */
    default String createSchemaSQL(String schemaName, String owner) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * update schema name sql
     *
     * @param oldSchemaName oldSchemaName
     * @param schemaName schemaName
     * @return String
     */
    default String updateSchemaNameSQL(String oldSchemaName, String schemaName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * update schema owner sql
     *
     * @param oldSchemaName oldSchemaName
     * @param owner owner
     * @return String
     */
    default String updateSchemaOwnerSQL(String oldSchemaName, String owner) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * update schema comment sql
     *
     * @param schemaName schemaName
     * @param description description
     * @return String
     */
    default String updateSchemaCommentSQL(String schemaName, String description) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * delete schema sql
     *
     * @param schemaName schemaName
     * @return String
     */
    default String deleteSchemaSQL(String schemaName) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
