/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.dto.topsql;

import lombok.Data;

/**
 * <p>
 * TopSQLInfo request dto
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/9/19 11:05
 */
@Data
public class TopSQLInfoReq {
    private String id;
    private String sqlId;
}
