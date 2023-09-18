/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * OuterPlan
 *
 * @author luomeng
 * @since 2023/9/4
 */
@Data
public class OuterPlan {
    @JsonProperty("Plan")
    private Plan plan;
}
