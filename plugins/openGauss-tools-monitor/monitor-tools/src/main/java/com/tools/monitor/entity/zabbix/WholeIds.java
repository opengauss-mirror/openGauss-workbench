/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
