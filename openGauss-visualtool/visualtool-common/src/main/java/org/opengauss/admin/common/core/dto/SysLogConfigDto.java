package org.opengauss.admin.common.core.dto;

import lombok.Data;

/**
 * System log config dto
 */
@Data
public class SysLogConfigDto {
    private String totalSizeCap;

    private Integer maxHistory;

    private String level;

    private String maxFileSize;
}
