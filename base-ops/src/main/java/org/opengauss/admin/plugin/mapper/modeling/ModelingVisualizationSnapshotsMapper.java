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
 * ModelingVisualizationSnapshotsMapper.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/mapper/modeling/ModelingVisualizationSnapshotsMapper.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.mapper.modeling;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationSnapshotsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LZW
* @description modeling_visualization_snapshots
* @createDate 2022-09-23 13:44:43
* @Entity generator.domain.ModelingVisualizationSnapshots
*/
@Mapper
public interface ModelingVisualizationSnapshotsMapper extends BaseMapper<ModelingVisualizationSnapshotsEntity> {

}




