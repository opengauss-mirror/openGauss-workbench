/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * IndexAdvisorDTO
 *
 * @author luomeng
 * @since 2023/8/17
 */
@Data
@Accessors(chain = true)
public class IndexAdvisorDTO {
    List<String> firstExplain;
    List<String> indexAdvisor;
    List<String> afterExplain;
}
