package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.io.IOException;
import java.util.List;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.nctigba.observability.sql.util.HistogramToHeatmap;
import com.nctigba.observability.sql.util.HistogramToHeatmap.HeatMap;

public abstract class AbstractHeatMap implements ResultAnalysis {
	@Autowired
	protected DiagnosisTaskResultMapper resultMapper;
	@Autowired
	private DiagnosisResourceMapper resourceMapper;

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		Resource resource;
		try {
			resource = new Resource(task, grabType).setF(file.getInputStream().readAllBytes());
			resourceMapper.insert(resource);
		} catch (IOException e) {
			throw new CustomException(grabType + ":res err", e);
		}
		var center = new TaskResult(task, ResultState.NoAdvice, grabType.getBelongs(), FrameType.HeatMap,
				bearing.center);
		var heatMap = HistogramToHeatmap.format(file, getUnit());

		generateSuggestion(grabType.getBelongs(), task, heatMap);
		heatMap.forEach(map -> {
			map.setSource(null);
		});
		center.setData(heatMap);
		resultMapper.insert(center);
	}

	protected abstract void generateSuggestion(ResultType resultType, Task task, List<HeatMap> heatMap);

	protected abstract String getUnit();
}