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
 * BackupVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/vo/ops/BackupVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.vo.ops;

import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import lombok.Data;

/**
 * @author lhf
 * @date 2022/11/13 09:44
 **/
@Data
public class BackupVO extends OpsBackupEntity {
    private String publicIp;
    private String privateIp;
    private String hostname;
}
