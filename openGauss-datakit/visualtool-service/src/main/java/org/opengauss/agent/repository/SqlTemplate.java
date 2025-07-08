/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.repository;

import cn.hutool.core.util.StrUtil;

import org.opengauss.admin.common.enums.DbDataLocationEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SqlTemplate
 * support dynamic sql template for opengauss and intarkdb
 *
 * @author: wangchao
 * @Date: 2025/5/26 16:26
 * @since 7.0.0-RC2
 **/
public class SqlTemplate {
    /**
     * build real-time table create sql
     *
     * @param taskId task id
     * @param extendTableFields extend table fields
     * @param dataLocationEnum dataLocationEnum
     * @return String
     */
    public static String buildRealTimeTableSql(String taskId, String extendTableFields,
        DbDataLocationEnum dataLocationEnum) {
        String tableName = TableNameGenerator.getRealTimeTableName(taskId);
        SqlGenerator generator = SqlGeneratorFactory.getGenerator(dataLocationEnum);
        return generator.buildRealTimeTableSql(tableName, extendTableFields);
    }

    /**
     * build real time table insert sql
     * insert into real_time_table (task_id, agent_id, cluster_id, field1, field2, ...)
     *
     * @param taskId task id
     * @param fieldNameList field name list
     * @return insert sql
     */
    public static String buildRealTimeTableInsertSql(String taskId, List<String> fieldNameList) {
        String placeholders = StrUtil.join(",", Collections.nCopies(fieldNameList.size(), "?"));
        String fields = fieldNameList.stream().map(String::trim).collect(Collectors.joining(", "));
        // @formatter:off
        return String.format("INSERT INTO %s (id,task_id, agent_id, cluster_node_id,row_id, %s ,create_time)\n"
                + " VALUES (?,?, ?, ?,?, %s,?)\n",
            // @formatter:on
            TableNameGenerator.getRealTimeTableName(taskId), fields, placeholders);
    }

    /**
     * build real time table exists sql
     *
     * @param taskId task id
     * @return exists sql
     */
    public static String buildRealTimeRowExistsSql(String taskId) {
        return String.format("SELECT 1 FROM %s WHERE id = ?", TableNameGenerator.getRealTimeTableName(taskId));
    }

    /**
     * build real time table update sql
     *
     * @param taskId task id
     * @param fieldNameList field name list
     * @return update sql
     */
    public static String buildRealTimeTableUpdateSql(String taskId, List<String> fieldNameList) {
        return String.format("UPDATE %s SET %s , create_time=? WHERE id = ?",
            TableNameGenerator.getRealTimeTableName(taskId),
            fieldNameList.stream().map(field -> field + " = ?").collect(Collectors.joining(", ")));
    }

    /**
     * build real time table delete sql
     *
     * @param taskId task id
     * @return delete sql
     */
    public static String buildRealTimeDeleteSql(String taskId) {
        return String.format("DELETE FROM %s WHERE task_id=? and agent_id=? and cluster_node_id = ?",
            TableNameGenerator.getRealTimeTableName(taskId));
    }

    /**
     * build history table insert sql
     *
     * @param taskId task id
     * @param fieldNameList field name list
     * @return insert sql
     */
    public static String buildHistoryTableInsertSql(String taskId, List<String> fieldNameList) {
        String placeholders = StrUtil.join(",", Collections.nCopies(fieldNameList.size(), "?"));
        String fields = fieldNameList.stream().map(String::trim).collect(Collectors.joining(", "));
        // @formatter:off
        return String.format("INSERT INTO %s (batch_id,task_id, agent_id, cluster_node_id, %s ,create_time)\n"
                + " VALUES (?,?, ?, ?, %s,?) RETURNING id\n",
            // @formatter:on
            TableNameGenerator.getRealTimeTableName(taskId), fields, placeholders);
    }

    /**
     * build history table create sql
     *
     * @param taskId task id
     * @param extendTableFields extend table fields
     * @param dataLocationEnum dataLocationEnum
     * @return String
     */
    public static String buildHistoryTableSql(String taskId, String extendTableFields,
        DbDataLocationEnum dataLocationEnum) {
        String tableName = TableNameGenerator.getHistoryTableName(taskId);
        SqlGenerator generator = SqlGeneratorFactory.getGenerator(dataLocationEnum);
        return generator.buildHistoryTableSql(tableName, extendTableFields);
    }

    /**
     * build fingerprint table create sql
     *
     * @param taskId task id
     * @param extendTableFields extend table fields
     * @param dataLocationEnum dataLocationEnum
     * @return String
     */
    public static String buildFingerprintTableSql(String taskId, String extendTableFields,
        DbDataLocationEnum dataLocationEnum) {
        String tableName = TableNameGenerator.getFingerprintTableName(taskId);
        SqlGenerator generator = SqlGeneratorFactory.getGenerator(dataLocationEnum);
        return generator.buildFingerprintTableSql(tableName, extendTableFields);
    }

