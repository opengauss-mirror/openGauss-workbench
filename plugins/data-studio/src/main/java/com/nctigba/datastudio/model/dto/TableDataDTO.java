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
 *  TableDataDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/TableDataDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.query.TableDataQuery;
import lombok.Data;
import lombok.Generated;

import java.util.Map;

/**
 * TableDataDTO
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class TableDataDTO {
    private String winId;
    private Map<String, Object> data;
    private Integer dataSize;
    private Integer pageTotal;
    private Integer pageNum;
    private Integer pageSize;
    private String sql;

    /**
     * set table data dto
     *
     * @param request request
     */
    public void setTableDataDTO(TableDataQuery request) {
        this.winId = request.getWinId();
        this.pageSize = request.getPageSize();
        this.pageNum = request.getPageNum();
    }
}
