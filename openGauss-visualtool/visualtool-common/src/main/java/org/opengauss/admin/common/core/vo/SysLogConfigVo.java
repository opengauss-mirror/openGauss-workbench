package org.opengauss.admin.common.core.vo;

import lombok.Data;

@Data
public class SysLogConfigVo {
    private String totalSizeCap;

    private Integer maxHistory;

    private String level;

    private String maxFileSize;
}
