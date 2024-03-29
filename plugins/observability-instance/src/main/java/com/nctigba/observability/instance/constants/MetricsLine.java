/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.constants;

import lombok.Generated;
import lombok.Getter;

@Getter
@Generated
public enum MetricsLine {
    // index
    CPU(Type.OS, "(avg(sum(irate(agent_cpu_seconds_total{mode!='idle',host='ogbrench'}[5m]))by (cpu))) * 100"),
    MEMORY(Type.OS,
            "(1 - (agent_memory_MemAvailable_bytes{host='ogbrench'} /"
                    + " (agent_memory_MemTotal_bytes{host='ogbrench'}))) * 100"),
    NETWORK_IN_TOTAL(Type.OS, "max(rate(agent_network_receive_bytes_total{host='ogbrench'}[5m])*8)"),
    NETWORK_OUT_TOTAL(Type.OS, "max(rate(agent_network_transmit_bytes_total{host='ogbrench'}[5m])*8)"),
    IO(Type.OS, "sum(rate(agent_disk_tot_ticks_total{host='ogbrench'}[5m])) / 1000"),
    SWAP(Type.OS, "agent_free_Swap_used_bytes{host='ogbrench'}/agent_free_Swap_total_bytes{host='ogbrench'} *100"),
    DB_THREAD_POOL(Type.DB, "local_threadpool_status_pool_utilization_rate{instanceId='ogbrench'}"),
    DB_ACTIVE_SESSION(Type.DB, "gauss_thread_wait_status_count{instanceId='ogbrench'}", "{wait_status}"),

    // CPU
    CPU_TOTAL(Type.OS, "(avg(sum(irate(agent_cpu_seconds_total{mode!='idle',host='ogbrench'}[5m]))by (cpu))) * 100"),
    CPU_USER(Type.OS, "(avg(sum(irate(agent_cpu_seconds_total{mode='user',host='ogbrench'}[5m]))by (cpu))) * 100"),
    CPU_SYSTEM(Type.OS, "(avg(sum(irate(agent_cpu_seconds_total{mode='system',host='ogbrench'}[5m]))by (cpu))) * 100"),
    CPU_IOWAIT(Type.OS, "(avg(sum(irate(agent_cpu_seconds_total{mode='iowait',host='ogbrench'}[5m]))by (cpu))) * 100"),
    CPU_DB(Type.DB, "top_db_cpu{instanceId='ogbrench'}"),

    CPU_TOTAL_5M_LOAD(Type.OS, "sum(agent_load5{host='ogbrench'})"),
    CPU_TOTAL_CORE_NUM(Type.OS, "count(agent_cpu_seconds_total{mode='system',host='ogbrench'}) by (host)"),
    CPU_TOTAL_AVERAGE_UTILIZATION(Type.OS,
            "avg(rate(agent_cpu_seconds_total{mode!='idle',host='ogbrench'}[5m])) by (host) * 100"),
    CPU_TIME(Type.OS, "increase(gauss_instance_time_value{host='ogbrench',stat_name='CPU_TIME'}[5m])"),
    NET_SEND_TIME(Type.OS, "increase(gauss_instance_time_value{host='ogbrench',stat_name='NET_SEND_TIME'}[5m])"),
    DATA_IO_TIME(Type.OS, "increase(gauss_instance_time_value{host='ogbrench',stat_name='DATA_IO_TIME'}[5m])"),

    // memory
    MEMORY_USED(Type.OS,
            "(agent_free_Mem_total_bytes{host='ogbrench'}-agent_free_Mem_available_bytes{host='ogbrench'})*100"
                    + "/agent_free_Mem_total_bytes{host='ogbrench'}"),
    MEMORY_DB_USED(Type.DB, "top_db_mem{instanceId='ogbrench'}"),
    MEMORY_SWAP(Type.OS,
            "agent_free_Swap_used_bytes{host='ogbrench'}/agent_free_Swap_total_bytes{host='ogbrench'} *100"),

