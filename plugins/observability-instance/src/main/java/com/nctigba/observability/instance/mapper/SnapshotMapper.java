/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.nctigba.observability.instance.aop.Ds;
import com.nctigba.observability.instance.entity.Snapshot;

/**
 * SnapshotMapper.java
 *
 * @since 2023-08-28
 */
@Mapper
public interface SnapshotMapper extends BaseMapper<Snapshot> {
    /**
     * getIdByTime
     *
     * @param id     id
     * @param column column
     * @param start  start
     * @param end    end
     * @return Long
     */
    @Ds
    default Long getIdByTime(String id, SFunction<Snapshot, ?> column, Date start, Date end) {
        var snapshot = selectOne(Wrappers.<Snapshot>lambdaQuery().between(column, start, end).last(" limit 1"));
        return snapshot == null ? 0 : snapshot.getSnapshotId();
    }
}