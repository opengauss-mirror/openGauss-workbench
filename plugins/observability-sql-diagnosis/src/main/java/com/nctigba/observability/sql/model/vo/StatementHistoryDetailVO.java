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
 *  StatementHistoryDetailVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/StatementHistoryDetailVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * StatementHistoryDetailVO
 *
 * @author luomeng
 * @since 2024/4/16
 */
@Data
public class StatementHistoryDetailVO {
    private String debugQueryId;
    private String dbName;
    private String userName;
    private String schemaName;
    private String clientAddr;
    private String sqlTemplate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date finishTime;
    private Long nReturnedRows;
    private Long nTuplesReturned;
    private double dbTime;
    private double cpuTime;
    private double executionTime;
    private double parseTime;
    private double plExecutionTime;
    private double dataIoTime;
    private double lockWaitTime;
    private String uniqueQueryId;
}
