package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.Frame.bearing;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.TableFormatter;

@Service
public class TcpLife implements ResultAnalysis {
	@Autowired
	private DiagnosisTaskResultMapper resultMapper;
	private static final String[] TIP = { "TcpLife.TIP0", "TcpLife.TIP1" };

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		var top = new TaskResult(task, ResultState.NoAdvice, ResultType.TcplifeTCP, FrameType.Table, bearing.center);
		var table = TableFormatter.format(file, "TIME");
		top.setData(table);
		resultMapper.insert(top);

		var counter = new HashMap<String, Set<String>>();
		var thisThread = new HashSet<>();
		table.getData().stream().forEach(row -> {
			var key = row.get("LADDR") + ":" + row.get("LPORT");
			var raddr = row.get("RADDR");
			if (NumberUtils.toInt(row.get("PID"), 0) == task.getData().getPid())
				thisThread.add(raddr);
			if (!counter.containsKey(key))
				counter.put(key, new HashSet<>());
			counter.get(key).add(raddr);
		});
		var suggestions = new ArrayList<>();
		if (thisThread.size() > 1)
			suggestions.add(LocaleString.format(TIP[0], task.getData().getPid(), thisThread.size()));
		counter.forEach((key, set) -> {
			if (set.size() > 1)
				suggestions.add(LocaleString.format(TIP[1], key, set.size()));
		});
		if (suggestions.size() > 0)
			resultMapper.insert(new TaskResult(task, ResultState.Suggestions, ResultType.TcplifeTCP,
					FrameType.Suggestion, bearing.top)
					.setData(Map.of("title", LocaleString.format("TcpLife.title"), "suggestions", suggestions)));
	}
}