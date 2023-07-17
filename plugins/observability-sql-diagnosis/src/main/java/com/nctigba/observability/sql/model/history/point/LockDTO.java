/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.nctigba.observability.sql.model.history.dto.LockAnalysisDTO;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * lock
 *
 * @author luomeng
 * @since 2023/6/12
 */
@Data
@Accessors(chain = true)
@Generated
public class LockDTO {
    private String chartName;
    private List<LockAnalysisDTO> lockAnalysisDTOList;
}
