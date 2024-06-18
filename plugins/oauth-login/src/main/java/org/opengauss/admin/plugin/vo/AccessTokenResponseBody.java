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
 * AccessTokenResponseBody.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/vo/AccessTokenResponseBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2024/5/31 19:14
 * @since 0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponseBody {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("expires_in")
    private int expiresIn;
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("scope")
    private String scope;
}
