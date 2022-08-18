package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author lhf
 * @date 2022/11/2 16:48
 **/
@Data
public class BackupInputDto {
    @NotBlank(message = "Cluster ID cannot be empty")
    private String clusterId;
    @NotBlank(message = "Backup directory cannot be empty")
    private String backupPath;

    private String remark;

    private String businessId;
}
