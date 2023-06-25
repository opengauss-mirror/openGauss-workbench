/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.query;

import lombok.Data;

/**
 * HisThresholdQuery
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
public class HisThresholdQuery {
    Integer id;
    String clusterId;
    String nodeId;
    String thresholdType;
    String thresholdName;
    String thresholdValue;
}