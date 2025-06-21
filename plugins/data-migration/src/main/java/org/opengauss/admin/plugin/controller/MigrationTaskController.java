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
 * MigrationTaskController.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/controller/MigrationTaskController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.MigrationMainTask;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.dto.MigrationTaskDto;
import org.opengauss.admin.plugin.dto.MigrationCurrentCheckInfoDto;
import org.opengauss.admin.plugin.dto.MigrationInfoDto;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;
import org.opengauss.admin.plugin.dto.MigrationLogsInfoDto;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.service.MigrationMainTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.FullCheckParam;
import org.opengauss.admin.system.plugin.facade.WsFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@RestController
@RequestMapping("/migration")
@Slf4j
public class MigrationTaskController extends BaseController {
    private static final String DATA_MIGRATION = "data-migration";

    @Autowired
    private MigrationTaskService migrationTaskService;

    @Autowired
    private MigrationMainTaskService migrationMainTaskService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private WsFacade wsFacade;

    /**
     * page list
     */
    @GetMapping("/list")
    public TableDataInfo list(MigrationMainTaskDto task) {
        IPage<MigrationMainTask> list = migrationMainTaskService.selectList(startPage(), task);
        return getDataTable(list);
    }

    /**
     * page subtask list
     */
    @GetMapping("/subTasks/{id}")
    public TableDataInfo listSubTask(@PathVariable("id") Integer id) {
        IPage<MigrationTask> list = migrationTaskService.selectList(startPage(), id);
        return getDataTable(list);
    }

    /**
     * list all create user
     */
    @GetMapping("/list/createUsers")
    public AjaxResult listCreateUsers() {
        return AjaxResult.success(migrationMainTaskService.selectCreateUsers());
    }

