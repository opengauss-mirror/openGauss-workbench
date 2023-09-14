/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * SessionMapper
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Mapper
public interface SessionMapper {
    /**
     * sessionIsWaiting
     *
     * @param session session
     * @return int
     */
    @Select("select count(waiting) as count from pg_stat_activity where sessionid = #{session} and waiting")
    int sessionIsWaiting(String session);

    /**
     * generalMesList
     *
     * @param session session
     * @return List
     */
    @Select("select a.state, a.sessionid,  a.datname, a.usename, "
            + "a.resource_pool, b.lwtid , TO_CHAR(a.backend_start,'YYYY-MM-DD HH:MI:SS') as backend_start, "
            + "TO_CHAR((now()- a.backend_start),'HH24:MI:SS') as backend_runtime, "
            + "host(a.client_addr) as client_addr, a.application_name, a.client_hostname, a.client_port, "
            + "TO_CHAR(a.xact_start ,'YYYY-MM-DD HH:MI:SS') as xact_start, "
            + "TO_CHAR(a.query_start ,'YYYY-MM-DD HH:MI:SS') as query_start, " + "a.datname, a.query_id, a.query "
            + "from pg_stat_activity a left join pg_thread_wait_status b on a.sessionid=b.sessionid "
            + "where a.sessionid = #{session}")
    List<Map<String, Object>> generalMesList(String session);

    /**
     * blockList
     *
     * @param session String
     * @return List
     */
    @Select("select a.block_sessionid, pg_relation_filepath(b.relation)"
            + " as filepath, b.page, b.tuple, b.bucket, a.wait_status, a.wait_event, a.lockmode, "
            + "(select d.nspname || '.' || c.relname from pg_class c left join pg_namespace d  "
            + "on c.relnamespace = d.oid  " + "where c.oid = b.relation) as namespace_relation "
            + "from pg_thread_wait_status a left join pg_locks b on a.sessionid = b.sessionid "
            + "where a.sessionid = #{session} and not granted")
    List<Map<String, Object>> blockList(String session);

    /**
     * statistic
     *
     * @param session session
     * @return List
     */
    @Select("select stat_name,value from GS_SESSION_TIME where sessid like '%' || #{session}")
    List<Map<String, Object>> statistic(String session);

    /**
     * runtime
     *
     * @param session session
     * @return List
     */
    @Select("select statname,value from GS_SESSION_STAT where sessid like '%' || #{session}")
    List<Map<String, Object>> runtime(String session);

    /**
     * detailWaiting
     *
     * @param session session
     * @return List
     */
    @Select("select sample_time,wait_status,event,lockmode,locktag_decode"
            + "(locktag) as locktag from GS_ASP where sessionid = #{session} order by sample_time desc limit 10")
    List<Map<String, Object>> detailWaiting(String session);

