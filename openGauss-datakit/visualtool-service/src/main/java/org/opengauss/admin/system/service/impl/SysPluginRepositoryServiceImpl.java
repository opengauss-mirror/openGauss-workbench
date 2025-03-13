/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 * SysPluginRepositoryServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/
 * system/service/impl/SysPluginRepositoryServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.SysPluginDownloadStatus;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.domain.SysPluginRepository;
import org.opengauss.admin.system.mapper.SysPluginMapper;
import org.opengauss.admin.system.mapper.SysPluginRepositoryMapper;
import org.opengauss.admin.system.service.ISysPluginRepositoryService;
import org.opengauss.admin.system.utils.OnlinePluginloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * SysPluginRepositoryServiceImpl
 *
 * @author zhaochen
 * @since 2025-03-20
 * @version 7.0.0-RC1
 */
@Service
@Slf4j
public class SysPluginRepositoryServiceImpl
    extends ServiceImpl<SysPluginRepositoryMapper, SysPluginRepository>
    implements ISysPluginRepositoryService {
    @Autowired
    private SysPluginMapper sysPluginMapper;
    @Autowired
    private SysPluginRepositoryMapper sysPluginRepositoryMapper;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private OnlinePluginloadUtil onlinePluginloadUtil;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Value("${version}")
    private String adminVersion;

    public String getAdminVersion() {
        return adminVersion;
    }

    @Override
    public void updatePluginDownloadStatus(List<String> pluginIds, Integer isDownload) {
        sysPluginRepositoryMapper.updatePluginDownloadStatus(pluginIds, isDownload);
    }

    @Override
    public List<String> getUnloadPluginIdsList() {
        List<String> loadedPluginIds = sysPluginMapper.getPluginIds();
        if (!loadedPluginIds.isEmpty()) {
            updatePluginDownloadStatus(loadedPluginIds, SysPluginDownloadStatus.DOWNLOADED_STATUS.getCode());
        }

        List<String> unloadedPluginIds = this.getUnloadPluginListByRepository(loadedPluginIds);

        if (!unloadedPluginIds.isEmpty()) {
            updatePluginDownloadStatus(unloadedPluginIds, SysPluginDownloadStatus.UN_DOWNLOAD_STATUS.getCode());
        }

        return unloadedPluginIds;
    }

    @Override
    public List<String> getUnloadPluginListByRepository(List<String> pluginIds) {
        return sysPluginRepositoryMapper.getUnloadPluginListByRepository(pluginIds);
    }

    @Override
    public String getCurrentVersion() {
        return this.getAdminVersion();
    }

    @Override
    public String getPluginUrlByIdAndVersion(String pluginId, String pluginVersion) {
        return sysPluginRepositoryMapper.getPluginUrlByIdAndVersion(pluginId, pluginVersion);
    }

    @Override
    public void saveOnlinePackage(String downloadUrl, String fileName, String wsBusinessId) {
        if (wsBusinessId.isEmpty()) {
            log.error("wsBusinessId is null in saveOnlinePackage");
        }
        // Attempt to get the session, if not found, throw OpsException
        WsSession wsSession = wsConnectorManager.getSession(wsBusinessId)
                .orElseThrow(() -> new OpsException(
                String.format("No WebSocket session found for businessId: %s", wsBusinessId)));
        // The download task is submitted asynchronously
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            onlinePluginloadUtil.download(downloadUrl, fileName, wsSession);
        });

        // Register download progress
        TaskManager.registry(wsBusinessId, future);
        try {
            future.get(); // Block until the task is completed
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
