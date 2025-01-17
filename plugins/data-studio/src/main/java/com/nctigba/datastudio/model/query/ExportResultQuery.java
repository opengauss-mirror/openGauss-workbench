/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
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
 *  ExportResultQuery.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/query/ExportResultQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * ExportResultQuery
 *
 * @author liupengfei
 * @since 2024/8/27
 */
@Data
@Generated
@NoArgsConstructor
public class ExportResultQuery extends ExportQuery {
    private Integer pageNum;
    private Long pageSize;
}
