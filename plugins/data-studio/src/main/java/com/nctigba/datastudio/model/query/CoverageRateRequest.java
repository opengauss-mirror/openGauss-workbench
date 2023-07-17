/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CoverageRateRequest
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class CoverageRateRequest {
    private String uuid;

    private Long oid;

    private List<Long> cidList;
}
