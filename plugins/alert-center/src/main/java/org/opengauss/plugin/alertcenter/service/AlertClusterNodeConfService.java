/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.dto.AlertClusterNodeConfDto;
import org.opengauss.plugin.alertcenter.entity.AlertClusterNodeConf;
import org.opengauss.plugin.alertcenter.model.AlertClusterNodeAndTemplateReq;
import org.opengauss.plugin.alertcenter.model.AlertClusterNodeConfReq;
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
