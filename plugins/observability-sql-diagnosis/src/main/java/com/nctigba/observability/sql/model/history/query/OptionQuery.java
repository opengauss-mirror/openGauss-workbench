/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.query;

import lombok.Data;

/**
 * OptionQuery
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
public class OptionQuery {
    private String option;
    private String name;
    private Boolean isCheck;
    private Integer sortNo;
}
