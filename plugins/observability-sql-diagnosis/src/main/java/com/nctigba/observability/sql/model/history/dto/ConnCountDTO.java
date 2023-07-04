/*
 * Copyright (c) 2023 Huawei Technologies Co.,Ltd.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ConnCountDTO
 *
 * @author luomeng
 * @since 2023/6/29
 */
@Data
@Accessors(chain = true)
public class ConnCountDTO {
    private String beforeStartTime;
    private String beforeEndTime;
    private String beforeSessionCount;
    private String nowStartTime;
    private String nowEndTime;
    private String nowSessionCount;
}
