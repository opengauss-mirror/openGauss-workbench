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

    Integer countRunningByTargetDb(String targetDb);

    Integer countRunningByHostId(String hostId);

    List<MigrationTask> listRunningTaskByHostId(String hostId);

    List<MigrationTask> listTaskByStatus(TaskStatus taskStatus);

    Integer countTaskByStatus(TaskStatus taskStatus);

    void updateStatus(Integer id, TaskStatus taskStatus);

    List<Map<String, Object>> countByMainTaskIdGroupByModel(Integer mainTaskId);

    void runTask(MigrationTaskHostRef h, MigrationTask t, List<MigrationTaskGlobalParam> globalParams);

    void doOfflineTaskRunScheduler();
}
