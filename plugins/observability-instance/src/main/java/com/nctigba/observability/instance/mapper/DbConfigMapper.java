/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DbConfigMapper {
    @Select("select memorytype, memorymbytes from dbe_perf.memory_node_detail")
    List<Map<String, Object>> memoryNodeDetail();

    @Select("select name, decode(unit,null,setting,setting||'('||unit||')') as value "
            + "from dbe_perf.GLOBAL_CONFIG_SETTINGS where name in ('max_process_memory',"
            + "'shared_buffers','temp_buffers','work_mem','query_mem','query_max_mem',"
            + "'maintenance_work_mem','cstore_buffers','memorypool_enable','memorypool_size')")
    List<Map<String, Object>> memoryConfig();
}