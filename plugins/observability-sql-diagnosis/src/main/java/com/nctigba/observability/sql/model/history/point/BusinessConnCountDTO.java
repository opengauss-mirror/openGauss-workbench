/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.nctigba.observability.sql.model.history.dto.ConnCountDTO;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * BusinessConnCountDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
@Generated
public class BusinessConnCountDTO {
    List<ConnCountDTO> connCount;
    List<AspAnalysisDTO> timeSlot;
}
