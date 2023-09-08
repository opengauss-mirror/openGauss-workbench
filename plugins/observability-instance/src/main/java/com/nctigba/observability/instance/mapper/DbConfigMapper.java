/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * DbConfigMapper.java
 *
 * @since 2023-08-28
 */
@Mapper
public interface DbConfigMapper {
    /**
     * memoryNodeDetail
     *
     * @return List
     */
    @Select("select memorytype, memorymbytes from dbe_perf.memory_node_detail")
    List<Map<String, Object>> memoryNodeDetail();

    /**
     * memoryConfig
     *
     * @return List
     */
    @Select("select name, case when unit = '8kB' then CAST(CAST(setting AS DECIMAL(10)) * 8 as varchar(10)) || '(kB)' "
            + "else decode(unit,null,setting,setting||'('||unit||')') end as value from dbe_perf.GLOBAL_CONFIG_SETTINGS"
            + " where name in ('max_process_memory', 'shared_buffers', 'temp_buffers', 'work_mem', 'query_mem', "
            + "'query_max_mem', 'maintenance_work_mem', 'cstore_buffers', 'memorypool_enable', 'memorypool_size')")
    List<Map<String, Object>> memoryConfig();

    /**
     * settings
     *
     * @return List
     */
    @Select("select name,setting from pg_settings")
    List<Map<String, String>> settings();

    /**
     * workMem
     *
     * @return String
     */
    @Select("select setting from pg_settings WHERE name = 'work_mem'")
    String workMem();

    /**
     * version
     *
     * @return String
     */
    @Select("SELECT version()")
    String version();

    /**
     * starttime
     *
     * @return String
     */
    @Select("SELECT pg_postmaster_start_time()")
    String starttime();

    /**
     * archiveMode
     *
     * @return String
     */
    @Select("SHOW archive_mode")
    String archiveMode();

    /**
     * env
     *
     * @return Map
     */
    @Select("select node_name, host, process, port, installpath, datapath, log_directory from pg_node_env")
    Map<String, String> env();

    /**
     * waitEvents
     *
     * @return List
     */
    @Select("select s.node_name, s.db_name, s.thread_name, s.lwtid tid, a.sessionid, "
            + "coalesce(s.block_sessionid, 0) block_sessionid, a.query_id, s.wait_status, s.wait_event, "
            + "coalesce(s.lockmode, ' ' ) lockmode, coalesce(s.locktag, ' ') locktag, "
            + "coalesce(locktag_decode(locktag), ' ') tag from pg_stat_activity a "
            + "left join dbe_perf.THREAD_WAIT_STATUS s on a.sessionid = s.sessionid "
            + "where state = 'active' and a.pid <> pg_backend_pid()")
    List<Map<String, Object>> waitEvents();
}