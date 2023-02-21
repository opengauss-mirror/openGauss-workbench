package com.nctigba.observability.log.service;

import com.nctigba.observability.log.model.dto.ContextSearchInfoDTO;
import com.nctigba.observability.log.model.dto.LogDistroMapDTO;
import com.nctigba.observability.log.model.dto.LogInfoDTO;
import com.nctigba.observability.log.model.dto.LogTypeTreeDTO;
import com.nctigba.observability.log.model.query.ContextSearchQuery;
import com.nctigba.observability.log.model.query.EsSearchQuery;

import java.util.List;

/**
 * <p>
 * Log-Search Service
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:10
 */
public interface LogSearchService {


    List<LogDistroMapDTO> getLogDistroMap(EsSearchQuery queryParam) throws Exception;

    LogInfoDTO getLogByQuery(EsSearchQuery queryParam) throws Exception;

    List<LogTypeTreeDTO> getLogType() throws Exception;

    List<String> getLogLevel() throws Exception;
    ContextSearchInfoDTO getContextSearch(ContextSearchQuery queryParam) throws Exception;
}
