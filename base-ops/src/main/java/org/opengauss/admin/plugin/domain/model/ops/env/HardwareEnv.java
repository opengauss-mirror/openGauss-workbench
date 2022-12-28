package org.opengauss.admin.plugin.domain.model.ops.env;

import lombok.Data;

import java.util.List;

/**
 * hardware environment
 *
 * @author lhf
 * @date 2022/8/10 10:32
 **/
@Data
public class HardwareEnv {
    private List<EnvProperty> envProperties;
}
