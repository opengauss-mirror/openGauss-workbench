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
 * MigrationTaskServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskExecResultDetail;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.domain.MigrationTaskParam;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;
import org.opengauss.admin.plugin.enums.TaskOperate;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationTaskMapper;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationTaskExecResultDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskGlobalParamService;
import org.opengauss.admin.plugin.service.MigrationTaskHostRefService;
import org.opengauss.admin.plugin.service.MigrationTaskOperateRecordService;
import org.opengauss.admin.plugin.service.MigrationTaskParamService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskServiceImpl extends ServiceImpl<MigrationTaskMapper, MigrationTask> implements MigrationTaskService {

    /**
     * special chars
     */
    private static final char[] SPECIAL_CHARS = {'\\', '+', '-', '!', '(', ')', ':', '^', '[', ']', '\"', '{', '}',
            '~', '*', '?', '>', '<', '|', '&', ';', '/', '.', '$'};

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

    @Autowired
    private MigrationTaskOperateRecordService migrationTaskOperateRecordService;

    @Value("${migration.taskOfflineSchedulerIntervalsMillisecond}")
    private Long taskOfflineSchedulerIntervalsMillisecond;

    @Value("${migration.portalPkgDownloadUrl}")
    private String portalPkgDownloadUrl;

    @Value("${migration.portalJarName}")
    private String portalJarName;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private MigrationHostPortalInstallHostService migrationHostPortalInstallHostService;

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
        List<Integer> ids = this.list(queryWrapper).stream().map(MigrationTask::getId).collect(Collectors.toList());
        this.removeBatchByIds(ids);
        migrationTaskExecResultDetailService.deleteByTaskIds(ids);
        migrationTaskStatusRecordService.deleteByTaskIds(ids);
        migrationTaskOperateRecordService.deleteByTaskIds(ids);
    }

    @Override
    public List<MigrationTask> listByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getMainTaskId, mainTaskId);
        return this.list(queryWrapper);
    }

    private Map<String,Integer> calculateDatabaseObjectCount(List<Map<String, Object>> objects, Integer ojbectType){
        int waitCount = Math.toIntExact(objects.stream().filter(m -> MapUtil.getInt(m, "status").equals(1)).count());
        Predicate<Map<String, Object>> runningFilter = t -> {
            return MapUtil.getInt(t, "status").equals(2);
        };
        if (ojbectType == 2) {
            runningFilter = t -> {
                return MapUtil.getInt(t, "status").equals(2);
            };
        }
        Predicate<Map<String, Object>> finishFilter = t -> {
            return MapUtil.getInt(t, "status").equals(3);
        };
        if (ojbectType == 2) {
            finishFilter = t -> {
                return MapUtil.getInt(t, "status").equals(3) ||
                        MapUtil.getInt(t, "status").equals(4) ||
                        MapUtil.getInt(t, "status").equals(5);
            };
        }
        int runningCount = Math.toIntExact(objects.stream().filter(runningFilter).count());
        int finishCount = Math.toIntExact(objects.stream().filter(finishFilter).count());
        int errorCount = Math.toIntExact(objects.stream().filter(m -> MapUtil.getInt(m, "status").equals(6) ||
                MapUtil.getInt(m, "status").equals(7)).count());
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("waitCount", waitCount);
        resultMap.put("runningCount", runningCount);
        resultMap.put("finishCount", finishCount);
        resultMap.put("errorCount", errorCount);
        return resultMap;
    }

    @Override
    public Map<String, Object> getTaskDetailById(Integer taskId) {
        MigrationTask task = this.getById(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        MigrationTaskExecResultDetail fullProcess = null;
        MigrationTaskExecResultDetail incrementalProcess = null;
        MigrationTaskExecResultDetail reverseProcess = null;

        if (!task.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode())
                && !task.getExecStatus().equals(TaskStatus.FULL_CHECK_FINISH.getCode())) {
            Map<String, Object> getResult = getSingleTaskStatusAndProcessByProtal(task);
            fullProcess = MigrationTaskExecResultDetail.builder().execResultDetail(MapUtil.getStr(getResult, "fullProcess")).build();
            if (task.getMigrationModelId().equals(2)) {
                incrementalProcess = MigrationTaskExecResultDetail.builder().execResultDetail(MapUtil.getStr(getResult, "incrementalProcess")).build();
                reverseProcess = MigrationTaskExecResultDetail.builder().execResultDetail(MapUtil.getStr(getResult, "reverseProcess")).build();
            }
        } else {
            fullProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(taskId, 1);
            if (task.getMigrationModelId().equals(2)) {
                incrementalProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(taskId, 2);
                reverseProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(taskId, 3);
            }
        }
        result.put("fullProcess", fullProcess);
        if (task.getMigrationModelId().equals(2)) {
            result.put("incrementalProcess", incrementalProcess);
            result.put("reverseProcess", reverseProcess);
            List<MigrationTaskStatusRecord> migrationTaskStatusRecords = migrationTaskStatusRecordService.selectByTaskId(taskId);
            Map<Integer, List<MigrationTaskStatusRecord>> recordMap = migrationTaskStatusRecords.stream().collect(Collectors.groupingBy(MigrationTaskStatusRecord::getOperateType));
            result.put("statusRecords", recordMap);
        }
        if (fullProcess != null && StringUtils.isNotBlank(fullProcess.getExecResultDetail())) {
            Map<String, Object> processMap = JSON.parseObject(fullProcess.getExecResultDetail());
            List<Map<String, Object>> tables = (List<Map<String, Object>>) processMap.get("table");
            List<Map<String, Object>> views = (List<Map<String, Object>>) processMap.get("view");
            List<Map<String, Object>> funcs = (List<Map<String, Object>>) processMap.get("function");
            List<Map<String, Object>> triggers = (List<Map<String, Object>>) processMap.get("trigger");
            List<Map<String, Object>> produces = (List<Map<String, Object>>) processMap.get("procedure");
            Map<String, Integer> tableCounts = calculateDatabaseObjectCount(tables, 2);
            Map<String, Integer> viewCounts = calculateDatabaseObjectCount(views, 1);
            Map<String, Integer> funcCounts = calculateDatabaseObjectCount(funcs, 1);
            Map<String, Integer> triggerCounts = calculateDatabaseObjectCount(triggers, 1);
            Map<String, Integer> produceCounts = calculateDatabaseObjectCount(produces, 1);
            result.put("tableCounts", tableCounts);
            result.put("viewCounts", viewCounts);
            result.put("funcCounts", funcCounts);
            result.put("triggerCounts", triggerCounts);
            result.put("produceCounts", produceCounts);
            Integer totalRunningCount = MapUtil.getInt(tableCounts,"runningCount") + MapUtil.getInt(viewCounts,"runningCount") +MapUtil.getInt(funcCounts,"runningCount") +
                MapUtil.getInt(triggerCounts, "runningCount") + MapUtil.getInt(produceCounts, "runningCount");
            Integer totalFinishCount = MapUtil.getInt(tableCounts,"finishCount") + MapUtil.getInt(viewCounts,"finishCount") +MapUtil.getInt(funcCounts,"finishCount") +
                    MapUtil.getInt(triggerCounts, "finishCount") + MapUtil.getInt(produceCounts, "finishCount");
            Integer totalErrorCount = MapUtil.getInt(tableCounts,"errorCount") + MapUtil.getInt(viewCounts,"errorCount") +MapUtil.getInt(funcCounts,"errorCount") +
                    MapUtil.getInt(triggerCounts, "errorCount") + MapUtil.getInt(produceCounts, "errorCount");
            Integer totalWaitCount = MapUtil.getInt(tableCounts,"waitCount") + MapUtil.getInt(viewCounts,"waitCount") +MapUtil.getInt(funcCounts,"waitCount") +
                    MapUtil.getInt(triggerCounts, "waitCount") + MapUtil.getInt(produceCounts, "waitCount");

            result.put("totalWaitCount", totalWaitCount);
            result.put("totalRunningCount", totalRunningCount);
            result.put("totalFinishCount", totalFinishCount);
            result.put("totalErrorCount", totalErrorCount);
        }
        List<String> logPaths = new ArrayList<>();
        if (!task.getExecStatus().equals(TaskStatus.NOT_RUN.getCode())) {
            MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(task.getRunHostId());
            logPaths = PortalHandle.getPortalLogPath(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), installHost.getInstallPath(), task);
        }
        result.put("logs", logPaths);
        return result;
    }


    @Override
    public Map<String, Object> getSingleTaskStatusAndProcessByProtal(MigrationTask t){
        Map<String, Object> result = new HashMap<>();
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(t.getRunHostId());
        String portalStatus = PortalHandle.getPortalStatus(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), installHost.getInstallPath(), t);
        log.info("get portal stauts content: {}, subTaskId: {}", portalStatus, t.getId());
        if (org.opengauss.admin.common.utils.StringUtils.isNotEmpty(portalStatus)) {
            List<Map<String,Object>> statusList = (List<Map<String, Object>>) JSON.parse(portalStatus);
            List<Map<String, Object>> statusResultList = statusList.stream().sorted(Comparator.comparing(m -> MapUtil.getLong(m,"timestamp"))).collect(Collectors.toList());
            Map<String, Object> lastStatus = statusResultList.get(statusResultList.size() - 1);
            Integer state = MapUtil.getInt(lastStatus, "status");
            MigrationTask update = MigrationTask.builder().id(t.getId()).build();
            migrationTaskStatusRecordService.saveTaskRecord(t.getId(), statusResultList);
            BigDecimal migrationProcess = new BigDecimal(0);
            String portalFullProcess = PortalHandle.getPortalFullProcess(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), installHost.getInstallPath(), t);
            log.info("get portal full process content: {}, subTaskId: {}", portalFullProcess, t.getId());
            if (StringUtils.isNotBlank(portalFullProcess)) {
                migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(), portalFullProcess.trim(), 1);
                migrationProcess = calculateFullMigrationProgress(portalFullProcess);
            }
            result.put("fullProcess", portalFullProcess);
            if (TaskStatus.INCREMENTAL_START.getCode().equals(state) || TaskStatus.INCREMENTAL_RUNNING.getCode().equals(state)) {
                String portalIncrementalProcess = PortalHandle.getPortalIncrementalProcess(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), installHost.getInstallPath(), t);
                log.info("get portal incremental process content: {}, subTaskId: {}", portalIncrementalProcess, t.getId());
                if (StringUtils.isNotBlank(portalIncrementalProcess)) {
                    migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(), portalIncrementalProcess.trim(), 2);
                }
                if(t.getMigrationProcess() == null) {
                    if (migrationProcess.intValue() > 0) {
                        migrationProcess = new BigDecimal(0.85f).divide(migrationProcess, 4, BigDecimal.ROUND_HALF_UP);
                    }
                } else {
                    if (Float.parseFloat(t.getMigrationProcess()) > 0) {
                        migrationProcess = new BigDecimal(0.85f).divide(new BigDecimal(t.getMigrationProcess()), 4, BigDecimal.ROUND_HALF_UP);
                    }
                }
                result.put("incrementalProcess", portalIncrementalProcess);
            } else if (TaskStatus.REVERSE_START.getCode().equals(state) || TaskStatus.REVERSE_RUNNING.getCode().equals(state)) {
                String portaReverselProcess = PortalHandle.getPortalReverseProcess(installHost.getHost(), installHost.getPort(), installHost.getRunUser(), installHost.getRunPassword(), installHost.getInstallPath(), t);
                log.info("get portal reverse process content: {}, subTaskId: {}", portaReverselProcess, t.getId());
                if (StringUtils.isNotBlank(portaReverselProcess)) {
                    migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(), portaReverselProcess.trim(), 3);
                }
                if(t.getMigrationProcess() == null) {
                    if (migrationProcess.intValue() > 0) {
                        migrationProcess = new BigDecimal(0.95f).divide(migrationProcess, 4, BigDecimal.ROUND_HALF_UP);
                    }
                } else {
                    if (Float.parseFloat(t.getMigrationProcess()) > 0) {
                        migrationProcess = new BigDecimal(0.95f).divide(new BigDecimal(t.getMigrationProcess()), 4, BigDecimal.ROUND_HALF_UP);
                    }
                }
                result.put("reverseProcess", portaReverselProcess);
            }
            if(state > t.getExecStatus()) {
                update.setExecStatus(state);
            }
            update.setMigrationProcess(migrationProcess.setScale(2, RoundingMode.UP).toPlainString());
            if (TaskStatus.FULL_CHECK_FINISH.getCode().equals(state) && t.getMigrationModelId().equals(1)) {
                update.setExecStatus(TaskStatus.MIGRATION_FINISH.getCode());
                update.setFinishTime(new Date());
            }
            if (TaskStatus.MIGRATION_ERROR.getCode().equals(state)) {
                String msg = MapUtil.getStr(lastStatus, "msg");
                update.setStatusDesc(msg);
            } else {
                update.setStatusDesc("");
            }
            this.updateById(update);
        }
        return result;
    }

    /**
     * Calculate the progress bar of the full migration subtask
     * @param portalProcess
     * @return
     */
    private BigDecimal calculateFullMigrationProgress(String portalProcess) {
        Map<String, Object> processMap = JSON.parseObject(portalProcess);
        List<Map<String, Object>> tables = (List<Map<String, Object>>) processMap.get("table");
        List<Map<String, Object>> views = (List<Map<String, Object>>) processMap.get("view");
        List<Map<String, Object>> funcs = (List<Map<String, Object>>) processMap.get("function");
        List<Map<String, Object>> triggers = (List<Map<String, Object>>) processMap.get("trigger");
        List<Map<String, Object>> produces = (List<Map<String, Object>>) processMap.get("procedure");

        Integer total = tables.size() * 10 + views.size() + funcs.size() + triggers.size() + produces.size();

        Map<String, Integer> tableCounts = calculateDatabaseObjectCount(tables, 2);
        Map<String, Integer> viewCounts = calculateDatabaseObjectCount(views, 1);
        Map<String, Integer> funcCounts = calculateDatabaseObjectCount(funcs, 1);
        Map<String, Integer> triggerCounts = calculateDatabaseObjectCount(triggers, 1);
        Map<String, Integer> produceCounts = calculateDatabaseObjectCount(produces, 1);

        int tableFinishCount = MapUtil.getInt(tableCounts, "runningCount");
        int viewFinishCount = MapUtil.getInt(viewCounts, "runningCount");
        int funcsFinishCount = MapUtil.getInt(funcCounts, "runningCount");
        int triggersFinishCount = MapUtil.getInt(triggerCounts, "runningCount");
        int producesFinishCount = MapUtil.getInt(produceCounts, "runningCount");

        Integer totalFinishCount = tableFinishCount * 10 + viewFinishCount + funcsFinishCount + triggersFinishCount + producesFinishCount;
        BigDecimal result = new BigDecimal(0);
        if (totalFinishCount > 0 && total > 0) {
            result = new BigDecimal((float) totalFinishCount / total);
        }
        return result;
    }

    /**
     * Query the number of tasks not finish by target
     *
     * @param targetNodeId targetNodeId of the OpsHost object
     * @param targetDb targetDb of the OpsHost object
     * @return number of tasks
     */
    @Override
    public Integer countNotFinishByTargetDb(String targetNodeId, String targetDb) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getTargetDb, targetDb).eq(MigrationTask::getTargetNodeId, targetNodeId);
        query.notIn(MigrationTask::getExecStatus, TaskStatus.MIGRATION_FINISH.getCode());
        return Math.toIntExact(this.count(query));
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
     * Query the number of tasks running on the machine
     *
     * @param hostId Id of the OpsHost object
     * @return number of tasks
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
        LoginUser loginUser = SecurityUtils.getLoginUser();
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(h.getRunHostId());
        PortalHandle.startPortal(installHost, t, portalJarName, getTaskParam(globalParams, t));
        MigrationTask update = MigrationTask.builder().id(t.getId()).runHostId(h.getRunHostId()).runHost(h.getHost()).runHostname(h.getHostName())
                .runPort(h.getPort()).runUser(h.getUser()).runPass(h.getPassword()).execStatus(TaskStatus.FULL_START.getCode()).execTime(new Date()).build();
        migrationTaskOperateRecordService.saveRecord(t.getId(), TaskOperate.RUN, loginUser.getUsername());
        this.updateById(update);
    }

    /**
     * special character handling
     *
     * @param str String that needs to be processed.
     * @return processed string
     */
    public String escapeChars(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < str.length(); index++) {
            char con = str.charAt(index);
            // These characters are part of the query syntax and must be escaped
            if (shouldEscape(con) || Character.isWhitespace(con)) {
                sb.append('\\');
            }
            sb.append(con);
        }
        return sb.toString();
    }

    /**
     * determine if a given character needs to be escaped
     *
     * @param con char content
     * @return result
     */
    private boolean shouldEscape(char con) {
        for (char ch : SPECIAL_CHARS) {
            if (ch == con || Character.isWhitespace(con)) {
                return true;
            }
        }
        return false;
    }

    private Map<String,String> getTaskParam(List<MigrationTaskGlobalParam> globalParams, MigrationTask task) {
        if (globalParams == null) {
            globalParams = migrationTaskGlobalParamService.selectByMainTaskId(task.getMainTaskId());
        }
        Map<String, String> globalParamMap = globalParams.stream().collect(Collectors.toMap(g -> g.getParamKey(), g -> g.getParamValue()));
        List<MigrationTaskParam> migrationTaskParams = migrationTaskParamService.selectByTaskId(task.getId());
        if (migrationTaskParams.size() > 0) {
            Map<String, String> taskParamMap = migrationTaskParams.stream().collect(Collectors.toMap(p -> p.getParamKey(), p -> p.getParamValue()));
            globalParamMap.putAll(taskParamMap);
        }
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("mysql.user.name", task.getSourceDbUser());
        resultMap.put("mysql.user.password", escapeChars(task.getSourceDbPass()));
        resultMap.put("mysql.database.host", task.getSourceDbHost());
        resultMap.put("mysql.database.port", task.getSourceDbPort());
        resultMap.put("mysql.database.name", task.getSourceDb());

        resultMap.put("opengauss.user.name", task.getTargetDbUser());
        resultMap.put("opengauss.user.password", escapeChars(task.getTargetDbPass()));
        resultMap.put("opengauss.database.host", task.getTargetDbHost());
        resultMap.put("opengauss.database.port", task.getTargetDbPort());
        resultMap.put("opengauss.database.name", task.getTargetDb());
        resultMap.put("opengauss.database.schema", task.getSourceDb());
        if (globalParamMap.keySet().size() > 0) {
            resultMap.putAll(globalParamMap);
        }
        return resultMap;
    }

    /**
     * Subtask Execution Offline Scheduler
     */
    @Override
    public void doOfflineTaskRunScheduler(){
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
                log.error("offline task scheduler error, message: {}", e.getMessage());
            }
        }
    }

}
