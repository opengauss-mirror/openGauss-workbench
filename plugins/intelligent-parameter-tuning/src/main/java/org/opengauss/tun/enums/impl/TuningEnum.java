/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.enums.impl;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.enums.TuningStrategy;
import org.opengauss.tun.enums.help.TuningHelper;
import org.opengauss.tun.utils.CommandLineRunner;

/**
 * AssessEnum
 *
 * @author liu
 * @since 2023-12-05
 */
@Slf4j
public enum TuningEnum implements TuningStrategy {
    SYSBENCH {
        @Override
        public void startIntelligentTuning(TrainingConfig config, String workPath, String executePath,
                                           OpsJdbcDbClusterNodeEntity node, String sysbenchCommand) {
            // run sysbench command 创建压测的数据库
            CommandLineRunner.appendToFile("Create a database for stress testing using the sysbench command",
                    config.getLogPath());
            boolean isSysbench = CommandLineRunner.runCommand(TuningHelper.getSysBenchCommand(node, config,
                    sysbenchCommand), FixedTuning.SYSBENCH_EXECUTE_PATH, config.getLogPath(), FixedTuning.TIME_OUT);
            if (isSysbench) {
                CommandLineRunner.appendToFile("Successfully created the database for stress "
                        + "testing and started executing python3 main.py", config.getLogPath());
                TuningHelper.commonTuningLogic(config, workPath, executePath);
            } else {
                CommandLineRunner.appendToFile("Failed to create database for stress testing, "
                        + "modify task status to failed", config.getLogPath());
                TuningHelper.updateStatusFailed(config.getTrainingId());
            }
        }
    },
    DWG {
        @Override
        public void startIntelligentTuning(TrainingConfig config, String workPath, String executePath,
                                           OpsJdbcDbClusterNodeEntity node, String sysbenchCommand) {
            // Update progress
            if (!config.getIsCustomPayloads().equals("1")) {
                // Use custom payload for all No
                TuningHelper.appendRes(config, workPath);
                String jsonHelpPath = workPath + "SuperWG/DWG/jsonHelper/";
                CommandLineRunner.appendToFile("Start executing jsonHelper.py", config.getLogPath());
                if (!CommandLineRunner.runCommand(FixedTuning.JSON_HELPER_COMMAND, jsonHelpPath, config.getLogPath(),
                        FixedTuning.TIME_OUT)) {
                    CommandLineRunner.appendToFile("Executing jsonHelper. py failed, "
                            + "modifying task status to failed", config.getLogPath());
                    TuningHelper.updateStatusFailed(config.getTrainingId());
                }
                String workLoadPath = workPath + "SuperWG/DWG/src/";
                CommandLineRunner.appendToFile("Start executing WorkloadGenerator.py", config.getLogPath());
                if (!CommandLineRunner.runCommand(FixedTuning.WORKLOAD_COMMAND, workLoadPath, config.getLogPath(),
                        FixedTuning.TIME_OUT_MAIN)) {
                    CommandLineRunner.appendToFile("Executing WorkloadGenerator. py failed, "
                            + "modifying task status to failed", config.getLogPath());
                    TuningHelper.updateStatusFailed(config.getTrainingId());
                }
                TuningHelper.execute(config, workPath, executePath);
            } else {
                TuningHelper.appendRes(config, workPath);
                TuningHelper.execute(config, workPath, executePath);
            }
        }
    }
}
