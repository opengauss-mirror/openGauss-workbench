/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ExecPlanDetailDTO
 *
 * @author luomeng
 * @since 2023/9/5
 */
@Data
@Accessors(chain = true)
public class ExecPlanDetailDTO {
    Plan data;
    JSONObject total;
}
