package org.opengauss.admin.plugin.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.enums.PortalInstallStatus;

import java.util.List;

@Data
public class MigrationHostDto {
    private List<MigrationTask> tasks;
    private List<String> baseInfos;
    private MigrationHostPortalInstall installInfo;
    private OpsHostEntity hostInfo;
    private Integer installPortalStatus;
}
