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
 * SysTaskServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysTaskServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.spring.extract.ExtractFactory;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.common.enums.SysTaskStatus;
import org.opengauss.admin.system.domain.SysTask;
import org.opengauss.admin.system.mapper.SysTaskMapper;
import org.opengauss.admin.system.plugin.beans.MigrationTaskDetail;
import org.opengauss.admin.system.plugin.beans.TaskExecProgressDto;
import org.opengauss.admin.system.plugin.extract.TaskExtract;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Task Service
 *
 * @author xielibo
 */
@Service
@Slf4j
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements ISysTaskService {

    @Autowired
    private SysTaskMapper sysTaskMapper;

    @Autowired
    private ExtractFactory extractFactory;

    /**
     * Query the task list by page
     *
     * @param task SysTask
     * @return SysTask
     */
    @Override
    public IPage<SysTask> selectList(IPage<SysTask> page, SysTaskDto task) {
        return sysTaskMapper.selectTaskPage(page, task);
    }

    /**
     * Query all task
     *
     * @param task SysTask
     * @return SysTask
     */
    @Override
    public List<SysTask> selectListAll(SysTaskDto task) {
        return sysTaskMapper.selectTaskList(task);
    }

    public Integer countByProcessing() {
        LambdaQueryWrapper<SysTask> query = new LambdaQueryWrapper<>();
        query.eq(SysTask::getExecStatus, SysTaskStatus.PROCESSING.getCode());
        return Math.toIntExact(this.count(query));
    }

    public List<SysTask> listByProcessing() {
        LambdaQueryWrapper<SysTask> query = new LambdaQueryWrapper<>();
        query.eq(SysTask::getExecStatus, SysTaskStatus.PROCESSING.getCode());
        return list(query);
    }


    @Override
    public Map<String, Object> getDetailById(Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        SysTask task = this.getById(taskId);
        List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the TaskExtract implementation class. {}", extractByInterClass.get(0).getClass().getPackage());
            List<MigrationTaskDetail> pluginTask = extractByInterClass.get(0).getPluginTask(taskId);
            result.put("subTask", pluginTask);
        } else {
            log.error("No implementation found");
        }
        result.put("mainTask", task);
        return result;
    }


    @Override
    public void deleteTask(Integer[] ids) {
        Arrays.asList(ids).stream().forEach(i -> {
            SysTask task = this.getById(i);
            List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
            if (extractByInterClass.size() > 0) {
                log.info("Get the TaskExtract implementation class. {}", extractByInterClass.get(0).getClass().getPackage());
                extractByInterClass.get(0).deleteTask(i);
                this.removeById(i);
            } else {
                log.error("No implementation found");
            }
        });
    }


    @Override
    public void updateStatus(Integer id, SysTaskStatus taskStatus) {
        SysTask task = new SysTask();
        task.setId(id);
        task.setExecStatus(taskStatus.getCode());
        this.updateById(task);
    }

    @Override
    public void startTask(Integer id) {
        SysTask task = this.getById(id);
        List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the TaskExtract implementation class. {}", extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).startTask(id);
        } else {
            log.error("No implementation found");
        }
        SysTask updateTask = new SysTask();
        updateTask.setId(id);
        updateTask.setExecTime(new Date());
        updateTask.setExecStatus(SysTaskStatus.PROCESSING.getCode());
        this.updateById(updateTask);
    }

    @Override
    public void stopTask(Integer id) {
        SysTask task = this.getById(id);
        List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the TaskExtract implementation class. {}", extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).stopTask(id);
        } else {
            log.error("No implementation found");
        }
        this.updateStatus(id, SysTaskStatus.STOP);
    }


    @Override
    public void refreshTaskStatus() {
        Integer staringCount = this.countByProcessing();
        if (staringCount > 0) {
            List<SysTask> migrationTasks = this.listByProcessing();
            migrationTasks.stream().forEach((x -> {
                List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(x.getPluginId(), TaskExtract.class);
                if (extractByInterClass.size() > 0) {
                    log.info("Get the TaskExtract implementation class. {}", extractByInterClass.get(0).getClass().getPackage());
                    TaskExecProgressDto taskStatus = extractByInterClass.get(0).getTaskStatus(x.getId());
                    SysTask task = new SysTask();
                    task.setId(x.getId());
                    task.setExecStatus(taskStatus.getTaskStatus().getCode());
                    task.setExecProgress(taskStatus.getExecProgress());
                    if (taskStatus.getTaskStatus().getCode().equals(SysTaskStatus.FINISH.getCode())) {
                        task.setFinishTime(new Date());
                    }
                    this.updateById(task);
                } else {
                    log.error("No implementation found");
                }
            }));
        }
    }

}
