/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.service.impl.collection.table;

import com.nctigba.observability.sql.constant.SqlConstants;
import org.springframework.stereotype.Service;

/**
 * DbShareMemoryItem
 *
 * @author luomeng
 * @since 2025/2/17
 */
@Service
public class DbShareMemoryItem extends DatabaseCollectionItem {
    @Override
    public String getDatabaseSql() {
        return SqlConstants.GET_DB_SHARE_MEM_SQL;
    }
}
