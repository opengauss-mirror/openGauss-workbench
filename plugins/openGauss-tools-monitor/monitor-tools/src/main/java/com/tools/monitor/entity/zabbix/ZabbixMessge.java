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

    private Integer father_item_id;

    private Integer itemPreprocId;

    private DataSource dataSource;

    private JdbcTemplate openGaussTemplate;

    private String hostName;

    public ZabbixMessge() {
    }

    public ZabbixMessge(Integer hostId, Integer father_item_id, Integer itemPreprocId, DataSource dataSource, String hostName) {
        this.hostId = hostId;
        this.father_item_id = father_item_id;
        this.itemPreprocId = itemPreprocId;
        this.dataSource = dataSource;
        this.hostName = hostName;
    }

    public ZabbixMessge(Integer hostId, Integer father_item_id,  DataSource dataSource, String hostName) {
        this.hostId = hostId;
        this.father_item_id = father_item_id;
        this.dataSource = dataSource;
        this.hostName = hostName;
    }
}
