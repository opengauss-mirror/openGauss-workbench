/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.model;

import lombok.Data;

/**
 * <p>
 * entity of index advice table
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/9/22 14:39
 */
@Data
public class IndexAdvice {
    String schema;
    String table;
    String column;
    String indexType;
}
