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

package com.nctigba.observability.instance.model.dto.asp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AnalysisDto
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisDTO {
    private String sampleid;
    private Date sampleTime;
    private String databaseid;
    private String threadId;
    private String sessionid;
    private Date startTime;
    private String event;
    private String userid;
    private String applicationName;
    private String clientAddr;
    private String clientHostname;
    private String clientPort;
    private String queryId;
    private String uniqueQueryId;
    private String userId;
    private String cnId;
    private String uniqueQuery;
    private String lockmode;
    private String waitStatus;
}
