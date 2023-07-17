/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.data;

import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import lombok.Data;
import lombok.Generated;

import java.util.List;

/**
 * AgentData
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Generated
public class AgentData {
    private String paramName;
    private List<AgentDTO> sysValue;
    private List<AgentDTO> dbValue;
}
