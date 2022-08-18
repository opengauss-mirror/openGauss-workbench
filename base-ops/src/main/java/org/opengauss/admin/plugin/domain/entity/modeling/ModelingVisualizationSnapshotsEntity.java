package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_visualization_snapshots
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_visualization_snapshots")
public class ModelingVisualizationSnapshotsEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String imgBase64;

    /**
     *
     */
    private String chartDataJson;

    /**
     *
     */
    private Long dataFlowId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
