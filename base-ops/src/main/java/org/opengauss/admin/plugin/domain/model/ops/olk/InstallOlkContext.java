package org.opengauss.admin.plugin.domain.model.ops.olk;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;

@Data
public class InstallOlkContext implements Cloneable {
    private WsSession retSession;
    private OlkConfig olkConfig;

    public void checkConfig() throws OpsException {
        if (StrUtil.isEmpty(olkConfig.getRuleYaml())) {
            throw new OpsException("Sharding rule yaml config content is empty, please generate rule yaml again");
        }
    }
}
