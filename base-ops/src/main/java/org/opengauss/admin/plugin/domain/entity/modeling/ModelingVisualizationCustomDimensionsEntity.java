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
