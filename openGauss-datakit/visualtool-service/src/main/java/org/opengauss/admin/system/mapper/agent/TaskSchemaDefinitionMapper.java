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
 */

package org.opengauss.admin.system.mapper.agent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.common.core.domain.entity.agent.TaskSchemaDefinition;

/**
 * TaskSchemaDefinitionMapper
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:20
 * @Description: TaskSchemaDefinitionMapper
 * @since 7.0.0-RC2
 **/
@Mapper
public interface TaskSchemaDefinitionMapper extends BaseMapper<TaskSchemaDefinition> {
}
