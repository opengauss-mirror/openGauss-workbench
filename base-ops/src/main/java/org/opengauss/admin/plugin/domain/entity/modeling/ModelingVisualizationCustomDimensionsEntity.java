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
 * ModelingVisualizationCustomDimensionsEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/modeling/ModelingVisualizationCustomDimensionsEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.modeling;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.domain.model.modeling.CustomDimension;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_visualization_custom_dimensions
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_visualization_custom_dimensions")
public class ModelingVisualizationCustomDimensionsEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * dimension name
     */
    private String name;

    /**
     * target field
     */
    private String field;

    /**
     * config json
     */
    private String categoriesJson;

    /**
     * belongs to which operator unique id
     */
    private String operatorId;

    /**
     * belongs to which data flow
     */
    private Long dataFlowId;

    private Object sortId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public ModelingVisualizationCustomDimensionsEntity from(CustomDimension customDimension) {

        this.setId(customDimension.getId());
        this.setField(customDimension.getField());
        this.setName(customDimension.getName());
        this.setCategoriesJson(JSONObject.toJSONString(customDimension.getCategories()));
        this.setOperatorId(customDimension.getOperatorId());
        this.setDataFlowId(customDimension.getDataFlowId());

        return this;
    }
}
