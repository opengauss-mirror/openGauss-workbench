/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection;

import com.nctigba.observability.sql.model.history.HisDiagnosisTask;

/**
 * CollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface CollectionItem<T> {
    T collectData(HisDiagnosisTask task);
    T queryData(HisDiagnosisTask task);
}
