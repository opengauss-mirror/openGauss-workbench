/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.opengauss.util.PSQLException;

import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.model.IndexAdvice;

import cn.hutool.core.util.StrUtil;

@Mapper
public interface TopSqlMapper {
    /**
     * current top sql list
     *
     * @return curr list top sql
     */
    @Select("select round(extract(epoch FROM (now() - query_start)),2) as duration,query_start,unique_sql_id,datname ,"
            + "usename,application_name ,datid,pid,sessionid,usesysid,usename,client_addr ,client_hostname,client_port,"
            + "backend_start ,xact_start ,state_change ,waiting,enqueue,state ,resource_pool,query_id ,query ,"
            + "connection_info,trace_id from pg_stat_activity where query_start is not null and unique_sql_id != 0 "
            + "and duration != 0 order by (now() - query_start) desc limit 10")
    List<Map<String, Object>> currentTopsqlList();

    /**
     * history top sql list
     *
     * @param topSQLListReq req
     * @return list top sql
     */
    @Select("select unique_query_id,debug_query_id,db_name,schema_name,user_name,application_name,query,start_time,"
            + "finish_time,db_time,cpu_time,execution_time, data_io_time "
            + "from dbe_perf.statement_history where debug_query_id != 0 "
            + "and finish_time >= #{startTimeTime} and finish_time <= #{finishTimeTime} order by ${orderField} desc,"
            + "execution_time desc,cpu_time desc,db_time desc limit 10")
    List<Map<String, Object>> historyTopsqlList(TopSQLListReq topSQLListReq);

    /**
     * sql detail
     *
     * @param id sqlId
     * @return detail
     */
    @Select("select query, query_id debug_query_id, unique_sql_id unique_query_id, datname db_name, usename user_name,"
            + " application_name , query_start start_time, client_addr || ':' || client_port socket , sessionid "
            + "from pg_stat_activity where query_id = #{id} limit 1")
    Map<String, Object> currentDetail(String id);

    /**
     * statistical info list
     *
     * @param id sqlId
     * @return statistical info
     */
    @Select("select query, debug_query_id, unique_query_id, db_name, schema_name, substring(start_time, 0, 20) "
            + "start_time, substring(finish_time, 0, 20) finish_time, user_name, application_name, client_addr || ':' "
            + "|| client_port socket, n_returned_rows, n_tuples_fetched, n_tuples_returned, n_tuples_inserted,"
            + " n_tuples_updated, n_tuples_deleted, lock_count, lock_wait_count, lock_max_count, (case when "
            + "n_blocks_fetched = 0 then '-' else substring((n_blocks_hit / n_blocks_fetched)* 100, 0, 6)|| '%' end) "
            + "as blocks_hit_rate, " + "json_extract_path_text(net_send_info::json,'size') net_send_info_size, "
            + "json_extract_path_text(net_recv_info::json,'size') net_recv_info_size, "
            + "json_extract_path_text(net_stream_send_info::json,'size') net_stream_send_info_size, "
            + "json_extract_path_text(net_stream_recv_info::json,'size') net_stream_recv_info_size, "
            + "json_extract_path_text(net_send_info::json,'n_calls') net_send_info_calls, "
            + "json_extract_path_text(net_recv_info::json,'n_calls') net_recv_info_calls, "
            + "json_extract_path_text(net_stream_send_info::json,'n_calls') net_stream_send_info_calls, "
            + "json_extract_path_text(net_stream_recv_info::json,'n_calls') net_stream_recv_info_calls, "
            + "json_extract_path_text(net_send_info::json,'time') net_send_info_time, "
            + "json_extract_path_text(net_recv_info::json,'time') net_recv_info_time, "
            + "json_extract_path_text(net_stream_send_info::json,'time') net_stream_send_info_time, "
            + "json_extract_path_text(net_stream_recv_info::json,'time') net_stream_recv_info_time,"
            + " n_soft_parse, n_hard_parse, db_time/1000 db_time, "
            + "cpu_time/1000 cpu_time, (db_time-cpu_time)/1000 wait_time, lock_time/1000 lock_time, "
            + "lock_wait_time/1000 lock_wait_time, execution_time/1000 execution_time, parse_time/1000 parse_time,"
            + " plan_time/1000 plan_time,rewrite_time/1000 rewrite_time, pl_execution_time/ 1000 pl_execution_time, "
            + "pl_compilation_time/1000 pl_compilation_time, data_io_time/1000 data_io_time"
            + " from dbe_perf.statement_history where debug_query_id = #{id} limit 1")
    Map<String, Object> historyDetail(String id);

