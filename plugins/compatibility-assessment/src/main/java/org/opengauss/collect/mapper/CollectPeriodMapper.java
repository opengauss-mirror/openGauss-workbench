/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.collect.domain.CollectPeriod;

/**
 * 功能描述
 *
 * @author liu
 * @since 2022-10-01
 */
@Mapper
public interface CollectPeriodMapper extends BaseMapper<CollectPeriod> {
    /**
     * listPidsByCurrentStatus
     *
     * @return List<String> list
     */
    List<String> selectListPid();

    /**
     * findDistinctHostByPid
     *
     * @param pid pid
     * @return String str
     */
    String findDistinctHostByPid(@Param("pid") String pid);

    /**
     * checkTime
     *
     * @param pid pid
     * @param time time
     * @return boolean boolean
     */
    boolean checkTime(@Param("pid") String pid, @Param("time") String time);
}
