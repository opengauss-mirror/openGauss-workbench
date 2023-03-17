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
