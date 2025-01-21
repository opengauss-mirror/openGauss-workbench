/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * FullCheckParam.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/vo/FullCheckParam.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

/**
 * FullCheckParam
 *
 * @author: wangchao
 * @Date: 2024/12/30 11:55
 * @Description: FullCheckDto
 * @since 7.0.0
 **/
@Data
public class FullCheckParam {
    Integer id;
    String status;
    int pageSize;
    int pageNum;
}
