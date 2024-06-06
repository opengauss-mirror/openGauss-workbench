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
 *  StatementHistoryDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/entity/StatementHistoryDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统表-慢日志明细
 */
@Data
@TableName("statement_history")
@Accessors(chain = true)
public class StatementHistoryDO {
	private String dbName;
	private String userName;
	private String schemaName;
	private String clientAddr;
	private Long uniqueQueryId;
	@TableField("query")
	private String sqlTemplate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC", shape = JsonFormat.Shape.ANY)
	private Date startTime;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC", shape = JsonFormat.Shape.ANY)
	private Date finishTime;
	private Long nReturnedRows;
	private Long nTuplesReturned;
	private Long dbTime;
	private Long cpuTime;
	private Long executionTime;
	private Long parseTime;
	private Long plExecutionTime;
	private Long dataIoTime;
	private Long lockWaitTime;
	private Boolean isSlowSql;
}