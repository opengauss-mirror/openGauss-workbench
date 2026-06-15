/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
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
