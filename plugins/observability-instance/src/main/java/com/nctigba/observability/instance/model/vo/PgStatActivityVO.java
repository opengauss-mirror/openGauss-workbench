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
 *  PgStatActivityVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/vo/PgStatActivityVO.java
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
 * PgStatActivityVO
 *
 * @author wuyuebin
 * @since 2024/3/25 09:17
 */
@Data
@Accessors(chain = true)
public class PgStatActivityVO {
    private Double duration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date queryStart;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uniqueSqlId;
    private String datname;
    private String usename;
    private String applicationName;
    private String datid;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionid;
    private String usesysid;
    private String clientAddr;
    private String clientHostname;
    private Integer clientPort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date backendStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date xactStart;
    private String xactDuration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stateChange;
    private Boolean waiting;
    private String enqueue;
    private String state;
    private String resourcePool;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long queryId;
    private String query;
    private String connectionInfo;
    private String traceId;
    private String queryDuration;
}
