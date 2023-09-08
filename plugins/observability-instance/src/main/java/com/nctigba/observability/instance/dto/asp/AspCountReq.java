/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.asp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AspCountReq
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AspCountReq {
    private String id;
    private Date startTime;
    private Date finishTime;
}