    /**
     * build tree table create sql
     *
     * @param taskId task id
     * @param dataLocationEnum dataLocationEnum
     * @return String
     */
    public static String buildTreeTableSql(String taskId, DbDataLocationEnum dataLocationEnum) {
        String tableName = TableNameGenerator.getTreeTableName(taskId);
        SqlGenerator generator = SqlGeneratorFactory.getGenerator(dataLocationEnum);
        return generator.buildTreeTableSql(tableName);
    }

    /**
     * build tree node table create sql
     *
     * @param taskId task id
     * @param nodeTableFields node table fields
     * @param dataLocationEnum dataLocationEnum
     * @return String
     */
    public static String buildTreeNodeTableSql(String taskId, String nodeTableFields,
        DbDataLocationEnum dataLocationEnum) {
        String tableName = TableNameGenerator.getTreeTableName(taskId);
        String nodeTable = TableNameGenerator.getTreeNodeTableName(taskId);
        SqlGenerator generator = SqlGeneratorFactory.getGenerator(dataLocationEnum);
        return generator.buildTreeNodeTableSql(tableName, nodeTable, nodeTableFields);
    }

    /**
     * build tree table index sql
     *
     * @param taskId task id
     * @return String
     */
    public static String buildTreeIndexSql(String taskId) {
        String nodeTable = TableNameGenerator.getTreeNodeTableName(taskId);
        // @formatter:off
        return String.format(" CREATE INDEX idx_tree_path_%s ON %s (tree_id, path_level, node_path);\n"
            + " CREATE INDEX idx_parent_tree_%s ON %s (tree_id, parent_id);", taskId, nodeTable, taskId, nodeTable);
        // @formatter:on
    }

    /**
     * build check table exists sql
     *
     * @param tableName tableName
     * @param dataLocationEnum dataLocationEnum
     * @return String
     */
    public static String buildCheckTableExistsSql(String tableName, DbDataLocationEnum dataLocationEnum) {
        SqlGenerator generator = SqlGeneratorFactory.getGenerator(dataLocationEnum);
        return generator.buildCheckTableExistsSql(tableName);
    }

    /**
     * build history table delete sql
     *
     * @param taskTemplateName task template name
     * @return String
     */
    public static String buildHistoryTableDeleteSql(String taskTemplateName) {
        return String.format("DELETE FROM %s WHERE task_id=? and create_time < ?",
            TableNameGenerator.getHistoryTableName(taskTemplateName));
    }

    /**
     * sql generator factory
     */
    static class SqlGeneratorFactory {
        private static final Map<DbDataLocationEnum, SqlGenerator> generators = new HashMap<>();

        static {
            generators.put(DbDataLocationEnum.INTARKDB, new IntarkSqlGenerator());
            generators.put(DbDataLocationEnum.OPENGAUSS, new OpenGaussSqlGenerator());
        }

        /**
         * get sql generator by db type
         *
         * @param dbType db type
         * @return sql generator
         */
        public static SqlGenerator getGenerator(DbDataLocationEnum dbType) {
            return generators.getOrDefault(dbType, new OpenGaussSqlGenerator());
        }
    }

    /**
     * sql generator interface
     */
    interface SqlGenerator {
        /**
         * build real time table table create sql for specific database
         *
         * @param tableName table name
         * @param extendTableFields extend table fields
         * @return String
         */
        String buildRealTimeTableSql(String tableName, String extendTableFields);

        /**
         * build history table create sql for specific database
         *
         * @param tableName table name
         * @param extendTableFields extend table fields
         * @return String
         */
        String buildHistoryTableSql(String tableName, String extendTableFields);

        /**
         * build fingerprint table create sql for specific database
         *
         * @param tableName table name
         * @param extendTableFields extend table fields
         * @return String
         */
        String buildFingerprintTableSql(String tableName, String extendTableFields);

        /**
         * build tree table create sql for specific database
         *
         * @param tableName table name
         * @return String
         */
        String buildTreeTableSql(String tableName);

        /**
         * build tree node table create sql for specific database
         *
         * @param tableName table name
         * @param nodeTable node table name
         * @param nodeTableFields extend table fields
         * @return String
         */
        String buildTreeNodeTableSql(String tableName, String nodeTable, String nodeTableFields);

        /**
         * build check table exists sql for specific database
         *
         * @param tableName table name
         * @return String
         */
        String buildCheckTableExistsSql(String tableName);
    }

    static class OpenGaussSqlGenerator implements SqlGenerator {
        @Override
        public String buildRealTimeTableSql(String tableName, String extendTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " id          BIGINT PRIMARY KEY,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " row_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " %s,\n"
                    + " create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
                    + " )",
                // @formatter:on
                tableName, extendTableFields);
        }

