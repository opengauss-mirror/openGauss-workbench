/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * SysPluginStatus.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/SysPluginDownloadStatus.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

/**
 * plugin download status
 *
 * @author zhaochen
 * @since 2025-03-20
 */
public enum SysPluginDownloadStatus {
    DOWNLOADED_STATUS(1, "downloaded"),
    UN_DOWNLOAD_STATUS(0, "undownloaded");

    private final Integer code;
    private final String info;

    SysPluginDownloadStatus(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    public Integer getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
