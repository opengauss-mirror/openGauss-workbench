package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/6 16:09
 **/
@Data
@TableName("ops_host_user")
@EqualsAndHashCode(callSuper = true)
public class OpsHostUserEntity extends BaseEntity {
    @TableId
    private String hostUserId;
    private String username;
    private String password;
    private String hostId;
    private Boolean sudo;
}
