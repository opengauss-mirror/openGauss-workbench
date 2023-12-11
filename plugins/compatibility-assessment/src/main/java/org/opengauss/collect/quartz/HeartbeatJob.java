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

import com.jcraft.jsch.Session;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.service.SqlTaskService;
import org.opengauss.collect.service.impl.SqlOperationImpl;
import org.opengauss.collect.utils.JschUtil;
import org.opengauss.collect.utils.MonitSpringUtils;
import org.opengauss.collect.utils.StringUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * HeartbeatJob
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class HeartbeatJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        LinuxConfig config = new LinuxConfig();
        Object obj = dataMap.get(Constant.CONFIG);
        if (obj != null && obj instanceof LinuxConfig) {
            config = (LinuxConfig) obj;
        }
        Session session = JschUtil.obtainSession(config);
        CollectPeriod task = new CollectPeriod();
        Object object = dataMap.get(Constant.TASK);
        if (object != null && object instanceof CollectPeriod) {
            task = (CollectPeriod) object;
        }
        String pid = StringUtil.getPid(task.getPid());
        String command = "if ps -p" + pid + "> /dev/null; "
                + "then echo \"yes\"; else echo \"not exist\"; fi";
        String res = JschUtil.executeCommand(session, command);
        // View the current task status
        Optional<CollectPeriod> optional = MonitSpringUtils.getClass(SqlTaskService.class)
                .selectById(task.getTaskId());
        if (res.contains("yes") && optional.isPresent() && optional.get().getCurrentStatus()
                .equals(Constant.TASK_RUN)) {
            log.info("SQL extraction task is in progress, please wait for the result.........");
        } else {
            log.info("SQL extraction task terminated due to non-existent or uncontrollable target process. "
                    + "Please restart");
            task.setCurrentStatus(Constant.TASK_STOP);
            MonitSpringUtils.getClass(SqlOperationImpl.class).updateCurrentStatus(task);
        }
    }
}
