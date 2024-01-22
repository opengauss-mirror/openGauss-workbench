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

package org.opengauss.tun.service.impl;

import java.util.List;
import org.opengauss.tun.domain.ParameterRecommendation;
import org.opengauss.tun.mapper.ParameterRecommendationMapper;
import org.opengauss.tun.service.ParameterRecommendationService;
import org.opengauss.tun.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ParameterRecommendationServiceImpl
 *
 * @author liu
 * @since 2023-12-20
 */
@Service
public class ParameterRecommendationServiceImpl implements ParameterRecommendationService {
    @Autowired
    private ParameterRecommendationMapper recommendationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveParameter(ParameterRecommendation parameterRecommendation) {
        return recommendationMapper.insert(parameterRecommendation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RespBean deleteSuggests(List<String> ids) {
        return RespBean.success("success", recommendationMapper.deleteBatchIds(ids));
    }
}
