/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.plugin.domain.TranscribeReplayHost;

import java.util.List;

/**
 * TranscribeReplayHostService
 *
 * @since 2025-02-10
 */

public interface TranscribeReplayHostService extends IService<TranscribeReplayHost> {
    /**
     * getHostsByTaskId
     *
     * @param taskId taskId
     * @return List<TranscribeReplayHost>
     */
    List<TranscribeReplayHost> getHostsByTaskId(Integer taskId);

    /**
     * getHostByTaskIdAndDbType
     *
     * @param taskId taskId
     * @param dbType dbType
     * @return TranscribeReplayHost
     */
    TranscribeReplayHost getHostByTaskIdAndDbType(Integer taskId, String dbType);

    /**
     * getReplayHostByTaskId
     *
     * @param taskId taskId
     * @return TranscribeReplayHost
     */
    TranscribeReplayHost getReplayHostByTaskId(Integer taskId);

    /**
     * deleteByTaskId
     *
     * @param taskId taskId
     */
    void deleteByTaskId(Integer taskId);
}
