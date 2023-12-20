/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  SlowLogServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/SlowLogServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

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
import com.nctigba.observability.sql.service.MyPage;
import com.nctigba.observability.sql.mapper.SlowLogMapper;
import com.nctigba.observability.sql.model.entity.StatementHistoryDO;
import com.nctigba.observability.sql.model.query.SlowLogQuery;

import lombok.Data;

@Service
public class SlowLogServiceImpl extends ServiceImpl<SlowLogMapper, StatementHistoryDO> {
	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private SlowLogMapper slowLogMapper;
	private Map<SlowLogQuery, cacheAble<Future<BasePageDTO<StatementHistoryDO>>>> cache = new WeakHashMap<>();

	public MyPage<StatementHistoryDO> listSlowSQLs(SlowLogQuery slowLogQuery) {
		synchronized (cache) {
			if (cache.containsKey(slowLogQuery)) {
				var cacheAble = cache.get(slowLogQuery);
				if (cacheAble.getObj().isDone() && System.currentTimeMillis() - cacheAble.getCurr() > 1000)
					cache.remove(slowLogQuery);
				else
					try {
						return cacheAble.getObj().get();
					} catch (InterruptedException e) {
						log.error("Interrupted!", e);
						Thread.currentThread().interrupt();
					} catch (ExecutionException e) {
						log.error("ExecutionException!", e);
					}
			}
		}
		var future = Executors.newSingleThreadExecutor().submit(() -> {
			clusterManager.setCurrentDatasource(slowLogQuery.getNodeId(), slowLogQuery.getDbName());
			try {
				var wrapper = Wrappers.<StatementHistoryDO>lambdaQuery()
						.eq(StringUtils.isNotBlank(slowLogQuery.getDbName()), StatementHistoryDO::getDbName,
								slowLogQuery.getDbName())
						.le(slowLogQuery.getFinishTime() != null, StatementHistoryDO::getStartTime,
								slowLogQuery.getFinishTime())
						.ge(slowLogQuery.getStartTime() != null, StatementHistoryDO::getFinishTime,
								slowLogQuery.getStartTime())
						.eq(StatementHistoryDO::getIsSlowSql, true);
				var page = new BasePageDTO<StatementHistoryDO>();
				if (slowLogQuery.getQueryCount())
					page.setTotal(slowLogMapper.selectCount(wrapper));
				page.setRecords(slowLogMapper.selectList(wrapper.orderByDesc(StatementHistoryDO::getStartTime)
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
			BasePageDTO<StatementHistoryDO> page = future.get();
			c.setCurr(System.currentTimeMillis());
			return page;
		} catch (InterruptedException e) {
			log.error("Interrupted!", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}catch (ExecutionException e) {
			log.error("ExecutionException!", e);
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