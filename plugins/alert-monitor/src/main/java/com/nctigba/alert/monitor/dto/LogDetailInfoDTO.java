/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.alert.monitor.constant.CommonConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * LogDetailInfoDTO
 *
 * @since 2023/8/11 15:25
 */
@Data
@Accessors(chain = true)
public class LogDetailInfoDTO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logTime;
    private String logType;
    private String logLevel;
    private String logData;
    private String logClusterId;
    private String logNodeId;
    private String id;

    /**
     * docMap to LogDetailInfoDTO
     *
     * @param docMap docMap
     */
    public void parse(Map docMap) {
        if (docMap.get(CommonConstants.TIMESTAMP) instanceof String) {
            String time = (String) docMap.get(CommonConstants.TIMESTAMP);
            this.setLogTime(LocalDateTime.parse(time,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'+08:00'")));
        }
        if (docMap.get(CommonConstants.FIELDS) instanceof Map) {
            Map logTypeMap = (Map) docMap.get(CommonConstants.FIELDS);
            if (logTypeMap.get(CommonConstants.LOG_TYPE) instanceof String) {
                this.setLogType((String) logTypeMap.get(CommonConstants.LOG_TYPE));
            }
        }
        if (docMap.get(CommonConstants.LOG_LEVEL) instanceof String) {
            this.setLogLevel((String) docMap.get(CommonConstants.LOG_LEVEL));
        }
        if (docMap.get(CommonConstants.MESSAGE) instanceof String) {
            this.setLogData((String) docMap.get(CommonConstants.MESSAGE));
        }
        if (docMap.get(CommonConstants.CLUSTER_ID) instanceof String) {
            this.setLogClusterId((String) docMap.get(CommonConstants.CLUSTER_ID));
        }
        if (docMap.get(CommonConstants.NODE_ID) instanceof String) {
            this.setLogNodeId((String) docMap.get(CommonConstants.NODE_ID));
        }
    }
}
