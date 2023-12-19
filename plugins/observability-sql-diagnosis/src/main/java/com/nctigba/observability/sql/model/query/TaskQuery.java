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
 *  TaskQuery.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/query/TaskQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * TaskQuery
 *
 * @author luomeng
 * @since 2023/7/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQuery extends PageBaseQuery {
    private String clusterId;
    private String nodeId;
    private String dbName;
    private String sqlId;
    private String name;
    private Date startTime;
    private Date endTime;
    private String diagnosisType;
}
