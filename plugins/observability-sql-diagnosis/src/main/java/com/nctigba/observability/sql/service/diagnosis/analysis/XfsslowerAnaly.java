package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.opengauss.admin.common.exception.CustomException;
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
import com.nctigba.observability.sql.util.TableFormatter;

@Service
public class XfsslowerAnaly implements ResultAnalysis {
	@Autowired
	private DiagnosisTaskResultMapper resultMapper;
	private static final String TIP = "XfsslowerAnaly.tip";

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		var center = new TaskResult(task, ResultState.NoAdvice, grabType.getBelongs(), FrameType.Table, bearing.center);
		var table = TableFormatter.format(file, "TIME");
		center.setData(table);
		resultMapper.insert(center);

		try {
			if (table == null || table.getData() == null)
				return;
			table.getData().stream().anyMatch(row -> {
				var find = NumberUtils.toInt(row.get("PID"), 0) == task.getData().getPid();
				if (find) {
					resultMapper.insert(new TaskResult(task, ResultState.Suggestions, grabType.getBelongs(),
							FrameType.Suggestion, bearing.top)
							.setData(Map.of("title", LocaleString.format("XfsslowerAnaly.title"), "suggestions",
									Arrays.asList(LocaleString.format(TIP, row.get("FILENAME"))))));
				}
				return find;
			});
		} catch (Exception e) {
			throw new CustomException("XfsslowerAnaly", e);
		}
	}
}