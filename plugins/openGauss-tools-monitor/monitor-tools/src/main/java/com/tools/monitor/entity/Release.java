/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity;

import lombok.Data;

/**
 * Release
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class Release {
    private String itemid;

    private String hostid;

    /**
     * Release
     */
    public Release() {
    }

    public Release(String itemid, String hostid) {
        this.itemid = itemid;
        this.hostid = hostid;
    }
}