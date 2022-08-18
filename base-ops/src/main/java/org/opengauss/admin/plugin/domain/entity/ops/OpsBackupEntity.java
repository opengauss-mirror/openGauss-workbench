package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/6 16:08
 **/
@Data
@TableName("ops_backup")
@EqualsAndHashCode(callSuper = true)
public class OpsBackupEntity extends BaseEntity {
    @TableId
    private String backupId;
    private String clusterId;
    private String hostId;
    private String clusterNodeId;
    private String backupPath;
}
