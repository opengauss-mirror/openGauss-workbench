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
	ExecPlanDesc(TaskInfo),;

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