package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkBody;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ShardingRuleDto;

public interface IOpsOlkService extends IService<OpsOlkEntity> {
    void install(InstallOlkBody installBody);

    void remove(String id);

    void destroy(String id);

    String generateRuleYaml(OlkConfig config);

    void start(String id);

    void stop(String id);
}
