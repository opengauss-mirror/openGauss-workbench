/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.constants;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public enum ParamType {
    tcpMaxTwBuckets("net.ipv4.tcp_max_tw_buckets"),
    mtu("MTU"),
    tcpTwReuse("net.ipv4.tcp_tw_reuse"),
    tcpTwRecycle("net.ipv4.tcp_tw_recycle"),
    tcpKeepaliveTime("net.ipv4.tcp_keepalive_time"),
    tcpKeepaliveProbes("net.ipv4.tcp_keepalive_probes"),
    tcpKeepaliveIntvl("net.ipv4.tcp_keepalive_intvl"),
    tcpRetries1("net.ipv4.tcp_retries1"),
    tcpSynRetries("net.ipv4.tcp_syn_retries"),
    tcpSynackRetries("net.ipv4.tcp_synack_retries"),
    tcpRetries2("net.ipv4.tcp_retries2"),
    overcommitMemory("vm.overcommit_memory"),
    tcpRmem("net.ipv4.tcp_rmem"),
    tcpWmem("net.ipv4.tcp_wmem"),
    wmemMax("net.core.wmem_max"),
    rmemMax("net.core.rmem_max"),
    wmemDefault("net.core.wmem_default"),
    rmemDefault("net.core.rmem_default"),
    ipLocalPortRange("net.ipv4.ip_local_port_range"),
    sem("kernel.sem"),
    minFreeKbytes("vm.min_free_kbytes"),
    somaxconn("net.core.somaxconn"),
    tcpSyncookies("net.ipv4.tcp_syncookies"),
    netdevMaxBacklog("net.core.netdev_max_backlog"),
    tcpMaxSynBacklog("net.ipv4.tcp_max_syn_backlog"),
    tcpFinTimeout("net.ipv4.tcp_fin_timeout"),
    shmall("kernel.shmall"),
    shmmax("kernel.shmmax"),
    tcpSack("net.ipv4.tcp_sack"),
    tcpTimestamps("net.ipv4.tcp_timestamps"),
    extfragThreshold("vm.extfrag_threshold"),
    overcommitRatio("vm.overcommit_ratio");
    /*public static final String DEFAULT = "MTU";
    public static final String tcp_max_tw_buckets = "tcpmaxtwbuckets";
    public static final String tcp_tw_reuse = "net.ipv4.tcp_tw_reuse";
    public static final String tcp_tw_recycle = "net.ipv4.tcp_tw_recycle";
    public static final String tcp_keepalive_time = "net.ipv4.tcp_keepalive_time";
    public static final String tcp_keepalive_probes = "net.ipv4.tcp_keepalive_probes";
    public static final String tcp_keepalive_intvl = "net.ipv4.tcp_keepalive_intvl";
    public static final String tcp_retries1 = "net.ipv4.tcp_retries1";
    public static final String tcp_syn_retries = "net.ipv4.tcp_syn_retries";
    public static final String tcp_synack_retries = "net.ipv4.tcp_synack_retries";
    public static final String tcp_retries2 = "net.ipv4.tcp_retries2";
    public static final String overcommit_memory = "vm.overcommit_memory";
    public static final String tcp_rmem = "net.ipv4.tcp_rmem";
    public static final String tcp_wmem = "net.ipv4.tcp_wmem";
    public static final String wmem_max = "net.core.wmem_max";
    public static final String rmem_max = "net.core.rmem_max";
    public static final String wmem_default = "net.core.wmem_default";
    public static final String rmem_default = "net.core.rmem_default";
    public static final String ip_local_port_range = "net.ipv4.ip_local_port_range";
    public static final String sem = "kernel.sem";
    public static final String min_free_kbytes = "vm.min_free_kbytes";
    public static final String somaxconn = "net.core.somaxconn";
    public static final String tcp_syncookies = "net.ipv4.tcp_syncookies";
    public static final String netdev_max_backlog = "net.core.netdev_max_backlog";
    public static final String tcp_max_syn_backlog = "net.ipv4.tcp_max_syn_backlog";
    public static final String tcp_fin_timeout = "net.ipv4.tcp_fin_timeout";
    public static final String shmall = "kernel.shmall";
    public static final String shmmax = "kernel.shmmax";
    public static final String tcp_sack = "net.ipv4.tcp_sack";
    public static final String tcp_timestamps = "net.ipv4.tcp_timestamps";
    public static final String extfrag_threshold = "vm.extfrag_threshold";
    public static final String overcommit_ratio = "vm.overcommit_ratio";*/

    private String param;
    ParamType(String param) {
        this.param=param;
    }

    public static boolean isExist(String filed){
        ParamType[] fields= ParamType.values();
        for(int i=0;i<fields.length;i++){
            System.out.println(fields[i]);
            if(filed.equals(fields[i].toString()))
            {
                return true;
            }
        }
        return false;
    }
}
