/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.query.TableDataQuery;
import lombok.Data;

import java.util.Map;

@Data
public class TableDataDTO {
    private String winId;
    private Map<String, Object> data;
    private Integer dataSize;
    private Integer pageTotal;
    private Integer pageNum;
    private Integer pageSize;

    public void setTableDataDTO(TableDataQuery request) {
        this.winId = request.getWinId();
        this.pageSize = request.getPageSize();
        this.pageNum = request.getPageNum();
    }
}
