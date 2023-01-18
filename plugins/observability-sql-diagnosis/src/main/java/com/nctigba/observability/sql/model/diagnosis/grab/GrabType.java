package com.nctigba.observability.sql.model.diagnosis.grab;

import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.service.diagnosis.analysis.Biolatency;
import com.nctigba.observability.sql.service.diagnosis.analysis.Biosnoop;
import com.nctigba.observability.sql.service.diagnosis.analysis.Cachestat;
import com.nctigba.observability.sql.service.diagnosis.analysis.FileTop;
import com.nctigba.observability.sql.service.diagnosis.analysis.MemleakAnaly;
import com.nctigba.observability.sql.service.diagnosis.analysis.MpstatP;
import com.nctigba.observability.sql.service.diagnosis.analysis.OffCpuAnaly;
import com.nctigba.observability.sql.service.diagnosis.analysis.OnCpuAnaly;
import com.nctigba.observability.sql.service.diagnosis.analysis.Pidstat1;
import com.nctigba.observability.sql.service.diagnosis.analysis.RunqlatAnaly;
import com.nctigba.observability.sql.service.diagnosis.analysis.RunqlenAnaly;
import com.nctigba.observability.sql.service.diagnosis.analysis.TcpLife;
import com.nctigba.observability.sql.service.diagnosis.analysis.TcpTop;
import com.nctigba.observability.sql.service.diagnosis.analysis.XfsdistAnaly;
import com.nctigba.observability.sql.service.diagnosis.analysis.XfsslowerAnaly;
import com.nctigba.observability.sql.util.BeanUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;

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
	pidstat1(ResultType.user, Pidstat1.class);

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