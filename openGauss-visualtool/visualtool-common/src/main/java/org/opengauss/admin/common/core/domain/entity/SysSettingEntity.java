package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * System Setting Model
 *
 * @author wangyl
 */
@Data
@TableName("sys_setting")
public class SysSettingEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotBlank(message = "uploadPath cannot be empty")
    private String uploadPath;
    @NotBlank(message = "uploadPath cannot be empty")
    private Integer userId;
}
