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

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.entity.SqlHistoryDO;

import java.util.List;

/**
 * SqlHistoryManagerService
 *
 * @since 2023-7-17
 */
public interface SqlHistoryManagerService {
    /**
     * query sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     * @return List
     */
    List<SqlHistoryDO> queryHistory(SqlHistoryDO sqlHistoryDO);

    /**
     * insert sql history
     *
     * @param sqlHistoryDOList sqlHistoryDOList
     */
    void insertHistory(List<SqlHistoryDO> sqlHistoryDOList);

    /**
     * update sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     */
    void updateHistory(SqlHistoryDO sqlHistoryDO);

    /**
     * delete sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     */
    void deleteHistory(SqlHistoryDO sqlHistoryDO);
}
