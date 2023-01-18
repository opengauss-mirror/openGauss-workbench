package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.ArrayList;
import java.util.Arrays;
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

import lombok.Getter;

@Service
@Getter
public class RunqlenAnaly extends AbstractHeatMap {
	private static final String TIP = "RunqlenAnaly.TIP";
	private static final String Unit = LocaleString.format("RunqlenAnaly.Unit");

	@Override
	protected void generateSuggestion(ResultType resultType, Task task, List<HeatMap> heatMap) {
		List<ArrayList<Integer>> source = heatMap.get(0).getSource();
		heatMap.get(0).setName(LocaleString.format("RunqlenAnaly.name"));
		ArrayList<Integer> sum = new ArrayList<>();
		for (ArrayList<Integer> arrayList : source)
			for (int i = 0; i < arrayList.size(); i++) {
				if (sum.size() <= i)
					sum.add(0);
				sum.set(i, arrayList.get(i) + sum.get(i));
			}
		int r3 = 0;
		int ra = 0;
		for (int i = 0; i < sum.size(); i++) {
			if (i >= 3)
				r3 += sum.get(i);
			ra += sum.get(i);
		}
		var rate = (float) r3 / ra * 100;
		if (rate > 5) {
			var top = new TaskResult(task, ResultState.Suggestions, resultType, FrameType.Suggestion, bearing.top);
			top.setData(Map.of("title", LocaleString.format("RunqlenAnaly.title"), "suggestions",
					Arrays.asList(LocaleString.format(TIP, rate, 5))));
			resultMapper.insert(top);
		}
	}

	@Override
	protected String getUnit() {
		return Unit;
	}
}