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
import org.opengauss.tun.domain.ParameterRecommendation;

/**
 * ParameterRecommendationMapper
 *
 * @author liu
 * @since 2023-12-20
 */
@Mapper
public interface ParameterRecommendationMapper extends BaseMapper<ParameterRecommendation> {
    /**
     * selectRecommendation
     *
     * @param recommendation recommendation
     * @return List<ParameterRecommendation>
     */
    List<ParameterRecommendation> selectRecommendation(ParameterRecommendation recommendation);

    /**
     * selectClusterNames
     *
     * @return  List<String>
     */
    List<String> selectClusterNames();

    /**
     * selectdbs
     *
     * @return  List<String>
     */
    List<String> selectdbs();
}
