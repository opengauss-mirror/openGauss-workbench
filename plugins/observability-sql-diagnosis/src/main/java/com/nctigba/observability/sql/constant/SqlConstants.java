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
 *  SqlConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/constant/SqlConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

/**
 * SqlConstants
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class SqlConstants {
    /**
     * select business connect count
     */
    public static final String BUSINESS_CONN_COUNT =
            "select count(1) as count from gs_asp where wait_status <>'none' and "
                    + "sample_time>='hisDataStartTime' and sample_time<='hisDataEndTime';";

    /**
     * select slow sql
     */
    public static final String SLOW_SQL =
            "select (now() - query_start) as during,t.* from pg_stat_activity t "
                    + "where (now() - query_start)>'duration';";

    /**
     * select poor sql
     */
    public static final String POOR_SQL =
            "select user_name,query,count(*)n_calls,sum(db_time)db_time,sum(cpu_time)cpu_time,"
                    + "sum(data_io_time)data_io_time "
                    + "from dbe_perf.statement_history where start_time>='hisDataStartTime' "
                    + "and finish_time<='hisDataEndTime' group by user_name,query;";

    /**
     * select cpu time top sql
     */
    public static final String CPU_TIME_TOP_SQL =
            "select * from dbe_perf.STATEMENT_HISTORY where start_time>='hisDataStartTime' "
                    + "and finish_time<='hisDataEndTime' order by cpu_time desc limit 10;";

    /**
     * select top cpu time sql
     */
    public static final String TOP_CPU_TIME_SQL =
            "select *  from dbe_perf.STATEMENT where 1=1 order by cpu_time desc limit 10;";

    /**
     * select wait event
     */
    public static final String WAIT_EVENT =
            "select wait_status,count(*) from pg_thread_wait_status where 1=1 group by "
                    + "wait_status order by 2;";


    /**
     * select enable_thread_pool param
     */
    public static final String DB_THREADS_CONF = "show enable_thread_pool";

    /**
     * select session id
     */
    public static final String SESSION_ID_SQL = "select sessionid from pg_stat_activity where %d=%d and query like "
            + "'select sessionid from pg_stat_activity where %d=%d%%'";

    /**
     * select all column name
     */
    public static final String QUERY_ALL_COLUMN_NAME = "select column_name from information_schema.columns "
            + "where table_name = 'TABLENAME'";

    /**
     * select work_mem param
     */
    public static final String WORK_MEM_SQL = "select setting from pg_settings WHERE name = 'work_mem'";

    /**
     * select table metadata sql
     */
    public static final String TABLE_METADATA_SQL =
            "select row_to_json(t) from (select schemaname,t1.relname,pg_relation_size(relid) object_size, "
                    + "relkind object_type,n_live_tup,n_dead_tup,"
                    + "case when n_live_tup+n_dead_tup=0 then '-' else "
                    + "round(n_dead_tup*100/(n_dead_tup+n_live_tup),2)||'%' end dead_tup_ratio,"
                    + "last_vacuum,last_autovacuum,last_analyze,last_autoanalyze "
                    + "from pg_catalog.pg_stat_all_tables t1 "
                    + "left join pg_catalog.pg_class t2 on t1.relid = t2.oid where t1.relname = 'TABLENAME')t";

    /**
     * select index sql
     */
    public static final String INDEX_SQL =
            "select row_to_json(t) from (select c2.relname,i.indisprimary,i.indisunique,i.indisclustered,i.indisvalid,"
                    + "i.indisreplident,pg_catalog.pg_get_indexdef(i.indexrelid,0,true) as def "
                    + "from pg_catalog.pg_class c, pg_catalog.pg_class c2,pg_catalog.pg_index i "
                    + "where c.relname='TABLENAME' and c.oid=i.indrelid and c2.oid=i.indexrelid) t";

    /**
     * select table structure sql
     */
    public static final String TABLE_STRUCTURE_SQL =
            "select row_to_json(t) from (select a.attnum,a.attname,t.typname,a.attlen,a.attnotnull,b.description "
                    + "from pg_catalog.pg_class c,pg_catalog.pg_attribute a "
                    + "left outer join pg_catalog.pg_description b on a.attrelid=b.objoid "
                    + "and a.attnum=b.objsubid,pg_catalog.pg_type t "
                    + "where c.relname='TABLENAME' and a.attnum>0 and a.attrelid=c.oid and a.atttypid=t.oid "
                    + "order by a.attnum) t";

    /**
     * select partition list sql
     */
    public static final String PARTITION_LIST_SQL = "select row_to_json(t) from (select partstrategy, partkey, "
            + "relpages, reltuples, relallvisible, interval from pg_partition "
            + "WHERE parttype = 'r' and relname = 'TABLENAME') t";

    /**
     * select track_stmt_parameter param
     */
    public static final String DEBUG_QUERY_CHECK = "show track_stmt_parameter";

    /**
     * select debug query id
     */
    public static final String DEBUG_QUERY_ID_SQL = "select query_id from pg_stat_activity where sessionid = '%d' "
            + "and state != 'idle' and query_id != 0";

    /**
     * select table all rows
     */
    public static final String QUERY_TABLE_ALL_ROWS = "SELECT reltuples FROM pg_class WHERE relname = 'TABLENAME'";

    /**
     * default select
     */
    public static final String DEFAULT = "select 1 from pg_sleep(1) where 1=1;";

    /**
     * select database all param
     */
    public static final String DATABASE_ALL_PARAM = "select name,setting from pg_settings;";

    /**
     * select database param value
     */
    public static final String DATABASE_PARAM = "select * from param_info where paramName= '%s' ;";

    /**
     * select os param
     */
    public static final String OS_PARAM = "select * from param_info where paramType='OS';";

    /**
     * select block session
     */
    public static final String BLOCK_SESSION = "SELECT CURRENT_TIMESTAMP,w.sessionid w_sessionid,"
            + "w.pid as w_pid,"
            + "l.sessionid l_sessionid,"
            + "l.pid as l_pid,"
            + "l.usename as l_user,"
            + "l.query as locking_query,"
            + "t.schemaname || '.' || t.relname as tablename,"
            + "l.application_name,l.client_addr,l.state,l2.mode"
            + " from pg_stat_activity w join pg_locks l1 on w.pid = l1.pid"
            + " and not l1.granted join pg_locks l2 on l1.relation = l2.relation"
            + " and l1.locktype = l2.locktype and l1.page = l2.page and l1.tuple = l2.tuple"
            + " and l2.granted join pg_stat_activity l on l2.pid = l.pid join "
            + "pg_stat_user_tables t on l1.relation = t.relid"
            + " where w.waiting;";

    /**
     * select execPlan detail
     */
    public static final String PLAN_DETAIL_SQL =
            "select query, debug_query_id, unique_query_id, db_name, schema_name, substring(start_time, 0, 20) "
                    + "start_time, substring(finish_time, 0, 20) finish_time, "
                    + "user_name, application_name, client_addr || ':' "
                    + "|| client_port socket, n_returned_rows, n_tuples_fetched, n_tuples_returned, n_tuples_inserted,"
                    + " n_tuples_updated, n_tuples_deleted, lock_count, lock_wait_count, lock_max_count, (case when "
                    + "n_blocks_fetched = 0 then '-' else "
                    + "substring((n_blocks_hit / n_blocks_fetched)* 100, 0, 6)|| '%' end) "
                    + "as blocks_hit_rate, "
                    + "json_extract_path_text(net_send_info::json,'size') net_send_info_size, "
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
                    + "lock_wait_time/1000 lock_wait_time, execution_time/1000 "
                    + "execution_time, parse_time/1000 parse_time,"
                    + " plan_time/1000 plan_time,rewrite_time/1000 rewrite_time, "
                    + "pl_execution_time/ 1000 pl_execution_time, "
                    + "pl_compilation_time/1000 pl_compilation_time, data_io_time/1000 data_io_time"
                    + " from dbe_perf.statement_history where debug_query_id = ? limit 1";

    /**
     * select param_info data
     */
    public static final String PARAM_INFO = "select * from param_info;";

    /**
     * select param_info count
     */
    public static final String PARAM_INFO_COUNT = "select count(*) from param_info;";

    /**
     * select param_info db data
     */
    public static final String PARAM_INFO_DB = "select * from param_info where paramType='DB';";

    /**
     * select param_info os data
     */
    public static final String PARAM_INFO_OS = "select * from param_info where paramType='OS';";

    /**
     * select core optimizer param
     */
    public static final String CORE_OPTIMIZER_PARAM = "select name,setting from pg_settings "
            + "where name in ('enable_bitmapscan','enable_hashjoin','enable_mergejoin',"
            + "'enable_nestloop','enable_seqscan');";

    /**
     * select random_page_cost param
     */
    public static final String RANDOM_PAGE_COST = "select name,setting from pg_settings "
            + "where name = 'random_page_cost';";

    /**
     * select object info
     */
    public static final String OBJECT_INFO = "select"
            + " t2.tablespace,"
            + " t1.schemaname,"
            + " t1.relname,"
            + " t1.n_tup_ins,"
            + " t1.n_tup_del ,"
            + " t1.n_tup_upd,"
            + " t1.n_live_tup,"
            + " (t1.n_tup_ins + t1.n_tup_del + t1.n_tup_upd) / t1.n_live_tup * 100 as ratio"
            + " from"
            + " pg_catalog.pg_stat_all_tables t1"
            + " left join pg_catalog.PG_TABLES t2"
            + " on"
            + " t1.schemaname = t2.schemaname"
            + " and t1.relname = t2.tablename"
            + " where"
            + " t1.n_live_tup <> 0"
            + " and t1.schemaname||'.'||t1.relname in (%s);";

    /**
     * select dead tuple
     */
    public static final String DEAD_TUPLE = "select"
            + " t2.tablespace,"
            + " t1.schemaname,"
            + " t1.relname,"
            + " t1.n_dead_tup,"
            + " t1.n_live_tup,"
            + " t1.n_dead_tup / t1.n_live_tup * 100 as ratio"
            + " from"
            + " pg_catalog.pg_stat_all_tables t1"
            + " left join pg_catalog.PG_TABLES t2"
            + " on"
            + " t1.schemaname = t2.schemaname"
            + " and t1.relname = t2.tablename"
            + " where"
            + " t1.n_live_tup <> 0"
            + " and t1.schemaname||'.'||t1.relname in (%s);";

    /**
     * select partition table reform
     */
    public static final String PARTITION_TABLE_REFORM = "select"
            + " t2.tablespace,"
            + " t1.schemaname,"
            + " t1.relname,"
            + " t1.n_dead_tup"
            + " from"
            + " pg_catalog.pg_stat_all_tables t1"
            + " left join pg_catalog.PG_TABLES t2"
            + " on"
            + " t1.schemaname = t2.schemaname"
            + " and t1.relname = t2.tablename"
            + " where"
            + " t1.n_live_tup <> 0"
            + " and t1.schemaname||'.'||t1.relname in (%s);";

    /**
     * select index lose efficacy
     */
    public static final String INDEX_LOSE_EFFICACY = "select "
            + "pi.indisvalid,pc.relname,pi.indisunique,tt.nspname,tt.relname,att.attname"
            + ",format_type(att.atttypid, att.atttypmod) AS type"
            + " from PG_INDEX pi"
            + " inner join PG_CLASS pc on pi.indexrelid = pc.oid and pc.relkind in ('i' , 'I')"
            + " inner join PG_ATTRIBUTE att on pc.oid = att.attrelid"
            + " inner join (select pc.relname,pn.nspname,pc.oid from PG_CLASS pc "
            + " inner join pg_namespace pn on pn.oid = pc.relnamespace"
            + " where pn.nspname ||'.'||pc.relname in (%s) ) tt on pi.indrelid = tt.oid;";

    /**
     * select agg function
     */
    public static final String AGG_FUNCTION = "select distinct proname from pg_proc "
            + "where proname like '%agg%' or proname like '%sum';";

    /**
     * select all function
     */
    public static final String ALL_FUNCTION = "select distinct proname from pg_proc where 1=1;";

    /**
     * get schema name sql
     */
    public static final String GET_SCHEMA_NAME_SQL = "SELECT nspname FROM pg_namespace order by nspname;";

    /**
     * get database name sql
     */
    public static final String GET_DATABASE_SQL = "SELECT datname FROM pg_database;";
}