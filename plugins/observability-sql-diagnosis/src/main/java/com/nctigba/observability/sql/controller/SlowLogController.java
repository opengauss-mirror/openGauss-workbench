package com.nctigba.observability.sql.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.common.mybatis.MyPage;
import com.nctigba.common.web.config.ControllerConfig;
import com.nctigba.observability.sql.model.StatementHistory;
import com.nctigba.observability.sql.model.query.SlowLogQuery;
import com.nctigba.observability.sql.service.SlowLogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Slow SQL")
@RestController
@RequestMapping("/sqlDiagnosis/api/v1")
public class SlowLogController extends ControllerConfig {
	@Autowired
	private SlowLogService slowLogService;

	@GetMapping("/slowSqls")
	@ApiOperation(value = "List Slow SQLs", notes = "List Slow SQLs")
	public MyPage<StatementHistory> listSlowSQLs(SlowLogQuery slowLogQuery) {
		return slowLogService.listSlowSQLs(slowLogQuery);
	}
}