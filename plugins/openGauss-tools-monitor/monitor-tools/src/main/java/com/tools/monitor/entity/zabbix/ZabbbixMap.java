package com.tools.monitor.entity.zabbix;

import lombok.Data;

/**
 * ZabbbixMap
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class ZabbbixMap {
    private String zabbixTargetName;

    private String zabbixKey;

    private String zabbixTargetProcess;

    public ZabbbixMap() {
    }

    public ZabbbixMap(String zabbixTargetName, String zabbixKey, String zabbixTargetProcess) {
        this.zabbixTargetName = zabbixTargetName;
        this.zabbixKey = zabbixKey;
        this.zabbixTargetProcess = zabbixTargetProcess;
    }
}
