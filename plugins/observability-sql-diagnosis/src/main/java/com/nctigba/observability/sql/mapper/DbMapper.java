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

package com.nctigba.observability.sql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import static com.nctigba.observability.sql.constant.SqlConstants.GET_DATABASE_SQL;
import static com.nctigba.observability.sql.constant.SqlConstants.GET_SCHEMA_NAME_SQL;

@Mapper
public interface DbMapper {
    /**
     * Query database name
     *
     * @return list
     */
    @Select(GET_DATABASE_SQL)
    List<String> dataBaseList();

    /**
     * Query schema name
     *
     * @return list
     */
    @Select(GET_SCHEMA_NAME_SQL)
    List<String> schemaList();
}