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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.tun.domain.TrainingProgress;

/**
 * TrainingProgressMapper
 *
 * @author liu
 * @since 2023-12-20
 */
@Mapper
public interface TrainingProgressMapper extends BaseMapper<TrainingProgress> {
    /**
     * selectMaxTpsByTrainingId
     *
     * @param trainingId trainingId
     * @return TrainingProgress
     */
    TrainingProgress selectMaxTpsByTrainingId(@Param("trainingId") String trainingId);

    /**
     * selectInitialTpsRecord
     *
     * @param trainingId trainingId
     * @param parameterType parameterType
     * @return TrainingProgress
     */
    TrainingProgress selectInitialTpsRecord(@Param("trainingId") String trainingId,
                                            @Param("parameterType") String parameterType);

    /**
     * selectCountByTrainingId
     *
     * @param trainingId trainingId
     * @return Integer
     */
    Integer selectCountByTrainingId(@Param("trainingId") Long trainingId);
}
