/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * IOpsClusterTaskService.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsClusterTaskNodeService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;

import java.util.List;

/**
 * IOpsClusterTaskNodeService
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
public interface IOpsClusterTaskNodeService extends IService<OpsClusterTaskNodeEntity> {

    /**
     * List OpsClusterTaskNodeEntity by cluster task id.
     *
     * @param taskId task id.
     * @return List of OpsClusterTaskNodeEntity.
     */
    List<OpsClusterTaskNodeEntity> listByClusterTaskId(String taskId);

    /**
     * list task node by host and user
     *
     * @param hostId     hostId
     * @param hostUserId hostUserId
     * @return list
     */
    List<OpsClusterTaskNodeEntity> listByInstallHostUser(String hostId, String hostUserId);

    /**
     * Remove OpsClusterTaskNodeEntity by cluster task id.
     *
     * @param taskId task id.
     */
    void removeByClusterId(String taskId);
}
