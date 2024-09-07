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
 * MainTaskEnvErrorHost.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/domain/MainTaskEnvErrorHost.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;

import java.util.Map;

/**
 * full migration process counter
 *
 * @since 2024-09-06
 */
@Data
@NoArgsConstructor
public class FullMigrationProcessCounter {
    private static final int TABLE_MULTIPLE = 10;

    private int totalCount;
    private int totalProcessCount;
    private int totalFinishCount;
    private int totalRunningCount;
    private int totalErrorCount;
    private int totalSuccessCount;
    private int totalWaitCount;
    private int unCountedNumber;

    /**
     * build FullMigrationProcessCounter by processMap
     *
     * @param processMap processMap
     */
    public FullMigrationProcessCounter(Map<String, Object> processMap) {
        for (FullMigrationDbObjEnum objEnum : FullMigrationDbObjEnum.values()) {
            FullMigrationSubProcessCounter subProcessCounter =
                    new FullMigrationSubProcessCounter(processMap, objEnum.getObjectType());
            if (objEnum == FullMigrationDbObjEnum.TABLE) {
                totalProcessCount += subProcessCounter.getTotalCount() * TABLE_MULTIPLE;
                totalFinishCount += calculateFinishCount(subProcessCounter) * TABLE_MULTIPLE;
            } else {
                totalProcessCount += subProcessCounter.getTotalCount();
                totalFinishCount += calculateFinishCount(subProcessCounter);
            }
            totalCount += subProcessCounter.getTotalCount();
            totalSuccessCount += subProcessCounter.getSuccessCount();
            totalErrorCount += subProcessCounter.getErrorCount();
            totalWaitCount += subProcessCounter.getWaitCount();
            unCountedNumber += subProcessCounter.isUncounted() ? 1 : 0;
        }
    }

    private int calculateFinishCount(FullMigrationSubProcessCounter processCounter) {
        return processCounter.getSuccessCount() + processCounter.getErrorCount();
    }
}
