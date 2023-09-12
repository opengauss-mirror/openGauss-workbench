/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.dto.LogInfoDTO;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/17 10:35
 * @description
 */
public interface AlertRecordService extends IService<AlertRecord> {
    Page<AlertRecordDto> getListPage(AlertRecordReq alertRecordReq, Page page);

    AlertStatisticsDto alertRecordStatistics(AlertStatisticsReq alertStatisticsReq);

    String markAsRead(String ids);

    AlertRecordDto getById(Long id);

    List<AlertRelationDto> getRelationData(Long id);

    String markAsUnread(String ids);

    /**
     * exportWorkbook
     *
     * @param alertStatisticsReq AlertStatisticsReq
     * @return Workbook
     */
    Workbook exportWorkbook(AlertStatisticsReq alertStatisticsReq);

    /**
     * exportReport
     *
     * @param alertStatisticsReq AlertStatisticsReq
     * @return String html
     */
    String exportReport(AlertStatisticsReq alertStatisticsReq);

    /**
     * getRelationLog
     *
     * @param id recordId
     * @param isAlertLog true or false
     * @param searchAfter  String
     * @return LogInfoDTO
     */
    LogInfoDTO getRelationLog(Long id, Boolean isAlertLog, String searchAfter);
}
