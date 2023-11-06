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

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.TbMigrationTaskGlobalToolsParam;
import org.opengauss.admin.plugin.mapper.TbMigrationTaskGlobalToolsParamMapper;
import org.opengauss.admin.plugin.service.TbMigrationTaskGlobalToolsParamService;
import org.springframework.stereotype.Service;

/**
 * TbMigrationTaskGlobalToolsParamServiceImpl
 *
 * @author: www
 * @date: 2023/11/28 15:38
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@Service
public class TbMigrationTaskGlobalToolsParamServiceImpl extends ServiceImpl<TbMigrationTaskGlobalToolsParamMapper,
        TbMigrationTaskGlobalToolsParam> implements TbMigrationTaskGlobalToolsParamService {
    /**
     * removeByHostId
     *
     * @author: www
     * @date: 2023/11/28 15:38
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param hostId hostId
     * @return boolean
     */
    @Override
    public boolean removeByHostId(String hostId) {
        LambdaQueryWrapper<TbMigrationTaskGlobalToolsParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMigrationTaskGlobalToolsParam::getPortalHostID, hostId);
        return remove(wrapper);
    }
}
