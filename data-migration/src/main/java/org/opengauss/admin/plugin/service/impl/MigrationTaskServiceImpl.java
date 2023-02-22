package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.*;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationTaskMapper;
import org.opengauss.admin.plugin.service.*;
import org.opengauss.admin.system.plugin.facade.*;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskServiceImpl extends ServiceImpl<MigrationTaskMapper, MigrationTask> implements MigrationTaskService {

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;


    @Autowired
    private MigrationTaskMapper migrationTaskMapper;

    @Autowired
    private MigrationTaskParamService migrationTaskParamService;

    @Autowired
    private MigrationTaskHostRefService migrationTaskHostRefService;

    @Autowired
    private MigrationTaskGlobalParamService migrationTaskGlobalParamService;

    @Autowired
    private MigrationTaskExecResultDetailService migrationTaskExecResultDetailService;

    @Autowired
    private MigrationTaskStatusRecordService migrationTaskStatusRecordService;

    @Value("${migration.taskOfflineSchedulerIntervalsMillisecond}")
    private Long taskOfflineSchedulerIntervalsMillisecond;
    @Value("${migration.portalHome}")
    private String portalHome;

    /**
     * Query the sub task page list by mainTaskId
     *
     * @param mainTaskId
     * @return MigrationTask
     */
    @Override
    public IPage<MigrationTask> selectList(IPage<MigrationTask> page, Integer mainTaskId) {
        return migrationTaskMapper.selectTaskPage(page, mainTaskId);
    }

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getMainTaskId, mainTaskId);
        this.remove(queryWrapper);
    }

    @Override
    public List<MigrationTask> listByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getMainTaskId, mainTaskId);
        return this.list(queryWrapper);
    }

    @Override
    public Map<String, Object> getTaskDetailById(Integer taskId) {
        MigrationTask task = this.getById(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        MigrationTaskExecResultDetail fullProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(taskId, 1);
        result.put("fullProcess", fullProcess);
        if (task.getMigrationModelId().equals(2)) {
            MigrationTaskExecResultDetail incrementalProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(taskId, 2);
            result.put("incrementalProcess", fullProcess);
            MigrationTaskExecResultDetail reverseProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(taskId, 3);
            result.put("reverseProcess", reverseProcess);
            List<MigrationTaskStatusRecord> migrationTaskStatusRecords = migrationTaskStatusRecordService.selectByTaskId(taskId);
            result.put("statusRecords", migrationTaskStatusRecords);
        }
        return result;
    }

    @Override
    public Integer countRunningByTargetDb(String targetDb) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getTargetDb, targetDb);
        query.in(MigrationTask::getExecStatus, TaskStatus.FULL_START.getCode(),
                TaskStatus.FULL_RUNNING.getCode(), TaskStatus.FULL_FINISH.getCode(),
                TaskStatus.FULL_CHECK_START.getCode(), TaskStatus.FULL_CHECKING.getCode(),
                TaskStatus.FULL_CHECK_FINISH.getCode(), TaskStatus.INCREMENTAL_START.getCode(),
                TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskStatus.REVERSE_START.getCode(),
                TaskStatus.REVERSE_RUNNING.getCode()
        );
        return Math.toIntExact(this.count(query));
    }

    /**
     *  Query the number of tasks running on the machine
     * @param hostId
     * @return
     */
    @Override
    public Integer countRunningByHostId(String hostId) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getRunHostId, hostId);
        query.in(MigrationTask::getExecStatus, TaskStatus.FULL_START.getCode(),
                TaskStatus.FULL_RUNNING.getCode(), TaskStatus.FULL_FINISH.getCode(),
                TaskStatus.FULL_CHECK_START.getCode(), TaskStatus.FULL_CHECKING.getCode(),
                TaskStatus.FULL_CHECK_FINISH.getCode(), TaskStatus.INCREMENTAL_START.getCode(),
                TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskStatus.REVERSE_START.getCode(),
                TaskStatus.REVERSE_RUNNING.getCode()
        );
        return Math.toIntExact(this.count(query));
    }

    @Override
    public List<MigrationTask> listRunningTaskByHostId(String hostId) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getRunHostId, hostId);
        query.in(MigrationTask::getExecStatus, TaskStatus.FULL_START.getCode(),
                TaskStatus.FULL_RUNNING.getCode(), TaskStatus.FULL_FINISH.getCode(),
                TaskStatus.FULL_CHECK_START.getCode(), TaskStatus.FULL_CHECKING.getCode(),
                TaskStatus.FULL_CHECK_FINISH.getCode(), TaskStatus.INCREMENTAL_START.getCode(),
                TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskStatus.REVERSE_START.getCode(),
                TaskStatus.REVERSE_RUNNING.getCode()
        );
        return this.list(query);
    }

    @Override
    public List<MigrationTask> listTaskByStatus(TaskStatus taskStatus) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getExecStatus, taskStatus.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public Integer countTaskByStatus(TaskStatus taskStatus) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getExecStatus, taskStatus.getCode());
        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public void updateStatus(Integer id, TaskStatus taskStatus) {
        MigrationTask migrationTask = new MigrationTask();
        migrationTask.setExecStatus(taskStatus.getCode());
        migrationTask.setId(id);
        this.updateById(migrationTask);
    }

    @Override
    public List<Map<String, Object>> countByMainTaskIdGroupByModel(Integer mainTaskId) {
        return migrationTaskMapper.countByMainTaskIdGroupByModelId(mainTaskId);
    }

    @Override
    public void runTask(MigrationTaskHostRef h, MigrationTask t, List<MigrationTaskGlobalParam> globalParams) {
        PortalHandle.startPortal(h, t, getTaskParam(globalParams, t), portalHome);
        MigrationTask update = MigrationTask.builder().id(t.getId()).runHostId(h.getRunHostId()).runHost(h.getHost()).runHostname(h.getHostName())
                .runPort(h.getPort()).runUser(h.getUser()).runPass(h.getPassword()).execStatus(TaskStatus.FULL_START.getCode()).execTime(new Date()).build();
        this.updateById(update);
    }

    private Map<String,String> getTaskParam(List<MigrationTaskGlobalParam> globalParams, MigrationTask task) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("mysql.user.name", task.getSourceDbUser());
        resultMap.put("mysql.user.password", task.getSourceDbPass());
        resultMap.put("mysql.database.host", task.getSourceDbHost());
        resultMap.put("mysql.database.port", task.getSourceDbPort());
        resultMap.put("mysql.database.name", task.getSourceDb());

        resultMap.put("opengauss.user.name", task.getTargetDbUser());
        resultMap.put("opengauss.user.password", task.getTargetDbPass());
        resultMap.put("opengauss.database.host", task.getTargetDbHost());
        resultMap.put("opengauss.database.port", task.getTargetDbPort());
        resultMap.put("opengauss.database.name", task.getTargetDb());
        resultMap.put("opengauss.database.schema", task.getSourceDb());

        return resultMap;
    }

    /**
     * Subtask Execution Offline Scheduler
     */
    private void doOfflineTaskRunScheduler(){
        while(true) {
            Integer waitRunCount = this.countTaskByStatus(TaskStatus.WAIT_RESOURCE);
            if (waitRunCount > 0) {
                log.info("waiting for the resource to execute，task count : {}", waitRunCount);
                List<MigrationTask> migrationTasks = this.listTaskByStatus(TaskStatus.WAIT_RESOURCE);
                migrationTasks.stream().forEach((t -> {
                    List<MigrationTaskHostRef> hosts = migrationTaskHostRefService.listByMainTaskId(t.getMainTaskId());
                    MigrationTaskHostRef selectHost = hosts.stream().filter(h -> h.getRunnableCount() > 0).findFirst().orElse(null);
                    if (selectHost != null) {
                        log.info("Offline scheduling successfully assigns tasks to host for execution. taskId : {}, HostId : {}, HostIP : {}",
                                t.getId(), selectHost.getRunHostId(),selectHost.getHost());
                        runTask(selectHost, t, null);
                    }
                }));
            }
            try {
                Thread.sleep(taskOfflineSchedulerIntervalsMillisecond);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PostConstruct
    public void offlineTaskRunScheduler(){
        threadPoolTaskExecutor.submit(() -> {
            try {
                doOfflineTaskRunScheduler();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("OffLineTaskRunScheduler error", e);
            }
        });
    }

}
