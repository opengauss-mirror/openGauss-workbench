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

package org.opengauss.tun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.tun.domain.TrainingConfig;

/**
 * TrainingConfigMpper
 *
 * @author liu
 * @since 2023-12-20
 */
@Mapper
public interface TrainingConfigMpper extends BaseMapper<TrainingConfig> {
    /**
     * selectAllConfig
     *
     * @param clusterName clusterName
     * @param db db
     * @param startTime startTime
     * @param endTime endTime
     * @return  List<TrainingConfig>
     */
    List<TrainingConfig> selectAllConfig(
            @Param("clusterName") String clusterName, @Param("db") String db,
            @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * updateStatusByTrainingId
     *
     * @param trainingId trainingId
     * @param status status
     * @param process process
     * @return int
     */
    int updateStatusByTrainingId(@Param("trainingId") Long trainingId,
                                 @Param("status") String status,
                                 @Param("process") Integer process);

    /**
     * selectClusterNames
     *
     * @return List<String>
     */
    List<String> selectClusterNames();

    /**
     * selectdbs
     *
     * @return List<String>
     */
    List<String> selectdbs();
}
