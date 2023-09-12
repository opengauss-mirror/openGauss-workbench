/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/14 15:03
 * @description
 */
@RestController
@RequestMapping("/api/v1/alertRecord")
public class AlertRecordController extends BaseController {
    @Autowired
    private AlertRecordService alertRecordService;

    @GetMapping("")
    public TableDataInfo alertRecordListPage(AlertRecordReq alertRecordReq) {
        Page<AlertRecordDto> page = alertRecordService.getListPage(alertRecordReq, startPage());
        return getDataTable(page);
    }

    @GetMapping("/statistics")
    public AjaxResult alertRecordStatistics(AlertStatisticsReq alertStatisticsReq) {
        AlertStatisticsDto statisticsDto = alertRecordService.alertRecordStatistics(alertStatisticsReq);
        return AjaxResult.success(statisticsDto);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        AlertRecordDto alertRecordDto = alertRecordService.getById(id);
        return AjaxResult.success(alertRecordDto);
    }

    /**
     * mark the read record as unread
     *
     * @param ids Primary key ID collection
     * @return return success info
     */
    @PostMapping("/markAsUnread")
    public AjaxResult markAsUnread(@RequestParam String ids) {
        String result = alertRecordService.markAsUnread(ids);
        return AjaxResult.success(result);
    }

    /**
     * mark the unread record as read
     *
     * @param ids Primary key ID collection
     * @return return success info
     */
    @PostMapping("/markAsRead")
    public AjaxResult markAsRead(@RequestParam String ids) {
        String result = alertRecordService.markAsRead(ids);
        return AjaxResult.success(result);
    }

    @GetMapping("/{id}/relation")
    public AjaxResult getRelationData(@PathVariable Long id) {
        List<AlertRelationDto> relationData = alertRecordService.getRelationData(id);
        return AjaxResult.success(relationData);
    }

    /**
     * get the relation logs
     *
     * @param id recordId
     * @param isAlertLog true or false
     * @param searchAfter String
     * @return AjaxResult.success: LogInfoDTO
     */
    @GetMapping("/{id}/relationLog")
    public AjaxResult getRelationLog(@PathVariable Long id, Boolean isAlertLog, String searchAfter) {
        return AjaxResult.success(alertRecordService.getRelationLog(id, isAlertLog, searchAfter));
    }

    /**
     * export record list
     *
     * @param alertStatisticsReq AlertStatisticsReq
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @GetMapping("/export")
    public void export(AlertStatisticsReq alertStatisticsReq, HttpServletResponse response) throws IOException {
        Workbook workbook = alertRecordService.exportWorkbook(alertStatisticsReq);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\""
            + MessageSourceUtil.get("alertRecord") + ".xlsx\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * export record report
     *
     * @param alertStatisticsReq AlertStatisticsReq
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @GetMapping("/exportReport")
    public void exportReport(AlertStatisticsReq alertStatisticsReq, HttpServletResponse response) throws IOException {
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", URLEncoder.encode(MessageSourceUtil.get("alertRecord") + ".html",
            StandardCharsets.UTF_8));
        response.addHeader("Response-Type", "blob");
        response.setCharacterEncoding("UTF-8");
        String html = alertRecordService.exportReport(alertStatisticsReq);
        try (
            InputStream inputStream = new ByteArrayInputStream(html.getBytes("UTF-8"));
            OutputStream outputStream = response.getOutputStream()
        ) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        }
    }
}
