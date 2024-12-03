/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * OpenGaussClusterLifeCycleService.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service
 * /OpenGaussClusterLifeCycleService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service;

import org.opengauss.admin.container.beans.OpenGaussClusterDetail;
import org.opengauss.admin.container.beans.OpenGaussClusterDto;
import org.opengauss.admin.container.beans.OpenGaussClusterSwitchoverDto;
import org.opengauss.admin.container.beans.OpenGaussPodVo;

import java.util.List;
import java.util.Map;

/**
 * OpenGaussClusterLifeCycleService
 *
 * @since 2024-08-29
 */
public interface OpenGaussClusterLifeCycleService {
    /**
     * 集群部署
     *
     * @param openGaussClusterDto openGaussClusterDto
     * @return 异常信息
     */
    String deploy(OpenGaussClusterDto openGaussClusterDto);

    /**
     * 集群释放
     *
     * @param id 集群id
     * @return 异常信息
     */
    String release(Integer id);

    /**
     * 集群列表
     *
     * @param k8sId    id
     * @param ogcName  ogcName
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @return map
     */
    Map<String, Object> openGaussClusterList(String k8sId, String ogcName, Integer pageNum, Integer pageSize);

    /**
     * 集群详情
     *
     * @param id 集群id
     * @return 详情
     */
    OpenGaussClusterDetail openGaussClusterDetail(Integer id);

    /**
     * 集群pod列表
     *
     * @param id id
     * @return OpenGaussClusterDetail
     */
    List<OpenGaussPodVo> openGaussClusterPodList(Integer id);

    /**
     * 重启opengaussCluster资源
     *
     * @param id 集群id
     * @return 异常信息
     */
    String restartOpengaussCluster(Long id);

    /**
     * 更新
     *
     * @param id                  id
     * @param opengaussClusterDto opengaussClusterDto
     * @return 异常信息
     */
    String updateOpengaussCluster(Long id, OpenGaussClusterDto opengaussClusterDto);

    /**
     * 释放指定pod
     *
     * @param id      数据库id
     * @param podName podName
     * @return 异常信息
     */
    String releaseOgcPod(Long id, String podName);

    /**
     * 集群主备切换
     *
     * @param switchoverDto switchoverDto
     * @return 异常信息
     */
    String switchoverOpengaussCluster(OpenGaussClusterSwitchoverDto switchoverDto);
}
