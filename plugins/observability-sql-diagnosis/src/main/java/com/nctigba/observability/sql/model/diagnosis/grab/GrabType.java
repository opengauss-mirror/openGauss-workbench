package com.nctigba.observability.sql.model.diagnosis.grab;

import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.service.diagnosis.analysis.*;
import com.nctigba.observability.sql.service.diagnosis.param.*;
import com.nctigba.observability.sql.util.BeanUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public enum GrabType {
	profile(ResultType.OnCpu, OnCpuAnaly.class),
	offcputime(ResultType.OffCpu, OffCpuAnaly.class),
	runqlen(ResultType.CpuWait, RunqlenAnaly.class),
	runqlat(ResultType.runqlat, RunqlatAnaly.class),
	cachestat(ResultType.cachestat, Cachestat.class),
	memleak(ResultType.Memleak, MemleakAnaly.class),
	xfsdist(ResultType.Xfsdist, XfsdistAnaly.class),
	xfsslower(ResultType.Xfsslower, XfsslowerAnaly.class),
	biolatency(ResultType.Biolatency, Biolatency.class),
	biosnoop(ResultType.Biosnoop, Biosnoop.class),
	filetop(ResultType.FileTop, FileTop.class),
	tcplife(ResultType.TcplifeTCP, TcpLife.class),
	tcptop(ResultType.TcpTop, TcpTop.class),
	mpstatP(ResultType.CPU, MpstatP.class),
	pidstat1(ResultType.user, Pidstat1.class),
	osParam(ResultType.OsParamInfo, OsParamAnalysis.class);
	/*tcpMaxTwBuckets(ResultType.TcpMaxTwBuckets, OsParamAnalysis.class),
	tcpTwReuse(ResultType.TcpTwReuse, OsParamAnalysis.class),
	tcpTwRecycle(ResultType.TcpTwRecycle, OsParamAnalysis.class),
	tcpKeepaliveTime(ResultType.TcpKeepaliveTime, OsParamAnalysis.class),
	tcpKeepaliveProbes(ResultType.TcpKeepaliveProbes, OsParamAnalysis.class),
	tcpKeepaliveIntvl(ResultType.TcpKeepaliveIntvl,OsParamAnalysis.class),
	tcpRetries1(ResultType.TcpRetries1,OsParamAnalysis.class),
	tcpSynRetries(ResultType.TcpSynRetries,OsParamAnalysis.class),
	tcpSynackRetries(ResultType.TcpSynackRetries,OsParamAnalysis.class),
	tcpRetries2(ResultType.TcpRetries2,OsParamAnalysis.class),
	overcommitMemory(ResultType.OvercommitMemory,OsParamAnalysis.class),
	tcpRmem(ResultType.TcpRmem,OsParamAnalysis.class),
	tcpWmem(ResultType.TcpWmem,OsParamAnalysis.class),
	wmemMax(ResultType.WmemMax,OsParamAnalysis.class),
	rmemMax(ResultType.RmemMax,OsParamAnalysis.class),
	wmemDefault(ResultType.WmemDefault,OsParamAnalysis.class),
	rmemDefault(ResultType.RmemDefault,OsParamAnalysis.class),
	ipLocalPortRange(ResultType.IpLocalPortRange,OsParamAnalysis.class),
	sem(ResultType.Sem,OsParamAnalysis.class),
	minFreeKbytes(ResultType.MinFreeKbytes,OsParamAnalysis.class),
	somaxconn(ResultType.Somaxconn,OsParamAnalysis.class),
	tcpSyncookies(ResultType.TcpSyncookies,OsParamAnalysis.class),
	netdevMaxBacklog(ResultType.NetdevMaxBacklog,OsParamAnalysis.class),
	tcpMaxSynBacklog(ResultType.TcpMaxSynBacklog,OsParamAnalysis.class),
	tcpFinTimeout(ResultType.TcpFinTimeout,OsParamAnalysis.class),
	shmall(ResultType.Shmall,OsParamAnalysis.class),
	shmmax(ResultType.Shmmax,OsParamAnalysis.class),
	tcpSack(ResultType.TcpSack,OsParamAnalysis.class),
	tcpTimestamps(ResultType.TcpTimestamps,OsParamAnalysis.class),
	extfragThreshold(ResultType.ExtfragThreshold,OsParamAnalysis.class),
	overcommitRatio(ResultType.OvercommitRatio,OsParamAnalysis.class),
	mtu(ResultType.Mtu, OsParamAnalysis.class);*/
	/*tcpMaxTwBuckets(ResultType.TcpMaxTwBuckets, TcpMaxTwBuckets.class),
	tcpTwReuse(ResultType.TcpTwReuse, TcpTwReuse.class),
	tcpTwRecycle(ResultType.TcpTwRecycle, TcpTwRecycle.class),
	tcpKeepaliveTime(ResultType.TcpKeepaliveTime, TcpKeepaliveTime.class),
	tcpKeepaliveProbes(ResultType.TcpKeepaliveProbes, TcpKeepaliveProbes.class),
	tcpKeepaliveIntvl(ResultType.TcpKeepaliveIntvl,TcpKeepaliveIntvl.class),
	tcpRetries1(ResultType.TcpRetries1,TcpRetries1.class),
	tcpSynRetries(ResultType.TcpSynRetries,TcpSynRetries.class),
	tcpSynackRetries(ResultType.TcpSynackRetries,TcpSynackRetries.class),
	tcpRetries2(ResultType.TcpRetries2,TcpRetries2.class),
	overcommitMemory(ResultType.OvercommitMemory,OvercommitMemory.class),
	tcpRmem(ResultType.TcpRmem,TcpRmem.class),
	tcpWmem(ResultType.TcpWmem,TcpWmem.class),
	wmemMax(ResultType.WmemMax,WmemMax.class),
	rmemMax(ResultType.RmemMax,RmemMax.class),
	wmemDefault(ResultType.WmemDefault,WmemDefault.class),
	rmemDefault(ResultType.RmemDefault,RmemDefault.class),
	ipLocalPortRange(ResultType.IpLocalPortRange,IpLocalPortRange.class),
	sem(ResultType.Sem,Sem.class),
	minFreeKbytes(ResultType.MinFreeKbytes,MinFreeKbytes.class),
	somaxconn(ResultType.Somaxconn,Somaxconn.class),
	tcpSyncookies(ResultType.TcpSyncookies,TcpSyncookies.class),
	netdevMaxBacklog(ResultType.NetdevMaxBacklog,NetdevMaxBacklog.class),
	tcpMaxSynBacklog(ResultType.TcpMaxSynBacklog,TcpMaxSynBacklog.class),
	tcpFinTimeout(ResultType.TcpFinTimeout,TcpFinTimeout.class),
	shmall(ResultType.Shmall,Shmall.class),
	shmmax(ResultType.Shmmax,Shmmax.class),
	tcpSack(ResultType.TcpSack,TcpSack.class),
	tcpTimestamps(ResultType.TcpTimestamps,TcpTimestamps.class),
	extfragThreshold(ResultType.ExtfragThreshold,ExtfragThreshold.class),
	overcommitRatio(ResultType.OvercommitRatio,OvercommitRatio.class),
	mtu(ResultType.Mtu, Mtu.class);*/

	private ResultType belongs;
	private ResultAnalysis analysis;

	GrabType(ResultType type, Class<? extends ResultAnalysis> c) {
		this.belongs = type;
		if (c != null)
			this.analysis = BeanUtils.getBean(c);
	}

	public void analysis(Task task, MultipartFile file) {
		if (this.analysis == null)
			return;
		try {
			this.analysis.analysis(this, task, file);
		} catch (Exception e) {
			task.addRemarks("analysis err " + this.name(), e);
		}
	}

}