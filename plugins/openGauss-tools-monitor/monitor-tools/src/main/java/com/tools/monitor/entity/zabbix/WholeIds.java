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
    private Integer item_preprocid;

    public WholeIds() {
    }

    public WholeIds(Integer hostid, Integer interfaceid, Integer itemid, Integer item_preprocid) {
        this.hostid = hostid;
        this.interfaceid = interfaceid;
        this.itemid = itemid;
        this.item_preprocid = item_preprocid;
    }
}
