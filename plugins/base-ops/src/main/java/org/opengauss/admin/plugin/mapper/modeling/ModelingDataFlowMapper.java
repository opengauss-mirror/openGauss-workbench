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
 * ModelingDataFlowMapper.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/mapper/modeling/ModelingDataFlowMapper.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.mapper.modeling;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author LZW
* @description modeling_data_flow
* @createDate 2022-08-10 11:41:13
* @Entity org.opengauss.admin.plugin.domain.modeling.dataflow.system.ModelingDataFlow
*/
@Mapper
public interface ModelingDataFlowMapper extends BaseMapper<ModelingDataFlowEntity> {
    @Select("SELECT * FROM modeling_data_flow WHERE name = #{name}")
    List<ModelingDataFlowEntity> findByName(String name);
}




