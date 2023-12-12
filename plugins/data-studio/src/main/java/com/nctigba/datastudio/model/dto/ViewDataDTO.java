/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;

import java.util.Map;

/**
 * ViewDataDTO
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class ViewDataDTO {
    private String winId;
    private Map<String, Object> data;
    private Integer dataSize;
    private Integer pageTotal;
    private Integer pageNum;
    private Integer pageSize;
    private String sql;

    /**
     * set view data dto
     *
     * @param request request
     */
    public void setViewDataDTO(DatabaseSelectViewDTO request) {
        this.pageSize = request.getPageSize();
        this.pageNum = request.getPageNum();
    }
}
