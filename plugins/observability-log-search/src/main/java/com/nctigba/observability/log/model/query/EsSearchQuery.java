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
 *  EsSearchQuery.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/query/EsSearchQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.model.query;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * <p>
 * Log-Search request dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
public class EsSearchQuery {
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

    public void setStartDate(String startDate) {
        this.startDate = DateUtil.parseUTC(startDate);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = DateUtil.parseUTC(endDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getRowCount() {
        return rowCount == 0 ? 10 : rowCount;
    }

    public boolean hasDateFilter() {
        return startDate != null || endDate != null;
    }

    public void setSearchParse(String searchPhrase) {
        this.searchPhrase = StringUtils.isNotBlank(searchPhrase) ? searchPhrase.toLowerCase(Locale.ROOT) : "";
    }

    /**
     * param isEmptyObject
     *
     * @return isEmptyObject boolean
     */
    public boolean isEmptyObject() {
        boolean isEmptyObject = startDate == null && endDate == null && nodeId == null && logType == null && logLevel == null
                && searchPhrase == null;
        return isEmptyObject;
    }
}
