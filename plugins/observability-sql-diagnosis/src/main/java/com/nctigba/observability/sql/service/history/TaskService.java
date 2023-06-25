/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history;

import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;

import java.util.List;

/**
 * TaskService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface TaskService {
    Integer add(HisDiagnosisTaskDTO hisDiagnosisTaskDTO);

    void start(int taskId, HisDiagnosisTaskDTO taskDTO);

    List<OptionQuery> getOption();
}
