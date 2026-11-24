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

package com.nctigba.observability.sql.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Log-Search request dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
public class EsSearchVO {
    private List<String> clusterId;
    private List<String> nodeId;
    private String searchPhrase;
    private String sort = "@timestamp";
    private int rowCount;
    private Date startDate;
    private Date endDate;
    private List<String> logType;
    private List<String> logLevel;
    private String scrollId;
    private String order;
    private List<String> sorts;
    private long interval;
    private String id;

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean hasDateFilter() {
        return startDate != null || endDate != null;
    }

    /**
     * param isEmptyObject
     *
     * @return isEmptyObject boolean
     */
    public boolean isEmptyObject() {
        boolean isDate = startDate == null && endDate == null;
        boolean isEmpty = startDate == null && endDate == null && nodeId == null && logType == null && logLevel == null
                && searchPhrase == null;
        return isEmpty;
    }
}
