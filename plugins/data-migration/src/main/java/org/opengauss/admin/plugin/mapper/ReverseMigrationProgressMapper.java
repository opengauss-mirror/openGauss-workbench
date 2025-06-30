/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.plugin.domain.ReverseMigrationProgress;

/**
 * Reverse migration progress mapper
 *
 * @since 2025/06/23
 */
@Mapper
public interface ReverseMigrationProgressMapper extends BaseMapper<ReverseMigrationProgress> {
}
