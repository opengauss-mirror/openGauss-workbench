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
 * CustomDbResource.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/dto/CustomDbResource.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.dto;

import lombok.Data;

/**
 * @className: CustomDbResource
 * @author: xielibo
 * @date: 2023-02-20 21:18
 **/
@Data
public class CustomDbResource {

    private String clusterName;

    private String dbUrl;

    private String userName;

    private String password;
}
