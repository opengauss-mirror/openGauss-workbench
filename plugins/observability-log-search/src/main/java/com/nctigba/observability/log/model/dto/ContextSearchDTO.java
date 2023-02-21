package com.nctigba.observability.log.model.dto;

import lombok.Data;

/**
 * <p>
 * Log-ContextSearch response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/02/01 09:05
 */
@Data
public class ContextSearchDTO {
    private Object logTime;
    private Object logType;
    private Object logLevel;
    private Object logData;
    private Object logClusterId;
    private Object logNodeId;
}
