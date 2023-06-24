/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.dto.topsql;

import lombok.Data;

/**
 * <p>
 * TopSQLList Request DTO
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 16:09
 */
@Data
public class TopSQLListReq {
    private String id;
    private String startTime;
    private String finishTime;
    private String orderField;
}
