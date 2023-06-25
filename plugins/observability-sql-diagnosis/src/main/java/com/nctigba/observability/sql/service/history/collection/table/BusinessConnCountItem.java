/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.table;

import com.nctigba.observability.sql.constants.history.SqlCommon;
import org.springframework.stereotype.Service;

/**
 * BusinessConnCountItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class BusinessConnCountItem extends DatabaseCollectionItem {
    @Override
    public String getDatabaseSql() {
        return SqlCommon.BUSINESS_CONN_COUNT;
    }
}
