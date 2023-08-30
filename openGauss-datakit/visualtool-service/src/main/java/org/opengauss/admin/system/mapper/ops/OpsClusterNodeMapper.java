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
 * OpsClusterNodeMapper.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/mapper/ops/OpsClusterNodeMapper.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lhf
 * @date 2022/8/18 09:14
 **/
@Mapper
public interface OpsClusterNodeMapper extends BaseMapper<OpsClusterNodeEntity> {
}
