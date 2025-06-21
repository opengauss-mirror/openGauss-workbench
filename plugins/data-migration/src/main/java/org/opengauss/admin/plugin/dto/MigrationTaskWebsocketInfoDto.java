/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 * MigrationCurrentCheckInfo.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/dto/MigrationCurrentCheckInfo.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;
import org.opengauss.admin.plugin.domain.FullMigrationSubProcessCounter;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskExecResultDetail;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;

import java.util.List;
import java.util.Map;

/**
 * MigrationTaskWebsocketInfoDto
 *
 * @since 2025-06-09
 * @author ybx
 */
@Data
public class MigrationTaskWebsocketInfoDto {
    private Integer currentExecStatus;
    private Integer execStatus;
    private long exceptionAlertTotalCount;
    private long executedTime;
    private MigrationTaskExecResultDetail fullProcess;
    private MigrationTaskExecResultDetail dataCheckProcess;
    private MigrationTaskExecResultDetail incrementalProcess;
    private MigrationTaskExecResultDetail reverseProcess;
    private Map<String, List<MigrationTaskStatusRecord>> statusRecords;
    private FullMigrationSubProcessCounter tableCounts;
    private FullMigrationSubProcessCounter viewCounts;
    private FullMigrationSubProcessCounter funcCounts;
    private FullMigrationSubProcessCounter triggerCounts;
    private FullMigrationSubProcessCounter produceCounts;
    private int totalWaitCount;
    private int totalRunningCount;
    private int totalSuccessCount;
    private int totalErrorCount;
    private MigrationTask task;
    private List<String> logs;
}
