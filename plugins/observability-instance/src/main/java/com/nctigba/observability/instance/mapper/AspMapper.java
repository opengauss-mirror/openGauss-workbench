/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nctigba.observability.instance.dto.asp.AnalysisDto;
import com.nctigba.observability.instance.dto.asp.AspCountReq;

/**
 * AspMapper.java
 *
 * @since 2023-08-25
 */
@Mapper
public interface AspMapper {
    /**
     * count
     *
     * @param req AspCountReq
     * @return List<Map<String, Object>>
     */
    @Select("select sample_time,a.session_count from ( "
            + "select LOCAL_ACTIVE_SESSION1.sampleid,sample_time,session_count from ( "
            + "select sampleid,count(*) session_count from dbe_perf.LOCAL_ACTIVE_SESSION " + "group by sampleid "
            + ")LOCAL_ACTIVE_SESSION1 " + "left join ( "
            + "SELECT sampleid, MIN(sample_time) AS sample_time FROM dbe_perf.LOCAL_ACTIVE_SESSION "
            + "GROUP BY sampleid "
            + ")LOCAL_ACTIVE_SESSION2 on LOCAL_ACTIVE_SESSION1.sampleid  = LOCAL_ACTIVE_SESSION2.sampleid "
            + "union all " + "select gs_asp1.sampleid,sample_time,session_count from ( "
            + "select sampleid,count(*) session_count from gs_asp " + "group by sampleid " + ")gs_asp1  "
            + "left join ( " + "SELECT sampleid, MIN(sample_time) AS sample_time FROM gs_asp " + "GROUP BY sampleid "
            + ")gs_asp2 on gs_asp1.sampleid  = gs_asp2.sampleid " + ")a "
            + "where a.sample_time >= #{startTime} and a.sample_time <= #{finishTime} " + "order by a.sample_time ")
    List<Map<String, Object>> count(AspCountReq req);

    /**
     * analysis
     *
     * @param req AspCountReq
     * @return List<AnalysisDto>
     */
    @Select("select * from (select gs_asp1.sampleid,gs_asp2.sample_time," + "gs_asp1.databaseid, "
            + "gs_asp1.thread_id,gs_asp1.sessionid,gs_asp1.start_time,gs_asp1.event,gs_asp1.userid, "
            + "gs_asp1.application_name,host(gs_asp1.client_addr) client_addr,gs_asp1.client_hostname, "
            + "gs_asp1.client_port,gs_asp1.query_id,gs_asp1.unique_query_id,gs_asp1.user_id, "
            + "gs_asp1.cn_id,gs_asp1.unique_query,gs_asp1.lockmode,gs_asp1.wait_status " + "from GS_ASP gs_asp1  "
            + "left join ( " + "SELECT sampleid, MIN(sample_time) AS sample_time FROM gs_asp "
            + "GROUP BY sampleid) gs_asp2 on gs_asp1.sampleid  = gs_asp2.sampleid "
            + "where gs_asp2.sample_time >= #{startTime} and gs_asp2.sample_time <= " + "#{finishTime}          "
            + "union all " + "select s1.sampleid,s2.sample_time " + "sample_time,s1.databaseid, "
            + "s1.thread_id,s1.sessionid,s1.start_time,s1.event,s1.userid, "
            + "s1.application_name,host(s1.client_addr) client_addr,s1.client_hostname, "
            + "s1.client_port,s1.query_id,s1.unique_query_id,s1.user_id, "
            + "s1.cn_id,s1.unique_query,s1.lockmode,s1.wait_status " + "from dbe_perf.LOCAL_ACTIVE_SESSION s1  "
            + "left join ( " + "SELECT sampleid, MIN(sample_time) AS sample_time FROM dbe_perf.LOCAL_ACTIVE_SESSION "
            + "GROUP BY sampleid) s2 on s1.sampleid  = s2.sampleid "
            + "where s2.sample_time >= #{startTime} and s2.sample_time <= #{finishTime}) " + "order by sample_time; ")
    List<AnalysisDto> analysis(AspCountReq req);
}
