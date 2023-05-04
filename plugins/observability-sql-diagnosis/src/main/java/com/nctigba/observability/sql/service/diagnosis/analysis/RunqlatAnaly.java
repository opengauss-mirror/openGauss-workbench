package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
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
public class RunqlatAnaly extends AbstractHeatMap {
	private static final String TIP = "RunqlatAnaly.TIP";
	private static final String Unit = LocaleString.format("RunqlatAnaly.unit");

	@Override
	protected void generateSuggestion(ResultType resultType, Task task, List<HeatMap> heatMap) {
		var map = heatMap.stream().filter(data -> {
			return NumberUtils.toInt(data.getName(), 0) == task.getData().getPid().intValue();
		}).findFirst().orElse(null);
		heatMap.clear();
		if (map == null)
			return;
		heatMap.add(map);
		List<ArrayList<Integer>> source = map.getSource();
		map.setName(LocaleString.format("RunqlatAnaly.name"));
		ArrayList<Integer> sum = new ArrayList<>();
		for (ArrayList<Integer> arrayList : source)
			for (int i = 0; i < arrayList.size(); i++) {
				if (sum.size() <= i)
					sum.add(0);
				sum.set(i, arrayList.get(i) + sum.get(i));
			}
		int r10 = 0;
		int ra = 0;
		for (int i = 0; i < sum.size(); i++) {
			if (i >= 10)
				r10 += sum.get(i);
			ra += sum.get(i);
		}
		float rate = 0;
		if(ra!=0){
			rate=(float) r10 / ra * 100;
		}

		if (rate > 5) {
			var top = new TaskResult(task, ResultState.Suggestions, resultType, FrameType.Suggestion, bearing.top);
			top.setData(Map.of("title", LocaleString.format("RunqlatAnaly.title"), "suggestions", Arrays.asList(LocaleString.format(TIP, rate, 5))));
			resultMapper.insert(top);
		}
	}

	@Override
	protected String getUnit() {
		return Unit;
	}
}