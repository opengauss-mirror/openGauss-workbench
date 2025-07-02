/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  HisSlowsqlInfoDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mode/entity/HisSlowsqlInfoDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * HisSlowsqlInfoDO
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class HisSlowsqlInfoDO {
    private String dbName;
    private String schemaName;
    private Integer originNode;
    private String userName;
    private String applicationName;
    private String clientAddr;
    private Integer clientPort;
    private String uniqueQueryId;
    private String debugQueryId;
    private String query;
    private Date startTime;
    private Date finishTime;
    private String slowSqlThreshold;
    private String transactionId;
    private String threadId;
    private String sessionId;
    private String nSoftParse;
    private String nHardParse;
    private String queryPlan;
    private Long nReturnedRows;
    private Long nTuplesFetched;
    private Long nTuplesReturned;
    private Long nTuplesInserted;
    private Long nTuplesUpdated;
    private Long nTuplesDeleted;
    private Long nBlocksFetched;
    private Long nBlocksHit;
    private double dbTime;
    private Long cpuTime;
    private Long executionTime;
    private Long parseTime;
    private Long planTime;
    private Long rewriteTime;
    private Long plExecutionTime;
    private Long plCompilationTime;
    private Long dataIoTime;
    private String netSendInfo;
    private String netRecvInfo;
    private String netStreamSendInfo;
    private String netStreamRecvInfo;
    private Long lockCount;
    private Long lockTime;
    private Long lockWaitCount;
    private Long lockWaitTime;
    private Long lockMaxCount;
    private Long lwlockCount;
    private Long lwlockWaitCount;
    private Long lwlockTime;
    private Long lwlockWaitTime;
    private String details;
    private Boolean isSlowSql;
    private String traceId;
    private String advise;
    private Long netSendTime;
}
