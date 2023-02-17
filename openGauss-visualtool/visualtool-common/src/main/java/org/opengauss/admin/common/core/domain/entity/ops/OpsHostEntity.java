package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/6 16:08
 **/
@Data
@TableName("ops_host")
@EqualsAndHashCode(callSuper = true)
public class OpsHostEntity extends BaseEntity {
    @TableId
    private String hostId;
    private String hostname;
    private String privateIp;
    private String publicIp;
    private Integer port;
    private String azId;
    private String os;
    private String cpuArch;
}
