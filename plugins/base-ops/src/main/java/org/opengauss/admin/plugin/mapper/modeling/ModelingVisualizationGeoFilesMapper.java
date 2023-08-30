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
 * ModelingVisualizationGeoFilesMapper.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/mapper/modeling/ModelingVisualizationGeoFilesMapper.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.mapper.modeling;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationGeoFilesEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LZW
* @description modeling_visualization_geo_files
* @createDate 2022-11-07 10:42:27
* @Entity generator.domain.ModelingVisualizationGeoFiles
*/
@Mapper
public interface ModelingVisualizationGeoFilesMapper extends BaseMapper<ModelingVisualizationGeoFilesEntity> {

}




