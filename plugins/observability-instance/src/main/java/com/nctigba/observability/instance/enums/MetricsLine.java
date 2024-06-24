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
 *  MetricsLine.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/enums/MetricsLine.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.enums;

import lombok.Getter;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

@Getter
public enum MetricsLine {
    // index
    CPU(Type.OS, "(avg(sum(irate(agent_cpu_seconds_total{mode!='idle',host='ogbrench'}[5m]))by (cpu,instance))) * 100"),
    MEMORY(
            Type.OS,
            "(1 - (agent_memory_MemAvailable_bytes{host='ogbrench'} /"
                    + " (agent_memory_MemTotal_bytes{host='ogbrench'}))) * 100"),
    NETWORK_IN_TOTAL(Type.OS, "max(rate(agent_network_receive_bytes_total{host='ogbrench'}[5m]))"),
    NETWORK_OUT_TOTAL(Type.OS, "max(rate(agent_network_transmit_bytes_total{host='ogbrench'}[5m]))"),
    IO(Type.OS, "sum(rate(agent_disk_tot_ticks_total{host='ogbrench'}[5m])) / 1000"),
    SWAP(Type.OS, "agent_free_Swap_used_bytes{host='ogbrench'}/agent_free_Swap_total_bytes{host='ogbrench'} *100"),
    DB_THREAD_POOL(Type.DB, "local_threadpool_status_pool_utilization_rate{instanceId='ogbrench'}"),
    DB_ACTIVE_SESSION(Type.DB, "gauss_thread_wait_status_count{instanceId='ogbrench'}", "{wait_status}"),

