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