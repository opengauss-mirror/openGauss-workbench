package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lhf
 * @date 2022/10/13 15:22
 **/
@Data
public class WdrGeneratorBody {
    @NotBlank(message = "The cluster ID cannot be empty")
    private String clusterId;
    @NotNull(message = "The report scope cannot be empty")
    private WdrScopeEnum scope;
    private String hostId;
    @NotNull(message = "The report type cannot be empty")
    private WdrTypeEnum type;
    @NotNull(message = "start Snapshot ID cannot be empty")
    private String startId;
    @NotNull(message = "end Snapshot ID cannot be empty")
    private String endId;
}
