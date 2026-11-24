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

import com.baomidou.dynamic.datasource.annotation.DS;
import com.nctigba.observability.sql.model.entity.TimePointDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * TimePointMapper
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Mapper
@DS("hisSlowSqlInfo")
public interface TimePointMapper {
    /**
     * record time snapshot
     *
     * @param tableName String
     * @param startTime Long
     * @param finishTime Long
     * @return int
     */
    @Insert("INSERT OR REPLACE INTO tb_time_point VALUES (#{tableName}, #{startTime}, #{finishTime});")
    int insertTimePoint(@Param("tableName") String tableName, @Param("startTime") Long startTime,
                      @Param("finishTime") Long finishTime);

    /**
     * record time snapshot
     *
     * @param tableName String
     * @return TimePointDO
     */
    @Select("select * from tb_time_point where node_tablename = #{tableName}")
    TimePointDO selectTimePoint(@Param("tableName") String tableName);
}
