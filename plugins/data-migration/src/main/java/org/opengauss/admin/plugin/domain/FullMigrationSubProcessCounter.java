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

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * full migration sub process counter
 *
 * @since 2024-09-06
 */
@Data
@NoArgsConstructor
public class FullMigrationSubProcessCounter {
    private static final Set<Integer> WAIT_CODE_SET = Set.of(1);
    private static final Set<Integer> ERROR_CODE_SET = Set.of(6, 7);
    private static final Set<Integer> RUNNING_CODE_SET = Set.of(2);
    private static final Set<Integer> SUCCESS_CODE_SET = Set.of(3);
    private static final Set<Integer> TABLE_SUCCESS_CODE_SET = Set.of(3, 4, 5);

    private int totalCount;
    private int successCount;
    private int waitCount;
    private int runningCount;
    private int errorCount;
    private boolean isUncounted;

    /**
     * build FullMigrationSubProcessCounter
     *
     * @param processMap processMap
     * @param objType objType
     */
    public FullMigrationSubProcessCounter(Map<String, Object> processMap, String objType) {
        List<Map<String, Object>> processList = (List<Map<String, Object>>) processMap.get(objType);
        this.totalCount = processList.size();
        this.isUncounted = CollectionUtils.isEmpty(processList);
        this.waitCount = getCountByStatus(processList, WAIT_CODE_SET);
        this.errorCount = getCountByStatus(processList, ERROR_CODE_SET);
        this.runningCount = getCountByStatus(processList, RUNNING_CODE_SET);
        if (FullMigrationDbObjEnum.TABLE.getObjectType().equals(objType)) {
            this.successCount = getCountByStatus(processList, TABLE_SUCCESS_CODE_SET);
        } else {
            this.successCount = getCountByStatus(processList, SUCCESS_CODE_SET);
        }
    }

    private int getCountByStatus(List<Map<String, Object>> processList, Set<Integer> codeSet) {
        return Math.toIntExact(processList.stream()
                .filter(m -> codeSet.contains(MapUtil.getInt(m, "status")))
                .count());
    }
}
