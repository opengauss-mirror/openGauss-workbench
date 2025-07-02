package com.nctigba.observability.sql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.entity.HisSlowsqlInfoDO;
import com.nctigba.observability.sql.model.entity.StatementHistoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.SQLException;
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
     * @param timeShot String
     * @return List<HisSlowsqlInfoDO>
     * @throws SQLException SQLException
     */
    @Select("select * from dbe_perf.get_statement_history(#{timeShot}::timestamptz) where debug_query_id > 0")
    List<HisSlowsqlInfoDO> selectSlowSqls(@Param("timeShot") Date timeShot) throws SQLException;

    /**
     * select slow sqls from primary node
     *
     * @param timeShot String
     * @return List<HisSlowsqlInfoDO>
     */
    @Select("select * from dbe_perf.statement_history where debug_query_id > 0 and finish_time > #{timeShot}")
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
}
