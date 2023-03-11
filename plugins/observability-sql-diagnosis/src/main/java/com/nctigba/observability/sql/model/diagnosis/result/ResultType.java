package com.nctigba.observability.sql.model.diagnosis.result;

import com.nctigba.observability.sql.util.LocaleString;
import lombok.Getter;

@Getter
public enum ResultType {
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
	ColdFunction(OffCpu)
	;

	private ResultType parent;

	ResultType(ResultType parent) {
		this.parent = parent;
	}

	public String getText() {
		return LocaleString.format("ResultType." + this.name());
	}

	public TreeNode T() {
		return new TreeNode(this);
	}
}