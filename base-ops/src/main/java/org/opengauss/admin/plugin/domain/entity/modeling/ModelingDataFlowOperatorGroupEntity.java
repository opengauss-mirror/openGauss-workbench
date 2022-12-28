package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_data_flow_operator_group
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_data_flow_operator_group")
public class ModelingDataFlowOperatorGroupEntity extends BaseEntity {

    @TableId
    private Object id;

    /**
     * group name in menu
     */
    private String name;

    /**
     * sort when display in menu
     */
    private Object sortId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
