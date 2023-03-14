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
