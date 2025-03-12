/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskAlert;
import org.opengauss.admin.plugin.domain.MigrationTaskAlertDetail;
import org.opengauss.admin.plugin.exception.MigrationTaskException;
import org.opengauss.admin.plugin.mapper.MigrationTaskAlertDetailMapper;
import org.opengauss.admin.plugin.service.MigrationTaskAlertDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * tb migration alert detail service impl
 *
 * @since 2024/12/17
 */
@Slf4j
@Service
public class MigrationTaskAlertDetailServiceImpl
        extends ServiceImpl<MigrationTaskAlertDetailMapper, MigrationTaskAlertDetail>
        implements MigrationTaskAlertDetailService {
    @Autowired
    private MigrationTaskAlertService alertService;

    @Autowired
    private MigrationTaskAlertDetailMapper alertDetailMapper;

    @Override
    public MigrationTaskAlertDetail getGroupDetailByAlertId(int alertId) {
        MigrationTaskAlert taskAlert = alertService.getById(alertId);
        if (taskAlert == null) {
            log.error("Alert cannot found by alert id: {}", alertId);
            throw new MigrationTaskException("Alert not found.");
        }

        List<Integer> alertIds = alertService.getGroupAlertIds(taskAlert);

        LambdaQueryWrapper<MigrationTaskAlertDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
        detailQueryWrapper.in(MigrationTaskAlertDetail::getAlertId, alertIds);
        detailQueryWrapper.orderByDesc(MigrationTaskAlertDetail::getAlertId);
        detailQueryWrapper.last("LIMIT 100");

        List<MigrationTaskAlertDetail> migrationTaskAlertDetails = list(detailQueryWrapper);
        if (migrationTaskAlertDetails == null || migrationTaskAlertDetails.isEmpty()) {
            log.error("The table data is abnormal. Alert detail cannot found by alert id: {}", alertId);
            throw new MigrationTaskException("Alert detail not found.");
        }
        MigrationTaskAlertDetail taskAlertDetail = migrationTaskAlertDetails.get(0);
        StringBuilder alertDetails = new StringBuilder();
        for (MigrationTaskAlertDetail alertDetail : migrationTaskAlertDetails) {
            alertDetails.append(alertDetail.getDetail()).append("\n\n");
        }
        if (alertIds.size() > 100) {
            alertDetails.append("...\n\n");
            alertDetails.append("Only the latest 100 alerts are displayed. For more details, please see the log file.");
        }
        taskAlertDetail.setDetail(alertDetails.toString());
        return taskAlertDetail;
    }
}
