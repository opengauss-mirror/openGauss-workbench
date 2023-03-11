package org.opengauss.admin.plugin.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.MainTaskEnvErrorHost;
import org.opengauss.admin.plugin.domain.MigrationMainTask;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;
import org.opengauss.admin.plugin.dto.MigrationTaskDto;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.service.MainTaskEnvErrorHostService;
import org.opengauss.admin.plugin.service.MigrationMainTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@RestController
@RequestMapping("/migration")
public class MigrationTaskController extends BaseController {

    @Autowired
    private MigrationTaskService migrationTaskService;

    @Autowired
    private MigrationMainTaskService migrationMainTaskService;

    @Autowired
    private MainTaskEnvErrorHostService mainTaskEnvErrorHostService;

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
    @Log(title = "task", businessType = BusinessType.STOP)
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
     * Download log file by filepath
     */
    @GetMapping("/subTask/log/download/{id}")
    public void logDownload(@PathVariable Integer id, String filePath, HttpServletResponse response) throws Exception {
        MigrationTask task = migrationTaskService.getById(id);
        byte[] bytes = PortalHandle.getTaskLogs(task.getRunHost(), task.getRunPort(), task.getRunUser(), task.getRunPass(), filePath).getBytes(StandardCharsets.UTF_8);
        String logName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String filename = "log_" + id + "_" + date + "_" + logName;

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength(bytes.length);
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);

        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
    }

    /**
     * Download taskEnv log file
     */
    @GetMapping("/log/downloadEnv/{id}")
    public void downloadEnvLog(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        MainTaskEnvErrorHost errorHost = mainTaskEnvErrorHostService.getOneByMainTaskId(id);
        String logPath = "~/portal/logs/portal_.log";
        byte[] bytes = PortalHandle.getTaskLogs(errorHost.getRunHost(), errorHost.getRunPort(), errorHost.getRunUser(), errorHost.getRunPass(), logPath).getBytes(StandardCharsets.UTF_8);
        String logName = "installError.log";
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String filename = "log_" + id + "_" + date + "_" + logName;
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength(bytes.length);
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
    }

}
