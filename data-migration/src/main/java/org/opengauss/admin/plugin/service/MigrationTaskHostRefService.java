/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * MigrationTaskHostRefService.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/MigrationTaskHostRefService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.dto.CustomDbResource;
import org.opengauss.admin.plugin.vo.TargetClusterVO;

import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskHostRefService extends IService<MigrationTaskHostRef> {


    void deleteByMainTaskId(Integer mainTaskId);

    List<MigrationTaskHostRef> listByMainTaskId(Integer mainTaskId);

    List<Map<String, Object>> getHosts();

    List<JdbcDbClusterVO> getSourceClusters();

    void saveDbResource(CustomDbResource dbResource);

    void saveSource(String clusterName, String dbUrl, String username, String password);

    List<TargetClusterVO> getTargetClusters();

    List<String> getMysqlClusterDbNames(String url, String username, String password);

    List<Map<String, Object>> getOpsClusterDbNames(OpsClusterNodeVO clusterNode);

    List<OpsHostUserEntity> getHostUsers(String hostId);

    AjaxResult installPortal(String hostId, String hostUserId, String installPath);


    AjaxResult retryInstallPortal(String hostId);

    String getPortalInstallLog(String hostId);
}
