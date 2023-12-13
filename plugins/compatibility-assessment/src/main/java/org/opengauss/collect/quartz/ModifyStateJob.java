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

package org.opengauss.collect.quartz;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.service.SqlTaskService;
import org.opengauss.collect.service.impl.SqlOperationImpl;
import org.opengauss.collect.utils.MonitSpringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * ModifyStateJob
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class ModifyStateJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        CollectPeriod task = new CollectPeriod();
        Object obj = dataMap.get(Constant.TASK);
        if (obj instanceof CollectPeriod) {
            task = (CollectPeriod) obj;
        }
        task.setCurrentStatus(Constant.TASK_COMPLETED);
        // View the current task status
        Optional<CollectPeriod> optional = MonitSpringUtils.getClass(SqlTaskService.class)
                .selectById(task.getTaskId());
        if (optional.isPresent() && optional.get().getCurrentStatus().equals(Constant.TASK_RUN)) {
            MonitSpringUtils.getClass(SqlOperationImpl.class).updateCurrentStatus(task);
        }
    }
}