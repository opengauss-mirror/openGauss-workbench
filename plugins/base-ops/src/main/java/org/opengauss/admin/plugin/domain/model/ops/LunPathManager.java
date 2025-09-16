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
 * LunPathManager.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/LunPathManager.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.enums.ops.DiskQueryMethodEnum;

/**
 * Lun path manager
 *
 * @author shenzheng
 * @since 2023.11.27
 **/
public class LunPathManager {
    /**
     * lun query method
     */
    public static DiskQueryMethodEnum lunQueryMethod;

    /**
     * wwn length of lun path
     */
    public static final int WWN_LEN_IN_HEX = 32;

    /**
     * len of wwn in softlink
     */
    public static final int SOFT_LINK_WWN_LEN = 6;

    private String disk;

    private String wwn;

    private String capacity;

    private String name;

    /**
     * lunResult should be like :
     * 1. SCSI "/dev/sdn   36eca1d110035234bd392b4cf00000035   322GB"
     * 2. UPADMIN "uptrapathfz  6eca1d110035234bd392b4cf00000036    322GB  MY_LUN"
     * 3. UPADMIN_PLUS "uptrapathfz  6eca1d110035234bd392b4cf00000036    322GB  MY_LUN"
     *
     * @param lunResult lun path result
     */
    public LunPathManager(String lunResult) {
        String[] parts = lunResult.trim().split(" ");
        if (parts.length < 3) {
            throw new OpsException("Invalid lun path result");
        }
        this.disk = parts[0].contains("/dev") ? parts[0] : ("/dev/" + parts[0]);
        this.wwn = parts[1];
        this.capacity = parts[2];
        this.name = (parts.length == 4) ? parts[3] : "";
    }

    public String getDisk() {
        return this.disk;
    }

    public String getName() {
        return this.name;
    }

    public String getWwn() {
        return this.wwn;
    }

    public String getCapacity() {
        return this.capacity;
    }

    /**
     * get lun disk name
     *
     * @param path LUN path result
     * @return disk name of LUN
     */
    public static String getLunDiskName(String path) {
        if (LunPathManager.lunQueryMethod == DiskQueryMethodEnum.UPADMIN) {
            return "/dev/" + path;
        } else {
            return path;
        }
    }

    /**
     * get lun link path
     *
     * @param path lun path Manager
     * @param prefix prefix of link path
     * @param isDisasterNeed this parameter function has been deprecated
     * @param installUser install user
     * @return soft link path of LUN
     */
    public static String getLunLinkPath(LunPathManager path, String prefix, boolean isDisasterNeed,
        String installUser) {
        if (isDisasterNeed) {
            return "/dev/" + installUser + "_dss_" + prefix;
        }
        return "/dev/ln_" + prefix + "_" + path.getWwn()
            .substring(path.getWwn().length() - LunPathManager.SOFT_LINK_WWN_LEN);
    }
}
