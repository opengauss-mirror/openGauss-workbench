package org.opengauss.admin.plugin.domain.model.ops.env;

import lombok.Data;

import java.util.List;

/**
 * Software Environment
 *
 * @author lhf
 * @date 2022/8/10 10:32
 **/
@Data
public class SoftwareEnv {
    private List<EnvProperty> envProperties;
}
