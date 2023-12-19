/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TipsException.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/exception/TipsException.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.exception;

import com.nctigba.observability.instance.util.MessageSourceUtils;
import lombok.experimental.StandardException;
import org.opengauss.admin.common.exception.CustomException;

/**
 * Exceptions want frontend to show
 *
 * @since 2023/12/1
 */
@StandardException
public class TipsException extends CustomException {
    public TipsException(String messageKey) {
        super(MessageSourceUtils.get(messageKey));
    }
}
