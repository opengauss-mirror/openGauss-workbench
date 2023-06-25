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
                    + "sample_time>='taskStartTime' and sample_time<='hisDataEndTime';";
    public static final String SLOW_SQL =
            "select (now() - query_start) as during,t.* from pg_stat_activity t "
                    + "where (now() - query_start)>'duration';";
    public static final String POOR_SQL =
            "select user_name,query,count(*)n_calls,sum(db_time)db_time,sum(cpu_time)cpu_time,"
                    + "sum(data_io_time)data_io_time "
                    + "from dbe_perf.statement_history where start_time>='taskStartTime' "
                    + "and finish_time<='hisDataEndTime' group by user_name,query;";
    public static final String CPU_TIME_TOP_SQL =
            "select * from dbe_perf.STATEMENT_HISTORY where start_time>='taskStartTime' "
                    + "and finish_time<='hisDataEndTime' order by cpu_time desc limit 10;";
    public static final String TOP_CPU_TIME_SQL =
            "select *  from dbe_perf.STATEMENT where 1=1 order by cpu_time desc limit 10;";
    public static final String WAIT_EVENT =
            "select wait_status,count(*) from pg_thread_wait_status where 1=1 group by "
                    + "wait_status order by 2;";
}
