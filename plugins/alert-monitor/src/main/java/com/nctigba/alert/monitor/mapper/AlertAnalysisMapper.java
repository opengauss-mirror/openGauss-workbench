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
 *  AlertAnalysisMapper.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/mapper/AlertAnalysisMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AlertAnalysisMapper
 *
 * @since 2023/12/5 16:00
 */
@Mapper
public interface AlertAnalysisMapper {
    /**
     * get the database connection count group by user
     *
     * @return List<Map<String, Object>>
     */
    @Select("select usename as username, count(1) as used_conn from pg_stat_activity group by usename order by"
        + " count(1) desc")
    List<Map<String, Object>> getConnectionList();

    /**
     * get the active database session count group by user
     *
     * @return List<Map<String, Object>>
     */
    @Select("select usename as username, count(1) as active_count from pg_stat_activity where state = 'active' "
        + "group by usename order by count(1) desc")
    List<Map<String, Object>> getActiveSessionList();

    /**
     * get blocking lock
     *
     * @return List<Map<String, Object>>
     */
    @Select("WITH RECURSIVE tree AS ( "
        + "  SELECT sessionid, psessionid,block_sessionid "
        + "  FROM PG_THREAD_WAIT_STATUS "
        + "  WHERE wait_status in ('acquire lock', 'acquire lwlock') "
        + "  UNION ALL "
        + "  SELECT t.sessionid, t.psessionid, t.block_sessionid "
        + "  FROM PG_THREAD_WAIT_STATUS t "
        + "  JOIN tree tr ON tr.psessionid = t.sessionid "
        + ") "
        + "SELECT distinct t1.block_sessionid,t2.waiting as is_block,t2.sessionid,t2.query as sql,"
        + "t2.xact_start as start_time "
        + "FROM tree t1, pg_stat_activity t2 where t1.sessionid = t2.sessionid")
    List<Map<String, Object>> getBlockLockList();

    /**
     * getTransactionCostTimeList
     *
     * @param limitValue String
     * @return List<Map<String, Object>>
     */
    @Select("WITH RECURSIVE tree AS (  "
        + "        SELECT t1.block_sessionid,t1.psessionid,t2.waiting as is_block,t2.sessionid,t2.query as sql,"
        + "t2.xact_start as start_time  "
        + "        FROM PG_THREAD_WAIT_STATUS t1, pg_stat_activity t2 where t1.sessionid = t2.sessionid  "
        + "        and t2.xact_start is not null and extract(epoch from now() - xact_start)> #{limitValue} "
        + "        UNION ALL  "
        + "        SELECT t1.block_sessionid,t1.psessionid,t2.waiting as is_block,t2.sessionid,t2.query as sql,"
        + "t2.xact_start as start_time  "
        + "        FROM PG_THREAD_WAIT_STATUS t1 join pg_stat_activity t2 on t1.sessionid = t2.sessionid   "
        + "        JOIN tree tr ON tr.psessionid = t1.sessionid  "
        + "        )  "
        + "        SELECT distinct * "
        + "        FROM tree ")
    List<Map<String, Object>> getTransactionCostTimeList(String limitValue);

    /**
     * remainTablespace
     *
     * @return List<Map<String, Object>>
     */
    @Select("SELECT "
        + " t1.spcname, "
        + " t1.spcmaxsize, "
        + " t1.spcmaxsize - t1.SIZE AS remainspc, "
        + " (t.spcmaxsize - t.SIZE AS remainspc) / t1.spcmaxsize * 100 AS remainpercent "
        + "FROM "
        + " ( "
        + " SELECT "
        + "  t.spcname, "
        + "  pg_tablespace_size(t.spcname)/ 1024 / 1024 / 1024 AS SIZE, "
        + "  substring(t.spcmaxsize FROM '^\\d+')::NUMERIC *  "
        + "          CASE "
        + "   WHEN substring(t.spcmaxsize FROM '\\s*(\\w+)$') = 'K' THEN 1 / 1024 / 1024 "
        + "   WHEN substring(t.spcmaxsize FROM '\\s*(\\w+)$') = 'M' THEN 1 / 1024 "
        + "   WHEN substring(t.spcmaxsize FROM '\\s*(\\w+)$') = 'G' THEN 1 "
        + "  END AS spcmaxsize "
        + " FROM "
        + "  pg_tablespace t "
        + " WHERE "
        + "  t.spcmaxsize IS NOT NULL ) t1")
    List<Map<String, Object>> remainTablespace();

    /**
     * largeTransaction
     *
     * @return List<Map<String, Object>>
     */
    @Select("select "
        + " sessionid, "
        + " sum(used_mem) as used_mem_size, "
        + " query as sql, "
        + " xact_start as start_time "
        + "from "
        + " gs_session_memory s, "
        + " pg_stat_activity a "
        + "where "
        + " substring_inner(sessid, "
        + " position('.' in sessid) + 1)= a.sessionid "
        + "group by "
        + " sessionid, "
        + " query, "
        + " a.xact_start "
        + "having "
        + " totalsize > 512 "
        + "order by "
        + " sum(used_mem) desc "
        + "limit 10")
    List<Map<String, Object>> largeTransaction();
}
