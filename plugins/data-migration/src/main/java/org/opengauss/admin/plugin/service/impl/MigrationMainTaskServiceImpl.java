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
 * MigrationMainTaskServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationMainTaskServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.core.dto.ops.ClusterNodeDto;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.plugin.domain.*;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;
import org.opengauss.admin.plugin.dto.MigrationTaskDto;
import org.opengauss.admin.plugin.enums.MainTaskStatus;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.enums.ProcessType;
import org.opengauss.admin.plugin.enums.TaskOperate;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationMainTaskMapper;
import org.opengauss.admin.plugin.service.*;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Task Service
 *
 * @author xielibo
 */
@Service
@Slf4j
public class MigrationMainTaskServiceImpl extends ServiceImpl<MigrationMainTaskMapper, MigrationMainTask> implements MigrationMainTaskService {

    private static Map<Integer, Long> taskRefreshRecord = new ConcurrentHashMap<>();
    @Autowired
    private MigrationMainTaskMapper migrationMainTaskMapper;
    @Autowired
    private MigrationTaskService migrationTaskService;
    @Autowired
    private MigrationTaskHostRefService migrationTaskHostRefService;
    @Autowired
    private MigrationTaskGlobalParamService migrationTaskGlobalParamService;
    @Autowired
    private MigrationTaskParamService migrationTaskParamService;
    @Autowired
    private MigrationTaskModelService migrationTaskModelService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private MigrationTaskOperateRecordService migrationTaskOperateRecordService;
    @Autowired
    private MigrationTaskStatusRecordService migrationTaskStatusRecordService;
    @Autowired
    private MigrationTaskExecResultDetailService migrationTaskExecResultDetailService;
    @Autowired
    private MainTaskEnvErrorHostService mainTaskEnvErrorHostService;
    @Autowired
    private MigrationHostPortalInstallHostService migrationHostPortalInstallHostService;
    @Value("${migration.taskRefreshIntervalsMillisecond}")
    private Long taskRefreshIntervalsMillisecond;
    @Value("${migration.mainTaskRefreshIntervalsMillisecond}")
    private Long mainTaskRefreshIntervalsMillisecond;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * Query the task list by page
     *
     * @param task SysTask
     * @return SysTask
     */
    @Override
    public IPage<MigrationMainTask> selectList(IPage<MigrationMainTask> page, MigrationMainTaskDto task) {
        return migrationMainTaskMapper.selectTaskPage(page, task);
    }

    /**
     * select all create user
     *
     * @return create users
     */
    @Override
    public List<String> selectCreateUsers() {
        return migrationMainTaskMapper.selectCreateUsers();
    }