    /**
     * current query plan
     *
     * @param id sqlId
     * @return query plan string
     */
    @Select("select query_plan from gs_wlm_session_statistics where queryid = #{id}")
    String currentPlan(String id);

    /**
     * history query plan for sql
     *
     * @param id sqlId
     * @return query plan string
     */
    @Select("select query_plan from dbe_perf.statement_history where debug_query_id=#{id}")
    String historyPlan(String id);

    /**
     * history sqlId to sql
     *
     * @param id sqlId
     * @return sql string
     */
    @Select("select query from dbe_perf.statement_history where debug_query_id=#{id}")
    String sql(String id);

    /**
     * advise for sql
     *
     * @param sql sql string
     * @return advises
     */
    default List<IndexAdvice> advise(String sql) {
        try {
            return defAdvise(sql.replace(StrUtil.LF, CommonConstants.BLANK).replace("'", "''"));
        } catch (PSQLException e) {
            return Collections.emptyList();
        }
    }

    /**
     * advice for sql from db
     *
     * @param sql formatted sql string, "{@code '}" to "{@code ''}" and {@code \n}
     *            to "{@code  }"
     * @return advise map
     * @throws PSQLException when sql error
     */
    @Select("select * from gs_index_advise('${sql}')")
    List<IndexAdvice> defAdvise(String sql) throws PSQLException;

    /**
     * current wait event
     *
     * @param id sqlId
     * @return wait event
     */
    @Select("select * from ( select query_id, sessionid, sample_time, wait_status, event, lockmode, "
            + "locktag_decode(locktag) as locktag, block_sessionid lo from dbe_perf.LOCAL_ACTIVE_SESSION "
            + "where query_id = #{id} union all "
            + "select query_id, sessionid, sample_time, wait_status, event, lockmode, locktag_decode(locktag), "
            + "block_sessionid lo from GS_ASP where query_id = #{id} ) order by sample_time")
    List<Map<String, Object>> currentWaitEvent(String id);

    /**
     * history sql wait event
     *
     * @param id sqlId
     * @return wait event json
     */
    @Select("select statement_detail_decode(details,'plaintext',false) w from dbe_perf.statement_history "
            + "where debug_query_id=#{id}")
    String waitEvent(String id);

    /**
     * table meta data
     *
     * @param relname table relname
     * @return meta data
     */
    @Select("select schemaname, t1.relname, pg_relation_size(relid) object_size, relkind object_type, n_live_tup, "
            + "n_dead_tup, case when n_live_tup + n_dead_tup = 0 then '0.00%' else round(n_dead_tup * 100 /(n_dead_tup "
            + "+ n_live_tup), 2)|| '%' end dead_tup_ratio, last_vacuum, last_autovacuum, last_analyze, last_autoanalyze "
            + "from pg_catalog.pg_stat_all_tables t1 left join pg_catalog.pg_class t2 on t1.relid = t2.oid "
            + "where t1.relname =#{relname} limit 1")
    Map<String, Object> tableMetaData(String relname);

    /**
     * table structure
     *
     * @param relname table relname
     * @return table structure
     */
    @Select("select a.attnum, a.attname, t.typname, a.attlen, a.attnotnull, b.description from pg_catalog.pg_class c, "
            + "pg_catalog.pg_attribute a left outer join pg_catalog.pg_description b on a.attrelid = b.objoid and "
            + "a.attnum = b.objsubid, pg_catalog.pg_type t where c.relname = #{relname} and a.attnum>0 "
            + "and a.attrelid = c.oid and a.atttypid = t.oid limit 1")
    List<Map<String, Object>> tableStructure(String relname);

    /**
     * index info for table
     *
     * @param relname table relname
     * @return index info
     */
    @Select("select a.attnum, a.attname, t.typname, a.attlen, a.attnotnull, b.description from pg_catalog.pg_class c, "
            + "pg_catalog.pg_attribute a left outer join pg_catalog.pg_description b on a.attrelid = b.objoid and "
            + "a.attnum = b.objsubid, pg_catalog.pg_type t where c.relname = #{relname} and a.attnum>0 and a.attrelid ="
            + " c.oid and a.atttypid = t.oid limit 1")
    List<Map<String, Object>> indexInfo(String relname);
}