/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TopSqlMapper {
    @Select("select statement_detail_decode(details,'plaintext',false) w from dbe_perf.statement_history where debug_query_id=#{id}")
    String waitEvent(String id);
}