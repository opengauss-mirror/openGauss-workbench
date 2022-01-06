package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.Frame.bearing;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;
import com.nctigba.observability.sql.util.HistogramToHeatmap.HeatMap;
import com.nctigba.observability.sql.util.LocaleString;

@Service
public class XfsdistAnaly extends AbstractHeatMap {
	private static final Map<String, String> TIP = Map.of("read",
			"XfsdistAnaly.read", "write",
			"XfsdistAnaly.write", "open",
			"XfsdistAnaly.open", "fsync",
			"XfsdistAnaly.fsync");

	@Override
	protected void generateSuggestion(ResultType resultType, Task task, List<HeatMap> heatMap) {
		var suggestions = new ArrayList<>();
		heatMap.forEach(map -> {
			if (!TIP.containsKey(map.getName()))
				return;
			var source = map.getSource();
			ArrayList<Integer> sum = new ArrayList<>();
			// 数据统计
			for (List<Integer> arrayList : source)
				for (int i = 0; i < arrayList.size(); i++) {
					if (sum.size() <= i)
						sum.add(0);
					sum.set(i, arrayList.get(i) + (sum.size() > i ? sum.get(i) : 0));
				}
			int r10 = 0;
			int ra = 0;
			for (int i = 0; i < sum.size(); i++) {
				if (i >= 10)
					r10 += sum.get(i);
				ra += sum.get(i);
			}
			var rate = (float) r10 / ra * 100;
			if (rate > 1)
				suggestions.add(LocaleString.format(TIP.get(map.getName()), rate, 5));
		});
		if (!suggestions.isEmpty()) {
			var suggestion = new TaskResult(task, ResultState.Suggestions, resultType, FrameType.Suggestion,
					bearing.top);
			suggestion.setData(Map.of("title", LocaleString.format("XfsdistAnaly.title"), "suggestions", suggestions));
			resultMapper.insert(suggestion);
		}
	}

	@Override
	protected String getUnit() {
		return LocaleString.format("XfsdistAnaly.unit");
	}
}