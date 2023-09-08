/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.constants.history;

/**
 * SqlCommon
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class SqlCommon {
    public static final String BUSINESS_CONN_COUNT =
            "select count(1) as count from gs_asp where wait_status <>'none' and "
                    + "sample_time>='hisDataStartTime' and sample_time<='hisDataEndTime';";
    public static final String SLOW_SQL =
            "select (now() - query_start) as during,t.* from pg_stat_activity t "
                    + "where (now() - query_start)>'duration';";
    public static final String POOR_SQL =
            "select user_name,query,count(*)n_calls,sum(db_time)db_time,sum(cpu_time)cpu_time,"
                    + "sum(data_io_time)data_io_time "
                    + "from dbe_perf.statement_history where start_time>='hisDataStartTime' "
                    + "and finish_time<='hisDataEndTime' group by user_name,query;";
    public static final String CPU_TIME_TOP_SQL =
            "select * from dbe_perf.STATEMENT_HISTORY where start_time>='hisDataStartTime' "
                    + "and finish_time<='hisDataEndTime' order by cpu_time desc limit 10;";
    public static final String TOP_CPU_TIME_SQL =
            "select *  from dbe_perf.STATEMENT where 1=1 order by cpu_time desc limit 10;";
    public static final String WAIT_EVENT =
            "select wait_status,count(*) from pg_thread_wait_status where 1=1 group by "
                    + "wait_status order by 2;";

    /**
     * sql statement
     */
    public static final String DB_THREADS_CONF = "show enable_thread_pool";

    /**
     * sql statement
     */
    public static final String SESSION_ID_SQL = "select sessionid from pg_stat_activity where %d=%d and query like "
            + "'select sessionid from pg_stat_activity where %d=%d%%'";

    /**
     * sql statement
     */
    public static final String QUERY_ALL_COLUMN_NAME = "select column_name from information_schema.columns "
            + "where table_name = 'TABLENAME'";

    /**
     * sql statement
     */
    public static final String WORK_MEM_SQL = "select setting from pg_settings WHERE name = 'work_mem'";

    /**
     * sql statement
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
     * sql statement
     */
    public static final String INDEX_SQL =
            "select row_to_json(t) from (select c2.relname,i.indisprimary,i.indisunique,i.indisclustered,i.indisvalid,"
                    + "i.indisreplident,pg_catalog.pg_get_indexdef(i.indexrelid,0,true) as def "
                    + "from pg_catalog.pg_class c, pg_catalog.pg_class c2,pg_catalog.pg_index i "
                    + "where c.relname='TABLENAME' and c.oid=i.indrelid and c2.oid=i.indexrelid) t";

    /**
     * sql statement
     */
    public static final String TABLE_STRUCTURE_SQL =
            "select row_to_json(t) from (select a.attnum,a.attname,t.typname,a.attlen,a.attnotnull,b.description "
                    + "from pg_catalog.pg_class c,pg_catalog.pg_attribute a "
                    + "left outer join pg_catalog.pg_description b on a.attrelid=b.objoid "
                    + "and a.attnum=b.objsubid,pg_catalog.pg_type t "
                    + "where c.relname='TABLENAME' and a.attnum>0 and a.attrelid=c.oid and a.atttypid=t.oid "
                    + "order by a.attnum) t";

    /**
     * sql statement
     */
    public static final String PARTITION_LIST_SQL = "select row_to_json(t) from (select partstrategy, partkey, "
            + "relpages, reltuples, relallvisible, interval from pg_partition "
            + "WHERE parttype = 'r' and relname = 'TABLENAME') t";

    /**
     * sql statement
     */
    public static final String DEBUG_QUERY_CHECK = "show track_stmt_parameter";

    /**
     * sql statement
     */
    public static final String DEBUG_QUERY_ID_SQL = "select query_id from pg_stat_activity where sessionid = '%d' "
            + "and state != 'idle' and query_id != 0";

    /**
     * sql statement
     */
    public static final String QUERY_TABLE_ALL_ROWS = "SELECT reltuples FROM pg_class WHERE relname = 'TABLENAME'";

    /**
     * sql statement
     */
    public static final String DEFAULT = "select 1 from pg_sleep(1) where 1=1;";

    /**
     * sql statement
     */
    public static final String DATABASE_PARAM = "select name,setting from pg_settings;";

    /**
     * block session sql statement
     */
    public static final String BLOCK_SESSION = "SELECT CURRENT_TIMESTAMP,w.query as waiting_query,"
            + "w.pid as w_pid,"
            + "w.usename as w_user,"
            + "l.query as locking_query,"
            + "l.pid as l_pid,"
            + "l.usename as l_user,"
            + "t.schemaname || '.' || t.relname as tablename"
            + " from pg_stat_activity w join pg_locks l1 on w.pid = l1.pid"
            + " and not l1.granted join pg_locks l2 on l1.relation = l2.relation"
            + " and l2.granted join pg_stat_activity l on l2.pid = l.pid join "
            + "pg_stat_user_tables t on l1.relation = t.relid"
            + " where w.waiting;";
}
