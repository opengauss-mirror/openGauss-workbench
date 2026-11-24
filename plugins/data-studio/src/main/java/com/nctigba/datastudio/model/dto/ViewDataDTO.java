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