        @Override
        public String buildHistoryTableSql(String tableName, String extendTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " id          BIGSERIAL PRIMARY KEY,\n"
                    + " batch_id    BIGINT NOT NULL,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " %s,\n"
                    + " create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
                    + " )",
                // @formatter:on
                tableName, extendTableFields);
        }

        @Override
        public String buildFingerprintTableSql(String tableName, String extendTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " id          BIGSERIAL PRIMARY KEY,\n"
                    + " fingerprint_id   VARCHAR(64) NOT NULL ,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " %s,\n"
                    + " create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
                    + " )",
                // @formatter:on
                tableName, extendTableFields);
        }

        @Override
        public String buildTreeTableSql(String tableName) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " tree_id     BIGSERIAL PRIMARY KEY,\n"
                    + " tree_name   VARCHAR(255) NOT NULL UNIQUE,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " separator   CHAR(1) DEFAULT '/' NOT NULL,\n"
                    + " create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
                    + " )",
                // @formatter:on
                tableName);
        }

        @Override
        public String buildTreeNodeTableSql(String treeTable, String nodeTable, String nodeTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " node_id     BIGSERIAL PRIMARY KEY,\n"
                    + " tree_id     BIGINT NOT NULL REFERENCES %s(tree_id) ON DELETE CASCADE,\n"
                    + " node_path   VARCHAR(4096) NOT NULL CHECK (node_path ~ '^/(\\\\d+/)*$'),\n"
                    + " parent_id   BIGINT REFERENCES %s(node_id) ON DELETE CASCADE,\n"
                    + " %s"
                    + " path_level  INT GENERATED ALWAYS AS (\n"
                    + " (length(node_path) - length(replace(node_path, '/', '')) - 1)) STORED,\n"
                    + " create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
                    + " )", nodeTable, treeTable, nodeTable, nodeTableFields);
            // @formatter:on
        }

        @Override
        public String buildCheckTableExistsSql(String tableName) {
            // @formatter:off
            return String.format("SELECT EXISTS (SELECT 1 FROM pg_tables WHERE tablename='%s' and schemaname='public')",
                // @formatter:on
                tableName);
        }
    }

    static class IntarkSqlGenerator implements SqlGenerator {
        @Override
        public String buildRealTimeTableSql(String tableName, String extendTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " id          BIGINT PRIMARY KEY,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " row_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " %s,\n"
                    + " create_time  TIMESTAMP DEFAULT now()\n"
                    + " )",
                // @formatter:on
                tableName, extendTableFields);
        }

        @Override
        public String buildHistoryTableSql(String tableName, String extendTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " id          BIGINT  PRIMARY KEY AUTOINCREMENT,\n"
                    + " batch_id    BIGINT NOT NULL,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " %s,\n"
                    + " create_time  TIMESTAMP DEFAULT now()\n"
                    + " )",
                // @formatter:on
                tableName, extendTableFields);
        }

        @Override
        public String buildFingerprintTableSql(String tableName, String extendTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " id           BIGINT  PRIMARY KEY AUTOINCREMENT,\n"
                    + " fingerprint_id   VARCHAR(64) NOT NULL ,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " %s,\n"
                    + " create_time  TIMESTAMP DEFAULT now()\n"
                    + " )",
                // @formatter:on
                tableName, extendTableFields);
        }

        @Override
        public String buildTreeTableSql(String tableName) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " tree_id     BIGINT  PRIMARY KEY AUTOINCREMENT,\n"
                    + " tree_name   VARCHAR(255) NOT NULL UNIQUE,\n"
                    + " task_id     BIGINT NOT NULL,\n"
                    + " agent_id    BIGINT NOT NULL,\n"
                    + " cluster_node_id  VARCHAR(64),\n"
                    + " separator   CHAR(1) DEFAULT '/' NOT NULL,\n"
                    + " create_time  TIMESTAMP DEFAULT now()\n"
                    + " )",
                // @formatter:on
                tableName);
        }

        @Override
        public String buildTreeNodeTableSql(String treeTable, String nodeTable, String nodeTableFields) {
            // @formatter:off
            return String.format(
                " CREATE TABLE IF NOT EXISTS %s (\n"
                    + " node_id     BIGINT  PRIMARY KEY AUTOINCREMENT,\n"
                    + " tree_id     BIGINT NOT NULL REFERENCES %s(tree_id) ON DELETE CASCADE,\n"
                    + " node_path   VARCHAR(4096) NOT NULL CHECK (node_path ~ '^/(\\\\d+/)*$'),\n"
                    + " parent_id   BIGINT REFERENCES %s(node_id) ON DELETE CASCADE,\n"
                    + " %s"
                    + " path_level  INT GENERATED ALWAYS AS (\n"
                    + " (length(node_path) - length(replace(node_path, '/', '')) - 1)) STORED,\n"
                    + " create_time  TIMESTAMP DEFAULT now()\n"
                    + " )", nodeTable, treeTable, nodeTable, nodeTableFields);
            // @formatter:on
        }

        @Override
        public String buildCheckTableExistsSql(String tableName) {
            return String.format("SELECT EXISTS (SELECT 1 FROM \"SYS_TABLES\" WHERE name = '%s')", tableName);
        }
    }
}
