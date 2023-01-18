package com.nctigba.observability.log.model.query;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * <p>
 * Log-Search request dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:05
 */
@Data
public class LogDistroMapQuery {
    private List<String> nodeId;
    private String searchPhrase;
    private Date startDate;
    private Date endDate;
    private List<String> logType;
    private List<String> logLevel;
}
