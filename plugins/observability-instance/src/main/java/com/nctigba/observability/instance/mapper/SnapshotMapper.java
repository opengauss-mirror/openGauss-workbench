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
 *  SnapshotMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/mapper/SnapshotMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.model.entity.SnapshotDO;

/**
 * SnapshotMapper.java
 *
 * @since 2023-08-28
 */
@Mapper
public interface SnapshotMapper extends BaseMapper<SnapshotDO> {
    /**
     * getIdByTime order by asc
     *
     * @param id     id
     * @param column column
     * @param start  start
     * @param end    end
     * @return Long
     */
    @Ds
    default Long getIdByTimeAsc(String id, SFunction<SnapshotDO, ?> column, Date start, Date end) {
        var snapshot = selectOne(Wrappers.<SnapshotDO>lambdaQuery().between(column, start, end).orderByAsc(column)
            .last(" limit 1"));
        return snapshot == null ? 0 : snapshot.getSnapshotId();
    }

    /**
     * getIdByTime order by desc
     *
     * @param id     id
     * @param column column
     * @param start  start
     * @param end    end
     * @return Long
     */
    @Ds
    default Long getIdByTimeDesc(String id, SFunction<SnapshotDO, ?> column, Date start, Date end) {
        var snapshot = selectOne(Wrappers.<SnapshotDO>lambdaQuery().between(column, start, end).orderByDesc(column)
            .last(" limit 1"));
        return snapshot == null ? 0 : snapshot.getSnapshotId();
    }
}