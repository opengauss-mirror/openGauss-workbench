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

import com.tools.monitor.entity.DataSource;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * ZabbixMessge
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class ZabbixMessge {
    private Integer hostId;

    private Integer fatherItemId;

    private Integer itemPreprocId;

    private DataSource dataSource;

    private JdbcTemplate openGaussTemplate;

    private String hostName;

    /**
     * ZabbixMessge
     */
    public ZabbixMessge() {
    }

    /**
     * ZabbixMessge
     *
     * @param hostId       hostId
     * @param fatherItemId fatherItemId
     * @param dataSource   dataSource
     * @param hostName     hostName
     */
    public ZabbixMessge(Integer hostId, Integer fatherItemId, DataSource dataSource, String hostName) {
        this.hostId = hostId;
        this.fatherItemId = fatherItemId;
        this.dataSource = dataSource;
        this.hostName = hostName;
    }
}
