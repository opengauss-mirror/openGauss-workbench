/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 */

package org.opengauss.admin.plugin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.FailSqlModel;
import org.opengauss.admin.plugin.domain.TranscribeReplayHost;
import org.opengauss.admin.plugin.domain.TranscribeReplaySlowSql;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskQueryDto;
import org.opengauss.admin.plugin.service.TranscribeReplayFailSqlService;
import org.opengauss.admin.plugin.service.TranscribeReplayHostService;
import org.opengauss.admin.plugin.service.TranscribeReplayService;
import org.opengauss.admin.plugin.service.TranscribeReplaySlowSqlService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * TranscribeReplayController
 *
 * @author zzh
 * @since 2025-02-10
 **/
@RestController
@RequestMapping("/transcribeReplay")
@Slf4j
public class TranscribeReplayController extends BaseController {
    private TranscribeReplayTaskDto transcribeReplayTaskDto;
    @Autowired
    private TranscribeReplayService transcribeReplayService;
    @Autowired
    private TranscribeReplayHostService transcribeReplayHostService;
    @Autowired
    private TranscribeReplaySlowSqlService transcribeReplaySlowSqlService;
    @Autowired
    private TranscribeReplayFailSqlService transcribeReplayFailSqlService;
    @Autowired(required = false)
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * transcribeReplay tools version
     *
     * @return AjaxResult
     */
    @GetMapping("/toolsVersion")
    public AjaxResult transcribeReplayToolsVersion() {
        List<String> toolsVersion = transcribeReplayService.getToolsVersion();
        return AjaxResult.success(toolsVersion);
    }

    /**
     * transcribeReplay tools download
     *
     * @param id id
     * @param config config
     * @return AjaxResult
     */
    @PostMapping("/downloadAndConfig")
    public AjaxResult transcribeReplayToolsDownLoadAndConfig(@RequestParam Integer id,
                                                             @RequestBody Map<String, Object> config) {
        transcribeReplayService.downloadAndConfig(transcribeReplayTaskDto, id, config);
        return AjaxResult.success();
    }

    /**
     * save task
     *
     * @param taskDto taskDto
     * @return AjaxResult
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody TranscribeReplayTaskDto taskDto) {
        this.transcribeReplayTaskDto = taskDto;
        return AjaxResult.success(transcribeReplayService.saveTask(taskDto));
    }

    /**
     * start task
     *
     * @param id id
     * @return AjaxResult
     */
    @PostMapping("/start/{id}")
    public AjaxResult start(@PathVariable Integer id) {
        return transcribeReplayService.startTask(id);
    }

    /**
     * finish task
     *
     * @param id id
     * @return AjaxResult
     */
    @PostMapping("/finish/{id}")
    public AjaxResult finish(@PathVariable Integer id) {
        transcribeReplayService.finishTask(id);
        return AjaxResult.success();
    }

    /**
     * delete task
     *
     * @param ids ids
     * @return AjaxResult
     */
    @PostMapping("/delete/{ids}")
    public AjaxResult delete(@PathVariable Integer[] ids) {
        transcribeReplayService.deleteTask(ids);
        return AjaxResult.success();
    }

    /**
     * page list
     *
     * @param taskQueryDto taskQueryDto
     * @return TableDataInfo
     */
    @GetMapping("/list")
    public TableDataInfo list(TranscribeReplayTaskQueryDto taskQueryDto) {
        IPage<TranscribeReplayTask> list = transcribeReplayService.selectList(startPage(), taskQueryDto);
        return getDataTable(list);
    }

    /**
     * getSlowSqlList
     *
     * @param taskId taskId
     * @param sql sql
     * @return TableDataInfo
     */
    @GetMapping("/list/slowSql/{taskId}")
    public TableDataInfo getSlowSqlList(@PathVariable int taskId, String sql) {
        IPage<TranscribeReplaySlowSql> iPage = transcribeReplayService.getSlowSql(startPage(), taskId, sql);
        return getDataTable(iPage);
    }

    /**
     * getFailSqlList
     *
     * @param taskId taskId
     * @param sql sql
     * @return TableDataInfo
     */
    @GetMapping("/list/failSql/{taskId}")
    public TableDataInfo getFailSqlList(@PathVariable int taskId, String sql) {
        IPage<FailSqlModel> iPage = transcribeReplayService.getFailSql(startPage(), taskId, sql);
        return getDataTable(iPage);
    }

    /**
     * getHostInfo
     *
     * @param taskId taskId
     * @return AjaxResult
     */
    @GetMapping("/getHostInfo/{taskId}")
    public AjaxResult getHostInfo(@PathVariable int taskId) {
        List<TranscribeReplayHost> list = transcribeReplayService.getHostInfo(taskId);
        return AjaxResult.success(list);
    }

    /**
     * downloadSlowSqlLog
     *
     * @param taskId taskId
     * @param response response
     */
    @GetMapping("/download/slowSql/{taskId}")
    public void downloadSlowSqlLog(@PathVariable Integer taskId, HttpServletResponse response) {
        List<TranscribeReplaySlowSql> slowSqls = transcribeReplaySlowSqlService.getListByTaskId(taskId);
        String slowStr = JSON.toJSONString(slowSqls, SerializerFeature.PrettyFormat);
        logDownload(slowStr, "slowSql_%s.txt", response);
    }

    /**
     * downloadFailSqlLog
     *
     * @param taskId taskId
     * @param response response
     */
    @GetMapping("/download/failSql/{taskId}")
    public void downloadFailSqlLog(@PathVariable Integer taskId, HttpServletResponse response) {
        List<FailSqlModel> failSqls = transcribeReplayFailSqlService.getListByTaskId(taskId);
        String failStr = JSON.toJSONString(failSqls, SerializerFeature.PrettyFormat);
        logDownload(failStr, "failSql_%s.txt", response);
    }

    /**
     * getSqlDuration
     *
     * @param taskId taskId
     * @return AjaxResult
     */
    @GetMapping("/getSqlDuration/{taskId}")
    public AjaxResult getSqlDuration(@PathVariable Integer taskId) {
        return AjaxResult.success(transcribeReplayService.getSqlDuration(taskId));
    }

    private void logDownload(String logContent, String fileName, HttpServletResponse response) {
        String content = StringUtils.isBlank(logContent) ? " " : logContent;
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String logName = String.format(fileName, date);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + logName + "\"");
            try (OutputStream output = new BufferedOutputStream(response.getOutputStream())) {
                output.write(bytes);
                output.flush();
            }
        } catch (IOException e) {
            throw new OpsException(e.getMessage());
        }
    }
}