    // IO
    IOPS_R(Type.OS, "sum(rate(agent_disk_rd_ios_total{host='ogbrench'}[5m]))by(device)", "{device}"),
    IOPS_W(Type.OS, "sum(rate(agent_disk_wr_ios_total{host='ogbrench'}[5m]))by(device)", "{device}"),
    IO_DISK_READ_BYTES_PER_SECOND(Type.OS, "sum(rate(agent_disk_rd_sectors_total{host='ogbrench'}[5m]))by(device) *512",
            "{device}"),
    IO_DISK_WRITE_BYTES_PER_SECOND(Type.OS,
            "sum(rate(agent_disk_wr_sectors_total{host='ogbrench'}[5m]))by(device) *512", "{device}"),
    IO_QUEUE_LENGTH(Type.OS, "sum(rate(agent_disk_rq_ticks_total{host='ogbrench'}[5m]))by(device)", "{device}"),
    IO_UTIL(Type.OS, "sum(rate(agent_disk_tot_ticks_total{host='ogbrench'}[5m])) by(device) / 1000", "{device}"),
    IO_AVG_REPONSE_TIME_READ(Type.OS,
            "sum(rate(agent_disk_rd_ticks_total{host='ogbrench'}[1m])"
                    + "/rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])) by (device)",
            "{device}"),
    IO_AVG_REPONSE_TIME_WRITE(Type.OS,
            "sum(rate(agent_disk_wr_ticks_total{host='ogbrench'}[1m])"
                    + "/rate(agent_disk_wr_ios_total{host='ogbrench'}[1m])) by (device)",
            "{device}"),
    IO_AVG_REPONSE_TIME_RW(Type.OS,
            "sum((rate(agent_disk_rd_ticks_total{host='ogbrench'}[1m])"
                    + "+rate(agent_disk_wr_ticks_total{host='ogbrench'}[1m]))"
                    + "/(rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])"
                    + "+rate(agent_disk_wr_ios_total{host='ogbrench'}[1m]))) by (device)",
            "{device}"),

    // network
    NETWORK_IN(Type.OS, "max(rate(agent_network_receive_bytes_total{host='ogbrench'}[5m])*8) by (device)", "{device}"),
    NETWORK_OUT(Type.OS, "max(rate(agent_network_transmit_bytes_total{host='ogbrench'}[5m])*8) by (device)",
            "{device}"),
    NETWORK_LOST_PACKAGE(Type.OS,
            "sum(rate(agent_network_transmit_dropped_total{host='ogbrench'}[5m])"
                    + "+rate(agent_network_receive_dropped_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_TCP_ALLOC(Type.OS, "agent_sockstat_TCP_alloc{host='ogbrench'}"),
    NETWORK_CURRESTAB(Type.OS, "agent_netstat_Tcp_CurrEstab{host='ogbrench'}"),
    NETWORK_TCP_INSEGS(Type.OS, "rate(agent_netstat_Tcp_InSegs{host='ogbrench'}[5m])"),
    NETWORK_TCP_OUTSEGS(Type.OS, "rate(agent_netstat_Tcp_OutSegs{host='ogbrench'}[5m])"),

    NETWORK_TCP_SOCKET(Type.OS, "agent_network_socket{proto=~'tcp|tcp6',host='ogbrench'}", "{state}"),
    NETWORK_UDP_SOCKET(Type.OS, "sum(agent_network_socket{proto=~'udp|udp6',host='ogbrench'})"),

    // opengauss instance
    INSTANCE_TPS_COMMIT(Type.DB, "sum(irate(pg_stat_database_xact_rollback_total{instanceId='ogbrench'}[5m]))"),
    INSTANCE_TPS_ROLLBACK(Type.DB, "sum(irate(pg_stat_database_xact_commit_total{instanceId='ogbrench'}[5m]))"),
    INSTANCE_TPS_CR(Type.DB,
            "sum(irate(pg_stat_database_xact_rollback_total{instanceId='ogbrench'}[5m])) "
                    + "+ sum(irate(pg_stat_database_xact_commit_total{instanceId='ogbrench'}[5m]))"),

    INSTANCE_QPS(Type.DB, "sum(rate(gauss_workload_sql_count_select_count{instanceId='ogbrench'}[5m]))"),
    INSTANCE_DB_CONNECTION_ACTIVE(Type.DB, "sum(pg_stat_activity_count{instanceId='ogbrench',state='active'})"),
    INSTANCE_DB_CONNECTION_IDLE(Type.DB, "sum(pg_stat_activity_count{instanceId='ogbrench',state='idle'})"),
    INSTANCE_DB_CONNECTION_CURR(Type.DB, "sum(pg_stat_activity_count{instanceId='ogbrench'})"),
    INSTANCE_DB_CONNECTION_TOTAL(Type.DB, "pg_connections_max_conn{instanceId='ogbrench'}"),

    INSTANCE_DB_SLOWSQL(Type.DB, "pg_stat_activity_slow_count{instanceId='ogbrench',state!='idle'}"),

    // opengauss session
    SESSION_MAX_CONNECTION(Type.DB, "pg_connections_max_conn{instanceId='ogbrench'}"),
    SESSION_IDLE_CONNECTION(Type.DB, "pg_state_activity_group_count{state='idle',instanceId='ogbrench'}"),
    SESSION_ACTIVE_CONNECTION(Type.DB, "pg_state_activity_group_count{state='active',instanceId='ogbrench'}"),
    SESSION_WAITING_CONNECTION(Type.DB, "pg_state_activity_group_count{state='waiting',instanceId='ogbrench'}"),

    // wait event !pg_wait_events_total_wait_time
    WAIT_EVENT_COUNT(Type.DB, "gauss_wait_events_value{instanceId='ogbrench'}", "{type}");

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

    public String promQl(String host, String node) {
        return this.expression.replace("ogbrench", this.type == Type.OS ? host : node);
    }
}