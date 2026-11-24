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

package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseConnectionDO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseConnectionDO {
    private String connectionid;

    private String id;

    private String type;

    private String name;

    private String driver;

    private String ip;

    private String port;

    private String dataName;

    private String userName;

    private String password;

    private String webUser;

    private String edition;
    private String isRememberPassword;
}
