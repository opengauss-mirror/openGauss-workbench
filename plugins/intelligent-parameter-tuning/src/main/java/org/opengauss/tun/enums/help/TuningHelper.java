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

package org.opengauss.tun.enums.help;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.domain.ParameterRecommendation;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.domain.TrainingProgress;
import org.opengauss.tun.domain.TuningLog;
import org.opengauss.tun.service.impl.ParameterRecommendationServiceImpl;
import org.opengauss.tun.service.impl.TrainingConfigImpl;
import org.opengauss.tun.service.impl.TrainingProgressServiceImpl;
import org.opengauss.tun.service.impl.TuningLogServiceImpl;
import org.opengauss.tun.service.impl.TuningServiceImpl;
import org.opengauss.tun.utils.Calculator;
import org.opengauss.tun.utils.CommandLineRunner;
import org.opengauss.tun.utils.IdUtils;
import org.opengauss.tun.utils.MonitSpringUtils;
import org.opengauss.tun.utils.SchedulerUtils;
import org.opengauss.tun.utils.file.FileExtractor;
import org.opengauss.tun.utils.file.FileLoader;

/**
 * TuningHelper
 *
 * @author liu liu
 * @since 2023-12-05
 */
@Slf4j
public class TuningHelper {
    /**
     * commonTuningLogic
     *
     * @param trainingConfig trainingConfig
     * @param workPath       workPath
     * @param executePath    executePath
     */
    public static void commonTuningLogic(TrainingConfig trainingConfig, String workPath, String executePath) {
        String trainingId = trainingConfig.getTrainingId();
        TrainingConfig config = MonitSpringUtils.getClass(TuningServiceImpl.class).selectById(trainingId);
        if (config.getStatus().equals(FixedTuning.RUN_FAILED)) {
            return;
        }
        boolean isMain = CommandLineRunner.runCommand(FixedTuning.MAIN_COMMAND, workPath, trainingConfig
                .getLogPath(), FixedTuning.TIME_OUT_MAIN);
        if (!isMain) {
            TuningHelper.updateStatusFailed(trainingConfig.getTrainingId());
        }
        TrainingProgress maxProgress = MonitSpringUtils.getClass(TrainingProgressServiceImpl.class)
                .getMaxTpsByByTunId(trainingConfig.getTrainingId());
        TrainingProgress initialProgress = MonitSpringUtils.getClass(TrainingProgressServiceImpl.class)
                .selectInitialTpsRecord(trainingConfig.getTrainingId(), FixedTuning.INITIALIZATION);
        if (ObjectUtil.isEmpty(maxProgress) || ObjectUtil.isEmpty(initialProgress)) {
            updateStatusFailed(trainingId);
            return;
        }
        ParameterRecommendation recommendation = createParameterRecommendation(trainingConfig, maxProgress,
                initialProgress);
        saveParameterAndLogInformation(recommendation, trainingConfig, workPath);
        int num = MonitSpringUtils.getClass(TrainingConfigImpl.class).update(trainingConfig.getTrainingId(),
                FixedTuning.RUN_SUCCESS, 100);
        if (num != 1) {
            updateStatusFailed(trainingId);
        }
    }

    /**
     * createParameterRecommendation
     *
     * @param trainingConfig  trainingConfig
     * @param maxProgress     maxProgress
     * @param initialProgress initialProgress
     * @return ParameterRecommendation
     */
    public static ParameterRecommendation createParameterRecommendation(TrainingConfig trainingConfig,
                                                                        TrainingProgress maxProgress,
                                                                        TrainingProgress initialProgress) {
        return ParameterRecommendation.builder()
                .parameterId(IdUtils.getId())
                .trainingId(trainingConfig.getTrainingId())
                .clusterName(trainingConfig.getClusterName())
                .startTime(trainingConfig.getStartTime())
                .initialTps(initialProgress.getTps())
                .optimalTps(maxProgress.getTps())
                .db(trainingConfig.getDb())
                .adviseParameterValues(maxProgress.getTrainingParameter())
                .initialParameterValues(initialProgress.getTrainingParameter())
                .performanceImprovement(Calculator.calculatePercentage(maxProgress.getTps(), initialProgress.getTps()))
                .build();
    }

