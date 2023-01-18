package com.nctigba.observability.sql.service.diagnosis.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.math.NumberUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.chart.LineChart;
import com.nctigba.observability.sql.model.diagnosis.Resource;
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
 * 04:45:22 PM UID PID %usr %system %guest %wait %CPU CPU Command 04:45:23 PM 0
 * 3221237 0.99 0.00 0.00 0.00 0.99 2 pidstat
 * 
 * 04:45:23 PM UID PID %usr %system %guest %wait %CPU CPU Command
 * 
 * 04:45:24 PM UID PID %usr %system %guest %wait %CPU CPU Command 04:45:25 PM 0
 * 2564 0.00 1.00 0.00 0.00 1.00 3 hostguard 04:45:25 PM 1001 303294 9.00 0.00
 * 0.00 0.00 9.00 2 gaussdb
 */
@Service
@RequiredArgsConstructor
public class Pidstat1 implements ResultAnalysis {
	private final DiagnosisTaskResultMapper resultMapper;
	private final DiagnosisResourceMapper resourceMapper;

	@Override
	public void analysis(GrabType grabType, Task task, MultipartFile file) {
		try {
			Resource resource = new Resource(task, grabType).setF(file.getInputStream().readAllBytes());
			resourceMapper.insert(resource);
			LineChart usr = new LineChart().setTitle(LocaleString.format("Pidstat1.usr"));
			LineChart system = new LineChart().setTitle(LocaleString.format("Pidstat1.system"));
			LineChart wait = new LineChart().setTitle(LocaleString.format("Pidstat1.wait"));
			try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {
				while (reader.ready()) {
					var line = reader.readLine();
					if (line.isBlank())
						continue;
					if (line.startsWith("Average")) {
						var data = line.substring(8).trim().split("\\s+");
						if (NumberUtils.toDouble(data[2]) < 1)
							usr.delLine(data[8]);
						continue;
					}
					var data = line.substring(11).trim().split("\\s+");
					if (data.length < 9 || data[8].equals("Command"))
						continue;
					var time = line.substring(0, 11);
					usr.addX(time);
					usr.addPoint(data[8], PercentUtil.parse(data[2]));
					system.addX(time);
					system.addPoint(data[8], PercentUtil.parse(data[3]));
					wait.addX(time);
					wait.addPoint(data[8], PercentUtil.parse(data[5]));
				}
			}
			usr.parse();
			system.parse();
			wait.parse();
			TaskResult usrResult = new TaskResult(task, ResultState.NoAdvice, ResultType.user, FrameType.LineChart,
					bearing.center);
			usrResult.setData(usr);
			resultMapper.insert(usrResult);
			TaskResult sysResult = new TaskResult(task, ResultState.NoAdvice, ResultType.system, FrameType.LineChart,
					bearing.center);
			sysResult.setData(system);
			resultMapper.insert(sysResult);
			TaskResult waitResult = new TaskResult(task, ResultState.NoAdvice, ResultType.iowait, FrameType.LineChart,
					bearing.center);
			waitResult.setData(wait);
			resultMapper.insert(waitResult);
		} catch (IOException e) {
			throw new CustomException("Pidstat1", e);
		}
	}
}