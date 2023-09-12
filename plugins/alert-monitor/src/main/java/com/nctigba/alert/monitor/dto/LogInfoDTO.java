/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * LogInfoDTO
 *
 * @since 2023/8/11 15:16
 */
@Data
@Accessors(chain = true)
public class LogInfoDTO {
    private List<LogDetailInfoDTO> logs;
    private String searchAfter;
    private List<Map> keyAndBlockWords;
}
