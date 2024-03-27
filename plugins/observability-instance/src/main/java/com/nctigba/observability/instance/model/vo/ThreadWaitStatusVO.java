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
 *  ThreadWaitStatusVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/vo/ThreadWaitStatusVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ThreadWaitStatusVO
 *
 * @author wuyuebin
 * @since 2024/3/25 14:33
 */
@Data
@Accessors(chain = true)
public class ThreadWaitStatusVO {
    private String nodeName;
    private String dbName;
    private String threadName;
    private Integer tid;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionid;
    private String blockSessionid;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long queryId;
    private String waitStatus;
    private String waitEvent;
    private String lockmode;
    private String locktag;
    private String tag;
}
