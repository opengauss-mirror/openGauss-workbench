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
 * MigrationMainTaskService.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/MigrationMainTaskService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.MigrationMainTask;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;
import org.opengauss.admin.plugin.dto.MigrationTaskDto;
import org.opengauss.admin.plugin.enums.MainTaskStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Task Service Interface
 *
 * @author xielibo
 */
public interface MigrationMainTaskService extends IService<MigrationMainTask> {

    IPage<MigrationMainTask> selectList(IPage<MigrationMainTask> page, MigrationMainTaskDto task);

    List<String> selectCreateUsers();

    Map<String, Object> getDetailById(Integer taskId);

    MigrationTaskDto getMigrationTaskDtoById(Integer taskId);

    void saveTask(MigrationTaskDto taskDto);

    @Transactional
    AjaxResult updateTask(MigrationTaskDto taskDto);

    void deleteTask(Integer[] ids);

    void updateStatus(Integer id, MainTaskStatus taskStatus);

    AjaxResult startTask(Integer id);

    void finishTask(Integer id);

    AjaxResult finishSubTask(Integer id);

    AjaxResult stopSubTaskIncremental(Integer id);

    AjaxResult startSubTaskReverse(Integer id);

    void refreshTaskStatusByPortal(Integer taskId);

    void doRefreshMainTaskStatus();
}
