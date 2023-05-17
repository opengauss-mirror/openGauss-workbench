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
 * MigrationTaskService.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/MigrationTaskService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.enums.TaskStatus;

import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskService extends IService<MigrationTask> {

    IPage<MigrationTask> selectList(IPage<MigrationTask> page, Integer mainTaskId);

    void deleteByMainTaskId(Integer mainTaskId);

    List<MigrationTask> listByMainTaskId(Integer mainTaskId);

    Map<String, Object> getTaskDetailById(Integer taskId);

    Map<String, Object> getSingleTaskStatusAndProcessByProtal(MigrationTask t);

    /**
     * Query the number of tasks not finish by target
     *
     * @param targetNodeId targetNodeId of the OpsHost object
     * @param targetDb targetDb of the OpsHost object
     * @return number of tasks
     */
    Integer countNotFinishByTargetDb(String targetNodeId, String targetDb);

    /**
     * count the number of running tasks by target DB
     *
     * @param targetDb targetDb of the OpsHost object
     * @return count the number
     */
    Integer countRunningByTargetDb(String targetDb);

    /**
     * count the number of running tasks by hostID
     *
     * @param hostId hostId of the OpsHost object
     * @return count the number
     */
    Integer countRunningByHostId(String hostId);

    /**
     * list data running tasks by hostID
     *
     * @param hostId hostId of the OpsHost object
     * @return tasks
     */
    List<MigrationTask> listRunningTaskByHostId(String hostId);

    /**
     * list data tasks by status
     *
     * @param taskStatus TaskStatus Object
     * @return tasks
     */
    List<MigrationTask> listTaskByStatus(TaskStatus taskStatus);

    /**
     * count the number by status
     *
     * @param taskStatus TaskStatus Object
     * @return count the number
     */
    Integer countTaskByStatus(TaskStatus taskStatus);

    /**
     * update the task by id
     *
     * @param id id of TaskStatus object
     * @param taskStatus TaskStatus Object
     */
    void updateStatus(Integer id, TaskStatus taskStatus);

    /**
     * count the number on the model grouping by mainTaskId
     *
     * @param mainTaskId mainTaskId of MigrationTask Object
     * @return count the number for each model
     */
    List<Map<String, Object>> countByMainTaskIdGroupByModel(Integer mainTaskId);

    /**
     * run task
     *
     * @param h MigrationTaskHostRef Object
     * @param t  MigrationTask Object
     * @param globalParams
     */
    void runTask(MigrationTaskHostRef h, MigrationTask t, List<MigrationTaskGlobalParam> globalParams);

    /**
     * subtask Execution Offline Scheduler
     */
    void doOfflineTaskRunScheduler();
}
