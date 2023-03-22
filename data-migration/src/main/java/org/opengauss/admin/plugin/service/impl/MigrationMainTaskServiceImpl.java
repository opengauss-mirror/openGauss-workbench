package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.plugin.domain.*;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;
import org.opengauss.admin.plugin.dto.MigrationTaskDto;
import org.opengauss.admin.plugin.enums.MainTaskStatus;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.enums.TaskOperate;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationMainTaskMapper;
import org.opengauss.admin.plugin.service.*;
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

    @Value("${migration.taskRefreshIntervalsMillisecond}")
    private Long taskRefreshIntervalsMillisecond;

    @Value("${migration.mainTaskRefreshIntervalsMillisecond}")
    private Long mainTaskRefreshIntervalsMillisecond;
    @Value("${migration.portalPkgDownloadUrl}")
    private String portalPkgDownloadUrl;

    private static Map<Integer, Long> taskRefreshRecord = new ConcurrentHashMap<>();
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
     * @param task SysTask
     * @return SysTask
     */
    @Override
    public List<String> selectCreateUsers() {
        return migrationMainTaskMapper.selectCreateUsers();
    }

    public Integer countByProcessing() {
        LambdaQueryWrapper<MigrationMainTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationMainTask::getExecStatus, MainTaskStatus.RUNNING.getCode());
        return Math.toIntExact(this.count(query));
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
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_STOP.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_RUNNING.getCode());
            };
        }
        long runningCount = tasks.stream().filter(runningFilter).count();
        Predicate<MigrationTask> finishFilter = t -> {
            return t.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode());
        };
        if (modelId.equals(1)) {
            finishFilter = t -> {
                return t.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode());
            };
        }
        long finishCount = tasks.stream().filter(finishFilter).count();
        long errorCount = tasks.stream().filter(t -> t.getExecStatus().equals(TaskStatus.MIGRATION_ERROR.getCode())).count();
        countMap.put("notRunCount", Math.toIntExact(notRunCount));
        countMap.put("runningCount", Math.toIntExact(runningCount));
        countMap.put("finishCount", Math.toIntExact(finishCount));
        countMap.put("errorCount", Math.toIntExact(errorCount));
        return countMap;
    }

    /**
     * Get task detail
     * @param taskId
     * @return
     */
    @Override
    public Map<String, Object> getDetailById(Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        MigrationMainTask task = this.getById(taskId);
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
     * @param taskId
     * @return
     */
    @Override
    public MigrationTaskDto getMigrationTaskDtoById(Integer taskId) {
        MigrationMainTask task = this.getById(taskId);
        MigrationTaskDto dto = new MigrationTaskDto();
        dto.setTaskId(task.getId());
        dto.setTaskName(task.getTaskName());

        List<MigrationTaskGlobalParam> globalParams = migrationTaskGlobalParamService.selectByMainTaskId(taskId);
        dto.setGlobalParams(globalParams);
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(taskId);
        tasks.stream().forEach(t -> {
            t.setTaskParams(migrationTaskParamService.selectByTaskId(t.getId()));
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
        this.save(mainTask);

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

        MigrationMainTask update = new MigrationMainTask();
        update.setTaskName(taskDto.getTaskName());
        update.setId(taskDto.getTaskId());
        this.updateById(update);

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
                batchTaskParams.addAll(batchTaskParams);
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
    public void deleteTask(Integer[] ids){
        Arrays.asList(ids).stream().forEach(i -> {
            this.removeById(i);
            migrationTaskService.deleteByMainTaskId(i);
            migrationTaskParamService.deleteByMainTaskId(i);
            migrationTaskGlobalParamService.deleteByMainTaskId(i);
            migrationTaskHostRefService.deleteByMainTaskId(i);
        });
    }


    @Override
    public void updateStatus(Integer id, MainTaskStatus taskStatus) {
        MigrationMainTask task = new MigrationMainTask();
        task.setId(id);
        task.setExecStatus(taskStatus.getCode());
        if (taskStatus.getCode().equals(MainTaskStatus.RUNNING.getCode())) {
            task.setExecTime(new Date());
        }
        if (taskStatus.getCode().equals(MainTaskStatus.FINISH.getCode())) {
            task.setFinishTime(new Date());
        }
        this.updateById(task);
    }

    /**
     * Start the task process:
     *  1. Query subtask list; query running configuration machine list; query global configuration and subtask configuration data;
     *  2. Loop through each machine, use the maximum number of executable tasks - the number of running tasks to get the number of tasks that can be executed by each machine, assign tasks according to the number of executable tasks, and judge whether the machine is installed with portal, if not Install, then install the portal. The following situations may be encountered when assigning tasks:
     *    a. If the total number of executable tasks of all machines > the number of subtasks,
     *       it is allocated according to the number of executable tasks of each machine, and then the configuration parameters are processed, the portal process is invoked, and task instructions are issued.
     *    b. The total number of executable tasks of all machines < the number of subtasks,
     *       For example, there are 3 machines, A machine can run 3, B machine can run 6, C machine can run 5,
     *       the overall number of tasks can be 14, and the number of subtasks is 20.
     *       At this time, the 14 tasks are first allocated according to the executable quantity of each machine,
     *       and then the configuration parameters are processed, the portal process is invoked, and task instructions are issued. The status of the remaining 6 tasks is changed to waiting for resources, which are called by the asynchronous scheduler.
     *       The asynchronous scheduler will regularly monitor whether the three machines have resources released, and if there are resources released, tasks will be assigned according to the carrying capacity.
     *  3. For the task assigned to the machine, log in to the remote server, process configuration parameters, start the portal process, and pass in the subtask ID to start the process. Then write the instructions of the task to the input file of Portal.
     *  4. Modify the subtask status, and modify the task to the running state.
     * @param id
     */
    @Override
    public AjaxResult startTask(Integer id){
        MigrationMainTask mainTask = this.getById(id);
        if (mainTask == null) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (mainTask.getExecStatus().equals(MainTaskStatus.RUNNING.getCode()) || mainTask.getExecStatus().equals(MainTaskStatus.FINISH.getCode())) {
            return AjaxResult.error(MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getCode(), MigrationErrorCode.MAIN_TASK_IS_RUNNING_ERROR.getMsg());
        }
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
                migrationTaskService.runTask(h, t, globalParams);
            }
        } else { //The number of tasks that the host can perform is less than the total number of tasks
            hosts.stream().forEach(h -> {
                Integer runnableCount = h.getRunnableCount();
                for (int i = tasks.size() - 1; i >= tasks.size() - runnableCount; i--) {
                    MigrationTask t = tasks.get(i);
                    migrationTaskService.runTask(h, t, globalParams);
                    tasks.remove(i);
                }
            });
            //For tasks that have not been assigned a host,
            // change the status to 1000 and be processed by the offline scheduler
            tasks.stream().forEach(t -> {
                migrationTaskService.updateStatus(t.getId(), TaskStatus.WAIT_RESOURCE);
            });
        }
        updateStatus(id, MainTaskStatus.RUNNING);
        return AjaxResult.success();
    }

    @Override
    public void finishTask(Integer id){
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(id);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        tasks.stream().forEach(t -> {
            PortalHandle.finishPortal(t.getRunHost(),t.getRunPort(),t.getRunUser(),t.getRunPass(), t);
            MigrationTask update = MigrationTask.builder().id(t.getId()).execStatus(TaskStatus.MIGRATION_FINISH.getCode()).finishTime(new Date()).build();
            migrationTaskOperateRecordService.saveRecord(t.getId(), TaskOperate.FINISH_MIGRATION, loginUser.getUsername());
            migrationTaskService.updateById(update);
        });
       updateStatus(id, MainTaskStatus.FINISH);
    }

    @Override
    public AjaxResult finishSubTask(Integer id){
        MigrationTask subTask = migrationTaskService.getById(id);
        if (subTask == null) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        PortalHandle.finishPortal(subTask.getRunHost(),subTask.getRunPort(),
                subTask.getRunUser(),subTask.getRunPass(), subTask);
        MigrationTask update = MigrationTask.builder().id(subTask.getId()).execStatus(TaskStatus.MIGRATION_FINISH.getCode()).finishTime(new Date()).build();
        migrationTaskService.updateById(update);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        migrationTaskOperateRecordService.saveRecord(subTask.getId(), TaskOperate.FINISH_MIGRATION, loginUser.getUsername());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult stopSubTaskIncremental(Integer id){
        MigrationTask subTask = migrationTaskService.getById(id);
        if (subTask == null) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (subTask.getExecStatus() != TaskStatus.INCREMENTAL_START.getCode()
            && subTask.getExecStatus() != TaskStatus.INCREMENTAL_RUNNING.getCode()) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_ERROR.getMsg());
        }
        PortalHandle.stopIncrementalPortal(subTask.getRunHost(), subTask.getRunPort(),
                subTask.getRunUser(), subTask.getRunPass(), subTask);
        MigrationTask update = MigrationTask.builder().id(subTask.getId()).execStatus(TaskStatus.INCREMENTAL_STOP.getCode()).execTime(new Date()).build();
        migrationTaskService.updateById(update);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        migrationTaskOperateRecordService.saveRecord(subTask.getId(), TaskOperate.STOP_INCREMENTAL, loginUser.getUsername());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult startSubTaskReverse(Integer id){
        MigrationTask subTask = migrationTaskService.getById(id);
        if (subTask == null) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_EXISTS_ERROR.getMsg());
        }
        if (subTask.getExecStatus() != TaskStatus.INCREMENTAL_STOP.getCode()) {
            return AjaxResult.error(MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_STOP_ERROR.getCode(), MigrationErrorCode.SUB_TASK_NOT_IN_INCREMENTAL_STOP_ERROR.getMsg());
        }
        PortalHandle.startReversePortal(subTask.getRunHost(), subTask.getRunPort(),
                subTask.getRunUser(), subTask.getRunPass(), subTask);
        MigrationTask update = MigrationTask.builder().id(subTask.getId()).execStatus(TaskStatus.REVERSE_START.getCode()).execTime(new Date()).build();
        migrationTaskService.updateById(update);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        migrationTaskOperateRecordService.saveRecord(subTask.getId(), TaskOperate.START_REVERSE, loginUser.getUsername());
        return AjaxResult.success();
    }

    /**
     * Update subtask status and progress bar data
     * @param taskId
     */
    @Override
    public void refreshTaskStatusByPortal(Integer taskId) {
        Long time = taskRefreshRecord.get(taskId);
        Long curTime = DateUtil.date().getTime();
        if(time == null || curTime > (time + taskRefreshIntervalsMillisecond)) {
            threadPoolTaskExecutor.submit(() -> {
                try {
                    log.info("Sync refresh task status,taskId:{}", taskId);
                    syncRefreshTaskStatusByPortal(taskId);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Sync refresh task status error", e);
                }
            });
        }
    }

    /**
     * Obtain data from portal status and progress files,
     * asynchronously update subtask status and progress bar data
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
                    t.getExecStatus().equals(TaskStatus.INCREMENTAL_STOP.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.REVERSE_START.getCode()) ||
                    t.getExecStatus().equals(TaskStatus.REVERSE_RUNNING.getCode());
        }).collect(Collectors.toList());
        if(runningTasks.size() > 0) {
            taskRefreshRecord.put(mainTaskId, DateUtil.date().getTime());
            runningTasks.forEach(t -> {
                migrationTaskService.getSingleTaskStatusAndProcessByProtal(t);
            });
            //refresh mainTask
            MigrationMainTask task = this.getById(mainTaskId);
            doRefreshSingleMainTask(task);
        }
    }



    /**
     * Update main task status and progress bar based on subtask status
     */
    @Override
    public void doRefreshMainTaskStatus() {
        while(true) {
            if (countByProcessing() > 0) {
                List<MigrationMainTask> migrationMainTasks = listByProcessing();
                migrationMainTasks.stream().forEach(mt -> {
                    doRefreshSingleMainTask(mt);
                });
            }
            try {
                Thread.sleep(mainTaskRefreshIntervalsMillisecond);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doRefreshSingleMainTask(MigrationMainTask mt){
        List<MigrationTask> tasks = migrationTaskService.listByMainTaskId(mt.getId());
        if (tasks.size() > 0) {
            Integer totalProcess = tasks.size() * 4;
            Integer fullRunningCount1 = 0;
            Integer fullFinishCount2 = 0;
            Integer incrementalAndReverseRunningCount3 = 0;
            Integer finishCount4 = 0;
            for (MigrationTask t : tasks) {
                if(t.getExecStatus().equals(TaskStatus.FULL_START.getCode()) || t.getExecStatus().equals(TaskStatus.FULL_RUNNING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_FINISH.getCode())
                ) {
                    fullRunningCount1 += 1;
                }
                if(t.getExecStatus().equals(TaskStatus.FULL_CHECK_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.FULL_CHECKING.getCode()) || t.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode())
                ){
                    fullFinishCount2 += 1;
                }
                if(t.getExecStatus().equals(TaskStatus.INCREMENTAL_START.getCode()) || t.getExecStatus().equals(TaskStatus.INCREMENTAL_RUNNING.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.INCREMENTAL_STOP.getCode()) ||  t.getExecStatus().equals(TaskStatus.REVERSE_START.getCode()) ||
                        t.getExecStatus().equals(TaskStatus.REVERSE_RUNNING.getCode()) || t.getExecStatus().equals(TaskStatus.REVERSE_STOP.getCode())
                ){
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
                MigrationMainTask mainTask = new MigrationMainTask();
                mainTask.setId(mt.getId());
                mainTask.setExecProgress(migrationProcess);
                if (process.equals(totalProcess)) {
                    mainTask.setExecStatus(MainTaskStatus.FINISH.getCode());
                    mainTask.setFinishTime(new Date());
                }
                this.updateById(mainTask);
            }
        }
    }
}