    public Integer countByProcessing() {
        LambdaQueryWrapper<MigrationMainTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationMainTask::getExecStatus, MainTaskStatus.RUNNING.getCode());
        return Math.toIntExact(count(query));
    }

    public List<MigrationMainTask> listByProcessing() {
        LambdaQueryWrapper<MigrationMainTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationMainTask::getExecStatus, MainTaskStatus.RUNNING.getCode());
        return list(query);
    }

    private Map<String, Integer> calculateTaskCount(List<MigrationTask> tasks, Integer modelId) {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("total", tasks.size());
        long notRunCount = tasks.stream().filter(t -> {
            return t.getExecStatus().equals(TaskStatus.NOT_RUN.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.WAIT_RESOURCE.getCode());
        }).count();

        Predicate<MigrationTask> runningFilter = t -> {
            return t.getExecStatus().equals(TaskStatus.FULL_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_RUNNING.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_FINISH.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_CHECK_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_CHECKING.getCode());
        };
        if (modelId.equals(2)) {
            runningFilter = t -> {
                return t.getExecStatus().equals(TaskStatus.FULL_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_RUNNING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_FINISH.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_CHECK_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_CHECKING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_RUNNING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_FINISHED.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_STOPPED.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_RUNNING.getCode());
            };
        }
        long runningCount = tasks.stream().filter(runningFilter).count();
        Predicate<MigrationTask> finishFilter = t -> t.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode());
        if (modelId.equals(1)) {
            finishFilter = t -> t.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode());
        }
        long finishCount = tasks.stream().filter(finishFilter).count();
        long errorCount = tasks.stream().filter(t -> t.getExecStatus().equals(TaskStatus.MIGRATION_ERROR.getCode()))
            .count();
        long checkErrorCount = tasks.stream().filter(t -> t.getExecStatus().equals(TaskStatus.CHECK_ERROR.getCode()))
            .count();
        countMap.put("notRunCount", Math.toIntExact(notRunCount));
        countMap.put("runningCount", Math.toIntExact(runningCount));
        countMap.put("finishCount", Math.toIntExact(finishCount));
        countMap.put("errorCount", Math.toIntExact(errorCount));
        countMap.put("checkErrorCount", Math.toIntExact(checkErrorCount));
        return countMap;
    }

    /**
     * Get task detail
     *
     * @param taskId
     * @return
     */
    @Override
    public Map<String, Object> getDetailById(Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        MigrationMainTask task = getById(taskId);
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(taskId);
        List<MigrationTask> offlineTasks = tasks.stream().filter(t -> t.getMigrationModelId().equals(1)).collect(Collectors.toList());
        List<MigrationTask> onlineTasks = tasks.stream().filter(t -> t.getMigrationModelId().equals(2)).collect(Collectors.toList());
        List<MigrationTaskHostRef> hosts = migrationTaskHostRefService.listByMainTaskId(taskId);
        result.put("offlineCounts", calculateTaskCount(offlineTasks, 1));
        result.put("onlineCounts", calculateTaskCount(onlineTasks, 2));
        result.put("hosts", hosts);
        result.put("task", task);
        List<MigrationTaskGlobalParam> globalParams = migrationTaskGlobalParamService.selectByMainTaskId(taskId);
        result.put("globalParams", globalParams);
        return result;
    }


    /**
     * Get task detail
     *
     * @param taskId
     * @return
     */
    @Override
    public MigrationTaskDto getMigrationTaskDtoById(Integer taskId) {
        MigrationMainTask task = getById(taskId);
        MigrationTaskDto dto = new MigrationTaskDto();
        dto.setTaskId(task.getId());
        dto.setTaskName(task.getTaskName());

        List<MigrationTaskGlobalParam> globalParams = migrationTaskGlobalParamService.selectByMainTaskId(taskId);
        dto.setGlobalParams(globalParams);
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(taskId);
        tasks.stream().forEach(t -> {
            t.setTaskParams(migrationTaskParamService.selectByTaskId(t.getId()));
            t.setIsSystemAdmin(JdbcUtil.judgeSystemAdmin(t.getTargetDbHost(), t.getTargetDbPort(), t.getTargetDbUser(),
                encryptionUtils.decrypt(t.getTargetDbPass())));
        });
        dto.setTasks(tasks);
        List<MigrationTaskHostRef> hosts = migrationTaskHostRefService.listByMainTaskId(taskId);
        dto.setHostIds(hosts.stream().map(MigrationTaskHostRef::getRunHostId).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public void saveTask(MigrationTaskDto taskDto) {
        MigrationMainTask mainTask = new MigrationMainTask();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        mainTask.setCreateUser(loginUser != null ? loginUser.getUsername() : "unknown");
        mainTask.setTaskName(taskDto.getTaskName());
        mainTask.setCreateTime(new Date());
        mainTask.setExecStatus(MainTaskStatus.NOT_RUN.getCode());
        save(mainTask);

        List<MigrationTaskParam> batchTaskParams = new ArrayList<>();
        taskDto.getTasks().stream().forEach(t -> {
            t.setMainTaskId(mainTask.getId());
            t.setCreateTime(new Date());
            t.setExecStatus(TaskStatus.NOT_RUN.getCode());
            MigrationTaskModel model = migrationTaskModelService.getById(t.getMigrationModelId());
            t.setMigrationOperations(model.getMigrationOperations());
            migrationTaskService.save(t);
            if (t.getTaskParams().size() > 0) {
                t.getTaskParams().stream().forEach(param -> {
                    param.setTaskId(t.getId());
                    param.setMainTaskId(mainTask.getId());
                });
                batchTaskParams.addAll(t.getTaskParams());
            }
        });
        batchTaskParams.stream().forEach(tp -> {
            tp.setId(null);
            migrationTaskParamService.save(tp);
        });
        taskDto.getHostIds().stream().forEach(h -> {
            MigrationTaskHostRef hostRef = MigrationTaskHostRef.builder().runHostId(h).mainTaskId(mainTask.getId()).createTime(new Date()).build();
            migrationTaskHostRefService.save(hostRef);
        });
        taskDto.getGlobalParams().stream().forEach(param -> {
            param.setId(null);
            param.setMainTaskId(mainTask.getId());
            migrationTaskGlobalParamService.save(param);
        });
    }

    @Override
    @Transactional
    public AjaxResult updateTask(MigrationTaskDto taskDto) {
        MigrationMainTask mainTask = migrationMainTaskMapper.selectById(taskDto.getTaskId());
        if (mainTask == null) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (mainTask.getExecStatus() != TaskStatus.NOT_RUN.getCode()) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getMsg());
        }

        MigrationMainTask update = getById(taskDto.getTaskId());
        update.setTaskName(taskDto.getTaskName());
        updateById(update);

        migrationTaskParamService.deleteByMainTaskId(taskDto.getTaskId());
        migrationTaskService.deleteByMainTaskId(taskDto.getTaskId());
        List<MigrationTaskParam> batchTaskParams = new ArrayList<>();
        taskDto.getTasks().stream().forEach(t -> {
            t.setMainTaskId(mainTask.getId());
            t.setCreateTime(new Date());
            t.setExecStatus(TaskStatus.NOT_RUN.getCode());
            MigrationTaskModel model = migrationTaskModelService.getById(t.getMigrationModelId());
            t.setMigrationOperations(model.getMigrationOperations());
            migrationTaskService.save(t);
            if (t.getTaskParams().size() > 0) {
                t.getTaskParams().stream().forEach(param -> {
                    param.setTaskId(t.getId());
                    param.setMainTaskId(mainTask.getId());
                });
                batchTaskParams.addAll(t.getTaskParams());
            }
        });
        batchTaskParams.stream().forEach(tp -> {
            tp.setId(null);
            migrationTaskParamService.save(tp);
        });
        migrationTaskHostRefService.deleteByMainTaskId(taskDto.getTaskId());
        taskDto.getHostIds().stream().forEach(h -> {
            MigrationTaskHostRef hostRef = MigrationTaskHostRef.builder().runHostId(h).mainTaskId(mainTask.getId()).createTime(new Date()).build();
            migrationTaskHostRefService.save(hostRef);
        });

        migrationTaskGlobalParamService.deleteByMainTaskId(taskDto.getTaskId());
        taskDto.getGlobalParams().stream().forEach(param -> {
            param.setId(null);
            param.setMainTaskId(mainTask.getId());
            migrationTaskGlobalParamService.save(param);
        });
        return AjaxResult.success();
    }

    @Override
    public void deleteTask(Integer[] ids) {
        Arrays.asList(ids).stream().forEach(i -> {
            removeById(i);
            migrationTaskService.deleteByMainTaskId(i);
            migrationTaskParamService.deleteByMainTaskId(i);
            migrationTaskGlobalParamService.deleteByMainTaskId(i);
            migrationTaskHostRefService.deleteByMainTaskId(i);
        });
    }


    @Override
    public void updateStatus(Integer id, MainTaskStatus taskStatus) {
        MigrationMainTask task = getById(id);
        task.setExecStatus(taskStatus.getCode());
        if (taskStatus.getCode().equals(MainTaskStatus.RUNNING.getCode())) {
            task.setExecTime(new Date());
        }
        if (taskStatus.getCode().equals(MainTaskStatus.FINISH.getCode())) {
            task.setFinishTime(new Date());
        }
        updateById(task);
    }

    /**
     * Start the task process:
     * 1. Query subtask list; query running configuration machine list; query global configuration and subtask configuration data;
     * 2. Loop through each machine, use the maximum number of executable tasks - the number of running tasks to get the number of tasks that can be executed by each machine, assign tasks according to the number of executable tasks, and judge whether the machine is installed with portal, if not Install, then install the portal. The following situations may be encountered when assigning tasks:
     * a. If the total number of executable tasks of all machines > the number of subtasks,
     * it is allocated according to the number of executable tasks of each machine, and then the configuration parameters are processed, the portal process is invoked, and task instructions are issued.
     * b. The total number of executable tasks of all machines < the number of subtasks,
     * For example, there are 3 machines, A machine can run 3, B machine can run 6, C machine can run 5,
     * the overall number of tasks can be 14, and the number of subtasks is 20.
     * At this time, the 14 tasks are first allocated according to the executable quantity of each machine,
     * and then the configuration parameters are processed, the portal process is invoked, and task instructions are issued. The status of the remaining 6 tasks is changed to waiting for resources, which are called by the asynchronous scheduler.
     * The asynchronous scheduler will regularly monitor whether the three machines have resources released, and if there are resources released, tasks will be assigned according to the carrying capacity.
     * 3. For the task assigned to the machine, log in to the remote server, process configuration parameters, start the portal process, and pass in the subtask ID to start the process. Then write the instructions of the task to the input file of Portal.
     * 4. Modify the subtask status, and modify the task to the running state.
     *
     * @param id
     */
    @Override
    public AjaxResult startTask(Integer id) {
        MigrationMainTask mainTask = getById(id);
        if (mainTask == null) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (mainTask.getExecStatus().equals(MainTaskStatus.RUNNING.getCode()) || mainTask.getExecStatus().equals(MainTaskStatus.FINISH.getCode())) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getMsg());
        }
        updateStatus(id, MainTaskStatus.RUNNING);
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(id);
        List<MigrationTaskHostRef> hosts = migrationTaskHostRefService.listByMainTaskId(id);
        List<MigrationTaskGlobalParam> globalParams = migrationTaskGlobalParamService.selectByMainTaskId(id);
        Integer totalRunnableCount = hosts.stream().mapToInt(MigrationTaskHostRef::getRunnableCount).sum();
        if (totalRunnableCount > tasks.size()) { //The number of host executable tasks is greater than the total number of tasks
            for (int x = 0; x < tasks.size(); x++) {
                int size = hosts.stream().filter(h -> h.getRunnableCount() > 0).collect(Collectors.toList()).size();
                int curHostIndex = x % size;
                MigrationTask t = tasks.get(x);
                MigrationTaskHostRef h = hosts.get(curHostIndex);
                h.addPlaceHolderCount();
                threadPoolTaskExecutor.submit(() -> migrationTaskService.runTask(h, t, globalParams));
            }
        } else { //The number of tasks that the host can perform is less than the total number of tasks
            hosts.stream().forEach(h -> {
                Integer runnableCount = h.getRunnableCount();
                for (int i = tasks.size() - 1; i >= tasks.size() - runnableCount; i--) {
                    MigrationTask t = tasks.get(i);
                    threadPoolTaskExecutor.submit(() -> migrationTaskService.runTask(h, t, globalParams));
                    tasks.remove(i);
                }
            });
            //For tasks that have not been assigned a host,
            // change the status to 1000 and be processed by the offline scheduler
            tasks.stream().forEach(t -> {
                migrationTaskService.updateStatus(t.getId(), TaskStatus.WAIT_RESOURCE);
            });
        }
        return AjaxResult.success();
    }
    
    @Transactional
    @Override
    public AjaxResult resetTask(Integer id) {
        MigrationMainTask mainTask = getById(id);
        if (mainTask == null || mainTask.getId() == null) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (!mainTask.getExecStatus().equals(MainTaskStatus.FINISH.getCode())) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getMsg());
        }
        mainTask.setExecStatus(MainTaskStatus.NOT_RUN.getCode());
        mainTask.setFinishTime(null);
        mainTask.setExecTime(null);
        mainTask.setExecProgress("0.0");
        migrationMainTaskMapper.updateById(mainTask);
        
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(id);
        migrationTaskService.deleteByMainTaskId(id);
        tasks.forEach(task -> {
            task.setId(null);
            task.setExecTime(null);
            task.setFinishTime(null);
            task.setExecStatus(TaskStatus.NOT_RUN.getCode());
            task.setTaskLog("");
            task.setStatusDesc("");
            migrationTaskService.save(task);
        });
        return AjaxResult.success();
    }

    @Override
    public void finishTask(Integer id) {
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(id);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        tasks.stream().forEach(t -> {
            MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(t.getRunHostId());
            PortalHandle.finishPortal(t.getRunHost(), t.getRunPort(), t.getRunUser(), encryptionUtils.decrypt(t.getRunPass()), installHost.getInstallPath(), installHost.getJarName(), t);
            MigrationTask update = MigrationTask.builder().id(t.getId()).execStatus(TaskStatus.MIGRATION_FINISH.getCode()).finishTime(new Date()).build();
            migrationTaskOperateRecordService.saveRecord(t.getId(), TaskOperate.FINISH_MIGRATION, loginUser.getUsername());
            migrationTaskService.updateById(update);
        });
        updateStatus(id, MainTaskStatus.FINISH);
    }

    @Override
    public AjaxResult finishSubTask(Integer id) {
        MigrationTask subTask = migrationTaskService.getById(id);
        if (subTask == null) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(subTask.getRunHostId());
        PortalHandle.finishPortal(subTask.getRunHost(), subTask.getRunPort(),
                subTask.getRunUser(), encryptionUtils.decrypt(subTask.getRunPass()), installHost.getInstallPath(), installHost.getJarName(), subTask);
        MigrationTask update = MigrationTask.builder().id(subTask.getId()).execStatus(TaskStatus.MIGRATION_FINISH.getCode()).finishTime(new Date()).build();
        migrationTaskService.updateById(update);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        migrationTaskOperateRecordService.saveRecord(subTask.getId(), TaskOperate.FINISH_MIGRATION, loginUser.getUsername());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult stopSubTaskIncremental(Integer id) {
        MigrationTask subTask = migrationTaskService.getById(id);
        if (subTask == null) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (subTask.getExecStatus() != TaskStatus.INCREMENTAL_START.getCode()
                && subTask.getExecStatus() != TaskStatus.INCREMENTAL_RUNNING.getCode()) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_ERROR.getMsg());
        }
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(subTask.getRunHostId());
        PortalHandle.stopIncrementalPortal(subTask.getRunHost(), subTask.getRunPort(),
                subTask.getRunUser(), encryptionUtils.decrypt(subTask.getRunPass()), installHost.getInstallPath(), installHost.getJarName(), subTask);
        MigrationTask update = MigrationTask.builder().id(subTask.getId()).execStatus(TaskStatus.INCREMENTAL_FINISHED.getCode()).execTime(new Date()).build();
        migrationTaskService.updateById(update);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        migrationTaskOperateRecordService.saveRecord(subTask.getId(), TaskOperate.STOP_INCREMENTAL, loginUser.getUsername());
        return AjaxResult.success();
    }


    /**
     * Check if the conditions for reverse migration are met.
     *
     * @param subTask MigrationTask Object
     * @return check result
     */
    private Optional<AjaxResult> checkReverseCondition(MigrationTask subTask) {
        ClusterNodeDto clusterNode = hostFacade.getClusterNodeByNodeId(subTask.getTargetNodeId());
        if (StringUtils.isBlank(clusterNode.getPublicIp())) {
            return Optional.of(AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_SUPPORT_REVERSE_ERROR.getCode(),
                    MigrationErrorCode.SUB_TASK_NOT_SUPPORT_REVERSE_ERROR.getMsg()));
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("dbUser", subTask.getTargetDbUser());
            String password = encryptionUtils.decrypt(clusterNode.getInstallPassword());
            boolean permiseIsOk = PortalHandle.checkTargetNodeReplicationPermise(clusterNode.getPublicIp(),
                    clusterNode.getPort(), clusterNode.getInstallUserName(), password, clusterNode.getDataPath(),
                    subTask.getTargetDbUser());
            result.put("replacationPermise", permiseIsOk);
            Map<String, Object> configCheckResult = PortalHandle.checkTargetNodeConfigCorrectness(clusterNode
                            .getPublicIp(), clusterNode.getPort(), clusterNode.getInstallUserName(), password,
                    clusterNode.getDataPath());
            boolean configIsOk = MapUtil.getBool(configCheckResult, "checkResult");
            result.putAll(configCheckResult);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT r.rolcanlogin AS rolcanlogin, r.rolreplication AS rolreplication ");
            stringBuilder.append("FROM pg_roles r WHERE r.rolname = current_user");
            List<Map<String, Object>> userPermiseResults = migrationTaskHostRefService.queryBySqlOnOpengauss(
                subTask.getTargetDbHost(), subTask.getTargetDbPort(), "postgres",
                subTask.getTargetDbUser(), encryptionUtils.decrypt(subTask.getTargetDbPass()),
                "public", stringBuilder.toString());
            boolean checkUserPermiseIsOk = false;
            if (userPermiseResults.size() > 0) {
                Map<String, Object> singleMap = userPermiseResults.get(0);
                String rolcanlogin = MapUtil.getStr(singleMap, "rolcanlogin");
                String rolreplication = MapUtil.getStr(singleMap, "rolreplication");
                result.put("rolcanlogin", rolcanlogin);
                result.put("rolreplication", rolreplication);
                checkUserPermiseIsOk = "true".equals(rolcanlogin) && "true".equals(rolreplication);
            }
            if (!(permiseIsOk && configIsOk && checkUserPermiseIsOk)) {
                result.put("checkResult", permiseIsOk && configIsOk && checkUserPermiseIsOk);
                return Optional.of(AjaxResult.errorAttachedData(MigrationErrorCode
                                .SUB_TASK_NOT_CONDITIONS_REVERSE_ERROR.getCode(),
                        MigrationErrorCode.SUB_TASK_NOT_CONDITIONS_REVERSE_ERROR.getMsg(), result));
            }
            return Optional.empty();
        }
    }

    /**
     * start reverse migration
     *
     * @param id id of Migration Task
     * @return AjaxResult
     */
    @Override
    public AjaxResult startSubTaskReverse(Integer id) {
        MigrationTask subTask = migrationTaskService.getById(id);
        if (subTask == null) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getCode(),
                MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (!subTask.getExecStatus().equals(TaskStatus.INCREMENTAL_STOPPED.getCode())) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_STOP_ERROR.getCode(),
                MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_STOP_ERROR.getMsg());
        }
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
            subTask.getRunHostId());
        installHost.setRunPassword(encryptionUtils.decrypt(installHost.getRunPassword()));
        List<MigrationTaskGlobalParam> globalParams = migrationTaskGlobalParamService.selectByMainTaskId(
            subTask.getMainTaskId());
        if (!migrationTaskService.execMigrationCheck(installHost, subTask, globalParams, "verify_reverse_migration")) {
            return AjaxResult.success();
        }
        PortalHandle.startReversePortal(subTask.getRunHost(), subTask.getRunPort(), subTask.getRunUser(),
            encryptionUtils.decrypt(subTask.getRunPass()), installHost.getInstallPath(), installHost.getJarName(),
            subTask);
        MigrationTask update = MigrationTask.builder()
            .id(subTask.getId())
            .execStatus(TaskStatus.REVERSE_START.getCode())
            .execTime(new Date())
            .build();
        migrationTaskService.updateById(update);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        migrationTaskOperateRecordService.saveRecord(subTask.getId(), TaskOperate.START_REVERSE,
            loginUser.getUsername());
        return AjaxResult.success();
    }

    /**
     * Update subtask status and progress bar data
     *
     * @param taskId
     */
    @Override
    public void refreshTaskStatusByPortal(Integer taskId) {
        Long time = taskRefreshRecord.get(taskId);
        Long curTime = DateUtil.date().getTime();
        if (time == null || curTime > (time + taskRefreshIntervalsMillisecond)) {
            threadPoolTaskExecutor.submit(() -> {
                try {
                    log.info("Sync refresh task status,taskId:{}", taskId);
                    syncRefreshTaskStatusByPortal(taskId);
                } catch (Exception e) {
                    log.error("Sync refresh task status error. message: {}", e.getMessage());
                }
            });
        }
    }

    /**
     * Obtain data from portal status and progress files,
     * asynchronously update subtask status and progress bar data
     *
     * @param mainTaskId
     */
    private void syncRefreshTaskStatusByPortal(Integer mainTaskId) {
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(mainTaskId);
        List<MigrationTask> runningTasks = tasks.stream().filter(t -> {
            return t.getExecStatus().equals(TaskStatus.FULL_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_RUNNING.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_FINISH.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_CHECK_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_CHECKING.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.INCREMENTAL_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.INCREMENTAL_RUNNING.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.INCREMENTAL_FINISHED.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.INCREMENTAL_STOPPED.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.REVERSE_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.REVERSE_RUNNING.getCode());
        }).collect(Collectors.toList());
        if (runningTasks.size() > 0) {
            taskRefreshRecord.put(mainTaskId, DateUtil.date().getTime());
            runningTasks.forEach(t -> {
                migrationTaskService.getSingleTaskStatusAndProcessByProtal(t);
            });
            //refresh mainTask
            MigrationMainTask task = getById(mainTaskId);
            doRefreshSingleMainTask(task);
        }
    }


    /**
     * Update main task status and progress bar based on subtask status
     */
    @Override
    public void doRefreshMainTaskStatus() {
        while (true) {
            if (countByProcessing() > 0) {
                List<MigrationMainTask> migrationMainTasks = listByProcessing();
                migrationMainTasks.stream().forEach(mt -> {
                    doRefreshSingleMainTask(mt);
                });
            }
            try {
                Thread.sleep(mainTaskRefreshIntervalsMillisecond);
            } catch (InterruptedException e) {
                log.error("refresh main task status error, message: {}", e.getMessage());
            }
        }
    }

    @Override
    public int checkDataLevelingAndIncrementFinish(Integer id) {
        MigrationTaskExecResultDetail detail = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(id,
            ProcessType.INCREMENTAL.getCode());
        int data = 1;
        if (detail == null) {
            return data;
        }
        JSONObject execResultDetailJson = JSONObject.parseObject(detail.getExecResultDetail());
        String rest = execResultDetailJson.getString("rest");
        String failCount = execResultDetailJson.getString("failCount");
        if ("0".equals(rest)) {
            if ("0".equals(failCount)) {
                data = 0;
            } else {
                data = 1;
            }
        } else {
            data = 2;
        }
        return data;
    }

    private void doRefreshSingleMainTask(MigrationMainTask mt) {
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(mt.getId());
        if (tasks.size() > 0) {
            Integer totalProcess = tasks.size() * 4;
            Integer fullRunningCount1 = 0;
            Integer fullFinishCount2 = 0;
            Integer incrementalAndReverseRunningCount3 = 0;
            Integer finishCount4 = 0;
            for (MigrationTask t : tasks) {
                if (t.getExecStatus().equals(TaskStatus.FULL_START.getCode()) || t.getExecStatus().equals(TaskStatus.FULL_RUNNING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_FINISH.getCode())
                ) {
                    fullRunningCount1 += 1;
                }
                if (t.getExecStatus().equals(TaskStatus.FULL_CHECK_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_CHECKING.getCode()) || t.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode())
                ) {
                    fullFinishCount2 += 1;
                }
                if (t.getExecStatus().equals(TaskStatus.INCREMENTAL_START.getCode()) || t.getExecStatus().equals(TaskStatus.INCREMENTAL_RUNNING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_FINISHED.getCode()) || t.getExecStatus().equals(TaskStatus.INCREMENTAL_STOPPED.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_RUNNING.getCode()) || t.getExecStatus().equals(TaskStatus.REVERSE_STOP.getCode())
                ) {
                    incrementalAndReverseRunningCount3 += 1;
                }
                if (t.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode()) || t.getExecStatus().equals(TaskStatus.MIGRATION_ERROR.getCode())) {
                    finishCount4 += 1;
                }
            }
            Integer process = fullRunningCount1 * 1 + fullFinishCount2 * 2
                    + incrementalAndReverseRunningCount3 * 3 + finishCount4 * 4;
            String migrationProcess = new BigDecimal((float) process / totalProcess).setScale(2, RoundingMode.UP).toPlainString();
            if (mt.getExecProgress() == null || !mt.getExecProgress().equals(migrationProcess)) {
                MigrationMainTask mainTask = getById(mt.getId());
                mainTask.setExecProgress(migrationProcess);
                if (process.equals(totalProcess)) {
                    mainTask.setExecStatus(MainTaskStatus.FINISH.getCode());
                    mainTask.setFinishTime(new Date());
                }
                updateById(mainTask);
            }
        }
    }
}
