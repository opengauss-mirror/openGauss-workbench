/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/4/14 14:50
 * @description
 */
@Data
@Generated
@Accessors(chain = true)
public class AlertStatisticsDto {
    private Integer totalNum = 0;
    private Integer unReadNum = 0;
    private Integer readNum = 0;
    private Integer firingNum = 0;
    private Integer recoverNum = 0;
    private Integer seriousNum = 0;
    private Integer warnNum = 0;
    private Integer infoNum = 0;
}
