/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Log-Search response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
public class LogInfoDTO {
    private String scrollId;
    private List<LogDetailInfoDTO> logs;
    private List<String> sorts;
}
