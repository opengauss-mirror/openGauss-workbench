package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.Frame.bearing;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PercentUtil;
import com.nctigba.observability.sql.util.TableFormatter;

@Service
public class Cachestat implements ResultAnalysis {
	private static final String TIP = "Cachestat.TIP";
	@Autowired
	private DiagnosisTaskResultMapper resultMapper;

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		var center = new TaskResult(task, ResultState.NoAdvice, grabType.getBelongs(), FrameType.Table, bearing.center);
		var table = TableFormatter.format(file, "TIME");
		center.setData(table);
		resultMapper.insert(center);

		if (table.getData() == null)
			return;
		table.getData().stream().anyMatch(row -> {
			if (PercentUtil.parse(row.get("HITRATIO"), 1) < 0.9) {
				resultMapper.insert(new TaskResult(task, ResultState.Suggestions, grabType.getBelongs(),
						FrameType.Suggestion, bearing.top)
						.setData(Map.of("title", LocaleString.format("Cachestat.title"), "suggestions", Arrays
								.asList(LocaleString.format(TIP, task.getData().getPid(), row.get("HITRATIO"))))));
				return true;
			}
			return false;
		});
	}
}