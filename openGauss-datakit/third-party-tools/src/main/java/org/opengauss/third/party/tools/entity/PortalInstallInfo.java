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

/**
 * Portal install info class
 *
 * @since 2026/6/8
 */
@Data
public class PortalInstallInfo extends BaseInstallInfo {
    /**
     * portal type: MYSQL_ONLY, MULTI_DB
     */
    private String portalType;

    public PortalInstallInfo() {
    }

    public PortalInstallInfo(String id, String ip, int port, String user, String installDir, String portalType) {
        super(id, ip, port, user, installDir);
        this.portalType = portalType;
    }

    @Override
    public String toString() {
        return "PortalInstallInfo{"
                + super.toString()
                + ", portalType='" + portalType + '\''
                + '}';
    }

    @Override
    public String getCsvHeader() {
        return String.format("%s,%s", super.getCsvHeader(), "portal_type");
    }

    @Override
    public String getCsvData() {
        checkIllegalCharacter(portalType);
        return String.format("%s,%s", super.getCsvData(), portalType);
    }
}
