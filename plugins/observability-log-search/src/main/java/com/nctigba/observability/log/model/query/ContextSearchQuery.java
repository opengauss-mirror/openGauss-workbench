package com.nctigba.observability.log.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Log-ContextSearch request dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/02/01 09:05
 */
@Data
public class ContextSearchQuery {
    private List<String> nodeId;
    private String searchPhrase;
    private int rowCount;
    private Date LogDate;
    private List<String> logType;
    private List<String> logLevel;
    private String scrollId;
    @NotNull(message = "aboveCount is empty")
    private Integer aboveCount;
    @NotNull(message = "belowCount is empty")
    private Integer belowCount;
}
