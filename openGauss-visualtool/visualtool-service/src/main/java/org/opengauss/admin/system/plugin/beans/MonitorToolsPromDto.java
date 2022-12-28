package org.opengauss.admin.system.plugin.beans;

import io.prometheus.client.Gauge;
import lombok.Data;

@Data
public class MonitorToolsPromDto {
    private Gauge gauge;

    private String gaugeName;

    public MonitorToolsPromDto() {
    }

    public MonitorToolsPromDto(Gauge gauge, String gaugeName) {
        this.gauge = gauge;
        this.gaugeName = gaugeName;
    }
}
