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
 * IOpsClusterService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsClusterService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.env.HostEnv;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.vo.ops.SessionVO;
import org.opengauss.admin.plugin.vo.ops.SlowSqlVO;
import org.opengauss.admin.plugin.vo.ops.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/8/6 17:37
 **/
public interface IOpsClusterService extends IService<OpsClusterEntity> {
    void download(DownloadBody downloadBody);

    void install(InstallBody installBody);

    UpgradeOsCheckVO upgradeOsCheck(String clusterId, String rootPassword);

    void upgrade(UpgradeBody upgradeBody);

    void upgradeCommit(UpgradeBody upgradeBody);

    void upgradeRollback(UpgradeBody upgradeBody);

    void quickInstall(InstallBody installBody);

    void ssh(SSHBody sshBody);

    List<HostFile> ls(String hostId, String path);

    OpsNodeLogVO logPath(String clusterId, String hostId);

    List<AuditLogVO> auditLog(Page page, String clusterId, String hostId, String start, String end);

    List<SessionVO> listSession(String clusterId, String hostId);

    List<SlowSqlVO> slowSql(Page page, String clusterId, String hostId, String start, String end);

    void download(String hostId, String path, String filename, HttpServletResponse response);

    void uninstall(UnInstallBody unInstallBody);

    void restart(OpsClusterBody restartBody);

    void start(OpsClusterBody startBody);

    void stop(OpsClusterBody stopBody);

    List<OpsClusterVO> listEnterpriseCluster();

    ClusterSummaryVO summary();

    /**
     * monitor
     *
     * @param clusterId clusterId
     * @param hostId hostId
     * @param businessId businessId
     * @param role role
     * @return String
     */
    String monitor(String clusterId, String hostId, String businessId, ClusterRoleEnum role);

    List<OpsHostEntity> listClusterHost(String clusterId);

    boolean hasName(String name);

    void generateconf(String clusterId, String hostId, String businessId);

    void switchover(String clusterId, String hostId, String businessId);

    void build(String clusterId, String hostId, String businessId);

    ListDir listInstallPackage(OpenGaussVersionEnum openGaussVersionEnum, Integer userId);

    HostEnv env(String hostId, OpenGaussSupportOSEnum expectedOs, String rootPassword);

    Map<String, Integer> threadPoolMonitor();

    void removeCluster(String clusterId);

    List<GucSettingVO> getGucSettingList(String clusterId, String hostId) throws OpsException;

    void batchConfigGucSetting(GucSettingDto gucSettingDto);
}
