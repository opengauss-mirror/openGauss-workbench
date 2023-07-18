/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.topsql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: Request param fro TOP SQL now
 * @author: Louis Yang
 * @date: 2023/5/23 14:49
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSQLNowReq {
    private String id;
}
