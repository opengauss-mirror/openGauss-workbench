/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  LockTimeoutItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/elastic/LockTimeoutItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.elastic;

import com.nctigba.observability.sql.constant.ElasticSearchConstants;
import org.springframework.stereotype.Service;

/**
 * LockTimeoutItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class LockTimeoutItem extends ElasticSearchCollectionItem {
    @Override
    public String getQueryParam() {
        return ElasticSearchConstants.LOCK_TIMEOUT;
    }
}
