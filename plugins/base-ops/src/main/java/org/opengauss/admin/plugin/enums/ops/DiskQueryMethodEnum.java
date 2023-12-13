/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * DiskQueryMethodEnum.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/DiskQueryMethodEnum.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.enums.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;

/**
 * Disk path query method
 *
 * @author shenzheng
 * @since 2023.12.9
 **/

@Getter
@AllArgsConstructor
public enum DiskQueryMethodEnum {
    /**
     * LS SCSI
     */
    SCSI(SshCommandConstants.LS_SCSI, SshCommandConstants.FIND_DISK_BY_SCSI),
    /**
     * UPADMIN show vlun
     */
    UPADMIN(SshCommandConstants.UPADMIN, SshCommandConstants.FIND_DISK_BY_UPADMIN),
    /**
     * UPADMIN_PLUS show vlun
     */
    UPADMIN_PLUS(SshCommandConstants.UPADMIN_PLUS, SshCommandConstants.FIND_DISK_BY_UPADMIN_PLUS);

    private final String queryAllDiskCmd;
    private final String queryDiskVolumeCmd;
}
