package org.opengauss.admin.common.core.vo;

import lombok.Data;

/**
 * Encapsulates some host attributes
 *
 * @author duanguoqiang
 * @version 0.0
 * @date 2024/2/5 14:27
 * @since 0.0
 */
@Data
public class HostInfoVo {
    private String hostname;
    private String os;
    private String osVersion;
    private String cpuArch;
}
