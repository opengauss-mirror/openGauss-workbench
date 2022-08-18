package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/13 11:50
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostInfoHolder {
    private OpsHostEntity hostEntity;
    private List<OpsHostUserEntity> hostUserEntities;
}

