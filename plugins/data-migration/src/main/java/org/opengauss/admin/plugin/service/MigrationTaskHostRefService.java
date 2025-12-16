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

import org.apache.poi.ss.formula.functions.T;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.dto.MigrationHostDto;
import org.opengauss.admin.plugin.dto.PortalInstallHostDto;
import org.opengauss.admin.plugin.enums.OpengaussSourceTable;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.vo.OpengaussClusterVo;
import org.opengauss.admin.plugin.vo.SourceClusterVo;
import org.opengauss.admin.plugin.vo.TargetClusterVo;
import org.opengauss.admin.plugin.vo.TargetDatabaseVo;
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

    /**
     * Get the host page list
     *
     * @param iPage page info
     * @param portalInstallHostDto portal install host info
     * @return host page list
     */
    IPage<MigrationHostDto> getHosts(IPage<MigrationHostDto> iPage, PortalInstallHostDto portalInstallHostDto);

    List<JdbcDbClusterVO> getSourceClusters();

    /**
     * getSourceClusters
     *
     * @param dbType database Type
     * @return JdbcDbClusterVOList
     */
    List<JdbcDbClusterVO> getSourceClusters(String dbType);

    List<String> getMysqlClusterDbNames(String url, String username, String password);

    /**
     * getOpengaussClusters
     *
     * @return OpengaussClusterVOList
     */
    List<OpengaussClusterVo> getOpengaussClusters();

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
     * is openGauss connect user admin
     *
     * @param clusterNode cluster node
     * @return boolean
     */
    boolean isConnectUserAdmin(OpsClusterNodeVO clusterNode);

    /**
     * isMasterNode
     *
     * @param clusterName cluster name
     * @return Map<String, Boolean>
     */
    Map<String, Boolean> getNodeRoleMap(String clusterName);

    /**
     * Get the source db clusters by db type
     *
     * @param dbType db type
     * @return SourceClusterVo list
     */
    List<SourceClusterVo> getSourceClusters(DbTypeEnum dbType);

    /**
     * Get source cluster node databases
     *
     * @param dbType db type
     * @param nodeId cluster node id
     * @return database list
     */
    List<String> getSourceDatabases(DbTypeEnum dbType, String nodeId);

    /**
     * Get source cluster node schemas
     *
     * @param dbType db type
     * @param nodeId cluster node id
     * @param dbName database name
     * @return schema list
     */
    List<String> getSourceSchemas(DbTypeEnum dbType, String nodeId, String dbName);

    /**
     * Get source cluster node tables
     *
     * @param dbType db type
     * @param nodeId cluster node id
     * @param dbName database name
     * @param schemaName schema name
     * @param page page info
     * @return table list
     */
    IPage<String> getSourceTablePage(DbTypeEnum dbType, String nodeId, String dbName, String schemaName, Page<T> page);

    /**
     * Get the target db clusters by db type
     *
     * @return TargetClusterVo list
     */
    List<TargetClusterVo> getTargetClusters();

    /**
     * Get target cluster detail
     *
     * @param sourceTable source table
     * @param clusterId cluster id
     * @return TargetClusterVo
     */
    TargetClusterVo getTargetDetail(OpengaussSourceTable sourceTable, String clusterId);

    /**
     * Get target cluster node databases
     *
     * @param sourceTable source table
     * @param nodeId cluster node id
     * @return TargetDatabaseVo list
     */
    List<TargetDatabaseVo> getTargetDatabases(OpengaussSourceTable sourceTable, String nodeId);
}
