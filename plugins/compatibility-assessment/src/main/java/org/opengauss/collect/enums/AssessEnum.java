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

package org.opengauss.collect.enums;

import cn.hutool.core.util.ObjectUtil;
import com.jcraft.jsch.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.manager.MonitorManager;
import org.opengauss.collect.service.impl.SqlOperationImpl;
import org.opengauss.collect.service.impl.SqlTaskServiceImpl;
import org.opengauss.collect.utils.AssertUtil;
import org.opengauss.collect.utils.JschUtil;
import org.opengauss.collect.utils.MonitSpringUtils;
import org.opengauss.collect.utils.file.FileUploadUtil;

/**
 * AssessEnum
 *
 * @author liu
 * @since 2023-12-05
 */
@Slf4j
public enum AssessEnum implements AssessmentStrategy {
    FILE_ASSESSMENT_STRATEGY {
        @Override
        public void startAssessment(Assessment assessment, Integer userId, String dataKitPath) {
            // The format of uploading and decompressing to dataKitPath is currently/ops/files/access/
            if (assessment.getFile() != null && !assessment.getFile().isEmpty()) {
                log.info("Start decompressing the uploaded file to the specified path " + dataKitPath);
                // After the evaluation is completed, delete the cache files in the assess directory
                FileUploadUtil.delCache(dataKitPath);
                FileUploadUtil.extractResource(assessment.getFile(), dataKitPath);
            }
        }
    },
    COLLECT_ASSESSMENT_STRATEGY {
        @Override
        public void startAssessment(Assessment assessment, Integer userId, String dataKitPath) {
            log.info("sqlInputType is COLLECT_ASSESSMENT_STRATEGY");
        }
    },
    PROCCESS_ASSESSMENT_STRATEGY {
        @Override
        public void startAssessment(Assessment assessment, Integer userId, String dataKitPath) {
            // Obtain the IP instrumentation file path for this server based on the ID. Currently,
            // dataKitPath is/ops/files/access/
            // After the evaluation is completed, delete the cache files in the assess directory
            FileUploadUtil.delCache(dataKitPath);
            String pid = assessment.getProccessPid();
            List<CollectPeriod> periodList = MonitSpringUtils.getClass(SqlTaskServiceImpl.class).getListByPid(pid);
            for (CollectPeriod period : periodList) {
                if (ObjectUtil.isEmpty(period)) {
                    continue;
                }
                Optional<LinuxConfig> config = MonitSpringUtils.getClass(SqlOperationImpl.class)
                        .getLinuxConfig(period.getHost());
                AssertUtil.isTrue(!config.isPresent(), "Host does not exist");
                Session session = JschUtil.obtainSession(config.get());
                // 获得文件路径
                String remotePath = period.getFilePath();
                List<String> fileNames = JschUtil.getFileNamesByPath(session, period.getFilePath(), true);
                List<String> res = new ArrayList<>();
                try {
                    for (String name : fileNames) {
                        Future<String> future = MonitorManager.mine().workDownload(new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return JschUtil.downLoadLocal(session, remotePath + name,
                                        dataKitPath + name);
                            }
                        });
                        res.add(future.get());
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    log.error(exception.getMessage());
                }
                // close session
                JschUtil.closeSession(session);
            }
        }
    }
}