    /**
     * blockTree
     *
     * @return List
     */
    @Select("with recursive tmp_lock as ( " + "    select distinct " + "           w.sessionid as id, "
            + "           r.sessionid as parentid, " + "           w.wait_status as wait_status, "
            + "           w.wait_event as wait_event, " + "           w.lockmode as lockmode         " + "    from ( "
            + "          select a.sessionid,a.locktype,a.database, "
            + "                 a.relation,a.page,a.tuple,a.classid, "
            + "                 a.objid,a.objsubid,a.pid,a.virtualtransaction,a.virtualxid, "
            + "                 a.transactionid, b.query as query, "
            + "                 b.xact_start,b.query_start,b.usename,b.datname ,c.wait_status,c.wait_event,c.lockmode "
            + "           from pg_locks a  " + "           left join pg_stat_activity b on a.sessionid=b.sessionid "
            + "           left join pg_thread_wait_status c on b.sessionid=c.sessionid "
            + "           where not a.granted  " + "          ) w, " + "         ( "
            + "          select a.sessionid,a.locktype,a.database, "
            + "                 a.relation,a.page,a.tuple,a.classid, "
            + "                 a.objid,a.objsubid,a.pid,a.virtualtransaction,a.virtualxid, "
            + "                 a.transactionid, b.query as query, "
            + "                 b.xact_start,b.query_start,b.usename,b.datname ,c.wait_status,c.wait_event,c.lockmode "
            + "           from pg_locks a  " + "           left join pg_stat_activity b on a.sessionid=b.sessionid "
            + "           left join pg_thread_wait_status c on b.sessionid=c.sessionid "
            + "           where a.granted  " + "          ) r  " + "    where 1=1 "
            + "      and r.locktype is not distinct from w.locktype  "
            + "      and r.database is not distinct from w.database  "
            + "      and r.relation is not distinct from w.relation  "
            + "      and r.page is not distinct from w.page  " + "      and r.tuple is not distinct from w.tuple  "
            + "      and r.classid is not distinct from w.classid  "
            + "      and r.objid is not distinct from w.objid  "
            + "      and r.objsubid is not distinct from w.objsubid  "
            + "      and r.transactionid is not distinct from w.transactionid  "
            + "      and r.sessionid <> w.sessionid " + "),tmp0 as ( " + "  select * " + "    from tmp_lock tl "
            + "   union all " + "  select t1.parentid,0::int4,p.wait_status,p.wait_event,null "
            + "    from tmp_lock t1 left join pg_thread_wait_status p on t1.parentid=p.sessionid  " + "   where 1=1 "
            + "     and t1.parentid not in (select id from tmp_lock) " + "), " + "  tmp3 as ( "
            + "  SELECT array[id]::text[] as pathid,1 as depth,id,parentid,wait_status,wait_event,lockmode "
            + "    FROM tmp0 " + "   where 1=1 " + "     and parentid=0 " + "   union "
            + "  SELECT t0.pathid||array[t1.id]::text[] as pathid,t0.depth+1 as depth,t1.id,t1.parentid, "
            + "         t1.wait_status,t1.wait_event,t1.lockmode " + "    FROM tmp0 t1,   " + "         tmp3 t0 "
            + "   where 1=1 " + "     and t1.parentid=t0.id " + ") " + "select distinct  "
            + "       '/'||array_to_string(a0.pathid,'/') as pathid, " + "       a0.depth, "
            + "       a0.id,a0.parentid, a0.pathid[1] as tree_id, "
            + "       lpad(a0.id::text, 2*a0.depth-1+length(a0.id::text),' ') as tree, "
            + "       a2.datname,a2.usename,a2.application_name,a2.client_addr::text client_addr,a2.state, "
            + "       TO_CHAR(a2.backend_start,'YYYY-MM-DD HH:MI:SS') as backend_start,a2.query,  "
            + "       a0.wait_status,a0.wait_event,a0.lockmode " + "  from tmp3 a0 "
            + "       left outer join (select distinct '/'||id||'/' as prefix_id,id "
            + "                        from tmp0 " + "             where 1=1 ) a1 "
            + "                  on position( a1.prefix_id in '/'||array_to_string(a0.pathid,'/')||'/' ) >0 "
            + "     left outer join pg_stat_activity a2 " + "                  on a0.id = a2.sessionid "
            + "order by '/'||array_to_string(a0.pathid,'/'),a0.depth;")
    List<Map<String, Object>> blockTree();

    /**
     * simpleStatistic
     *
     * @return Map
     */
    @Select("select a.max_conn, b.active, c.waiting, d.max_runtime from "
            + "(select setting::int max_conn, 1 as key from pg_settings where name='max_connections') a "
            + "left join  " + "(select count(*) as active, 1 as key from pg_stat_activity where state = 'active') b "
            + "on a.key=b.key " + " left join "
            + " (select count(*) as waiting, 1 as key from pg_stat_activity where waiting is true) c "
            + " on b.key=c.key " + " left join "
            + " (select extract(EPOCH from(max(now() - backend_start)))::INTEGER as max_runtime, 1 as key from "
            + "pg_stat_activity where application_name not in "
            + "('WLMArbiter','workload','WorkloadMonitor','WDRSnapshot','JobScheduler','PercentileJob'"
            + ",'statement flush thread','Asp','ApplyLauncher') and application_name not like 'DataKit%' ) d "
            + " on c.key = d.key")
    Map<String, Object> simpleStatistic();

    /**
     * longTxc
     *
     * @return List
     */
    @Select("SELECT pid,sessionid,usename,datname,application_name,"
            + "client_addr::text client_addr,query, xact_start, (now() - xact_start)::text xact_duration, query_start,"
            + " (now() - query_start)::text query_duration, STATE FROM pg_stat_activity WHERE STATE <>'idle' "
            + "and application_name not in  ('WLMArbiter','workload',"
            + "'WorkloadMonitor','WDRSnapshot','JobScheduler','PercentileJob','statement flush thread','Asp',"
            + "'ApplyLauncher') and application_name not like 'DataKit%' "
            + "and now()-xact_start > interval '30 SECOND' " + "ORDER BY xact_start;")
    List<Map<String, Object>> longTxc();
}