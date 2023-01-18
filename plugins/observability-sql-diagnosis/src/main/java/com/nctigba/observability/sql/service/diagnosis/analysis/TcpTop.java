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
public class TcpTop implements ResultAnalysis {
	@Autowired
	private DiagnosisTaskResultMapper resultMapper;
	private static final String[] TIP = { "TcpTop.TIP0", "TcpTop.TIP1" };

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		var top = new TaskResult(task, ResultState.NoAdvice, ResultType.TcpTop, FrameType.Table, bearing.center);
		var table = TableFormatter.format(file, "PID");
		top.setData(table);
		resultMapper.insert(top);

		var counter = new HashMap<String, Set<String>>();
		String[] str = new String[1];
		if (table.getData() == null)
			return;
		table.getData().stream().forEach(row -> {
			var comm = row.get("COMM");
			if (str[0] == null && NumberUtils.toInt(row.get("PID"), 0) == task.getData().getPid())
				str[0] = comm;
			if (!counter.containsKey(comm))
				counter.put(comm, new HashSet<>());
			counter.get(comm).add(row.get("RADDR"));
		});
		var suggestions = new ArrayList<>();
		if (str[0] != null && counter.get(str[0]).size() > 1)
			suggestions.add(LocaleString.format(TIP[0], task.getData().getPid(), counter.get(str[0]).size(), 1));
		counter.forEach((key, set) -> {
			if (key.equals(str[0]))
				return;
			if (set.size() > 1)
				suggestions.add(LocaleString.format(TIP[1], key, set.size(), 1));
		});
		if (suggestions.size() > 0)
			resultMapper.insert(
					new TaskResult(task, ResultState.Suggestions, ResultType.TcpTop, FrameType.Suggestion, bearing.top)
							.setData(Map.of("title", LocaleString.format("TcpTop.title"), "suggestions", suggestions)));
	}
}