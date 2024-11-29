/*
 *  Copyright (c) GBA-NCTI-ISDC. 2024-2024.
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
 *  SqlCodeDo.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/entity/SqlCodeDo.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * SqlCodeDo
 *
 * @author liupengfei
 * @since 2024/11/25
 */
@NoArgsConstructor
@Data
@Generated
public class SqlCodeDo {
    private Integer id;
    private String name;
    private String code;
    private String description;
}
