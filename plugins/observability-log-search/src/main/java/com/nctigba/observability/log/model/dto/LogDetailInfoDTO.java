package com.nctigba.observability.log.model.dto;

import lombok.Data;

/**
 * <p>
 * Log-Search response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
public class LogDetailInfoDTO {
    private Object logTime;
    private Object logType;
    private Object logLevel;
    private Object logData;
    private Object logClusterId;
    private Object logNodeId;
}
