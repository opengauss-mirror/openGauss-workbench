/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.tun.enums;

import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.tun.domain.TrainingConfig;

/**
 * AssessmentStrategy
 *
 * @author liu
 * @since 2023-12-05
 */
public interface TuningStrategy {
    /**
     * startIntelligentTuning
     *
     * @param trainingConfig trainingConfig
     * @param workPath workPath
     * @param executePath executePath
     * @param node node
     * @param sysbenchCommand sysbenchCommand
     */
    void startIntelligentTuning(TrainingConfig trainingConfig, String workPath,
                                String executePath, OpsJdbcDbClusterNodeEntity node, String sysbenchCommand);
}