    /**
     * getById
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(migrationMainTaskService.getDetailById(id));
    }

    /**
     * getEditInfo
     */
    @GetMapping(value = "/editInfo/{id}")
    public AjaxResult getEditInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(migrationMainTaskService.getMigrationTaskDtoById(id));
    }

    /**
     * refreshStatus
     */
    @GetMapping(value = "/refreshStatus/{id}")
    public AjaxResult refreshStatus(@PathVariable("id") Integer id) {
        migrationMainTaskService.refreshTaskStatusByPortal(id);
        return AjaxResult.success();
    }

    /**
     * delete task
     */
    @Log(title = "task", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        migrationMainTaskService.deleteTask(ids);
        return AjaxResult.success();
    }

    /**
     * start task
     */
    @Log(title = "task", businessType = BusinessType.START)
    @PostMapping("/start/{id}")
    public AjaxResult start(@PathVariable Integer id ) {
        return migrationMainTaskService.startTask(id);
    }
    
    /**
     * reset task
     */
    @Log(title = "task", businessType = BusinessType.RESET)
    @PostMapping("/reset/{id}")
    public AjaxResult reset(@PathVariable Integer id ) {
        return migrationMainTaskService.resetTask(id);
    }

    /**
     * finish task
     */
    @Log(title = "task", businessType = BusinessType.STOP)
    @PostMapping("/finish/{id}")
    public AjaxResult finish(@PathVariable Integer id ) {
        migrationMainTaskService.finishTask(id);
        return AjaxResult.success();
    }

    /**
     * save task
     * @param taskDto
     * @return
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody MigrationTaskDto taskDto) {
        migrationMainTaskService.saveTask(taskDto);
        return AjaxResult.success();
    }

    /**
     * update task
     * @param taskDto
     * @return
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody MigrationTaskDto taskDto) {
        return migrationMainTaskService.updateTask(taskDto);
    }

    /**
     * finish subtask
     */
    @Log(title = "task", businessType = BusinessType.STOP)
    @PostMapping("/subTask/finish/{id}")
    public AjaxResult finishSubTask(@PathVariable Integer id ) {
        return migrationMainTaskService.finishSubTask(id);
    }

    /**
     * stop subtask incremental
     */
    @Log(title = "task", businessType = BusinessType.STOP)
    @PostMapping("/subTask/stop/incremental/{id}")
    public AjaxResult stopSubTaskIncremental(@PathVariable Integer id ) {
        return migrationMainTaskService.stopSubTaskIncremental(id);
    }

    /**
     * start subtask reverse
     */
    @Log(title = "task", businessType = BusinessType.START)
    @PostMapping("/subTask/start/reverse/{id}")
    public AjaxResult startSubTaskReverse(@PathVariable Integer id ) {
        return migrationMainTaskService.startSubTaskReverse(id);
    }

    /**
     * get subtask detail
     */
    @GetMapping(value = "/subTaskInfo/{id}")
    public AjaxResult getSubTaskInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(migrationTaskService.getTaskDetailById(id));
    }

    /**
     * get full migration data by websocket
     *
     * @param id id
     * @param sessionId sessionId
     * @return AjaxResult
     */
    @GetMapping(value = "/subTaskInfo/{id}/{sessionId}")
    public AjaxResult sendMigrationDataByWebsocket(@PathVariable("id") Integer id,
                                                   @PathVariable("sessionId") String sessionId) {
        threadPoolTaskExecutor.submit(() -> {
            while (!sessionId.isEmpty()) {
                wsFacade.sendMessage(DATA_MIGRATION, sessionId, JSON.toJSONString(migrationTaskService
                    .getFullDataById(id)));
                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
            }
        });
        return AjaxResult.success();
    }

    /**
     * get SubTask Basic Info
     *
     * @param id id
     * @return AjaxResult
     */
    @GetMapping(value = "/subTaskBasicInfo/{id}")
    public AjaxResult getSubTaskBasicInfo(@PathVariable("id") Integer id) {
        MigrationInfoDto migrationInfo = migrationTaskService.getSubTaskBasicInfo(id);
        return AjaxResult.success(migrationInfo);
    }

    /**
     * get full migration current type info
     *
     * @param id id
     * @param currentInfoType currentInfoType
     * @param info info
     * @return AjaxResult
     */
    @PostMapping(value = "/fullMigInfo/{id}/{currentInfoType}")
    public AjaxResult getFullMigCurrentTypeInfo(@PathVariable("id") Integer id,
                                                @PathVariable("currentInfoType") String currentInfoType,
                                                @RequestBody MigrationCurrentCheckInfoDto info) {
        return AjaxResult.success(migrationTaskService.getFullMigCurrentTypeInfo(id, currentInfoType, info));
    }

    /**
     * get migration logs info
     *
     * @param id id
     * @param info info
     * @return AjaxResult
     */
    @PostMapping(value = "/getMigLogsInfo/{id}")
    public AjaxResult getMigLogsInfo(@PathVariable("id") Integer id, @RequestBody MigrationLogsInfoDto info) {
        return AjaxResult.success(migrationTaskService.getMigLogsInfo(id, info));
    }

    /**
     * Download log file by filepath
     */
    @GetMapping("/subTask/log/download/{id}")
    public void logDownload(@PathVariable Integer id, String filePath, HttpServletResponse response) throws Exception {
        MigrationTask task = migrationTaskService.getById(id);
        String logContent = PortalHandle.getTaskLogs(task.getRunHost(), task.getRunPort(), task.getRunUser(), encryptionUtils.decrypt(task.getRunPass()), filePath);
        if (StringUtils.isBlank(logContent)) {
            logContent = " ";
        }
        byte[] bytes = logContent.getBytes(StandardCharsets.UTF_8);
        String logName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String date = DateUtil.format(new Date(), "yyyyMMddhhmmss");
        String filename = "log_" + id + "_" + date + "_" + logName;
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, filename);
        OutputStream output = new BufferedOutputStream(response.getOutputStream());
        output.write(bytes);
        output.flush();
        output.close();
    }

    /**
     * Check status of incremental or reverse migration task
     *
     * @param id task id
     * @return status
     */
    @GetMapping(value = "/check/incremental/reverse/status/{id}")
    public AjaxResult checkStatusOfIncrementalOrReverseMigrationTask(@PathVariable("id") Integer id) {
        return AjaxResult.success(migrationTaskService.checkStatusOfIncrementalOrReverseMigrationTask(id));
    }

    /**
     * Start incremental or reverse migration task
     *
     * @param id task id
     * @param name endpoint name
     * @return result
     */
    @PostMapping(value = "/start/incremental/reverse/task/process/{id}")
    public AjaxResult startTaskOfOnlineOrReverseMigrationProcess(@PathVariable("id") Integer id,
        @RequestParam("name") String name) {
        return AjaxResult.success(migrationTaskService.startTaskOfIncrementalOrReverseMigrationProcess(id, name));
    }

    /**
     * Query full check summary of migration task
     *
     * @param id task
     * @return summary
     */
    @GetMapping(value = "/query/full/check/summary/{id}")
    public AjaxResult queryFullCheckSummary(@PathVariable("id") Integer id) {
        return AjaxResult.success(migrationTaskService.queryFullCheckSummaryOfMigrationTask(id));
    }

    /**
     * Query full check detail of migration task
     *
     * @param fullCheckParam fullCheckParam
     * @return result
     */
    @PostMapping(value = "/query/full/check/detail")
    public AjaxResult queryFullCheckDetail(@RequestBody FullCheckParam fullCheckParam) {
        return AjaxResult.success(migrationTaskService.queryFullCheckDetailOfMigrationTask(fullCheckParam));
    }

    /**
     * Download repair file
     *
     * @param id task id
     * @param repairFileName repair file name
     * @param response response
     * @throws IOException IOException
     */
    @GetMapping(value = "/download/repair/file/{id}/{repairFileName}")
    public void downloadRepairFile(@PathVariable Integer id, @PathVariable String repairFileName,
        HttpServletResponse response) throws IOException {
        String content = migrationTaskService.downloadRepairFile(id, repairFileName);
        if (StringUtils.isBlank(content)) {
            content = " ";
        }
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, repairFileName);
        try (OutputStream output = new BufferedOutputStream(response.getOutputStream())) {
            output.write(bytes);
            output.flush();
        }
    }
}
