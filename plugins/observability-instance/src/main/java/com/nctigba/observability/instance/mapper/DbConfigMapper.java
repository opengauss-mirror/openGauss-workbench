/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DbConfigMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/mapper/DbConfigMapper.java
 *
 *  -------------------------------------------------------------------------
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

    /**
     * waitEvents total
     *
     * @return COUNT
     */
    @Select("select count(1) "
        + "from pg_stat_activity a "
        + "left join dbe_perf.thread_wait_status s on a.sessionid = s.sessionid "
        + "where state = 'active' and a.pid != pg_backend_pid() "
        + "and s.wait_event != 'none'")
    Integer waitEventTotal();

    /**
     * cache hit rate
     *
     * @return List<Map<String, Object>>
     */
    @Select("select datid, datname,blks_hit / (blks_read + blks_hit) * 100 as hit, stats_reset as resetTime from "
        + "pg_stat_database where blks_read > 0 or blks_hit > 0")
    List<Map<String, Object>> cacheHit();

    /**
     * select tablespace data
     *
     * @return List<Map<String,Object>>
     */
    @Select("SELECT spcname, pg_get_userbyid(spcowner) AS owner,spcoptions FROM pg_tablespace;")
    List<Map<String, Object>> tablespaceInfo();

    /**
     * Query the top ten tables by capacity
     *
     * @return List<Map<String,Object>>
     */
    @Select("select t2.table_catalog datname,t1.schemaname, "
        + " t1.relname tableName, pg_relation_size(t1.relid) / 1024 / 1024 as tableSize from pg_stat_user_tables t1 "
        + "left join information_schema.tables t2 on "
        + " t1.relname = t2.table_name "
        + " and t1.schemaname = t2.table_schema order by pg_relation_size(t1.relid) desc limit 10")
    List<Map<String, Object>> tablesTop10();

    /**
     * Query the top ten Indexs by capacity
     *
     * @return List<Map<String,Object>>
     */
    @Select("select t2.table_catalog datname,t1.schemaname,t1.relname tableName,t1.indexrelname indexName,"
        + "pg_relation_size(t1.relid)/1024/1024 as indexSize"
        + " from pg_stat_user_indexes t1 left join information_schema.tables t2 on t1.relname = t2.table_name"
        + " and t1.schemaname = t2.table_schema order by pg_relation_size(relid) desc limit 10")
    List<Map<String, Object>> indexsTop10();

    /**
     * Query the top ten deadTable by capacity
     *
     * @return List<Map<String,Object>>
     */
    @Select("select t2.table_catalog datname,t1.schemaname, "
        + "       t1.relname tableName, t1.n_dead_tup from pg_stat_user_tables t1 "
        + "       left join information_schema.tables t2 on "
        + "       t1.relname = t2.table_name "
        + "       and t1.schemaname = t2.table_schema order by t1.n_dead_tup desc limit 10")
    List<Map<String, Object>> deadTableTop10();

    /**
     * Query the top ten vacuum
     *
     * @return List<Map<String,Object>>
     */
    @Select("select t2.table_catalog datname,t1.schemaname, "
        + "       t1.relname tableName from pg_stat_user_tables t1 "
        + "       left join information_schema.tables t2 on t1.relname = t2.table_name "
        + "       and t1.schemaname = t2.table_schema "
        + "       where (t1.n_tup_upd + t1.n_tup_del) / t1.n_live_tup > 0.1 "
        + "       and t1.n_tup_upd + t1.n_tup_del > 1000 "
        + "       and t1.n_live_tup > 0 "
        + "       order by ((t1.n_tup_upd + t1.n_tup_del) / t1.n_live_tup) desc limit 10")
    List<Map<String, Object>> vacuumTop10();
}