/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nctigba.observability.instance.aop.Ds;
import com.nctigba.observability.instance.dto.cluster.NodeRelationDto;
import com.nctigba.observability.instance.dto.cluster.SyncSituation;

/**
 * ClustersMapper
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Mapper
public interface ClustersMapper {
    /**
     * getSyncSituation
     *
     * @param id String
     * @return List<SyncSituation>
     */
    @Select("SELECT host(r.client_addr) host_ip, "
            + "pg_size_pretty(pg_xlog_location_diff(s.sender_sent_location,s.receiver_received_location)) "
            + "received_delay, "
            + "pg_size_pretty(pg_xlog_location_diff(s.sender_write_location,s.receiver_write_location)) write_delay, "
            + "pg_size_pretty(pg_xlog_location_diff(s.sender_replay_location,s.receiver_replay_location)) "
            + "replay_delay, " + "s.sync_state as sync, s.state as wal_sync_state, s.sync_priority "
            + "FROM pg_stat_replication r , pg_stat_get_wal_senders() s " + "where r.pid = s.pid;")
    @Ds
    List<SyncSituation> getSyncSituation(String id);

    /**
     * relation
     *
     * @param id String
     * @return List<NodeRelationDto>
     */
    @Select("SELECT host(client_addr) host_ip, "
            + "pg_xlog_location_diff(sender_sent_location,receiver_replay_location) replay_delay, "
            + "sync_state FROM pg_stat_replication")
    @Ds
    List<NodeRelationDto> relation(String id);
}