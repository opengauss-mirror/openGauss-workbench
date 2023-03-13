package com.nctigba.observability.sql.mapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.model.diagnosis.Task;

@Service
public class DiagnosisTaskMapper implements ApplicationRunner {
	@Autowired
	private DiagnosisTaskBaseMapper baseMapper;
	private static final WeakHashMap<Serializable, Task> taskMap = new WeakHashMap<>();
	private static final List<Task> updates = new CopyOnWriteArrayList<Task>();
	private static final List<Task> inserts = new CopyOnWriteArrayList<>();
	private static final List<Serializable> deletes = new CopyOnWriteArrayList<>();
	private static int max = 0;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		var maxtask = baseMapper.selectList(Wrappers.<Task>lambdaQuery().orderByDesc(Task::getId).last(" limit 1"));
		if (!maxtask.isEmpty())
			max = maxtask.get(0).getId();
	}

	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void save() {
		if (updates.isEmpty() && inserts.isEmpty() || deletes.isEmpty())
			return;
		var list = new ArrayList<>(updates);
		updates.clear();
		list.stream().forEach(t -> {
			baseMapper.updateById(t);
		});
		list = new ArrayList<>(inserts);
		inserts.clear();
		list.stream().forEach(t -> {
			baseMapper.insert(t);
		});
		var ids = new ArrayList<Serializable>(deletes);
		deletes.clear();
		ids.stream().forEach(t -> {
			baseMapper.deleteById(t);
		});
	}

	public synchronized int insert(Task entity) {
		max++;
		entity.setId(max);
		inserts.add(entity);
		taskMap.put(entity.getId(), entity);
		return max;
	}

	public int deleteById(Serializable id) {
		taskMap.remove(id);
		synchronized (inserts) {
			for (Task task : inserts)
				if (id.equals(task.getId())) {
					inserts.remove(task);
					return 0;
				}
		}
		synchronized (updates) {
			for (Task task : updates)
				if (id.equals(task.getId())) {
					updates.remove(task);
					break;
				}
		}
		deletes.add(id);
		return 0;
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
		return baseMapper.selectById(id);
	}

	public List<Task> selectList(Wrapper<Task> queryWrapper) {
		return baseMapper.selectList(queryWrapper);
	}

	public <P extends IPage<Task>> P selectPage(P page, Wrapper<Task> queryWrapper) {
		return baseMapper.selectPage(page, queryWrapper);
	}
}