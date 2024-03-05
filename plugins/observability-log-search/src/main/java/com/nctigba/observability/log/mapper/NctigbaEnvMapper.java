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
 *  NctigbaEnvMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/env/NctigbaEnvMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.vo.AgentStatusVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * NctigbaEnvMapper
 *
 * @author luomeng
 * @since 2022/11/17 09:00
 */
@Mapper
public interface NctigbaEnvMapper extends BaseMapper<NctigbaEnvDO> {
    /**
     * Select data
     *
     * @param type Enum value
     * @return List
     */
    @Select("SELECT id, status, update_time FROM nctigba_env where type = #{type}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "status", property = "status"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<AgentStatusVO> selectStatusByType(String type);
}