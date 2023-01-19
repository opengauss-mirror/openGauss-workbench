/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity.zabbix;

import lombok.Data;

/**
 * WholeIds
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class WholeIds {
    private Integer hostid;
    private Integer interfaceid;
    private Integer itemid;
    private Integer itemPreprocid;

    /**
     * WholeIds
     */
    public WholeIds() {
    }

    /**
     * WholeIds
     *
     * @param hostid hostid
     * @param interfaceid interfaceid
     * @param itemid itemid
     * @param itemPreprocid itemPreprocid
     */
    public WholeIds(Integer hostid, Integer interfaceid, Integer itemid, Integer itemPreprocid) {
        this.hostid = hostid;
        this.interfaceid = interfaceid;
        this.itemid = itemid;
        this.itemPreprocid = itemPreprocid;
    }
}
