package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_data_flow_operator
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_data_flow_operator")
public class ModelingDataFlowOperatorEntity extends BaseEntity {

    @TableId
    private Object id;

    /**
     * operator config name
     */
    private String name;

    /**
     * packagePath
     */
    private String packagePath;

    /**
     * enable in which type of query
     */
    private Integer type;

    /**
     * group in menu
     */
    private Integer groupId;

    /**
     * sort id
     */
    private Integer sortId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
