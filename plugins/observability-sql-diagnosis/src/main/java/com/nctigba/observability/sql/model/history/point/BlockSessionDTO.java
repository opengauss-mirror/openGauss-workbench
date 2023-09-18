/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * BlockSessionDTO
 *
 * @author luomeng
 * @since 2023/8/22
 */
@Data
@Accessors(chain = true)
public class BlockSessionDTO {
    private String startTime;
    private String endTime;
    private String wSessionId;
    private String wPid;
    private String lSessionId;
    private String lPid;
    private String lUser;
    private String lockingQuery;
    private String tableName;
    private String applicationName;
    private String clientAddress;
    private String state;
    private String mode;
}
