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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.dto.CustomDbResource;
import org.opengauss.admin.plugin.dto.MigrationHostDto;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.vo.TargetClusterVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskHostRefService extends IService<MigrationTaskHostRef> {


    void deleteByMainTaskId(Integer mainTaskId);

    List<MigrationTaskHostRef> listByMainTaskId(Integer mainTaskId);

    List<MigrationHostDto> getHosts();

    List<JdbcDbClusterVO> getSourceClusters();

    void saveDbResource(CustomDbResource dbResource);

    void saveSource(String clusterName, String dbUrl, String username, String password);

    List<TargetClusterVO> getTargetClusters();

    List<String> getMysqlClusterDbNames(String url, String username, String password);

    List<Map<String, Object>> getOpsClusterDbNames(OpsClusterNodeVO clusterNode);

    /**
     * SQL querying based on OpenGauss.
     *
     * @param host     host of db
     * @param port     host of db
     * @param database database of db
     * @param dbUser   user of db
     * @param dbPass   password of db
     * @param schema   schema of db
     * @param sql      sql
     * @return result list
     */
    List<Map<String, Object>> queryBySqlOnOpengauss(String host, String port, String database, String dbUser,
                                                    String dbPass, String schema, String sql);

    List<OpsHostUserEntity> getHostUsers(String hostId);

    AjaxResult installPortal(String hostId, MigrationHostPortalInstall install);

    AjaxResult installPortalFromDatakit(String hostId, MigrationHostPortalInstall install, Integer userId) throws PortalInstallException;

    AjaxResult deletePortal(String hostId, Boolean onlyPkg);

    AjaxResult retryInstallPortal(String hostId, MigrationHostPortalInstall install);

    String getPortalInstallLog(String hostId);

    UploadInfo upload(MultipartFile file, Integer userId) throws PortalInstallException;

    /**
     * find tables task
     *
     * @param page      page
     * @param dbName    database name
     * @param url       source database connection
     * @param username  username of db connection
     * @param password  password of db connection
     * @return tables
     */
    IPage<Object> pageByDB(Page page, String dbName, String url, String username, String password);

    /**
     * is openGauss connect user admin
     *
     * @param clusterNode cluster node
     * @return boolean
     */
    boolean isConnectUserAdmin(OpsClusterNodeVO clusterNode);
}
