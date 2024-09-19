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
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsClusterTaskService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.enums.ops.OpsClusterTaskStatusEnum;
import org.opengauss.admin.common.enums.ops.OpsHostPortUsedStatusEnum;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterTaskVO;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskQueryParamDTO;
import org.opengauss.admin.plugin.domain.model.ops.dto.OpsClusterTaskDTO;
import org.opengauss.admin.plugin.vo.ops.ClusterEnvCheck;
import org.opengauss.admin.plugin.vo.ops.TaskStatusVo;
import org.opengauss.admin.plugin.vo.ops.ClusterPortVo;

import java.util.List;
import java.util.Map;

/**
 * IOpsClusterTaskService
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
public interface IOpsClusterTaskService extends IService<OpsClusterTaskEntity> {

    /**
     * page query ops cluster task entity by condition
     *
     * @param page page condition
     * @param dto  query condition
     * @return IPage<OpsClusterTaskEntity>
     */
    IPage<OpsClusterTaskEntity> pageByCondition(Page page, OpsClusterTaskQueryParamDTO dto);

    /**
     * delete ops cluster task entity by task id
     *
     * @param taskIds task id list
     * @return Map<String, String>
     */
    Map<String, String> deleteClusterTask(List<String> taskIds);

    /**
     * query ops cluster task detail
     *
     * @param taskId task id
     * @return OpsClusterTaskVO
     */
    OpsClusterTaskVO queryClusterTaskDetail(String taskId);

    /**
     * create ops cluster task
     *
     * @param dto dto
     * @return task id
     */
    String createClusterTask(OpsClusterTaskDTO dto);

    /**
     * update ops cluster task
     *
     * @param dto dto
     */
    void updateClusterTask(OpsClusterTaskDTO dto);

    /**
     * confirm ops cluster task
     *
     * @param taskId task id
     */
    void confirmClusterTask(String taskId);

    /**
     * check cluster install environment
     *
     * @param taskId task id
     * @return List<OpsClusterNodeEnvCheck>
     */
    ClusterEnvCheck checkClusterInstallEnvironment(String taskId);

    /**
     * get cluster install environment
     *
     * @param taskId task id
     * @return List<OpsClusterNodeEnvCheck>
     */
    ClusterEnvCheck getClusterInstallEnvironment(String taskId);

    /**
     * check cluster install environment
     *
     * @param taskId task id
     * @return List<OpsClusterNodeEnvCheck>
     */
    String copyClusterTask(String taskId);

    /**
     * check cluster name exist
     *
     * @param clusterId   cluster id
     * @param clusterName cluster name
     * @return boolean
     */
    boolean checkClusterNameExist(String clusterId, String clusterName);

    /**
     * get host user by host id
     *
     * @param hostId host id
     * @return List<OpsHostUserEntity>
     */
    List<OpsHostUserEntity> listHostUserByHostId(String hostId);

    /**
     * get host user by host id
     *
     * @param hostIp host ip
     * @return List<OpsHostUserEntity>
     */
    OpsHostEntity getHostByIp(String hostIp);

    /**
     * check host port
     *
     * @param taskId   taskId
     * @param hostId   host id
     * @param hostPort host port
     * @return OpsHostPortUsedStatusEnum
     */
    OpsHostPortUsedStatusEnum checkHostPort(String taskId, String hostId, Integer hostPort);

    /**
     * check cluster install prot info
     *
     * @param clusterId cluster id
     * @return port used info
     */
    Map<String, ClusterPortVo> checkTaskHostPort(String clusterId);

    /**
     * batch install cluster
     *
     * @param taskIds task ids
     * @return Map<String, String>
     * @return Map<String, String>
     */
    Map<String, String> batchInstallCluster(List<String> taskIds);

    /**
     * reinstall cluster
     *
     * @param taskId taskId
     * @return String
     */
    void reInstallCluster(String taskId);

    /**
     * get install cluster status by task ids
     *
     * @param taskIds taskIds
     * @return status
     */
    Map<String, TaskStatusVo> getBatchInstallTaskStatus(List<String> taskIds);

    /**
     * update cluster task status
     *
     * @param clusterId                clusterId
     * @param opsClusterTaskStatusEnum opsClusterTaskStatusEnum
     * @param remark                   remark
     */
    void updateClusterTaskStatus(String clusterId, OpsClusterTaskStatusEnum opsClusterTaskStatusEnum, String remark);

    /**
     * get host list
     *
     * @param os        os
     * @param osVersion osVersion
     * @param cpuArch   cpuArch
     * @return list
     */
    List<OpsHostEntity> getHostList(String os, String osVersion, String cpuArch);

    /**
     * full check cluster task : node nums, disk space, host port ,deploy type
     *
     * @param clusterId cluster id
     * @return check result
     */
    boolean checkClusterTask(String clusterId);

    /**
     * check host user can install cluster node
     *
     * @param hostIp       host ip
     * @param hostUsername username
     * @return can install
     */
    boolean checkHostInstanceCanInstallClusterNode(String hostIp, String hostUsername);

    /**
     * check host paths disk space
     *
     * @param hostId host id
     * @param paths  host path
     * @return check result
     */
    Map<String, String> checkHostDiskSpace(String hostId, List<String> paths);

    /**
     * check host and user can install cluster
     *
     * @param clusterId  clusterId
     * @param hostId     hostId
     * @param hostUserId hostUserId
     * @return check result
     */
    boolean checkHostAndUserInstallCluster(String clusterId, String hostId, String hostUserId);

    /**
     * modify cluster task of node change
     *
     * @param clusterId  clusterId
     * @param hostId     hostId
     * @param hostUserId hostUserId
     * @param nodeCount  nodeCount
     */
    void modifyClusterTaskOfNodeChange(String clusterId, String hostId, String hostUserId, int nodeCount);

    /**
     * delete cluster task force
     *
     * @param taskId task id
     * @return result
     */
    String deleteClusterTaskForce(String taskId);
}
