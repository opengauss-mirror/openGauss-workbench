package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
/**
 * @author LZW
 * @TableName modeling_visualization_params
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_visualization_params")
public class ModelingVisualizationParamsEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * chart name
     */
    private String name;

    /**
     * chart type
     */
    private String type;

    /**
     * chart config params json
     */
    private String paramsJson;

    /**
     * belongs to one operator unique id
     */
    @NotEmpty(message = "operator info not found")
    private String operatorId;

    /**
     * belongs to one dataflow
     */
    @NotEmpty(message = "dataflow info not found")
    private Long dataFlowId;

    private Object sortId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
