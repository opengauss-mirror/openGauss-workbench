package com.tools.monitor.entity;

import io.prometheus.client.Gauge;
import lombok.Data;

/**
 * Prom
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class Prom {
    private Gauge gaugs;

    private String gaugeName;

    public Prom() {
    }

    public Prom(Gauge gaugs, String gaugeName) {
        this.gaugs = gaugs;
        this.gaugeName = gaugeName;
    }
}
