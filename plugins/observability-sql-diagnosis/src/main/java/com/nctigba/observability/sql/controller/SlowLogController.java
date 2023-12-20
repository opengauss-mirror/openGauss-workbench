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
 *  SlowLogController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/SlowLogController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.sql.service.MyPage;
import com.nctigba.observability.sql.config.ControllerConfig;
import com.nctigba.observability.sql.model.entity.StatementHistoryDO;
import com.nctigba.observability.sql.model.query.SlowLogQuery;
import com.nctigba.observability.sql.service.impl.SlowLogServiceImpl;

@RestController
@RequestMapping("/sqlDiagnosis/api/v1")
public class SlowLogController extends ControllerConfig {
	@Autowired
	private SlowLogServiceImpl slowLogServiceImpl;

	@GetMapping("/slowSqls")
	public MyPage<StatementHistoryDO> listSlowSQLs(SlowLogQuery slowLogQuery) {
		return slowLogServiceImpl.listSlowSQLs(slowLogQuery);
	}
}