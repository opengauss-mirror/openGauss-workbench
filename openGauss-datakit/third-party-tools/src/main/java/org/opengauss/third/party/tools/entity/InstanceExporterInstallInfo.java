/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.entity;

import lombok.Data;

import java.util.Locale;

/**
 * Instance exporter install info class
 *
 * @since 2026/6/8
 */
@Data
public class InstanceExporterInstallInfo extends BaseInstallInfo {
    /**
     * instance exporter server port
     */
    private Integer serverPort;

    @Override
    public String toString() {
        return "InstanceExporterInstallInfo"
                + super.toString()
                + ", serverPort=" + serverPort
                + '}';
    }

    @Override
    public String getCsvHeader() {
        return String.format("%s,%s", super.getCsvHeader(), "server_port");
    }

    @Override
    public String getCsvData() {
        if (serverPort == null) {
            throw new IllegalArgumentException("Instance-exporter install info serverPort must not be null");
        }
        return String.format(Locale.ROOT, "%s,%d", super.getCsvData(), serverPort);
    }
}