    /**
     * saveParameterAndLogInformation
     *
     * @param recommendation recommendation
     * @param trainingConfig trainingConfig
     * @param workPath       workPath
     */
    public static void saveParameterAndLogInformation(ParameterRecommendation recommendation,
                                                      TrainingConfig trainingConfig, String workPath) {
        int num1 = MonitSpringUtils.getClass(ParameterRecommendationServiceImpl.class).saveParameter(recommendation);
        TuningLog log = new TuningLog();
        log.setTrainingId(trainingConfig.getTrainingId());
        log.setClusterName(trainingConfig.getClusterName());
        log.setStartTime(trainingConfig.getStartTime());
        int num2 = MonitSpringUtils.getClass(TuningLogServiceImpl.class).save(FileExtractor.getLog(log, workPath));
        if (num1 != 1 || num2 != 1) {
            updateStatusFailed(trainingConfig.getTrainingId());
        }
    }

    /**
     * getSysBenchCommand
     *
     * @param node            node
     * @param trainingConfig  trainingConfig
     * @param sysbenchCommand sysbenchCommand
     * @return String
     */
    public static String getSysBenchCommand(OpsJdbcDbClusterNodeEntity node, TrainingConfig trainingConfig,
                                            String sysbenchCommand) {
        return String.format(sysbenchCommand, node.getIp(), node.getPort(),
                node.getUsername(), node.getPassword(), trainingConfig.getDb(),
                trainingConfig.getTables(), trainingConfig.getTableSize(), trainingConfig.getMode());
    }

    /**
     * updateStatus
     *
     * @param trainingId trainingId
     */
    public static void updateStatusFailed(String trainingId) {
        MonitSpringUtils.getClass(TrainingConfigImpl.class).update(trainingId, FixedTuning.RUN_FAILED, null);
        MonitSpringUtils.getClass(SchedulerUtils.class).deleteJob(trainingId + FixedTuning.PROCESS_JOB,
                FixedTuning.JOB_DEFAULT);
        MonitSpringUtils.getClass(SchedulerUtils.class).deleteJob(trainingId + FixedTuning.EXECUTE_JOB,
                FixedTuning.JOB_DEFAULT);
        throw new ServiceException("Task interrupted, changing task status to failed");
    }

    /**
     * Progress of task modification in operation
     *
     * @param trainingId trainingId
     * @param process    process
     */
    public static void updateProcess(String trainingId, Integer process) {
        MonitSpringUtils.getClass(TrainingConfigImpl.class).update(trainingId, FixedTuning.RUNING, process);
    }

    /**
     * updateStatusSucc
     *
     * @param trainingId trainingId
     * @param process    process
     */
    public static void updateStatusSucc(String trainingId, Integer process) {
        MonitSpringUtils.getClass(TrainingConfigImpl.class).update(trainingId, FixedTuning.RUN_SUCCESS, process);
    }

    /**
     * appendFileContent
     *
     * @param customLoad customLoad
     * @param workPath   workPath
     */
    public static void appendFileContent(List<File> customLoad, String workPath) {
        try (FileWriter fileWriter = new FileWriter(new File(workPath + FixedTuning.LOAD_FILE_PATH),
                true);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            for (File file : customLoad) {
                try (FileReader fileReader = new FileReader(file);
                     BufferedReader reader = new BufferedReader(fileReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * appendRes
     *
     * @param config   config
     * @param workPath workPath
     */
    public static void appendRes(TrainingConfig config, String workPath) {
        if (config.getFileName() != null && !config.getFileName().isEmpty()) {
            // 全部使用自定义负载 是
            List<String> fileNames = Arrays.asList(config.getFileName().split(","));
            if (CollectionUtil.isNotEmpty(fileNames)) {
                List<File> files = FileLoader.getFilesFromNames(fileNames, FixedTuning.UPLOAD_FILE_PATH);
                TuningHelper.appendFileContent(files, workPath);
            }
        }
    }

    /**
     * record
     *
     * @param content content
     * @param path    path
     */
    public static void record(String content, String path) {
        CommandLineRunner.appendToFile(content, path);
    }

    /**
     * execute
     *
     * @param config      config
     * @param workPath    workPath
     * @param executePath executePath
     */
    public static void execute(TrainingConfig config, String workPath, String executePath) {
        CommandLineRunner.appendToFile("start executing python3 main. py", config.getLogPath());
        commonTuningLogic(config, workPath, executePath);
    }
}