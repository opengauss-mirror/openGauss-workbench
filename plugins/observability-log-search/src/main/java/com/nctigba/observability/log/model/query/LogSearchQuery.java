package com.nctigba.observability.log.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
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
public class LogSearchQuery {
    private List<String> nodeId;
    private String searchPhrase;
    @NotNull(message = "rowCount is empty")
    private int rowCount;
    private Date startDate;
    private Date endDate;
    private List<String> logType;
    private List<String> logLevel;
    private String scrollId;
    private List<String> sorts;
    private String id;
}
