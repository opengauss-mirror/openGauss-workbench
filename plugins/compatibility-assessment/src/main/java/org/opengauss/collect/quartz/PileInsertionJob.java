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
import lombok.extern.slf4j.Slf4j;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.utils.JschUtil;
import org.opengauss.collect.utils.StringUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * PileInsertionJob
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class PileInsertionJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("start inserting piles............");
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
        String executeTime = dataMap.getString(Constant.EXECUTE_TIME);
        String pid = StringUtil.getPid(task.getPid());
        String command = getCommand(pid, task) + "neverStop=false executionTime=" + executeTime + " " + "unit=minutes "
                + "writePath=" + task.getFilePath() + " threshold=10";
        log.info(command);
        String res = JschUtil.executeTask(task, command, session);
        if (!res.contains(Constant.PILE_SUCCESS)) {
            JschUtil.updateStatus(task);
            log.error("The insertion of the target process failed for an unknown reason. Please try again");
        }
    }

    private String getCommand(String pid, CollectPeriod task) {
        return Constant.INSERTION_ENVIRONMENT + ";" + Constant.INSERTION_PREFIX + " "
                + task.getFilePath() + Constant.INSERTION_ATTACHNAME + " "
                + pid + " " + task.getFilePath() + Constant.INSERTION_AGENTNAME
                + " " + "install" + " ";
    }
}
