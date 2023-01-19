/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity.zabbix;

import lombok.Data;

/**
 * FailMessage
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class FailMessage {
    private String sourceName;

    private String targetName;

    private String failResion;

    /**
     * FailMessage
     */
    public FailMessage() {
    }

    public FailMessage(String sourceName, String targetName, String failResion) {
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.failResion = failResion;
    }
}
