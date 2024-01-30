/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertRecordService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/AlertRecordService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.dto.AlertRecordDTO;
import com.nctigba.alert.monitor.model.dto.AlertRelationDTO;
import com.nctigba.alert.monitor.model.dto.AlertStatisticsDTO;
import com.nctigba.alert.monitor.model.dto.LogInfoDTO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.query.AlertRecordQuery;
import com.nctigba.alert.monitor.model.query.AlertStatisticsQuery;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/17 10:35
 * @description
 */
public interface AlertRecordService extends IService<AlertRecordDO> {
    Page<AlertRecordDTO> getListPage(AlertRecordQuery alertRecordQuery, Page page);

    AlertStatisticsDTO alertRecordStatistics(AlertStatisticsQuery alertStatisticsQuery);

    String markAsRead(String ids);

    AlertRecordDTO getById(Long id);

    List<AlertRelationDTO> getRelationData(Long id);

    String markAsUnread(String ids);

    /**
     * exportWorkbook
     *
     * @param alertStatisticsQuery AlertStatisticsReq
     * @return Workbook
     */
    Workbook exportWorkbook(AlertStatisticsQuery alertStatisticsQuery);

    /**
     * exportReport
     *
     * @param alertStatisticsQuery AlertStatisticsReq
     * @return String html
     */
    String exportReport(AlertStatisticsQuery alertStatisticsQuery);

    /**
     * getRelationLog
     *
     * @param id recordId
     * @param isAlertLog true or false
     * @param searchAfter  String
     * @return LogInfoDTO
     */
    LogInfoDTO getRelationLog(Long id, Boolean isAlertLog, String searchAfter);

    /**
     * getList
     *
     * @param clusterNodeId clusterNodeId
     * @param templateId templateId
     * @param templateRuleId templateRuleId
     * @return List<AlertRecord>
     */
    List<AlertRecordDO> getList(String clusterNodeId, Long templateId, Long templateRuleId);
}