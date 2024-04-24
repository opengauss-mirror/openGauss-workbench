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
 *  SlowLogMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mapper/SlowLogMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.dto.SlowSqlDTO;
import com.nctigba.observability.sql.model.entity.StatementHistoryDO;
import com.nctigba.observability.sql.model.vo.StatementHistoryAggVO;
import com.nctigba.observability.sql.model.vo.StatementHistoryDetailVO;
import com.nctigba.observability.sql.service.impl.SqlProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface SlowLogMapper extends BaseMapper<StatementHistoryDO> {
    /**
     * Select aggregated data
     *
     * @param slowSqlDTO String
     * @return List
     */
    @SelectProvider(type = SqlProvider.class, method = "getAggSlowSql")
    List<StatementHistoryAggVO> selectAggSlowSql(SlowSqlDTO slowSqlDTO);

    /**
     * Select detail data
     *
     * @param slowSqlDTO String
     * @return List
     */
    @SelectProvider(type = SqlProvider.class, method = "getAllSlowSql")
    List<StatementHistoryDetailVO> selectAllSlowSql(SlowSqlDTO slowSqlDTO);
}