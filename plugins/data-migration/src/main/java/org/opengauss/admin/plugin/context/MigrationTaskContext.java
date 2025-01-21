/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 * MigrationTaskContext.java
 *
 * IDENTIFICATION
 *plugins/data-migration/src/main/java/org/opengauss/admin/plugin/context/MigrationTaskContext.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.context;

import lombok.Data;

import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressSummaryService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;

/**
 * MigrationTaskContext
 *
 * @author: wangchao
 * @Date: 2024/12/31 10:58
 * @Description: MigrationTaskContext
 * @since 7.0.0
 **/
@Data
public class MigrationTaskContext {
    private MigrationHostPortalInstall installHost;
    private MigrationTask migrationTask;
    private Integer id;
    private EncryptionUtils encryptionUtils;
    private MigrationTaskCheckProgressSummaryService migrationTaskCheckProgressSummaryService;
    private MigrationTaskCheckProgressDetailService migrationTaskCheckProgressDetailService;
}
