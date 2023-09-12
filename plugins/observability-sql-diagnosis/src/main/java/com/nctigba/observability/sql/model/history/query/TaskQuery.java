/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.query;

import com.nctigba.common.web.model.query.PageBaseQuery;
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
    private String taskName;
    private Date startTime;
    private Date endTime;
    private String diagnosisType;
}
