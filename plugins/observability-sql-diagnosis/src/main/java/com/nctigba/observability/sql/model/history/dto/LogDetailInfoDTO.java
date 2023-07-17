/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.Generated;

/**
 * <p>
 * Log-Search response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
@Generated
public class LogDetailInfoDTO {
    private Object logTime;
    private Object logType;
    private Object logLevel;
    private Object logData;
    private Object logClusterId;
    private Object logNodeId;
    private String id;
}
