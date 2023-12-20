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
 *  ResultTypeEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/enums/ResultTypeEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.enums;

import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.Getter;

@Getter
public enum ResultTypeEnum {
    TaskInfo(null),
    ObjectInfoCheck(TaskInfo),
    ObjectRecommendedToUpdateStatistics(ObjectInfoCheck),
    ExecPlan(TaskInfo),
    PlanRecommendedToCreateIndex(ExecPlan),
    PlanChangedToPartitionTable(ExecPlan),
    PlanRecommendedToQueryBasedOnPartition(ExecPlan),
    PlanRecommendedToDoVacuumCleaning(ExecPlan),
    PlanRecommendedToOptimizeStatementsOrAddWorkMemSize(ExecPlan),
    CPU(ExecPlan),
    OnCpu(CPU),
    user(CPU),
    CpuWait(user),
    runqlat(user),
    RUNQLEN(user),
    system(CPU),
    idle(CPU),
    TcplifeTCP(idle),
    TcpTop(idle),
    iowait(CPU),
    OffCpu(iowait),
    Xfsdist(iowait),
    Xfsslower(iowait),
    Biolatency(iowait),
    Biosnoop(iowait),
    FileTop(iowait),
    Memory(ExecPlan),
    cachestat(Memory),
    CACHETOP(Memory),
    Memleak(Memory),
    FileSystem(ExecPlan),
    NetWork(ExecPlan),
    ExecPlanDesc(TaskInfo),
    ParamInfo(TaskInfo),
    DatabaseParamInfo(ParamInfo),
    OsParamInfo(ParamInfo),
    MaxProcessMemory(DatabaseParamInfo),
    WorkMem(DatabaseParamInfo),
    PagewriterSleep(DatabaseParamInfo),
    BgwriterDelay(DatabaseParamInfo),
    BgwriterThreadNum(DatabaseParamInfo),
    MaxIoCapacity(DatabaseParamInfo),
    LogMinDurationStatement(DatabaseParamInfo),
    LogDuration(DatabaseParamInfo),
    TrackStmtStatLevel(DatabaseParamInfo),
    TrackStmtRetentionTime(DatabaseParamInfo),
    EnableThreadPool(DatabaseParamInfo),
    ThreadPoolAttr(DatabaseParamInfo),
    LogStatement(DatabaseParamInfo),
    LogErrorVerbosity(DatabaseParamInfo),
    LogMinMessages(DatabaseParamInfo),
    LogMinErrorStatement(DatabaseParamInfo),
    TcpMaxTwBuckets(OsParamInfo),
    TcpTwReuse(OsParamInfo),
    TcpTwRecycle(OsParamInfo),
    TcpKeepaliveTime(OsParamInfo),
    TcpKeepaliveProbes(OsParamInfo),
    TcpKeepaliveIntvl(OsParamInfo),
    TcpRetries1(OsParamInfo),
    TcpSynRetries(OsParamInfo),
    TcpSynackRetries(OsParamInfo),
    TcpRetries2(OsParamInfo),
    OvercommitMemory(OsParamInfo),
    TcpRmem(OsParamInfo),
    TcpWmem(OsParamInfo),
    WmemMax(OsParamInfo),
    RmemMax(OsParamInfo),
    WmemDefault(OsParamInfo),
    RmemDefault(OsParamInfo),
    IpLocalPortRange(OsParamInfo),
    Sem(OsParamInfo),
    MinFreeKbytes(OsParamInfo),
    Somaxconn(OsParamInfo),
    TcpSyncookies(OsParamInfo),
    NetdevMaxBacklog(OsParamInfo),
    TcpMaxSynBacklog(OsParamInfo),
    TcpFinTimeout(OsParamInfo),
    Shmall(OsParamInfo),
    Shmmax(OsParamInfo),
    TcpSack(OsParamInfo),
    TcpTimestamps(OsParamInfo),
    ExtfragThreshold(OsParamInfo),
    OvercommitRatio(OsParamInfo),
    Mtu(OsParamInfo),
    HotFunction(OnCpu),
    ColdFunction(OffCpu),
    AUTOVACUUM_MODE(DatabaseParamInfo);

    private ResultTypeEnum parent;

    ResultTypeEnum(ResultTypeEnum parent) {
        this.parent = parent;
    }

    public String getText() {
        return LocaleStringUtils.format("ResultType." + this.name());
    }
}