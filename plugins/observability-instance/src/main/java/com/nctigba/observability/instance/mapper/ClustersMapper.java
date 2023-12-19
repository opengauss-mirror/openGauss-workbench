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
 *  ClustersMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/mapper/ClustersMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.mapper;

import java.util.List;

import com.nctigba.observability.instance.model.dto.cluster.NodeRelationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.model.dto.cluster.SyncSituationDelayDTO;

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
    List<SyncSituationDelayDTO> getSyncSituation(String id);

    /**
     * relation
     *
     * @param id String
     * @return List<NodeRelationDTO>
     */
    @Select("SELECT host(client_addr) host_ip, "
            + "pg_xlog_location_diff(sender_sent_location,receiver_replay_location) replay_delay, "
            + "sync_state FROM pg_stat_replication")
    @Ds
    List<NodeRelationDTO> relation(String id);
}