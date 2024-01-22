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

package org.opengauss.tun.quartz;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.tun.cache.CacheFactory;
import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.enums.help.TuningHelper;
import org.opengauss.tun.service.impl.TrainingConfigImpl;
import org.opengauss.tun.service.impl.TuningServiceImpl;
import org.opengauss.tun.utils.MonitSpringUtils;
import org.opengauss.tun.utils.SchedulerUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * ProgressJob
 *
 * @author liu
 * @since 2023-12-20
 */
@Slf4j
public class ProgressJob implements Job {
    private final Integer totalTaskProgress = 100;

    private final Integer other = 20;

    private final Integer mainPy = 80;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String trainId = dataMap.getString(FixedTuning.TRAININGID);
        TrainingConfig config = MonitSpringUtils.getClass(TuningServiceImpl.class).selectById(trainId);
        Integer currentProgress = config.getProcess();
        Integer currentNum = getCurrentPro(trainId);
        if (ObjectUtil.isEmpty(currentProgress)) {
            log.error("Training ID does not exist");
            return;
        }
        if (config.getStatus().equals(FixedTuning.RUNING)) {
            if (currentProgress >= other && CacheFactory.getLogCache().obtain(trainId) != currentNum) {
                int res = cal(getCurrentPro(trainId), getTotal(trainId));
                MonitSpringUtils.getClass(TrainingConfigImpl.class).update(trainId, FixedTuning.RUNING,
                        other + res);
                CacheFactory.getLogCache().put(trainId, currentNum);
            }
        }
        if (config.getStatus().equals(FixedTuning.RUN_SUCCESS)) {
            TuningHelper.updateStatusSucc(trainId, totalTaskProgress);
            MonitSpringUtils.getClass(SchedulerUtils.class).deleteJob(trainId + FixedTuning.PROCESS_JOB,
                    FixedTuning.JOB_DEFAULT);
        }
    }

    private int cal(int current, int total) {
        return Math.round((float) current * mainPy / total);
    }

    private int getCurrentPro(String trainId) {
        return MonitSpringUtils.getClass(TuningServiceImpl.class).selectCountById(trainId);
    }

    private int getTotal(String trainId) {
        TrainingConfig config = MonitSpringUtils.getClass(TuningServiceImpl.class).selectById(trainId);
        if (ObjectUtil.isEmpty(config)) {
            TuningHelper.updateStatusFailed(trainId);
        }
        // 判断离线还是在线
        int total = 1;
        if (FixedTuning.FALSE.equals(config.getOnline())) {
            // 离线
            total = total + Integer.valueOf(config.getSamplingNumber()) + Integer.valueOf(config.getIteration());
        } else {
            total = total + Integer.valueOf(config.getIteration());
        }
        return total;
    }
}
