/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.dto.AlertClusterNodeConfDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.model.AlertClusterNodeAndTemplateReq;
import com.nctigba.alert.monitor.model.AlertClusterNodeConfReq;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/22 10:46
 * @description
 */
public interface AlertClusterNodeConfService extends IService<AlertClusterNodeConf> {
    void saveClusterNodeConf(AlertClusterNodeConfReq alertClusterNodeConfReq);

    AlertClusterNodeConf getByClusterNodeId(String clusterNodeId);

    void saveAlertTemplateAndConfig(AlertClusterNodeAndTemplateReq clusterNodeAndTemplateReq);

    List<AlertClusterNodeConfDto> getList();
}
