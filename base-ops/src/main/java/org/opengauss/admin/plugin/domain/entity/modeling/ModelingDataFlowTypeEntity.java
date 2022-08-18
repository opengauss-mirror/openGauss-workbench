package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 * @TableName modeling_data_flow_type
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_data_flow_type")
public class ModelingDataFlowTypeEntity extends BaseEntity {

    @TableId
    private Object id;

    /**
     * type name
     */
    private String name;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
