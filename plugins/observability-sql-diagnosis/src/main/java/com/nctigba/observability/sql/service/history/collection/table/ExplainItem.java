/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.table;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import org.springframework.stereotype.Service;

/**
 * ExplainItem
 *
 * @author luomeng
 * @since 2023/8/2
 */
@Service
public class ExplainItem extends DatabaseCollectionItem {
    @Override
    public String getDatabaseSql() {
        return SqlCommon.DEFAULT;
    }

    public String getCollectionType() {
        return CollectionTypeCommon.AFTER;
    }
}
