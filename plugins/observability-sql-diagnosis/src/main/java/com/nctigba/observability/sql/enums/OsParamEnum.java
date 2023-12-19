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
 *  OsParamEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/enums/OsParamEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum OsParamEnum {
    TcpMaxTwBuckets("net.ipv4.tcp_max_tw_buckets"),
    TcpTwReuse("net.ipv4.tcp_tw_reuse"),
    TcpTwRecycle("net.ipv4.tcp_tw_recycle"),
    TcpKeepaliveTime("net.ipv4.tcp_keepalive_time"),
    TcpKeepaliveProbes("net.ipv4.tcp_keepalive_probes"),
    TcpKeepaliveIntvl("net.ipv4.tcp_keepalive_intvl"),
    TcpRetries1("net.ipv4.tcp_retries1"),
    TcpSynRetries("net.ipv4.tcp_syn_retries"),
    TcpSynackRetries("net.ipv4.tcp_synack_retries"),
    TcpRetries2("net.ipv4.tcp_retries2"),
    OvercommitMemory("vm.overcommit_memory"),
    TcpRmem("net.ipv4.tcp_rmem"),
    TcpWmem("net.ipv4.tcp_wmem"),
    WmemMax("net.core.wmem_max"),
    RmemMax("net.core.rmem_max"),
    WmemDefault("net.core.wmem_default"),
    RmemDefault("net.core.rmem_default"),
    IpLocalPortRange("net.ipv4.ip_local_port_range"),
    Sem("kernel.sem"),
    MinFreeKbytes("vm.min_free_kbytes"),
    Somaxconn("net.core.somaxconn"),
    TcpSyncookies("net.ipv4.tcp_syncookies"),
    NetdevMaxBacklog("net.core.netdev_max_backlog"),
    TcpMaxSynBacklog("net.ipv4.tcp_max_syn_backlog"),
    TcpFinTimeout("net.ipv4.tcp_fin_timeout"),
    Shmall("kernel.shmall"),
    Shmmax("kernel.shmmax"),
    TcpSack("net.ipv4.tcp_sack"),
    TcpTimestamps("net.ipv4.tcp_timestamps"),
    ExtfragThreshold("vm.extfrag_threshold"),
    OvercommitRatio("vm.overcommit_ratio"),
    Mtu("MTU");
    private String paramName;

    OsParamEnum(String paramName) {
        this.paramName=paramName;
    }
}
