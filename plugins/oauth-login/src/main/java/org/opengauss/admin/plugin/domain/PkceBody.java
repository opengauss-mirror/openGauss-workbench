/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * PkceBody.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/domain/PkceBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2024/5/31 18:06
 * @since 0.0
 */
@Data
@AllArgsConstructor
public class PkceBody {
    private String code_verifier;
    private String code_challenge;
    private String code_challenge_method;
}
