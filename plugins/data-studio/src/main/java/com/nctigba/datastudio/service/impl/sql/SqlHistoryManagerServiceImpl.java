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
 *  SqlHistoryManagerServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/SqlHistoryManagerServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.dao.SqlHistoryTemplateDAO;
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
    private SqlHistoryTemplateDAO sqlHistoryTemplateDAO;

    @Override
    public List<SqlHistoryDO> queryHistory(SqlHistoryDO sqlHistoryDO) {
        return sqlHistoryTemplateDAO.queryTable(sqlHistoryDO);
    }

    @Override
    public void insertHistory(List<SqlHistoryDO> sqlHistoryDOList) {
        sqlHistoryTemplateDAO.insertSqlHistory(sqlHistoryDOList);
    }

    @Override
    public void updateHistory(SqlHistoryDO sqlHistoryDO) {
        sqlHistoryTemplateDAO.updateStatusById(sqlHistoryDO);
    }

    @Override
    public void deleteHistory(SqlHistoryDO sqlHistoryDO) {
        sqlHistoryTemplateDAO.deleteById(sqlHistoryDO);
    }
}
