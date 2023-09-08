/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
