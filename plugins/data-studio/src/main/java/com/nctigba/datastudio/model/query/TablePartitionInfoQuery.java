/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;

@Data
public class TablePartitionInfoQuery {
    private String type;
    private String valueInterval;
    private String partitionColumn;
    private String partitionName;
    private String partitionValue;
    private String tableSpace;
}
