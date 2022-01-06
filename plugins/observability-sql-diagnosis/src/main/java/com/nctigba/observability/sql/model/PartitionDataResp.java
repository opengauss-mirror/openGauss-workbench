package com.nctigba.observability.sql.model;

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