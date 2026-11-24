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
