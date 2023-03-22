package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.chart.LineChart;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.Frame.bearing;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;
import com.nctigba.observability.sql.service.diagnosis.ResultAnalysis;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PercentUtil;

import lombok.RequiredArgsConstructor;
/**
 * 
 * CPU all 折线图，x时间，
 * y- %usr、%sys、%iowait、%irq、%soft、%idle
 * %system 0~7 ResultType.system 折线图，x时间，y百分比
11:58:52 AM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle
11:58:53 AM  all    0.25    0.00    0.13    0.00    0.00    0.00    0.00    0.00    0.00   99.62
11:58:53 AM    0    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00  100.00
11:58:53 AM    1    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00  100.00
11:58:53 AM    2    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00  100.00
11:58:53 AM    3    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00  100.00
11:58:53 AM    4    1.98    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00   98.02
11:58:53 AM    5    0.00    0.00    1.00    0.00    0.00    0.00    0.00    0.00    0.00   99.00
11:58:53 AM    6    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00  100.00
11:58:53 AM    7    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00    0.00  100.00
 * @author user
 *
 */
@Service
@RequiredArgsConstructor
public class MpstatP implements ResultAnalysis {
	private final DiagnosisTaskResultMapper resultMapper;
	private static final String[] KEYS = { "CPU", "%usr", "%nice", "%sys", "%iowait", "%irq", "%soft", "%steal",
			"%guest", "%gnice", "%idle" };
	private static final int[] INDEX = { 1, 3, 4, 5, 6, 10 };

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		try {
			LineChart all = new LineChart().setTitle(LocaleString.format("MpstatP.title"));
			LineChart idle = new LineChart().setTitle(LocaleString.format("MpstatP.idle"));
			try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {
				while (reader.ready()) {
					var line = reader.readLine();
					if (line.isBlank())
						continue;
					if (line.startsWith("Average"))
						break;
					var data = line.substring(11).trim().split("\\s+");
					if (data[0].equals("CPU"))
						continue;
					var time = line.substring(0, 11);
					if (data[0].equals("all")) {
						all.addX(time);
						for (int i : INDEX)
							all.addPoint(KEYS[i], PercentUtil.parse(data[i]));
						idle.addX(time);
						idle.addPoint(KEYS[10], PercentUtil.parse(data[10]));
					}
				}
			}
			all.parse();
			idle.parse();
			TaskResult result = new TaskResult(task, ResultState.NoAdvice, ResultType.CPU, FrameType.LineChart,
					bearing.center);
			result.setData(all);
			resultMapper.insert(result);
			TaskResult idleResult = new TaskResult(task, ResultState.NoAdvice, ResultType.idle, FrameType.LineChart,
					bearing.center);
			idleResult.setData(idle);
			resultMapper.insert(idleResult);
		} catch (IOException e) {
			throw new CustomException("MpstatP", e);
		}
	}
}