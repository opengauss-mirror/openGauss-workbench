package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class OpsImportSshEntity {
    @TableField(value = "host_id")
    private Long hostId;
    @TableField(value = "host_user_id")
    private Long hostUserId;
    @TableField(value = "port")
    private Integer port;
    @TableField(value = "password")
    private String password;
}
