package com.nctigba.observability.log.model.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Log-ContextSearch response dto
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/2/1 17:05
 */
@Data
public class ContextSearchInfoDTO {
    private String scrollId;
    private List<ContextSearchDTO> logs;
    private List<String> sorts;
}
