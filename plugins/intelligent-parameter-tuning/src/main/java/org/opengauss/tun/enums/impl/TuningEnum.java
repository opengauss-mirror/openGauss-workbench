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
import org.opengauss.tun.domain.dto.CommandExecution;
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
            CommandExecution sysbench = CommandExecution.builder().command(TuningHelper.getSysBenchCommand(node, config,
                            sysbenchCommand)).dbPassword(config.getPassword())
                    .filePath(FixedTuning.SYSBENCH_EXECUTE_PATH).writePath(config.getLogPath())
                    .timeOut(FixedTuning.TIME_OUT).build();
            boolean isSysbench = CommandLineRunner.runCommand(sysbench);
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
                CommandExecution jsonHelp = CommandExecution.builder().command(FixedTuning.JSON_HELPER_COMMAND)
                        .filePath(jsonHelpPath)
                        .dbPassword(config.getPassword())
                        .installPassword(config.getOmmPassword())
                        .rootPassword(config.getRootPassword())
                        .writePath(config.getLogPath())
                        .timeOut(FixedTuning.TIME_OUT).build();
                if (!CommandLineRunner.runCommand(jsonHelp)) {
                    CommandLineRunner.appendToFile("Executing jsonHelper. py failed, "
                            + "modifying task status to failed", config.getLogPath());
                    TuningHelper.updateStatusFailed(config.getTrainingId());
                }
                String workLoadPath = workPath + "SuperWG/DWG/src/";
                CommandLineRunner.appendToFile("Start executing WorkloadGenerator.py", config.getLogPath());
                CommandExecution workLoad = CommandExecution.builder().command(FixedTuning.WORKLOAD_COMMAND)
                        .filePath(workLoadPath)
                        .writePath(config.getLogPath())
                        .dbPassword(config.getPassword())
                        .installPassword(config.getOmmPassword())
                        .rootPassword(config.getRootPassword())
                        .timeOut(FixedTuning.TIME_OUT).build();
                if (!CommandLineRunner.runCommand(workLoad)) {
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
