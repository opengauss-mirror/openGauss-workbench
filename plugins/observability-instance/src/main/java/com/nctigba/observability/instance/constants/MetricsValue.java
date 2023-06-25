/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.constants;

import lombok.Getter;

@Getter
public enum MetricsValue {
    // memory
    MEM_TOTAL(Type.OS, "agent_free_Mem_total_bytes{host='ogbrench'}"),
    MEM_USED(Type.OS, "agent_free_Mem_used_bytes{host='ogbrench'}"),
    MEM_FREE(Type.OS, "agent_free_Mem_free_bytes{host='ogbrench'}"),
    MEM_CACHE(Type.OS, "agent_free_Mem_cache_bytes{host='ogbrench'}"),
    SWAP_TOTAL(Type.OS, "agent_free_Swap_total_bytes{host='ogbrench'}"),
    SWAP_USED(Type.OS, "agent_free_Swap_used_bytes{host='ogbrench'}"),
    SWAP_FREE(Type.OS, "agent_free_Swap_free_bytes{host='ogbrench'}"),

    // i/o table
    IO_TPS(Type.OS,
            "sum((rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])+"
                    + "rate(agent_disk_wr_ios_total{host='ogbrench'}[1m]))) by (device)",
            "{device}"),
    IO_RD(Type.OS, "sum(rate(agent_disk_rd_sectors_total{host='ogbrench'}[1m]))by(device)", "{device}"),
    IO_WT(Type.OS, "sum(rate(agent_disk_wr_sectors_total{host='ogbrench'}[1m]))by(device)", "{device}"),
    IO_AVGRQ_SZ(Type.OS,
            "sum((rate(agent_disk_rd_sectors_total{host='ogbrench'}[1m])+"
                    + "rate(agent_disk_wr_sectors_total{host='ogbrench'}[1m]))"
                    + "/(rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])+"
                    + "rate(agent_disk_wr_ios_total{host='ogbrench'}[1m]))) by (device)",
            "{device}"),
    IO_AVGQU_SZ(Type.OS, "sum(rate(agent_disk_rq_ticks_total{host='ogbrench'}[1m])) by (device)", "{device}"),
    IO_AWAIT(Type.OS,
            "sum((rate(agent_disk_rd_ticks_total{host='ogbrench'}[1m])+"
                    + "rate(agent_disk_wr_ticks_total{host='ogbrench'}[1m]))"
                    + "/(rate(agent_disk_rd_ios_total{host='ogbrench'}[1m])+"
                    + "rate(agent_disk_wr_ios_total{host='ogbrench'}[1m]))) by (device)",
            "{device}"),
    IO_UTIL(Type.OS, "sum(rate(agent_disk_tot_ticks_total{host='ogbrench'}[5m])) by(device) / 1000", "{device}"),

    // network table
    NETWORK_RXPCK(Type.OS, "max(rate(agent_network_receive_packets_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_TXPCK(Type.OS, "max(rate(agent_network_transmit_packets_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_RX(Type.OS, "max(rate(agent_network_receive_bytes_total{host='ogbrench'}[5m])*8) by (device)", "{device}"),
    NETWORK_TX(Type.OS, "max(rate(agent_network_transmit_bytes_total{host='ogbrench'}[5m])*8) by (device)", "{device}"),
    NETWORK_RXERR(Type.OS, "max(rate(agent_network_receive_errors_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_TXERR(Type.OS, "max(rate(agent_network_transmit_errors_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_RXDROP(Type.OS, "max(rate(agent_network_receive_dropped_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_TXDROP(Type.OS, "max(rate(agent_network_transmit_dropped_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_RXFIFO(Type.OS, "max(rate(agent_network_receive_fifo_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),
    NETWORK_TXFIFO(Type.OS, "max(rate(agent_network_transmit_fifo_total{host='ogbrench'}[5m])) by (device)",
            "{device}"),;

    private enum Type {
        OS,
        DB
    }

    private Type type;
    private String expression;
    private String template;

    private MetricsValue(Type type, String expression) {
        this.type = type;
        this.expression = expression;
    }

    private MetricsValue(Type type, String expression, String template) {
        this.type = type;
        this.expression = expression;
        this.template = template;
    }

    public String promQl(String host, String node) {
        if (this.expression == null)
            return null;
        return this.expression.replace("ogbrench", this.type == Type.OS ? host : node);
    }
}