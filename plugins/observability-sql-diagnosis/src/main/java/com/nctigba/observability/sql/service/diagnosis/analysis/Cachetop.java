package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
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
public class Cachetop implements ResultAnalysis {
	@Autowired
	private DiagnosisTaskResultMapper resultMapper;
	private static final String TIP = "Cachetop.TIP";

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		var center = new TaskResult(task, ResultState.NoAdvice, grabType.getBelongs(), FrameType.Table, bearing.center);
		var table = TableFormatter.format(file, "PID");
		center.setData(table);
		resultMapper.insert(center);

		table.getData().stream().anyMatch(row -> {
			if (NumberUtils.toInt(row.get("PID"), 0) == task.getData().getPid()) {
				var READ_HIT = PercentUtil.parse(row.get("READ_HIT"), 1);
				var WRITE_HIT = PercentUtil.parse(row.get("WRITE_HIT"), 1);
				if (READ_HIT < 1 || WRITE_HIT < 1)
					resultMapper.insert(new TaskResult(task, ResultState.Suggestions, grabType.getBelongs(),
							FrameType.Suggestion, bearing.top)
							.setData(Map.of("title", LocaleString.format("Cachetop.title"), "suggestions", Arrays
									.asList(LocaleString.format(TIP, task.getData().getPid(), READ_HIT, WRITE_HIT)))));
				return true;
			}
			return false;
		});
	}
}