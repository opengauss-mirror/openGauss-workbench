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
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/vo/StatementHistoryVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * StatementHistoryVO
 *
 * @author wuyuebin
 * @since 2024/3/22 16:04
 */
@Data
@Accessors(chain = true)
public class StatementHistoryVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uniqueQueryId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long debugQueryId;
    private String dbName;
    private String schemaName;
    private String userName;
    private String applicationName;
    private String query;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dbTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cpuTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executionTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataIoTime;
}
