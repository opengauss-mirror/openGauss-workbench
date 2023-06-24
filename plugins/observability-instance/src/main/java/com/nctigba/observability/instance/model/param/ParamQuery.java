/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.model.param;

import lombok.Data;

@Data
public class ParamQuery {
    private String nodeId;
    private String password;
    private String isRefresh;
}