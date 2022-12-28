package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/8/10 14:08
 **/
@Data
public class JschResult {
    private Integer exitCode;
    private String result;
}
