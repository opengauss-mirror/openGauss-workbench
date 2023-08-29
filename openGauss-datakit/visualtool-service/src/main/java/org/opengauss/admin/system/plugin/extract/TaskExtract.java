/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * TaskExtract.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/extract/TaskExtract.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.extract;

import org.opengauss.admin.system.plugin.beans.BasePluginTaskDetailDto;
import org.opengauss.admin.system.plugin.beans.MigrationTaskDetail;
import org.opengauss.admin.system.plugin.beans.TaskExecProgressDto;

import java.util.List;

/**
 * @className: TaskExtract
 * @description: Used to receive messages from clients
 * @author: xielibo
 * @date: 2022-11-14 21:33
 **/
public interface TaskExtract {

    /**
     * Get subtask details
     * @param taskId
     * @return
     */
    public List<MigrationTaskDetail> getPluginTask(Integer taskId);

    /**
     * Get the task status, summarize from the subtasks in the plug-in,
     * the specific logic is implemented by the plug-in, and return the plug-in status enumeration defined by the platform
     * @param taskId
     * @return
     */
    public TaskExecProgressDto getTaskStatus(Integer taskId);


    /**
     * start task
     * @param taskId
     * @return
     */
    public void startTask(Integer taskId);


    /**
     * stop task
     * @param taskId
     * @return
     */
    public void stopTask(Integer taskId);

    /**
     * delete task
     * @param taskId
     * @return
     */
    public void deleteTask(Integer taskId);
}
