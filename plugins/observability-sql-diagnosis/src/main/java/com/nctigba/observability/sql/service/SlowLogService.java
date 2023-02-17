package com.nctigba.observability.sql.service;

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

@Service
public class SlowLogService extends ServiceImpl<SlowLogMapper, StatementHistory> {
	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private SlowLogMapper slowLogMapper;

	public MyPage<StatementHistory> listSlowSQLs(SlowLogQuery slowLogQuery) {
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
			page.setRecords(slowLogMapper
					.selectList(wrapper
							.orderByDesc(StatementHistory::getStartTime)
							.last(" limit " + (slowLogQuery.getPageNum() - 1) * slowLogQuery.getPageSize()
							+ "," + slowLogQuery.getPageSize())));
			page.setCurrent(slowLogQuery.getPageNum()).setSize(slowLogQuery.getPageSize());
			return page;
		} finally {
			clusterManager.pool();
		}
	}
}