/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.TbMigrationTaskGlobalToolsParam;

/**
 * TbMigrationTaskGlobalToolsParamService
 *
 * @author: www
 * @date: 2023/11/28 15:32
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
public interface TbMigrationTaskGlobalToolsParamService extends IService<TbMigrationTaskGlobalToolsParam> {
    /**
     * removeByHostId
     *
     * @param hostId hostId
     * @return boolean
     * @author: www
     * @date: 2023/11/28 15:32
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     */
    boolean removeByHostId(String hostId);

    /**
     * check paramKey existence
     *
     * @param paramKey param key
     * @param configId config id
     * @param portalHostID portal host id
     * @return boolean
     */
    boolean checkParamKeyExistence(String paramKey, Integer configId, String portalHostID);
}
