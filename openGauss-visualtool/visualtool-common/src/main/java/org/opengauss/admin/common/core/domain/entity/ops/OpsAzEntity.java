package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author lhf
 * @date 2022/8/6 15:53
 **/
@Data
@TableName("ops_az")
@EqualsAndHashCode(callSuper = true)
public class OpsAzEntity extends BaseEntity {
    @TableId
    private String azId;
    @NotBlank(message = "Name is required")
    private String name;
    private Integer priority;
    @NotBlank(message = "Actual location cannot be empty")
    private String address;
}
