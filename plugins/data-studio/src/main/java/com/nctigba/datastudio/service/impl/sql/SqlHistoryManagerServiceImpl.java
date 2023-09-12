/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.dao.SqlHistoryTemplate;
import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.service.SqlHistoryManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SqlHistoryManagerServiceImpl
 *
 * @since 2023-7-17
 */
@Slf4j
@Service
public class SqlHistoryManagerServiceImpl implements SqlHistoryManagerService {
    @Autowired
    private SqlHistoryTemplate sqlHistoryTemplate;

    @Override
    public List<SqlHistoryDO> queryHistory(SqlHistoryDO sqlHistoryDO) {
        return sqlHistoryTemplate.queryTable(sqlHistoryDO);
    }

    @Override
    public void insertHistory(List<SqlHistoryDO> sqlHistoryDOList) {
        sqlHistoryTemplate.insertSqlHistory(sqlHistoryDOList);
    }

    @Override
    public void updateHistory(SqlHistoryDO sqlHistoryDO) {
        sqlHistoryTemplate.updateStatusById(sqlHistoryDO);
    }

    @Override
    public void deleteHistory(SqlHistoryDO sqlHistoryDO) {
        sqlHistoryTemplate.deleteById(sqlHistoryDO);
    }
}
