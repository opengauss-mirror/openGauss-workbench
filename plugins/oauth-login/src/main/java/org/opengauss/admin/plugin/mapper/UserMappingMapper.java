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
 * UserMappingMapper.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/mapper/UserMappingMapper.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.plugin.domain.entity.UserMapping;

/**
 * @date 2024/6/7 11:36
 * @since 0.0
 */
@Mapper
public interface UserMappingMapper extends BaseMapper<UserMapping> {
}
