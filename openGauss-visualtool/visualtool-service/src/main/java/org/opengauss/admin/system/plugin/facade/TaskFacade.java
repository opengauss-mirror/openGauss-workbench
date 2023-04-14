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
 * TaskFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/TaskFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.enums.SysTaskStatus;
import org.opengauss.admin.common.enums.SysTaskType;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.domain.SysTask;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @className: TaskFacade
 * @description: Task service provided to plugins.
 * @author: xielibo
 * @date: 2023-01-13 8:59 PM
 **/
@Service
public class TaskFacade {

    @Autowired
    private ISysTaskService sysTaskService;

    /**
     * Add tasks to the platform task list
     * @param pluginId
     * @return taskId
     */
    public Integer saveTask(String pluginId, String hostId, String taskName, SysTaskType taskType, Integer subTaskCount){
        SysTask task = new SysTask();
        task.setPluginId(pluginId);
        task.setTaskType(taskType.getCode());
        task.setTaskName(taskName);
        task.setExecHostId(hostId);
        task.setCreateTime(new Date());
        task.setExecStatus(SysTaskStatus.CREATED.getCode());
        task.setSubTaskCount(subTaskCount);
        sysTaskService.save(task);
        return task.getId();
    }
}
