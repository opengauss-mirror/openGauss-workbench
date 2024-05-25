/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ShellInfoVo.java
 *
 *  IDENTIFICATION
 *  plugins/data-migration/src/main/java/org/opengauss/admin/plugin/vo/ShellInfoVo.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

/**
 * Params for creating a JSCH session
 *
 * @date 2024/5/20 16:27
 * @since  0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShellInfoVo {
    String ip;
    Integer port;
    String username;
    String password;

    /**
     * get ShellInfoVo instance
     *
     * @param opsHost ops host
     * @param username user name
     * @param password password
     * @return ShellInfoVo
     */
    public static ShellInfoVo getInstance(OpsHostEntity opsHost, String username, String password) {
        return new ShellInfoVo(opsHost.getPublicIp(), opsHost.getPort(), username, password);
    }
}
