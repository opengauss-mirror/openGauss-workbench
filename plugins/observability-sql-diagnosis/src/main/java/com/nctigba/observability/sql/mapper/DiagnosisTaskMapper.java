package com.nctigba.observability.sql.mapper;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nctigba.observability.sql.model.diagnosis.Task;

@Service
public class DiagnosisTaskMapper {
	@Autowired
	private DiagnosisTaskBaseMapper baseMapper;
	private static final WeakHashMap<Serializable, Task> taskMap = new WeakHashMap<>();
	private static final Queue<Task> updates = new LinkedBlockingQueue<Task>();

	@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
	public void save() {
		if (updates.isEmpty())
			return;
		var set = new LinkedHashSet<Task>();
		while (!updates.isEmpty()) {
			set.add(updates.poll());
		}
		set.stream().forEach(t -> {
			baseMapper.updateById(t);
		});
	}

	public int insert(Task entity) {
		var t = baseMapper.insert(entity);
		taskMap.put(entity.getId(), entity);
		return t;
	}

	public int deleteById(Serializable id) {
		var t = taskMap.remove(id);
		if (t != null)
			updates.remove(t);
		return baseMapper.deleteById(id);
	}

	public int deleteById(Task entity) {
		return deleteById(entity.getId());
	}

	public int updateById(Task entity) {
		updates.add(entity);
		return 0;
	}

	public Task selectById(Serializable id) {
		if (taskMap.containsKey(id))
			return taskMap.get(id);
		var task = baseMapper.selectById(id);
		taskMap.put(id, task);
		return task;
	}

	public List<Task> selectList(Wrapper<Task> queryWrapper) {
		return baseMapper.selectList(queryWrapper);
	}

	public <P extends IPage<Task>> P selectPage(P page, Wrapper<Task> queryWrapper) {
		return baseMapper.selectPage(page, queryWrapper);
	}
}