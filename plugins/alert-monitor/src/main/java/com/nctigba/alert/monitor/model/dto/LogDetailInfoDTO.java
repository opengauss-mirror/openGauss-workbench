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
 *  LogDetailInfoDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/LogDetailInfoDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

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
