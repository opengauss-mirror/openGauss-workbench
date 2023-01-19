package com.tools.monitor.entity.zabbix;

import lombok.Data;

/**
 * ZabbixSql
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class ZabbixSql {
    private String sql;
    private String only;
}
