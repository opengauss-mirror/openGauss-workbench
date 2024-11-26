/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsParseExcelEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsParseExcelEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.opengauss.admin.system.plugin.beans.SshLogin;

/**
 * ParseClusterExcel properties
 *
 * @author ybx
 * @version 1.0
 * @date: 2024/8/5 21:13 PM
 * @since JDK 11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpsParseExcelEntity {
    private String versionType;
    private SshLogin sshLogin;
    private OpsImportEntity opsImportEntity;
    private OpsImportSshEntity hostAndUserId;
    private String publicIp;
    private int ipSequence;
    private int ipNum;
}
