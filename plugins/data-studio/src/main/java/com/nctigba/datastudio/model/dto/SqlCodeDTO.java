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
 *  SqlCodeDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/SqlCodeDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.entity.SqlCodeDo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SqlCodeDTO
 *
 * @author liupengfei
 * @since 2024/11/25
 */
@Data
@NoArgsConstructor
public class SqlCodeDTO {
    private List<SqlCodeDo> data;
    private Integer pageTotal;
    private Long dataSize;
    private Integer pageNum;
    private Integer pageSize;
}
