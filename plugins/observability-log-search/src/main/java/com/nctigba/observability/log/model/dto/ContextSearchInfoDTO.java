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

package com.nctigba.observability.log.model.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Log-ContextSearch response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/2/1 17:05
 */
@Data
public class ContextSearchInfoDTO {
    private String scrollId;
    private List<ContextSearchDTO> logs;
    private List<String> sorts;
}
