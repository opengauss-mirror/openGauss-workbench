package com.nctigba.observability.sql.service;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.common.mybatis.BasePageDTO;
import com.nctigba.common.mybatis.MyPage;
import com.nctigba.observability.sql.mapper.SlowLogMapper;
import com.nctigba.observability.sql.model.StatementHistory;
import com.nctigba.observability.sql.model.query.SlowLogQuery;

import lombok.Data;

@Service
public class SlowLogService extends ServiceImpl<SlowLogMapper, StatementHistory> {
	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private SlowLogMapper slowLogMapper;
	private Map<SlowLogQuery, cacheAble<Future<BasePageDTO<StatementHistory>>>> cache = new WeakHashMap<>();

	public MyPage<StatementHistory> listSlowSQLs(SlowLogQuery slowLogQuery) {
		synchronized (cache) {
			if (cache.containsKey(slowLogQuery)) {
				var cacheAble = cache.get(slowLogQuery);
				if (cacheAble.getObj().isDone() && System.currentTimeMillis() - cacheAble.getCurr() > 1000)
					cache.remove(slowLogQuery);
				else
					try {
						return cacheAble.getObj().get();
					} catch (InterruptedException | ExecutionException e) {
					}
			}
		}
		var future = Executors.newSingleThreadExecutor().submit(() -> {
			clusterManager.setCurrentDatasource(slowLogQuery.getNodeId(), slowLogQuery.getDbName());
			try {
				var wrapper = Wrappers.<StatementHistory>lambdaQuery()
						.eq(StringUtils.isNotBlank(slowLogQuery.getDbName()), StatementHistory::getDbName,
								slowLogQuery.getDbName())
						.le(slowLogQuery.getFinishTime() != null, StatementHistory::getStartTime,
								slowLogQuery.getFinishTime())
						.ge(slowLogQuery.getStartTime() != null, StatementHistory::getFinishTime,
								slowLogQuery.getStartTime())
						.eq(StatementHistory::getIsSlowSql, true);
				var page = new BasePageDTO<StatementHistory>();
				if (slowLogQuery.getQueryCount())
					page.setTotal(slowLogMapper.selectCount(wrapper));
				page.setRecords(slowLogMapper.selectList(wrapper.orderByDesc(StatementHistory::getStartTime)
						.last(" limit " + (slowLogQuery.getPageNum() - 1) * slowLogQuery.getPageSize() + ","
								+ slowLogQuery.getPageSize())));
				page.setCurrent(slowLogQuery.getPageNum()).setSize(slowLogQuery.getPageSize());
				return page;
			} finally {
				clusterManager.pool();
			}
		});
		var c = new cacheAble<>(future);
		cache.put(slowLogQuery, c);
		
		try {
			BasePageDTO<StatementHistory> page = future.get();
			c.setCurr(System.currentTimeMillis());
			return page;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Data
	static class cacheAble<T> {
		long curr = System.currentTimeMillis();
		T obj;

		public cacheAble(T obj) {
			this.obj = obj;
		}
	}
}