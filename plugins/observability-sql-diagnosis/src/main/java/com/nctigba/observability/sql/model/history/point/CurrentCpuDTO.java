/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * CurrentCpuDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class CurrentCpuDTO {
    private String chartName;
    private List<AgentDTO> dataList;
}
