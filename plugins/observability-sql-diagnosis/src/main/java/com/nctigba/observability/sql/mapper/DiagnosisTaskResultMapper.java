package com.nctigba.observability.sql.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;

@Service
public class DiagnosisTaskResultMapper {
	@Autowired
	private DiagnosisTaskResultBaseMapper baseMapper;
	private static final List<TaskResult> updates = new CopyOnWriteArrayList<TaskResult>();

	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void save() {
		if(updates.size()==0)
			return;
		var list = new ArrayList<>(updates);
		updates.clear();
		list.stream().forEach(t -> {
			baseMapper.insert(t);
		});
	}

	public List<TaskResult> selectList(LambdaQueryWrapper<TaskResult> eq) {
		return baseMapper.selectList(eq);
	}

	public void insert(TaskResult t) {
		updates.add(t);
	}
}