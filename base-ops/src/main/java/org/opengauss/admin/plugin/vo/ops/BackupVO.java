package org.opengauss.admin.plugin.vo.ops;

import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import lombok.Data;

/**
 * @author lhf
 * @date 2022/11/13 09:44
 **/
@Data
public class BackupVO extends OpsBackupEntity {
    private String publicIp;
    private String privateIp;
    private String hostname;
}
