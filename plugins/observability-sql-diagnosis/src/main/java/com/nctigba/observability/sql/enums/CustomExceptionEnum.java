/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CustomExceptionEnum
 *
 * @author luomeng
 * @since 2023/6/20
 */
@Getter
@AllArgsConstructor
public enum CustomExceptionEnum {
    SUCCESS(200, "200"),
    NOT_FOUND_ERROR(404, "404"),
    INTERNAL_SERVER_ERROR(500, "500"),
    PARAM_INVALID_ERROR(400, "400"),
    UNAUTHORIZED_ERROR(401, "401"),
    FORBIDDEN_ERROR(403, "403"),
    GET_TOKEN_ERROR(502, "502"),
    TOLSQL_IS_RIGHT_PARAM(602, "602"),
    /**
     * monitoring data is empty
     */
    MONITORING_ACCESS_DATA_ERROR(601, "601");

    private final Integer code;
    private final String msg;
}