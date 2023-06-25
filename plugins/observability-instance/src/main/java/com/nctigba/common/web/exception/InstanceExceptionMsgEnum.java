/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.common.web.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author liupengfei
 * @since 2023/6/19 
 */
@Getter
@AllArgsConstructor
public enum InstanceExceptionMsgEnum {
    SESSION_DETAIL_GENERAL_MESSAGE(500, "session.detail.general.message"),
    SESSION_DETAIL_BLOCK_MESSAGE(500, "session.detail.block.message");
    private final Integer code;
    private final String msg;
}
