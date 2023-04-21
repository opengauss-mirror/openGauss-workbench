/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * SyncConfigDto.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/bean/SyncConfigDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @className: SyncConfigDto
 * @description: SyncConfigDto
 * @author: xielibo
 * @date: 2022-10-28 12:55
 **/
@Data
public class SyncConfigDto {

    @NotNull
    public String mysqlHost;
    @NotNull
    public String mysqlPort;
    @NotNull
    public String mysqlUser;
    @NotNull
    public String mysqlPass;
    @NotNull
    public String mysqlSchema;
    @NotNull
    public String ogHost;
    @NotNull
    public String ogPort;
    @NotNull
    public String ogUser;
    @NotNull
    public String ogPass;
    @NotNull
    public String ogDatabase;
    @NotNull
    public String ogSchema;
}
