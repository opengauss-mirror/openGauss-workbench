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
 *  DynamicHisSlowSqlMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mapper/DynamicHisSlowSqlMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.dto.SlowSqlDTO;
import com.nctigba.observability.sql.model.entity.HisSlowsqlInfoDO;
import com.nctigba.observability.sql.model.vo.StatementHistoryAggVO;
import com.nctigba.observability.sql.model.vo.StatementHistoryDetailVO;
import com.nctigba.observability.sql.service.impl.SqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.DeleteProvider;

import java.util.Date;
import java.util.List;

/**
 * DynamicHisSlowSqlMapper
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Mapper
@DS("hisSlowSqlInfo")
public interface DynamicHisSlowSqlMapper extends BaseMapper<HisSlowsqlInfoDO> {
    /**
     * insert into hisSlowSqlInfo table
     *
     * @param tableName String
     * @param data HisSlowsqlInfoDO
     * @return int inserted rows
     */
    @InsertProvider(type = SqlProvider.class, method = "buildInsertSql")
    int insert(@Param("tableName") String tableName, @Param("data") HisSlowsqlInfoDO data);

    /**
     * create hisSlowSqlInfo table
     *
     * @param tableName String
     * @return boolean
     */
    @InsertProvider(type = SqlProvider.class, method = "buildCreateSql")
    boolean createTable(@Param("tableName") String tableName);

    /**
     * select count
     *
     * @param tableName String
     * @param queryWrapper Wrapper<HisSlowsqlInfoDO>
     * @return Long
     */
    @Select("SELECT COUNT(1) FROM ${tableName} ${ew.customSqlSegment}")
    Long selectCount(@Param("tableName") String tableName, @Param("ew") Wrapper<HisSlowsqlInfoDO> queryWrapper);

    /**
     * select all slow sql
     *
     * @param tableName String
     * @param slowSqlDTO SlowSqlDTO
     * @return List<StatementHistoryDetailVO>
     */
    @SelectProvider(type = SqlProvider.class, method = "getAllSlowSql")
    List<StatementHistoryDetailVO> selectAllSlowSql(@Param("tableName") String tableName, SlowSqlDTO slowSqlDTO);

    /**
     * select statistics slow sql info
     *
     * @param tableName String
     * @param slowSqlDTO SlowSqlDTO
     * @return List<StatementHistoryAggVO>
     */
    @SelectProvider(type = SqlProvider.class, method = "getAggSlowSql")
    List<StatementHistoryAggVO> selectAggSlowSql(String tableName, SlowSqlDTO slowSqlDTO);

    /**
     * clean expired slow sqls
     *
     * @param tableName String
     * @param peroid int
     * @return int
     */
    @DeleteProvider(type = SqlProvider.class, method = "getCleanupSql")
    int cleanExpiredData(String tableName, int peroid);

    /**
     * record time snapshot
     *
     * @param tableName String
     * @param maxTime Long
     * @return int
     */
    @Insert("INSERT OR REPLACE INTO tb_max_finish_time VALUES (#{tableName}, #{maxTime});")
    int insertMaxtime(@Param("tableName") String tableName, @Param("maxTime") Long maxTime);

    /**
     * record time snapshot
     *
     * @param tableName String
     * @return Date
     */
    @Select("select max_finish_time from tb_max_finish_time where node_tablename = #{tableName}")
    Date selectTimeshot(@Param("tableName") String tableName);

    /**
     * get number of sqls at one point
     *
     * @param tableName String
     * @param pointTime Long
     * @param dbName String
     * @return int number of active sqls at pointTime
     */
    @Select("select count(1) from ${tableName} where start_time <= #{pointTime} "
            + " and finish_time >= #{pointTime} and db_name = #{dbName}")
    int selectActiveSlowsqls(@Param("tableName") String tableName, @Param("pointTime") Long pointTime,
                             @Param("dbName") String dbName);
}
