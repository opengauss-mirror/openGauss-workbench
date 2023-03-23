package com.nctigba.observability.sql.mapper;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.DataSourceIniter;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiagnosisTaskResultMapper {
	private final DiagnosisTaskResultBaseMapper baseMapper;
	private static final Queue<TaskResult> updates = new LinkedBlockingQueue<TaskResult>();

	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void save() {
		if(updates.size()==0)
			return;
		while (!updates.isEmpty()) {
			baseMapper.insert(updates.poll());
		}
	}

	public List<TaskResult> selectList(LambdaQueryWrapper<TaskResult> eq) {
		return baseMapper.selectList(eq);
	}

	public void insert(TaskResult t) {
		updates.add(t);
	}

	@Mapper
	@DS(DataSourceIniter.diagnosisSource)
	public interface DiagnosisTaskResultBaseMapper extends BaseMapper<TaskResult> {
	}
}