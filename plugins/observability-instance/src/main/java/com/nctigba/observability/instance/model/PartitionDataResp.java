/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.model;

import lombok.Data;

@Data
public class PartitionDataResp {

    String partStrategy;
    String partKey;
    String relPages;
    String relTuples;
    String relallVisible;
    String interval;
}
