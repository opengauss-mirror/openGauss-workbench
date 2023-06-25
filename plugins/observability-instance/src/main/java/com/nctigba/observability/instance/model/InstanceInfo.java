/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.model;

import java.util.List;

import lombok.Data;

@Data
public class InstanceInfo {
    private String name;

    private String type;

    private String dbVersion;

    private String remark;

    private List<InstanceInfo> nodeList;
}
