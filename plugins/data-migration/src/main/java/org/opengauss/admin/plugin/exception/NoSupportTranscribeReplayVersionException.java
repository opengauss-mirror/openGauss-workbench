/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.exception;

import org.opengauss.admin.common.exception.base.BaseException;

/**
 * NoSupportTranscribeReplayVersionException
 *
 * @author wangchao
 * @since 2024/10/28
 */
public class NoSupportTranscribeReplayVersionException extends BaseException {
    /**
     * current transcribe replay version is not support
     *
     * @param noSupportVersion noSupportVersion
     */
    public NoSupportTranscribeReplayVersionException(String noSupportVersion) {
        super(noSupportVersion);
    }
}
