/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  OpengaussAllSlowsqlMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mapper/
 *  OpengaussAllSlowsqlMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.entity.HisSlowsqlInfoDO;
import com.nctigba.observability.sql.model.entity.StatementHistoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * OpengaussAllSlowsqlMapper
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Mapper
public interface OpengaussAllSlowsqlMapper extends BaseMapper<StatementHistoryDO> {
    /**
     * select slow sqls from database
     *
     * @param startPoint Date
     * @param finishPoint Date
     * @return List<HisSlowsqlInfoDO>
     */
    @Select("select * from dbe_perf.get_statement_history(#{startPoint}::timestamptz, #{finishPoint}::timestamptz)"
            + "  where debug_query_id > 0")
    List<HisSlowsqlInfoDO> selectSlowSqls(@Param("startPoint") Date startPoint,
                                          @Param("finishPoint") Date finishPoint);

    /**
     * select slow sqls from primary node
     *
     * @param timeShot String
     * @return List<HisSlowsqlInfoDO>
     */
    @Select("select * from dbe_perf.statement_history where debug_query_id > 0 and start_time > #{timeShot}")
    List<HisSlowsqlInfoDO> selectPrimarySlowSqls(@Param("timeShot") String timeShot);

    /**
     * select slow sqls from stabdby node
     *
     * @param timeShot String
     * @return List<HisSlowsqlInfoDO>
     */
    @Select("select * from dbe_perf.standby_statement_history("
            + "true, #{timeShot}::timestamptz, now()) where debug_query_id > 0")
    List<HisSlowsqlInfoDO> selectStandbySlowSqls(@Param("timeShot") Date timeShot);

    /**
     * get database node status
     *
     * @return String
     */
    @Select("select local_role from pg_stat_get_stream_replications()")
    String selectNodeStatus();

    /**
     * get slow sql threshold
     *
     * @return String
     */
    @Select("show log_min_duration_statement")
    String getSlowsqlThreshold();
}
