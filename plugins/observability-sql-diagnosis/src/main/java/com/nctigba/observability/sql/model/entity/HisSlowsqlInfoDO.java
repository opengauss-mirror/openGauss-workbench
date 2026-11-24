/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
