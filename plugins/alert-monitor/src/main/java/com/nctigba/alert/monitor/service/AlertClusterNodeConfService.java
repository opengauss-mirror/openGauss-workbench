/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.dto.AlertClusterNodeConfDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeAndTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeConfQuery;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/22 10:46
 * @description
 */
public interface AlertClusterNodeConfService extends IService<AlertClusterNodeConfDO> {
    void saveClusterNodeConf(AlertClusterNodeConfQuery alertClusterNodeConfQuery);

    /**
     * getByClusterNodeId
     *
     * @param clusterNodeId String
     * @param type String
     * @return AlertClusterNodeConfDO
     */
    AlertClusterNodeConfDO getByClusterNodeId(String clusterNodeId, String type);

    void saveAlertTemplateAndConfig(AlertClusterNodeAndTemplateQuery clusterNodeAndTemplateReq);

    /**
     * getList
     *
     * @param type String
     * @return List<AlertClusterNodeConfDTO>
     */
    List<AlertClusterNodeConfDTO> getList(String type);

    /**
     * unbindByIds
     *
     * @param clusterNodeIds String
     * @param type String
     */
    void unbindByIds(String clusterNodeIds, String type);
}
