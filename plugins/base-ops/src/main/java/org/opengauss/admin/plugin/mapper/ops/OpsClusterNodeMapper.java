/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * cluster node mapper
 *
 * @author wbd
 * @since 2024/1/20 09:14
 **/
@Mapper
public interface OpsClusterNodeMapper extends BaseMapper<OpsClusterNodeEntity> {
}
