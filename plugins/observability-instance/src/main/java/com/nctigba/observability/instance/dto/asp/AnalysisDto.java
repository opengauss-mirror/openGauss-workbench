/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.asp;

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
public class AnalysisDto {
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
