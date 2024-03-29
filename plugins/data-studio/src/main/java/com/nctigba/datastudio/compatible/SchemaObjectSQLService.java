/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.util.DebugUtils;
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
