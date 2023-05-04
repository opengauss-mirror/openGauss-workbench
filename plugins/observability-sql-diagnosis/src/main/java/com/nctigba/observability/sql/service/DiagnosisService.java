package com.nctigba.observability.sql.service;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.handler.TopSQLHandler;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.model.diagnosis.result.TreeNode;
import com.nctigba.observability.sql.model.query.TaskQuery;
import com.nctigba.observability.sql.service.diagnosis.TaskCreator;

@Service
public class DiagnosisService {
	@Autowired
	private DiagnosisTaskMapper taskMapper;
	@Autowired
	private DiagnosisTaskResultMapper taskResultMapper;
	@Autowired
	private TaskCreator taskCreator;
	@Autowired
	private TopSQLHandler topSQLHandler;

	public Task start(Task task) {
		// save Task
		taskMapper.insert(task);
		taskCreator.createTask(task);
		return task;
	}

	public Task one(Integer id) {
		return taskMapper.selectById(id);
	}

	public void delete(Integer id) {
		taskCreator.remove(id);
		taskMapper.deleteById(id);
	}

	public IPage<Task> page(TaskQuery taskQuery) {
		return taskMapper.selectPage(taskQuery.iPage(), Wrappers.lambdaQuery(Task.class)
				.eq(StringUtils.isNotBlank(taskQuery.getClusterId()), Task::getClusterId, taskQuery.getClusterId())
				.eq(StringUtils.isNotBlank(taskQuery.getNodeId()), Task::getNodeId, taskQuery.getNodeId())
				.eq(StringUtils.isNotBlank(taskQuery.getDbName()), Task::getDbName, taskQuery.getDbName())
				.eq(StringUtils.isNotBlank(taskQuery.getSqlId()), Task::getSqlId, taskQuery.getSqlId())
				.and(StringUtils.isNotBlank(taskQuery.getName()), c -> {
					c.like(Task::getName, taskQuery.getName()).or().like(Task::getSql, taskQuery.getName().replaceAll(" ", "%"));
				}).ge(taskQuery.getStartTime() != null, Task::getCreatetime, taskQuery.getStartTime())
				.le(taskQuery.getEndTime() != null, Task::getCreatetime, taskQuery.getEndTime())
				.orderByDesc(Task::getId));
	}

	public TreeNode detail(int id, boolean all) {
		TreeNode base = null;
		var m = new HashMap<ResultType, TreeNode>();
		for (ResultType type : ResultType.values())
			m.put(type, type.T());
		for (ResultType type : ResultType.values())
			if (type.getParent() == null)
				base = m.get(type);
			else
				m.get(type.getParent()).appendChild(m.get(type));
		if (!all) {
			List<TaskResult> list = taskResultMapper
					.selectList(Wrappers.<TaskResult>lambdaQuery().eq(TaskResult::getTaskid, id)
							.select(TaskResult::getTaskid, TaskResult::getState, TaskResult::getResultType));
			for (TaskResult result : list)
				m.get(result.getResultType()).setState(result.getState());
		} else {
			m.forEach((key, value) -> {
				value.setHidden(false);
			});
		}
		return base;
	}

	public Frame result(int id, ResultType type) {
		if (type == ResultType.TaskInfo)
			return taskMapper.selectById(id).toUL();
		List<TaskResult> list = taskResultMapper.selectList(Wrappers.<TaskResult>query().lambda()
				.eq(TaskResult::getTaskid, id).eq(TaskResult::getResultType, type));
		if (list.size() == 0) {
			return new Frame(FrameType.NONE).setData("NONE");
		}
		if (list.size() == 1 && list.get(0).getBearing() == null)
			return list.get(0).toFrame();
		Frame f = new Frame();
		for (TaskResult taskResult : list) {
			f.addChild(taskResult.getBearing(), taskResult.toFrame());
		}
		return f;
	}

	public void diagnosisResult(String id, GrabType type, MultipartFile file) {
		Task task = null;
		if (StringUtils.isNumeric(id))
			task = taskMapper.selectById(id);
		if (task == null)
			return;
		task.setState(null);
		var s = new StopWatch();
		try {
			s.start();
			type.analysis(task, file);
		} catch (Exception e) {
			task.addRemarks("type:" + type, e);
			taskMapper.updateById(task);
		} finally {
			s.stop();
			task.addRemarks("bcc filesize:" + file.getSize() + ",type:" + type + ",cost:" + s.getTotalTimeMillis());
			taskMapper.updateById(task);
		}
	}

	public JSONObject plan(String nodeId, String sqlId) {
		try {
			return topSQLHandler.getExecutionPlan(nodeId, sqlId, "");
		} catch (Exception e) {
			return null;
		}
	}
}