    // CPU
    CPU_TOTAL(
            Type.OS,
            "clamp_max((avg(sum(irate(agent_cpu_seconds_total"
                    + "{mode!='idle',host='ogbrench'}[5m]))by (instance,cpu))by (instance)) * 100, 100)"),
    CPU_USER(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='user',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_SYSTEM(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='system',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_IOWAIT(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='iowait',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_IRQ(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='irq',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_SOFTIRQ(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='softirq',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_NICE(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='nice',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_STEAL(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='steal',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_IDLE(
            Type.OS,
            "(avg(sum(irate(agent_cpu_seconds_total{mode='idle',host='ogbrench'}[5m]))"
                    + "by (instance,cpu))by (instance)) * 100"),
    CPU_DB(Type.DB, "top_db_cpu{instanceId='ogbrench'}"),

    CPU_TOTAL_1M_LOAD(Type.OS, "sum(agent_load1{host='ogbrench'})by (instance)"),
    CPU_TOTAL_5M_LOAD(Type.OS, "sum(agent_load5{host='ogbrench'})by (instance)"),
    CPU_TOTAL_15M_LOAD(Type.OS, "sum(agent_load15{host='ogbrench'})by (instance)"),
    CPU_TOTAL_CORE_NUM(Type.OS, "count(agent_cpu_seconds_total{mode='system',host='ogbrench'}) by (host,instance)"),
    CPU_TOTAL_AVERAGE_UTILIZATION(
            Type.OS,
            "avg(rate(agent_cpu_seconds_total{mode!='idle',host='ogbrench'}[5m])) by (host,instance) * 100"),
    CPU_TIME(Type.OS, "increase(gauss_instance_time_value{host='ogbrench',stat_name='CPU_TIME'}[5m])"),
    NET_SEND_TIME(Type.OS, "increase(gauss_instance_time_value{host='ogbrench',stat_name='NET_SEND_TIME'}[5m])"),
    DATA_IO_TIME(Type.OS, "increase(gauss_instance_time_value{host='ogbrench',stat_name='DATA_IO_TIME'}[5m])"),

    // memory
    MEMORY_USED(
            Type.OS,
            "(agent_free_Mem_total_bytes{host='ogbrench'}-agent_free_Mem_available_bytes{host='ogbrench'})*100"
                    + "/agent_free_Mem_total_bytes{host='ogbrench'}"),
    MEMORY_DB_USED(Type.DB, "top_db_mem{instanceId='ogbrench'}"),
    MEMORY_SWAP(
            Type.OS,
            "agent_free_Swap_used_bytes{host='ogbrench'}/agent_free_Swap_total_bytes{host='ogbrench'} *100"),

    // IO
    IOPS_R(Type.OS, "sum(rate(agent_disk_rd_ios_total{host='ogbrench'}[5m]))by(device)", "{device}"),
    IOPS_R_TOTAL(Type.OS, "sum(rate(agent_disk_rd_ios_total{host='ogbrench'}[5m]))"),
    IOPS_W(Type.OS, "sum(rate(agent_disk_wr_ios_total{host='ogbrench'}[5m]))by(device)", "{device}"),
    IO_DISK_READ_BYTES_PER_SECOND(Type.OS, "sum(rate(agent_disk_rd_sectors_total{host='ogbrench'}[5m]))by(device) "
            + "*512 /1024",
            "{device}"),
    IO_DISK_WRITE_BYTES_PER_SECOND(Type.OS,
            "sum(rate(agent_disk_wr_sectors_total{host='ogbrench'}[5m]))by(device) *512 /1024", "{device}"),
    IO_QUEUE_LENGTH(Type.OS, "sum(rate(agent_disk_rq_ticks_total{host='ogbrench'}[5m]))by(device)", "{device}"),
    IO_UTIL(Type.OS, "sum(rate(agent_disk_tot_ticks_total{host='ogbrench'}[5m])) by(device) / 1000", "{device}"),
    IO_AVG_REPONSE_TIME_READ(
            Type.OS,
            "sum(rate(agent_disk_rd_ticks_total{host='ogbrench'}[1m])"
                    + "/rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])) by (device)",
            "{device}"),
    IO_AVG_REPONSE_TIME_WRITE(
            Type.OS,
            "sum(rate(agent_disk_wr_ticks_total{host='ogbrench'}[1m])"
                    + "/rate(agent_disk_wr_ios_total{host='ogbrench'}[1m])) by (device)",
            "{device}"),
    IO_AVG_REPONSE_TIME_RW(
            Type.OS,
            "sum((rate(agent_disk_rd_ticks_total{host='ogbrench'}[1m])"
                    + "+rate(agent_disk_wr_ticks_total{host='ogbrench'}[1m]))"
                    + "/(rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])"
                    + "+rate(agent_disk_wr_ios_total{host='ogbrench'}[1m]))) by (device)",
            "{device}"),
    IO_DISK_USAGE(
            Type.OS,
            "sum(agent_filesystem_used_size_kbytes{host='ogbrench'}) by (device,instance) / "
                    + "sum(agent_filesystem_size_kbytes{host='ogbrench'}) by (device,instance) * 100",
            "{device}"),
    IO_DISK_INODE_USAGE(
            Type.OS,
            "sum(agent_filesystem_inode_used_size{host='ogbrench'}) by (device) / "
                    + "sum(agent_filesystem_inode_size{host='ogbrench'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_VOLUME_DATA(
            Type.DB,
            "db_filesystem_data_used_size_kbytes{instance='ogbrench'}",
            "{dir},{part}"),
    IO_DISK_DB_VOLUME_XLOG(
            Type.DB,
            "db_filesystem_xlog_used_size_kbytes{instance='ogbrench'}",
            "{dir},{part}"),
    IO_DISK_DB_USAGE_DATA(
            Type.DB,
            "sum(db_filesystem_data_used_size_kbytes{instance='ogbrench'}) by (device) / "
                    + "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_USAGE_XLOG(
            Type.DB,
            "sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench'}) by (device) / "
                    + "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_VOLUME_TOTAL(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) ",
            "{device}"),
    IO_DISK_XLOG_VOLUME_TOTAL(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) ",
            "{device}"),
    IO_DISK_DB_VOLUME_USED(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)",
            "{device}"),
    IO_DISK_DB_VOLUME_OTHER(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_data_used_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench'}) by (device)",
            "{device}"),
    IO_DISK_DB_VOLUME_OTHER_TWO(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_data_used_size_kbytes{instance='ogbrench'}) by (device)",
            "{device}"),
    IO_DISK_XLOG_VOLUME_USED(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)",
            "{device}"),
    IO_DISK_XLOG_VOLUME_OTHER(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_data_used_size_kbytes{instance='ogbrench'}) by (device)"
                    + " - sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench'}) by (device)",
            "{device}"),
    IO_DISK_XLOG_VOLUME_OTHER_TWO(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)",
            "{device}"),
    IO_DISK_DB_VOLUME_FREE(
            Type.DB,
            "sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) ",
            "{device}"),
    IO_DISK_DB_USAGE_TOTAL(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) / "
                    + "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) * 100",
            "{device}"),
    IO_DISK_XLOG_USAGE_TOTAL(
            Type.DB,
            "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) / "
                    + "sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_USAGE_USED(
            Type.DB,
            "(sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device))"
                    + " / sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_USAGE_OTHER(
            Type.DB,
            "(sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_data_used_size_kbytes{instance='ogbrench'}) by (device)"
                    + " - sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench'}) by (device))"
                    + " / sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_USAGE_OTHER_TWO(
            Type.DB,
            "(sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device)"
                    + " - sum(db_filesystem_data_used_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device))"
                    + " / sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='dataDir'}) by (device) * 100",
            "{device}"),
    IO_DISK_XLOG_USAGE_USED(
            Type.DB,
            "(sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device))"
                    + " / sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) * 100",
            "{device}"),
    IO_DISK_XLOG_USAGE_OTHER(
            Type.DB,
            "(sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_data_used_size_kbytes{instance='ogbrench'}) by (device)"
                    + " - sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench'}) by (device))"
                    + " / sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) * 100",
            "{device}"),
    IO_DISK_XLOG_USAGE_OTHER_TWO(
            Type.DB,
            "(sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_free_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device)"
                    + " - sum(db_filesystem_xlog_used_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device))"
                    + " / sum(db_filesystem_size_kbytes{instance='ogbrench',dirType='xlog'}) by (device) * 100",
            "{device}"),
    IO_DISK_DB_USAGE_FREE(
            Type.DB,
            "sum(db_filesystem_free_size_kbytes{instance='ogbrench'}) by (dirType) / "
                    + "sum(db_filesystem_size_kbytes{instance='ogbrench'}) by (dirType) * 100",
            "{device}"),
    // network
    NETWORK_IN(Type.OS, "max(rate(agent_network_receive_bytes_total{host='ogbrench'}[5m]) / 1024) by (device)",
            "{device}"),
    NETWORK_OUT(Type.OS, "max(rate(agent_network_transmit_bytes_total{host='ogbrench'}[5m]) / 1024) by (device)",
            "{device}"),
    NETWORK_LOST_PACKAGE_IN(
            Type.OS,
            "sum(rate(agent_network_receive_dropped_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_ERR_PACKAGE_IN(
            Type.OS,
            "sum(rate(agent_network_receive_errors_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_LOST_PACKAGE_OUT(
            Type.OS,
            "sum(rate(agent_network_transmit_dropped_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_ERR_PACKAGE_OUT(
            Type.OS,
            "sum(rate(agent_network_transmit_errors_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_LOST_PACKAGE(
            Type.OS,
            "sum(rate(agent_network_transmit_dropped_total{host='ogbrench'}[5m])"
                    + "+rate(agent_network_receive_dropped_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_TCP_ALLOC(Type.OS, "agent_sockstat_TCP_alloc{host='ogbrench'}"),
    NETWORK_CURRESTAB(Type.OS, "agent_netstat_Tcp_CurrEstab{host='ogbrench'}"),
    NETWORK_TCP_INSEGS(Type.OS, "rate(agent_netstat_Tcp_InSegs{host='ogbrench'}[5m])"),
    NETWORK_TCP_OUTSEGS(Type.OS, "rate(agent_netstat_Tcp_OutSegs{host='ogbrench'}[5m])"),

    NETWORK_TCP_SOCKET(Type.OS, "agent_network_socket{proto=~'tcp|tcp6',host='ogbrench'}", "{state}"),
    NETWORK_UDP_SOCKET(Type.OS, "agent_network_socket{proto=~'udp|udp6',host='ogbrench'}"),

    // opengauss instance
    INSTANCE_TPS_COMMIT(Type.DB, "sum(irate(pg_stat_database_xact_rollback_total{instanceId='ogbrench'}[5m]))"),
    INSTANCE_TPS_ROLLBACK(Type.DB, "sum(irate(pg_stat_database_xact_commit_total{instanceId='ogbrench'}[5m]))"),
    INSTANCE_TPS_CR(
            Type.DB,
            "sum(irate(pg_stat_database_xact_rollback_total{instanceId='ogbrench'}[5m])) "
                    + "+ sum(irate(pg_stat_database_xact_commit_total{instanceId='ogbrench'}[5m]))"),

    INSTANCE_QPS(Type.DB, "sum(rate(gauss_workload_sql_count_select_count{instanceId='ogbrench'}[5m]))"),
    INSTANCE_DB_CONNECTION_ACTIVE(Type.DB, "sum(pg_stat_activity_count{instanceId='ogbrench',state='active'})"),
    INSTANCE_DB_CONNECTION_IDLE(Type.DB, "sum(pg_stat_activity_count{instanceId='ogbrench',state='idle'})"),
    INSTANCE_DB_CONNECTION_CURR(Type.DB, "sum(pg_stat_activity_count{instanceId='ogbrench'})"),
    INSTANCE_DB_CONNECTION_TOTAL(Type.DB, "pg_connections_max_conn{instanceId='ogbrench'}"),

    INSTANCE_DB_SLOWSQL(Type.DB, "sum(pg_stat_activity_slow_count{instanceId='ogbrench',state!='idle'})"),
    INSTANCE_DB_SLOWSQL_DBNAME(
            Type.DB, "sum(pg_stat_activity_slow_count{instanceId='ogbrench',state!='idle',datname='postgres'})"),
    INSTANCE_DB_RESPONSETIME_P80(Type.DB, "gauss_statement_responsetime_percentile_p80{instanceId="
            + "'ogbrench'}"),
    INSTANCE_DB_RESPONSETIME_P95(Type.DB, "gauss_statement_responsetime_percentile_p95{instanceId="
            + "'ogbrench'}"),
    INSTANCE_DB_DATABASE_INS(Type.DB, "sum(rate(pg_stat_database_tup_inserted_total{instanceId='ogbrench'}[5m])) by "
            + "(datname)", "{datname}"),
    INSTANCE_DB_DATABASE_UPD(Type.DB, "sum(rate(pg_stat_database_tup_updated_total{instanceId='ogbrench'}[5m])) by "
            + "(datname)", "{datname}"),
    INSTANCE_DB_DATABASE_DEL(Type.DB, "sum(rate(pg_stat_database_tup_deleted_total{instanceId='ogbrench'}[5m])) by "
            + "(datname)", "{datname}"),
    INSTANCE_DB_DATABASE_RETURN(Type.DB, "sum(rate(pg_stat_database_tup_returned_total{instanceId='ogbrench'}[5m])) "
            + "by (datname)", "{datname}"),
    INSTANCE_DB_DATABASE_FECTH(Type.DB, "sum(rate(pg_stat_database_tup_fetched_total{instanceId='ogbrench'}[5m])) "
            + "by (datname)", "{datname}"),
    INSTANCE_DB_DATABASE_BLK(Type.DB, "sum(rate(pg_stat_database_blks_read_total{instanceId='ogbrench'}[5m])) "
            + "by (datname)", "{datname}"),
    INSTANCE_DB_BGWRITER_CHECKPOINT(Type.DB, "rate(pg_stat_bgwriter_buffers_checkpoint_total{instanceId='ogbrench"
            + "'}[5m])"),
    INSTANCE_DB_BGWRITER_CLEAN(Type.DB, "rate(pg_stat_bgwriter_buffers_clean_total{instanceId='ogbrench"
            + "'}[5m])"),
    INSTANCE_DB_BGWRITER_BACKEND(Type.DB, "rate(pg_stat_bgwriter_buffers_backend_total{instanceId='ogbrench"
            + "'}[5m])"),

    PG_TABLESPACE_SIZE(Type.DB, "pg_tablespace_size{instanceId='ogbrench'}", "{spcname}"),

    // opengauss session
    SESSION_MAX_CONNECTION(Type.DB, "pg_connections_max_conn{instanceId='ogbrench'}"),
    SESSION_IDLE_CONNECTION(Type.DB, "pg_state_activity_group_count{state='idle',instanceId='ogbrench'}"),
    SESSION_ACTIVE_CONNECTION(Type.DB, "pg_state_activity_group_count{state='active',instanceId='ogbrench'}"),
    SESSION_WAITING_CONNECTION(Type.DB, "pg_state_activity_group_count{state='waiting',instanceId='ogbrench'}"),

    // wait event !pg_wait_events_total_wait_time
    WAIT_EVENT_COUNT(Type.DB, "gauss_wait_events_value{instanceId='ogbrench'}", "{type}"),

    // cluster
    CLUSTER_PRIMARY_WAL_WRITE_TOTAL(Type.DB, "increase(pg_wal_write_total_count{instanceId='ogbrench'}[1d])"),
    CLUSTER_PRIMARY_WAL_SEND_PRESSURE(Type.DB, "pg_wal_send_pressure_count{instanceId='ogbrench'}"),
    CLUSTER_PRIMARY_WAL_WRITE_PER_SEC(Type.DB, "rate(pg_wal_write_total_count{instanceId='ogbrench'}[2m])"),
    CLUSTER_WAL_RECEIVED_DELAY(Type.DB, "pg_wal_delay_received{instanceId='ogbrench'}"),
    CLUSTER_WAL_WRITE_DELAY(Type.DB, "pg_wal_delay_write{instanceId='ogbrench'}"),
    CLUSTER_WAL_REPLAY_DELAY(Type.DB, "pg_wal_delay_replay{instanceId='ogbrench'}");

    private enum Type {
        OS,
        DB
    }

    private Type type;
    private String expression;
    private String template;

    private MetricsLine(Type type, String expression) {
        this.type = type;
        this.expression = expression;
    }

    private MetricsLine(Type type, String expression, String template) {
        this.type = type;
        this.expression = expression;
        this.template = template;
    }

    /**
     * node to promQl
     *
     * @param node instance node
     * @return promQl
     */
    public String promQl(OpsClusterNodeVO node) {
        return this.promQl(node.getHostId(), node.getNodeId());
    }

    public String promQl(String host, String node) {
        return this.expression.replace("ogbrench", this.type == Type.OS ? host : node);
    }
}