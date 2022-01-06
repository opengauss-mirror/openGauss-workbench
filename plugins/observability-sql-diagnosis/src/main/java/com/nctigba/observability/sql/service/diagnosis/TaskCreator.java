package com.nctigba.observability.sql.service.diagnosis;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nctigba.observability.sql.config.Pool;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.TaskState;
import com.nctigba.observability.sql.service.diagnosis.caller.Caller;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCreator {
	public static final Semaphore SEMAPHORE = new Semaphore(1);
	private final DiagnosisTaskMapper mapper;
	private final SqlExecuter nextStep;
	private final List<Caller> callers;

	@Async(Pool.DIAGNOSIS)
	public void createTask(Task task) {
		task.addRemarks("before execute");
		mapper.updateById(task);
		for (Caller caller : callers)
			try {
				caller.beforeStart(task);
			} catch (CustomException e) {
				mapper.updateById(task);
				return;
			} catch (Exception e) {
				task.addRemarks(TaskState.err, e);
				mapper.updateById(task);
				return;
			}
		// call sql and fetch pid
		task.addRemarks(TaskState.waiting);
		mapper.updateById(task);
		try {
			SEMAPHORE.acquire();
		} catch (InterruptedException e) {
			throw new CustomException("", e);
		}
		task.setStarttime(new Date());
		mapper.updateById(task);
		nextStep.executeSql(task);
		for (Caller caller : callers) {
			caller.start(task);
		}
	}
}