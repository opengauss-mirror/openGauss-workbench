/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CoverageRateDO {
    private String name;

    private Long oid;

    private Long cid;

    private Integer serialNumber;

    private Integer totalRows;

    private Integer executionRows;

    private String totalCoverage;

    private String allLineNumber;

    private String executionLineNumber;

    private String executionCoverage;

    private String inputParams;

    private String updateTime;

    private String sourcecode;
}
