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

package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;

/**
 * MigrationThirdPartySoftwareInstanceMapper
 *
 * @author: www
 * @date: 2023/11/28 15:28
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@Mapper
public interface MigrationThirdPartySoftwareInstanceMapper extends BaseMapper<MigrationThirdPartySoftwareConfig> {
    /**
     * selectMQPage
     *
     * @author: www
     * @date: 2023/11/28 15:30
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param page page
     * @param entity entity
     * @return IPage<MigrationThirdPartySoftwareConfig>
     */
    IPage<MigrationThirdPartySoftwareConfig> selectMQPage(
            IPage<MigrationThirdPartySoftwareConfig> page, @Param("entity") MigrationThirdPartySoftwareConfig entity);
}




