/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.dto.AlertRecordDto;
import org.opengauss.plugin.alertcenter.dto.AlertRelationDto;
import org.opengauss.plugin.alertcenter.dto.AlertStatisticsDto;
import org.opengauss.plugin.alertcenter.entity.AlertRecord;
import org.opengauss.plugin.alertcenter.model.AlertRecordReq;
import org.opengauss.plugin.alertcenter.model.AlertStatisticsReq;

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
}
