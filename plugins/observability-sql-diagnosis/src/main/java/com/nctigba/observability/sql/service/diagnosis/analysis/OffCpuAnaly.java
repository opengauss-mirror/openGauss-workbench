package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.io.IOException;
import java.util.Map;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Resource;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.Frame.bearing;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;

@Service
public class OffCpuAnaly implements ResultAnalysis {
	@Autowired
	private DiagnosisTaskResultMapper resultMapper;
	@Autowired
	private DiagnosisResourceMapper resourceMapper;

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		try {
			Resource resource = new Resource(task, grabType).setF(file.getInputStream().readAllBytes());
			resourceMapper.insert(resource);
			TaskResult resultSvg = new TaskResult(task,
					task.getConf().isOffCpu() ? ResultState.Suggestions : ResultState.NoAdvice, ResultType.OffCpu,
					FrameType.Flamefigure, bearing.top).setData(Map.of("title", LocaleString.format("OffCpuAnaly.title"), "id", resource.getId()));
			resultMapper.insert(resultSvg);
		} catch (IOException e) {
			throw new CustomException("offCpu err", e);
		}
	}
}