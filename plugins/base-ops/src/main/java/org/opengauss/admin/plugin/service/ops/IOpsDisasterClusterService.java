/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDisasterClusterEntity;
import org.opengauss.admin.plugin.domain.model.ops.DisasterBody;
import org.opengauss.admin.plugin.domain.model.ops.OpsDisasterCluster;
import org.opengauss.admin.plugin.domain.model.ops.OpsDisasterHost;

import java.util.List;
import java.util.Map;

/**
 * disaster cluster service interface
 *
 * @author wbd
 * @since 2024/1/25 17:37
 **/
public interface IOpsDisasterClusterService extends IService<OpsDisasterClusterEntity> {
    /**
     * list all disaster cluster
     *
     * @return List
     */
    List<OpsDisasterCluster> listCluster();

    /**
     * monitor disaster cluster and sub cluster status
     *
     * @param clusterId clusterId
     * @param businessId businessId
     */
    void monitor(String clusterId, String businessId);

    /**
     * list all cluster and device manager
     *
     * @return Map
     */
    Map<String, Object> listClusterAndDeviceManager();

    /**
     * install disaster cluster
     *
     * @param disasterBody disasterBody
     * @return AjaxResult
     */
    AjaxResult install(DisasterBody disasterBody);

    /**
     * switchover disaster cluster
     *
     * @param disasterBody disasterBody
     * @return AjaxResult
     */
    AjaxResult switchover(DisasterBody disasterBody);

    /**
     * delete disaster cluster
     *
     * @param disasterBody disasterBody
     */
    void removeDisasterCluster(DisasterBody disasterBody);

    /**
     * when install failed,get all host nodes of disaster cluster
     *
     * @param primaryClusterName primaryClusterName
     * @param standbyClusterName standbyClusterName
     * @return List
     */
    List<OpsDisasterHost> getHosts(String primaryClusterName, String standbyClusterName);
}
