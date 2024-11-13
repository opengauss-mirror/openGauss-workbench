/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * login user body
 *
 * @since 2024/10/21
 */
@Data
@AllArgsConstructor
public class LoginBody {
    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;
}
