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
 *  StatementHistoryVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/StatementHistoryVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo;

import lombok.Data;

/**
 * StatementHistoryAggVO
 *
 * @author luomeng
 * @since 2024/4/9
 */
@Data
public class StatementHistoryAggVO {
    private String uniqueQueryId;
    private String sqlTemplate;
    private Long executeNum;
    private double totalExecuteTime;
    private double avgExecuteTime;
    private Long totalScanRows;
    private Long avgScanRows;
    private Long totalRandomScanRows;
    private Long avgRandomScanRows;
    private Long totalOrderScanRows;
    private Long avgOrderScanRows;
    private Long avgReturnRows;
    private double avgLockTime;
    private String firstExecuteTime;
    private String finalExecuteTime;
}
